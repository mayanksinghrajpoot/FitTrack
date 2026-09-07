# 🏃‍♂️ FitTrack — GPS Fitness & Route Tracker (Android)

> **Modern Athletic GPS Running & Fitness Route Tracker** built with **Kotlin** and **Jetpack Compose** featuring a high-contrast **Crimson Red & Crisp White** athletic design system.

---

## 📸 Highlights & Features

- **🔴 Athletic Red & White Design System:** High-visibility contrast engineered for outdoor running, sports statistics, and route tracking.
- **📍 Real-Time GPS Tracking & Map Path:** Tracks live location updates and draws an athletic Crimson Red polyline route on Google Maps.
- **⚡ Foreground Tracking Service:** Continues recording location, distance, time, and pace even if the app is minimized or screen is locked, backed by an active status-bar notification.
- **💾 Room Database (SQLite):** Persists every run session (distance, duration, average pace, calories burned, and serialized GPS coordinates) with zero data loss.
- **📜 Modern RecyclerView (LazyColumn):** Renders recent activities and comprehensive workout history with smooth performance and item deletion.
- **⏱️ Live Metrics Engine:** Real-time distance calculation (Haversine formula), pace in `min'sec" /km`, and calorie estimation.

---

## 🧩 Android Concepts Implemented

| Concept | Implementation in FitTrack |
| :--- | :--- |
| **RecyclerView** | Replaced with modern, idiomatic Jetpack Compose **`LazyColumn`** (`HomeScreen.kt` & `RunHistoryScreen.kt`) with recycled item composition and unique stable keys. |
| **Kotlin Coroutines & Flow** | Used extensively across the app: `StateFlow` for reactive UI states, `Flow` in Room queries, and `Dispatchers.IO` for async database and GPS math operations. |
| **Foreground Service** | `TrackingService.kt` runs as a foreground service with `FOREGROUND_SERVICE_TYPE_LOCATION`, maintaining live updates and notifications. |
| **SQLite & Room Database** | `RunDatabase.kt`, `RunDao.kt`, and `RunEntity.kt` manage local persistence, type conversion, and aggregation queries (`SUM`, `COUNT`). |
| **Location with Map** | Uses Google Play Services `FusedLocationProviderClient` + official Google Maps Compose (`maps-compose`) with dynamic polyline route drawing. |

---

## 🏗️ Architecture: Clean Architecture + MVVM + UDF

```
┌─────────────────────────────────────────────────────────────┐
│                 PRESENTATION LAYER (UI)                     │
│  • Jetpack Compose Screens (HomeScreen, ActiveTracking,     │
│    RunHistoryScreen)                                        │
│  • LazyColumn (Compose RecyclerView)                        │
│  • Google Maps Compose (Live Red Polyline)                  │
└──────────────────────────────▲──────────────────────────────┘
                               │ StateFlow (UiState)
                               ▼ Events (Start, Pause, Stop)
┌─────────────────────────────────────────────────────────────┐
│                    VIEWMODEL LAYER                          │
│  • HomeViewModel, ActiveTrackingViewModel, HistoryViewModel │
│  • Manages Coroutines (viewModelScope)                      │
└──────────────────────────────▲──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                    REPOSITORY LAYER                         │
│  • Single Source of Truth (RunRepository)                   │
└──────────────▲───────────────────────────────▲──────────────┘
               │                               │
┌──────────────▼──────────────┐ ┌──────────────▼──────────────┐
│       SERVICE LAYER         │ │      DATA LAYER (ROOM)      │
│  • TrackingService          │ │  • RunEntity & SQLite DB    │
│    (Foreground Service)     │ │  • RunDao (Flow Queries)    │
│  • FusedLocationClient      │ │  • Dispatchers.IO for async │
│  • Persistent Notification  │ │    database operations      │
└─────────────────────────────┘ └─────────────────────────────┘
```

---

## 🚀 How to Open and Run in Android Studio

1. **Clone / Open the Project:**
   - Launch **Android Studio** (Hedgehog, Iguana, Jellyfish, or newer).
   - Select **File > Open...** and navigate to this `FitTrack` folder.

2. **Gradle Sync:**
   - Android Studio will automatically recognize the Gradle wrapper and version catalog (`gradle/libs.versions.toml`).
   - Allow Gradle to download dependencies and sync.

3. **(Optional) Add Google Maps API Key:**
   - In `app/build.gradle.kts`, locate:
     ```kotlin
     manifestPlaceholders["MAPS_API_KEY"] = "YOUR_GOOGLE_MAPS_API_KEY"
     ```
   - Replace `"YOUR_GOOGLE_MAPS_API_KEY"` with your valid Google Maps API Key from the Google Cloud Console (with *Maps SDK for Android* enabled).
   - *Note: The app will run and track location/metrics even without a key, but the map tiles will show the Google watermark until a valid key is provided.*

4. **Run the App:**
   - Connect an Android device or start an Android Virtual Device (AVD Emulator).
   - Click **Run (Shift + F10)**.
   - Grant the Location and Notification permissions when prompted.
   - Tap **START NEW RUN** to record your first workout!
