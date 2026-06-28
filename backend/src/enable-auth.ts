import "dotenv/config";
import admin from "firebase-admin";
import { projectId } from "./firebase";

// Enable Email/Password sign-in via the Identity Platform admin API, using the
// service account (so no manual Firebase console toggle is needed).
async function main() {
  const credential = admin.app().options.credential!;
  const token = await credential.getAccessToken();
  const res = await fetch(
    `https://identitytoolkit.googleapis.com/admin/v2/projects/${projectId}/config?updateMask=signIn.email.enabled,signIn.email.passwordRequired`,
    {
      method: "PATCH",
      headers: { Authorization: `Bearer ${token.access_token}`, "Content-Type": "application/json" },
      body: JSON.stringify({ signIn: { email: { enabled: true, passwordRequired: true } } }),
    },
  );
  const text = await res.text();
  console.log("status:", res.status);
  console.log(text.slice(0, 400));
  process.exit(res.ok ? 0 : 1);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
