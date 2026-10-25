# Major's Diary

A personal daily planner built around the BSSE-5D class timetable — classes, study blocks, home workouts (no gym), naps and work-from-home shifts — with daily check-offs.

**Web app:** https://zahid-56.github.io/Major-s-diary/
**Android widget:** download the latest `MajorsDiary.apk` from [Releases](https://github.com/Zahid-56/Major-s-diary/releases/latest)

## Features

- Full day plan from wake-up to sleep, with a day picker for the week
- Home bodyweight workouts — no gym needed
- Highlights what you should be doing right now
- Tap any item to check it off; progress resets every day
- Installable web app (PWA) that works offline
- Native Android home-screen widget with live progress

## Android widget

1. Download and install `MajorsDiary.apk` from the latest release.
2. Long-press your home screen → **Widgets** → **Major's Diary**.
3. Tap a row to check it off. Tap the header to open the full planner.

## Project structure

```
index.html, sw.js, manifest.webmanifest, icon*   Web app (GitHub Pages)
android/                                          Android app + widget (Java)
.github/workflows/android.yml                     Builds the APK and publishes a release
```

## Updating the schedule

- Web: edit the `week` data in `index.html`.
- Android widget: edit `android/app/src/main/java/com/zahid/majorsdiary/Schedule.java`.

Pushing changes to `android/` automatically builds a new APK release.
