# Habs 🌱

A habit tracker Android app with Google Calendar integration, built with Jetpack Compose and Material You.

## Tech stack
- **UI** — Jetpack Compose + Material 3
- **Architecture** — Clean Architecture (data / domain / presentation)
- **DI** — Hilt
- **Database** — Room
- **Background work** — WorkManager
- **Calendar** — Google Calendar API v3
- **Auth** — Google Sign-In

## Project structure
```
app/src/main/java/com/habs/
├── data/
│   ├── local/         # Room database, DAOs, entities
│   ├── remote/        # Google Calendar API wrapper
│   └── repository/    # Repository implementations
├── domain/
│   ├── model/         # Plain Kotlin data models
│   ├── repository/    # Repository interfaces
│   └── usecase/       # Business logic use cases
├── presentation/
│   ├── today/         # Today screen + ViewModel
│   ├── stats/         # Stats screen + ViewModel (monthly/yearly/heatmap)
│   ├── calendar/      # Calendar sync screen + ViewModel
│   ├── theme/         # Material You theme
│   └── navigation/    # NavGraph
├── di/                # Hilt modules
└── worker/            # WorkManager (CalendarSyncWorker, BootReceiver)
```

## Setup

### 1. Google Calendar API
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a project → Enable **Google Calendar API**
3. Create OAuth 2.0 credentials (Android app)
4. Add your SHA-1 fingerprint and package name `com.habs`
5. Download `google-services.json` → place in `app/`

### 2. Build & run
```bash
./gradlew assembleDebug
```

### 3. Signing (release)
```bash
./gradlew assembleRelease
```

## Features
- ✅ Daily habit tracking with completion toggle
- 📊 Monthly & yearly stats with heatmap
- 📅 Google Calendar sync — habits become recurring events
- 🔥 Streak tracking per habit
- 🌙 Material You dark/light mode
- ⚙️ Background calendar sync via WorkManager
