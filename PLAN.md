# Citana — Appointment Booking (Android)

A native Android booking app for appointment businesses (clinics, salons, spas):
browse providers, pick a free time slot, book it, and track your bookings with
live status updates. The mobile sibling of the Reservo web project — same domain,
a different, native client.

Proposed repo: `citana` (alternatives: `turno`, `marca`). Remote
`git@github.com:varunsainani/citana.git`, branch `main`. Android package
`com.citana.app`.

## Stack (hits every item in the brief)

- **Kotlin + Jetpack Compose (Material 3)** — single-activity, Navigation Compose,
  MVVM (ViewModel + StateFlow), Hilt DI, Coroutines/Flow, Coil.
- **Firebase Auth** — email/password sign-in (the single source of identity).
- **Node.js REST API** (Express + TypeScript, Vercel serverless) — the domain logic;
  every protected request carries the Firebase **ID token**, which the backend
  verifies with `firebase-admin`. Datastore = **Firestore** (via firebase-admin),
  so the only cloud account is one free Firebase project (no second database).
- **Retrofit + OkHttp** — REST client, with an interceptor that attaches the ID token.
- **Room** — offline-first cache (providers, services, my bookings) so the app opens
  and browses with no connection, then syncs.
- **Firestore (client)** — realtime layer: a booking's status updates live in the app
  the moment the provider confirms/cancels.
- i18n via Android string resources: **EN + ES + PT** (`values/`, `values-es/`,
  `values-pt/`); screenshots in EN.

## App flow / screens

1. **Auth** — sign up / sign in (Firebase Auth) + a one-tap demo account.
2. **Home** — categories (Salon, Clinic, Spa, Dental, Barber, Wellness) + featured
   providers.
3. **Browse / search** — providers by category, searchable.
4. **Provider detail** — profile, rating, services (name, price, duration),
   pick a service.
5. **Book** — choose a date → server returns free slots (availability minus existing
   bookings; conflict-checked) → confirm.
6. **My bookings** — upcoming + past, status (pending/confirmed/cancelled), cancel.
   Status updates **live** via Firestore.
7. **Profile** — account, language, light/dark, sign out.

## Backend (Node + Express + TS on Vercel)

- Verifies Firebase ID tokens (`firebase-admin`); Firestore as the store.
- Endpoints: `GET /categories`, `GET /providers?category=`, `GET /providers/:id`,
  `GET /providers/:id/availability?date=`, `POST /bookings` (transactional slot
  conflict check), `GET /me/bookings`, `PATCH /bookings/:id` (cancel). Writes booking
  status to a Firestore doc for realtime.
- Seed script: demo categories, providers, services, availability, and a demo user.

## Design (distinct)

Warm, friendly booking aesthetic with a coral/amber accent (not Reservo's teal),
Material 3, rounded cards, large clear booking CTAs, smooth light/dark.

## Build process

1. Scaffold Gradle (Kotlin DSL + version catalog), Compose, Hilt, Retrofit, Room,
   Firebase. minSdk 26, compileSdk 35.
2. Backend: Node API + Firestore + seed (needs Firebase creds).
3. Android: theme/design system + nav scaffold → data layer (Retrofit + Room + repos
   + Firebase auth) → feature screens (parallel agents once the contract exists) →
   i18n strings.
4. **Verify for real:** `./gradlew assembleDebug` (APK), then run on the `Pixel_6a`
   emulator (KVM) and drive the full flow. (RAM check before launching the emulator,
   per the full-audit rule.)
5. EN screenshots (light + dark) from the emulator, README, memory, Fiverr copy.

## Deliverables (Android = no live URL)

GitHub repo + a **debug APK** + **phone screenshots** + README. The **backend** is
live on Vercel. The app is an installable APK, not a link.

## What I'll need from you

A free **Firebase project** (I'll guide you, like the Supabase setup):
- Register an Android app with package **`com.citana.app`** → download `google-services.json`.
- Enable **Authentication → Email/Password**.
- Create **Firestore** (production mode; I'll supply security rules).
- Project settings → Service accounts → **Generate new private key** (JSON) for the
  Node backend.
Plus the Vercel token (already have it) and confirm pushing to `varunsainani`. Goes in
the tokens file. Not needed to start scaffolding — only to wire up auth/data.
