import "dotenv/config";
import fs from "node:fs";
import path from "node:path";
import type { Server } from "node:http";
import app from "./app";

const PORT = 8799;
const BASE = `http://127.0.0.1:${PORT}`;
let pass = 0;
let fail = 0;
const ok = (cond: boolean, label: string) => {
  if (cond) { pass++; console.log("  ok  " + label); }
  else { fail++; console.log("  XX  " + label); }
};

const gsPath = path.resolve(__dirname, "../../app/google-services.json");
const API_KEY = JSON.parse(fs.readFileSync(gsPath, "utf8")).client[0].api_key[0].current_key as string;

async function idToken(email: string, password: string): Promise<string> {
  const r = await fetch(
    `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${API_KEY}`,
    { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ email, password, returnSecureToken: true }) },
  );
  const d = (await r.json()) as { idToken?: string; error?: unknown };
  if (!d.idToken) throw new Error("sign-in failed: " + JSON.stringify(d.error));
  return d.idToken;
}

const nextDow = (dow: number) => {
  for (let i = 1; i <= 8; i++) {
    const x = new Date(Date.now() + i * 86_400_000);
    if (x.getUTCDay() === dow) return x.toISOString().slice(0, 10);
  }
  return new Date().toISOString().slice(0, 10);
};

async function req(method: string, p: string, opts: { token?: string; body?: unknown } = {}) {
  const headers: Record<string, string> = {};
  if (opts.token) headers.Authorization = `Bearer ${opts.token}`;
  if (opts.body !== undefined) headers["Content-Type"] = "application/json";
  const res = await fetch(`${BASE}${p}`, { method, headers, body: opts.body !== undefined ? JSON.stringify(opts.body) : undefined });
  const data = res.status === 204 ? null : await res.json().catch(() => null);
  return { status: res.status, data } as { status: number; data: any };
}

async function main() {
  const server: Server = await new Promise((r) => { const s = app.listen(PORT, () => r(s)); });
  try {
    let r = await req("GET", "/health");
    ok(r.status === 200 && r.data?.ok === true, `GET /health (${r.data?.projectId})`);

    r = await req("GET", "/categories");
    ok(r.status === 200 && r.data.length === 6, `GET /categories (${r.data?.length})`);

    r = await req("GET", "/providers");
    ok(r.status === 200 && r.data.length === 10, `GET /providers (${r.data?.length})`);
    r = await req("GET", "/providers?category=salon");
    ok(r.status === 200 && r.data.length === 2, `GET /providers?category=salon (${r.data?.length})`);

    r = await req("GET", "/providers/p1");
    ok(r.status === 200 && r.data?.id === "p1" && r.data.services.length >= 2, "GET /providers/p1");

    r = await req("GET", "/bookings", {});
    r = await req("GET", "/me/bookings");
    ok(r.status === 401, "GET /me/bookings without token -> 401");

    console.log("  ... signing in (Firebase) ...");
    const token = await idToken("demo@citana.app", "demo1234");

    const date = nextDow(1); // Monday
    r = await req("GET", `/providers/p1/availability?date=${date}&serviceId=s1`);
    ok(r.status === 200 && Array.isArray(r.data?.slots) && r.data.slots.length > 0, `availability (${r.data?.slots?.length} slots on ${date})`);
    const slot = r.data.slots[0];
    const startAt = `${date}T${slot}:00.000Z`;

    r = await req("POST", "/bookings", { token, body: { providerId: "p1", serviceId: "s1", startAt } });
    ok(r.status === 201 && !!r.data?.id && r.data.status === "confirmed", `POST /bookings (${r.status})`);
    const bookingId = r.data?.id;

    r = await req("GET", `/providers/p1/availability?date=${date}&serviceId=s1`);
    ok(!r.data.slots.includes(slot), "booked slot removed from availability");

    r = await req("GET", "/me/bookings", { token });
    ok(r.status === 200 && r.data.some((b: any) => b.id === bookingId), `GET /me/bookings (${r.data?.length})`);

    r = await req("PATCH", `/bookings/${bookingId}`, { token, body: { status: "cancelled" } });
    ok(r.status === 200 && r.data?.status === "cancelled", "PATCH cancel booking");

    // cleanup the test booking
    await import("./firebase").then(({ db }) => db.collection("bookings").doc(bookingId).delete());
  } finally {
    server.close();
  }
  console.log(`\nBACKEND SMOKE: ${pass} passed, ${fail} failed`);
  process.exit(fail > 0 ? 1 : 0);
}

main().catch((e) => { console.error(e); process.exit(1); });
