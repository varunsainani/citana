import express, { type NextFunction, type Request, type Response } from "express";
import cors from "cors";
import { db, authAdmin, projectId } from "./firebase";
import { computeSlots, type Availability } from "./slots";

const app = express();
app.use(cors());
app.use(express.json());

type AuthedRequest = Request & { uid?: string; email?: string };

async function requireAuth(req: AuthedRequest, res: Response, next: NextFunction) {
  const header = req.headers.authorization || "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : "";
  if (!token) return res.status(401).json({ error: "unauthenticated" });
  try {
    const decoded = await authAdmin.verifyIdToken(token);
    req.uid = decoded.uid;
    req.email = decoded.email;
    next();
  } catch {
    return res.status(401).json({ error: "invalid_token" });
  }
}

type ServiceDoc = { id: string; name: string; durationMin: number; priceCents: number; currency?: string };

function mapProvider(id: string, d: FirebaseFirestore.DocumentData) {
  return {
    id,
    name: d.name,
    categorySlug: d.categorySlug,
    bio: d.bio ?? "",
    city: d.city ?? "",
    rating: d.rating ?? 0,
    ratingCount: d.ratingCount ?? 0,
    imageUrl: d.imageUrl ?? "",
    services: (d.services ?? []) as ServiceDoc[],
  };
}

app.get("/health", (_req, res) => res.json({ ok: true, service: "citana", projectId }));

app.get("/categories", async (_req, res) => {
  const snap = await db.collection("categories").get();
  const cats = snap.docs
    .map((d) => ({ id: d.id, ...d.data() }))
    .sort((a, b) => Number((a as { sort?: number }).sort ?? 0) - Number((b as { sort?: number }).sort ?? 0));
  res.json(cats);
});

app.get("/providers", async (req, res) => {
  const category = String(req.query.category ?? "").trim();
  let q: FirebaseFirestore.Query = db.collection("providers");
  if (category) q = q.where("categorySlug", "==", category);
  const snap = await q.get();
  const providers = snap.docs
    .map((d) => mapProvider(d.id, d.data()))
    .sort((a, b) => b.rating - a.rating);
  res.json(providers);
});

app.get("/providers/:id", async (req, res) => {
  const doc = await db.collection("providers").doc(req.params.id).get();
  if (!doc.exists) return res.status(404).json({ error: "not_found" });
  res.json(mapProvider(doc.id, doc.data()!));
});

app.get("/providers/:id/availability", async (req, res) => {
  const date = String(req.query.date ?? "");
  const serviceId = String(req.query.serviceId ?? "");
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) return res.status(400).json({ error: "bad_date" });

  const doc = await db.collection("providers").doc(req.params.id).get();
  if (!doc.exists) return res.status(404).json({ error: "not_found" });
  const data = doc.data()!;
  const service = (data.services as ServiceDoc[]).find((s) => s.id === serviceId);
  if (!service) return res.status(404).json({ error: "service_not_found" });

  const bookingSnap = await db.collection("bookings").where("providerId", "==", req.params.id).get();
  const taken = bookingSnap.docs
    .map((b) => b.data())
    .filter((b) => b.status === "confirmed" && String(b.startAt).slice(0, 10) === date)
    .map((b) => ({ startAt: b.startAt as string, endAt: b.endAt as string }));

  const slots = computeSlots({
    date,
    availability: (data.availability ?? {}) as Availability,
    durationMin: service.durationMin,
    taken,
    nowIso: new Date().toISOString(),
  });
  res.json({ date, serviceId, durationMin: service.durationMin, slots });
});

app.post("/bookings", requireAuth, async (req: AuthedRequest, res) => {
  const { providerId, serviceId, startAt } = req.body ?? {};
  if (!providerId || !serviceId || !startAt) return res.status(400).json({ error: "bad_request" });
  const start = new Date(startAt);
  if (Number.isNaN(start.getTime())) return res.status(400).json({ error: "bad_start" });

  const doc = await db.collection("providers").doc(providerId).get();
  if (!doc.exists) return res.status(404).json({ error: "provider_not_found" });
  const provider = doc.data()!;
  const service = (provider.services as ServiceDoc[]).find((s) => s.id === serviceId);
  if (!service) return res.status(404).json({ error: "service_not_found" });

  const end = new Date(start.getTime() + service.durationMin * 60_000);
  const startIso = start.toISOString();

  const existing = await db.collection("bookings").where("providerId", "==", providerId).get();
  const clash = existing.docs.some((d) => {
    const b = d.data();
    return b.status === "confirmed" && b.startAt === startIso;
  });
  if (clash) return res.status(409).json({ error: "slot_taken" });

  const booking = {
    userId: req.uid!,
    providerId,
    providerName: provider.name,
    serviceId,
    serviceName: service.name,
    startAt: startIso,
    endAt: end.toISOString(),
    priceCents: service.priceCents,
    currency: service.currency ?? "USD",
    status: "confirmed" as const,
    createdAt: new Date().toISOString(),
  };
  const ref = await db.collection("bookings").add(booking);
  res.status(201).json({ id: ref.id, ...booking });
});

app.get("/me/bookings", requireAuth, async (req: AuthedRequest, res) => {
  const snap = await db.collection("bookings").where("userId", "==", req.uid!).get();
  const bookings = snap.docs
    .map((d) => ({ id: d.id, ...d.data() }))
    .sort((a, b) =>
      String((b as { startAt?: string }).startAt).localeCompare(String((a as { startAt?: string }).startAt)),
    );
  res.json(bookings);
});

app.patch("/bookings/:id", requireAuth, async (req: AuthedRequest, res) => {
  const status = String(req.body?.status ?? "");
  if (status !== "cancelled") return res.status(400).json({ error: "bad_status" });
  const ref = db.collection("bookings").doc(req.params.id);
  const doc = await ref.get();
  if (!doc.exists || doc.data()!.userId !== req.uid) return res.status(404).json({ error: "not_found" });
  await ref.update({ status: "cancelled" });
  res.json({ id: doc.id, ...doc.data(), status: "cancelled" });
});

export default app;
