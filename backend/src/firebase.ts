import admin from "firebase-admin";
import fs from "node:fs";

function loadServiceAccount(): Record<string, unknown> {
  const b64 = process.env.FIREBASE_SERVICE_ACCOUNT_B64;
  if (b64) return JSON.parse(Buffer.from(b64, "base64").toString("utf8"));
  const raw = process.env.FIREBASE_SERVICE_ACCOUNT;
  if (raw) return JSON.parse(raw);
  const path = process.env.FIREBASE_SERVICE_ACCOUNT_PATH;
  if (path && fs.existsSync(path)) return JSON.parse(fs.readFileSync(path, "utf8"));
  throw new Error("No Firebase service account (set FIREBASE_SERVICE_ACCOUNT_B64 / _PATH / _SERVICE_ACCOUNT)");
}

const svc = loadServiceAccount();

if (!admin.apps.length) {
  admin.initializeApp({ credential: admin.credential.cert(svc as admin.ServiceAccount) });
}

export const db = admin.firestore();
db.settings({ ignoreUndefinedProperties: true });
export const authAdmin = admin.auth();
export const projectId = String(svc.project_id ?? "");
