# Citana

A native **Android appointment-booking app** for clinics, salons and spas: browse
providers, pick a free time slot, book it, and track your bookings with live
status. The mobile sibling of the Reservo web project — same domain, a native
client.

Built with **Kotlin + Jetpack Compose (Material 3)**, **Firebase Auth**, a
**Node.js REST API** (Firestore-backed), **Retrofit**, and **Room** offline
cache.

> Android apps don't have a live URL. This repo is the app; the **backend is live**
> at `https://citana-api.vercel.app`, and a debug **APK + screenshots** are the
> deliverables. Demo account: `demo@citana.app` / `demo1234` (or "Try the demo").

## Features

- 🔐 **Firebase Auth** — email/password sign in / sign up, plus one-tap demo.
- 🏠 **Browse** — categories (salon, barber, spa, dental, clinic, wellness) and
  featured providers, with search.
- 📅 **Real booking flow** — a provider's weekly availability minus existing
  bookings is computed server-side into free time slots; pick a date and time and
  confirm. Double-booking is prevented.
- 🗓️ **My bookings** — upcoming and past appointments with confirmed/cancelled
  status; cancel in a tap. Updates live (Firestore realtime, with a REST fallback).
- 📴 **Offline-first** — categories and providers are cached in **Room**, so the
  app opens and browses with no connection and syncs when it's back.
- 🎨 **Material 3**, light + dark, a warm coral/amber design.

## Architecture

```
Android (Kotlin, Compose, MVVM + Hilt)
  Firebase Auth ──► identity (ID token attached to every API call)
  Retrofit ───────► Node REST API ──► Firestore (datastore)
  Room ───────────► offline cache of catalog
  Firestore client ► realtime booking status (best-effort)

Backend (Node + Express + TypeScript, Vercel serverless)
  verifies Firebase ID tokens (firebase-admin)
  Firestore as the database
  computes availability / enforces booking conflicts
```

- Auth is unified: the Android app signs in with Firebase, and the backend
  verifies the Firebase ID token on every protected request — so one identity
  drives both the client and the server.
- Money is integer cents; the slot engine and conflict checks live on the server.

## Tech stack

| Layer    | Technology                                                                   |
| -------- | ---------------------------------------------------------------------------- |
| Android  | Kotlin, Jetpack Compose (Material 3), MVVM, Hilt, Coroutines/Flow, Navigation, Coil |
| Network  | Retrofit + OkHttp + kotlinx.serialization                                    |
| Local    | Room (offline cache)                                                         |
| Identity | Firebase Auth                                                                |
| Backend  | Node.js, Express, TypeScript, firebase-admin, Firestore — on Vercel          |

## Project structure

```
app/                Android app (com.citana.app)
  data/             remote (Retrofit), local (Room), repo, auth, mappers
  domain/model      domain models
  di/               Hilt modules
  ui/               theme, components, nav, and feature screens
backend/            Node REST API (Express + Firestore on Vercel)
```

## Running it

### App
1. Open the project in **Android Studio** (JDK 17+).
2. Add your own Firebase project's `app/google-services.json` (an Android app
   registered with package `com.citana.app`), and point `API_BASE_URL` in
   `app/build.gradle.kts` at your backend.
3. Run on an emulator or device (minSdk 26).

Or build from the CLI:
```bash
./gradlew assembleDebug   # app/build/outputs/apk/debug/app-debug.apk
```

### Backend
```bash
cd backend
npm install
cp .env.example .env       # FIREBASE_SERVICE_ACCOUNT_PATH=<service-account json>
npm run seed               # categories, providers, demo user
npm run start              # local API on :8787
```
Deploy to Vercel with `FIREBASE_SERVICE_ACCOUNT_B64` set (base64 of the service
account). `firestore.rules` ships the intended security rules (a user can read
only their own bookings).

## i18n

The UI ships English, Spanish and Portuguese string resources (`values/`,
`values-es/`, `values-pt/`) and follows the device language.
