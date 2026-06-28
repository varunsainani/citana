import "dotenv/config";
import fs from "node:fs";
import path from "node:path";
import admin from "firebase-admin";
import { projectId } from "./firebase";

// Deploy Firestore security rules via the Firebase Rules REST API, using the
// service account (no firebase CLI / console needed).
async function main() {
  const token = (await admin.app().options.credential!.getAccessToken()).access_token;
  const source = fs.readFileSync(path.resolve(__dirname, "../firestore.rules"), "utf8");

  const create = await fetch(`https://firebaserules.googleapis.com/v1/projects/${projectId}/rulesets`, {
    method: "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: JSON.stringify({ source: { files: [{ name: "firestore.rules", content: source }] } }),
  });
  const ruleset = (await create.json()) as { name?: string };
  if (!ruleset.name) {
    console.error("ruleset create failed:", create.status, JSON.stringify(ruleset).slice(0, 300));
    process.exit(1);
  }
  console.log("ruleset:", ruleset.name);

  const releaseName = `projects/${projectId}/releases/cloud.firestore`;
  const headers = { Authorization: `Bearer ${token}`, "Content-Type": "application/json" };

  // Try to create the release; if it already exists, update it.
  let rel = await fetch(`https://firebaserules.googleapis.com/v1/projects/${projectId}/releases`, {
    method: "POST",
    headers,
    body: JSON.stringify({ name: releaseName, rulesetName: ruleset.name }),
  });
  let text = await rel.text();
  if (!rel.ok && (rel.status === 409 || text.includes("ALREADY_EXISTS"))) {
    rel = await fetch(`https://firebaserules.googleapis.com/v1/${releaseName}?updateMask=rulesetName`, {
      method: "PATCH",
      headers,
      body: JSON.stringify({ rulesetName: ruleset.name }),
    });
    text = await rel.text();
  }
  console.log("release:", rel.status, text.slice(0, 200));
  process.exit(rel.ok ? 0 : 1);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
