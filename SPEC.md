# Citana — Build Contract (SPEC)

Native Android appointment-booking app. Kotlin + Jetpack Compose (Material 3),
MVVM + Hilt, Firebase Auth, a Node REST API (Vercel, Firestore-backed, verifies
Firebase ID tokens), Retrofit, Room offline cache, Firestore realtime for live
booking status. EN/ES/PT. Coral/amber design.

## Domain model

- **Category**: id, slug, name, icon (material icon key).
- **Provider**: id, name, categorySlug, bio, city, rating (0-5), ratingCount,
  imageUrl, weekly availability (per-weekday open windows), services[].
- **Service**: id, name, durationMin, priceCents, currency ("USD").
- **Slot**: a free start time "HH:mm" for a date (server-computed).
- **Booking**: id, userId, providerId, providerName, serviceId, serviceName,
  startAt (ISO), endAt (ISO), priceCents, currency, status
  (confirmed|cancelled), createdAt.

## Backend — Node + Express + TS on Vercel (`backend/`)

- `firebase-admin` init from `FIREBASE_SERVICE_ACCOUNT` (JSON string env);
  Firestore is the datastore; `auth.verifyIdToken` for protected routes.
- Base URL (prod): `https://citana-api.vercel.app/`. Routes (no /api prefix):
  - `GET /health`
  - `GET /categories`
  - `GET /providers?category=<slug>` · `GET /providers/:id`
  - `GET /providers/:id/availability?date=YYYY-MM-DD&serviceId=<id>` -> `{ date, slots: ["09:00", ...] }`
    (weekly window − existing confirmed bookings, sliced by service duration, no past times)
  - `POST /bookings` (auth) `{ providerId, serviceId, startAt }` -> Booking
    (re-checks slot is free; writes Firestore `bookings` doc; status `confirmed`)
  - `GET /me/bookings` (auth) -> Booking[] (the caller's, newest first)
  - `PATCH /bookings/:id` (auth) `{ status: "cancelled" }` -> Booking
- Firestore collections: `categories`, `providers` (services + availability on
  the doc), `bookings` (per-user). Security rules: a signed-in user may READ
  categories/providers, and read/update only their own `bookings` docs
  (deployed via the service account; client realtime read path).
- `seed.ts`: 6 categories, ~10 providers (services + availability), demo user
  `demo@citana.app` / `demo1234`.

## Android packages (`com.citana.app`)

- `data.remote`: `CitanaApi` (Retrofit), DTOs, `AuthInterceptor` (attaches Firebase ID token).
- `data.local`: Room `CitanaDb`, entities (ProviderEntity, BookingEntity), DAOs.
- `data.auth`: `AuthRepository` (FirebaseAuth: signIn/signUp/signOut/currentUser, demo).
- `data.repo`: `CatalogRepository` (categories/providers, offline-first via Room),
  `BookingRepository` (availability, create, list, cancel; Firestore realtime flow
  for live status).
- `domain.model`: Category, Provider, Service, Slot, Booking.
- `di`: NetworkModule, DbModule, FirebaseModule, RepoModule.
- `ui.theme`: done. `ui.components`: buttons, cards, top bar, rating, loading/empty/error, avatar.
- `ui.<feature>`: each = `<Feature>Screen.kt` + `<Feature>ViewModel.kt` (StateFlow UiState):
  `auth` (sign in/up + one-tap demo), `home` (categories + featured providers),
  `browse` (by category, search), `provider` (detail + services + pick),
  `booking` (date picker + slots + confirm), `bookings` (list, live status, cancel),
  `profile` (account, language, theme, sign out).
- `ui.nav`: `CitanaNavHost` (routes) + bottom bar (Home, Bookings, Profile). Auth
  gate: unauthenticated -> auth screen.

## Navigation routes

`auth` · `home` · `browse/{categorySlug}` · `provider/{id}` ·
`book/{providerId}/{serviceId}` · `bookings` · `profile`.

## State pattern

Each ViewModel exposes `StateFlow<UiState>` where UiState = Loading | Error(msg) |
Success(data). Repos return `Result<T>`; offline-first reads emit Room first then refresh.

## i18n

`res/values/strings.xml` (en), `values-es/strings.xml`, `values-pt/strings.xml` —
exact key parity. All UI text via `stringResource`. Screenshots in EN.

## Design tokens (in ui.theme)

Coral primary `#F15A38`, amber secondary, peach containers, warm neutrals,
Material 3 light + dark. Rounded 16-20dp cards, large booking CTAs.

## Deliverables / verify

`./gradlew assembleDebug` (cached gradle 8.9) clean; run on `Pixel_6a` emulator
(KVM, RAM check first), drive auth -> browse -> book -> my bookings -> cancel ->
offline. Backend live on Vercel + Firestore rules deployed. EN screenshots
(light+dark). GitHub repo + debug APK + README. No live app URL (it's an APK).
