import "dotenv/config";
import { db, authAdmin } from "./firebase";

const monFri = {
  "1": [{ start: "09:00", end: "17:00" }],
  "2": [{ start: "09:00", end: "17:00" }],
  "3": [{ start: "09:00", end: "17:00" }],
  "4": [{ start: "09:00", end: "17:00" }],
  "5": [{ start: "09:00", end: "17:00" }],
};
const withSat = { ...monFri, "6": [{ start: "10:00", end: "14:00" }] };

const svc = (id: string, name: string, durationMin: number, priceCents: number) => ({
  id,
  name,
  durationMin,
  priceCents,
  currency: "USD",
});

const categories = [
  { slug: "salon", name: "Hair & Salon", icon: "content_cut", sort: 0 },
  { slug: "barber", name: "Barber", icon: "face_retouching_natural", sort: 1 },
  { slug: "spa", name: "Spa & Massage", icon: "spa", sort: 2 },
  { slug: "dental", name: "Dental", icon: "medical_services", sort: 3 },
  { slug: "clinic", name: "Clinic", icon: "local_hospital", sort: 4 },
  { slug: "wellness", name: "Wellness", icon: "self_improvement", sort: 5 },
];

const providers = [
  { name: "Bloom Hair Studio", categorySlug: "salon", city: "Austin", rating: 4.8, ratingCount: 212, availability: withSat,
    bio: "Modern cuts, color and styling in a relaxed studio.",
    services: [svc("s1", "Haircut & Style", 45, 4500), svc("s2", "Color & Highlights", 120, 12000), svc("s3", "Blowout", 30, 3500)] },
  { name: "Glow Beauty Lounge", categorySlug: "salon", city: "Miami", rating: 4.7, ratingCount: 265, availability: withSat,
    bio: "Hair, nails and beauty under one roof.",
    services: [svc("s1", "Women's Cut", 60, 5500), svc("s2", "Manicure", 40, 3000), svc("s3", "Makeup", 50, 6500)] },
  { name: "The Sharp Fade", categorySlug: "barber", city: "Austin", rating: 4.7, ratingCount: 180, availability: withSat,
    bio: "Precision fades and classic grooming.",
    services: [svc("s1", "Skin Fade", 30, 3000), svc("s2", "Beard Trim", 20, 1800), svc("s3", "Cut + Beard", 45, 4200)] },
  { name: "Classic Cuts Barbershop", categorySlug: "barber", city: "Brooklyn", rating: 4.6, ratingCount: 142, availability: withSat,
    bio: "Old-school barbershop, walk-in or book ahead.",
    services: [svc("s1", "Classic Cut", 30, 2800), svc("s2", "Hot Towel Shave", 30, 3200)] },
  { name: "Serenity Spa & Massage", categorySlug: "spa", city: "Denver", rating: 4.9, ratingCount: 340, availability: monFri,
    bio: "Therapeutic and relaxation massage by certified therapists.",
    services: [svc("s1", "Swedish Massage", 60, 9000), svc("s2", "Deep Tissue", 90, 12500), svc("s3", "Hot Stone", 75, 11000)] },
  { name: "Lumière Skin & Spa", categorySlug: "spa", city: "Los Angeles", rating: 4.8, ratingCount: 301, availability: monFri,
    bio: "Facials, skincare and spa rituals.",
    services: [svc("s1", "Signature Facial", 60, 8500), svc("s2", "Express Facial", 30, 4500)] },
  { name: "BrightSmile Dental", categorySlug: "dental", city: "Seattle", rating: 4.6, ratingCount: 156, availability: monFri,
    bio: "Gentle, modern dentistry for the whole family.",
    services: [svc("s1", "Checkup & Clean", 45, 9500), svc("s2", "Teeth Whitening", 60, 19000)] },
  { name: "Pulse Family Clinic", categorySlug: "clinic", city: "Chicago", rating: 4.5, ratingCount: 98, availability: monFri,
    bio: "Primary care and same-week appointments.",
    services: [svc("s1", "General Consultation", 30, 7500), svc("s2", "Health Screening", 45, 11000)] },
  { name: "Zen Wellness Center", categorySlug: "wellness", city: "Portland", rating: 4.8, ratingCount: 210, availability: withSat,
    bio: "Acupuncture, yoga and mindfulness sessions.",
    services: [svc("s1", "Acupuncture", 60, 8000), svc("s2", "Private Yoga", 60, 7000)] },
  { name: "Harmony Physio & Wellness", categorySlug: "wellness", city: "Boston", rating: 4.9, ratingCount: 188, availability: monFri,
    bio: "Physiotherapy and recovery for active lives.",
    services: [svc("s1", "Physio Assessment", 45, 9000), svc("s2", "Sports Massage", 60, 9500)] },
];

async function wipe(collection: string) {
  const snap = await db.collection(collection).get();
  await Promise.all(snap.docs.map((d) => d.ref.delete()));
}

async function main() {
  console.log("[seed] categories…");
  for (const c of categories) await db.collection("categories").doc(c.slug).set(c);

  console.log("[seed] providers…");
  await wipe("providers");
  let i = 1;
  for (const p of providers) {
    const id = `p${i}`;
    await db.collection("providers").doc(id).set({ ...p, imageUrl: `https://picsum.photos/seed/citana-${id}/640/420` });
    i++;
  }

  console.log("[seed] clearing bookings…");
  await wipe("bookings");

  console.log("[seed] demo user…");
  const email = "demo@citana.app";
  const password = "demo1234";
  try {
    const u = await authAdmin.getUserByEmail(email);
    await authAdmin.updateUser(u.uid, { password, displayName: "Demo User" });
    console.log("  demo user updated");
  } catch {
    await authAdmin.createUser({ email, password, displayName: "Demo User", emailVerified: true });
    console.log("  demo user created");
  }

  console.log(`[seed] done: ${categories.length} categories, ${providers.length} providers`);
  process.exit(0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
