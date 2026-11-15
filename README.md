# Memories — Your Photos, Your Memories.

Developer: Ahmed Munna · Package: `com.ahmedmunna.memories` · Min SDK 29 (Android 10) · Target SDK 35

Kotlin · Jetpack Compose · Material 3 · Hilt · Coil · Media3 · DataStore. MediaStore is the single source of truth for local media.

## Build status — read this first

**Nothing in this project has been compiled or run yet.** The workspace it was written in has no Android SDK and no network, so there was no way to build, install on an emulator, or produce an APK. Treat the code as a carefully written first draft that must be built once on a machine with Android Studio, where compile errors are likely and should be fixed first.

Dependency versions in `gradle/libs.versions.toml` were chosen from memory and not checked against Maven. If sync complains, bump them (Android Studio suggests versions).

## How to build

1. Install Android Studio (current stable) with Android SDK 35.
2. Open this folder (`File → Open`). Let Gradle sync. If no `gradlew` exists, Android Studio will use its bundled Gradle; or run `gradle wrapper` once.
3. Run on a device/emulator with photos (emulator: drag images in, or use Device Explorer).
4. Debug APK: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/`.

Release builds and AAB signing are Phase 5.

## Phase 1 — what exists

| Area | Implemented in code (untested) |
|---|---|
| Splash | System splash + animated Compose splash (logo, MEMORIES, tagline, "Developed by Ahmed Munna") |
| Navigation | Bottom bar with 5 tabs; Photos and Albums work, Memories/Search/Cloud show a clearly labelled "planned for Phase N" screen with no controls |
| Photos | MediaStore timeline of photos+videos, day headers, 2–5 columns with pinch to change density, video duration badge, GIF badge, multi-select with select all, share, move to trash, sort (date/name/size/type, asc/desc), filters (all/photos/videos/screenshots/GIFs/RAW) |
| Albums | Device folders detected automatically (Camera, Screenshots, Download, WhatsApp, …), cover thumbnail, item count, sort, open an album with the same grid |
| Viewer | Swipe between items, pinch zoom, double-tap zoom, swipe down to close, share, move to trash, open with, set as wallpaper, details sheet (name, date, resolution, size, format, folder, camera, exposure, GPS + open in maps) |
| Video | Media3 ExoPlayer with the standard controller (play/pause, seek, its built-in settings menu for speed) |
| Settings | Theme (system/light/dark), dynamic colors, default grid columns, default sort — all persisted |
| About | Logo, tagline, developer, version, package, copyright, privacy note, open-source license list |
| Icon | Adaptive icon with themed (monochrome) layer |

## Phase 1 — known gaps (not done, not faked)

- **Android 10:** "move to trash" needs Android 11+ (`createTrashRequest`); on Android 10 the app shows a message instead.
- **Trash screen / restore**, **favorites**, **recently viewed**, **hidden media**, **recently added**: not built. Favorites/hidden need the Room database, which arrives with Phase 2/3.
- **Album management:** create/rename/delete albums, move/copy, custom covers, hide/lock, smart albums are not built (create/move/copy need MediaStore write requests; hide/lock need the private store from Phase 3).
- **Fast date scrubber** on the timeline, month/year grouping levels: not built (day grouping only).
- **Viewer:** copy image, move image, favorite not built.
- **Video player:** volume/brightness gestures, picture-in-picture, forced landscape/fullscreen handling not built. Subtitle and speed selection rely on the stock Media3 controller and are unverified.
- **Android 14 partial photo access** ("Select photos") is not handled; the app expects full media access.
- **Localization:** UI strings are hardcoded English for now.
- **Thumbnails:** Coil decodes from content URIs. A `ContentResolver.loadThumbnail` fetcher would be faster on huge libraries (Phase 5).

## Roadmap

2. Cloud auth, backup, sync (needs your Firebase project: `google-services.json`, Auth, Storage, Firestore, security rules)
3. Private gallery, app lock, encryption, photo editor
4. On-device search/AI, memories and stories
5. Tests, performance pass, signed release APK/AAB
