<div align="center">

# Major's Diary

**A daily planner and class portal for BSSE 5th Semester, Section D — routine, notes, quizzes and reminders in one app.**

[![Download APK](https://img.shields.io/badge/Download-Android%20APK-E2C27D?style=for-the-badge&logo=android&logoColor=1a1204)](https://github.com/Zahid-56/Major-s-diary/releases/latest/download/MajorsDiary.apk)
[![Open web app](https://img.shields.io/badge/Open-Web%20App-1a2133?style=for-the-badge&logo=googlechrome&logoColor=E2C27D)](https://zahid-56.github.io/Major-s-diary/)

<img src="docs/assets/screenshot.jpg" alt="Major's Diary app" width="360">

</div>

## Features

**Class portal (BSSE-5D)**
- **Notes** — the CR uploads PDFs, slides or links by subject; students download them in one tap
- **Quizzes** — MCQ quizzes with optional time limit and deadline; graded on the server, answers stay private
- **Announcements** — class updates from the CR at the top of the Class tab
- **Notifications** — 15-minute class reminders, plus alerts for new notes, quizzes, announcements and quizzes closing soon
- **CR Panel** — sign-in for the CR to upload notes, build quizzes, post announcements and download results as CSV

**Planner**

- **Full day plan** — wake-up to sleep, built around the BSSE-5D class timetable
- **Tap to check off** — progress ring and daily reset
- **Make it yours** — edit any day, copy between days, or start from a blank routine
- **Home workouts** — bodyweight sessions, no gym needed
- **Beautiful backgrounds** — coding and nature photos that change every 2 minutes, or your own photo
- **Personal greeting & quotes** — "Good morning, Major Sahab"; tap the name to change it
- **Home-screen widgets** — a cute 2×2 *Mini* widget (now · next · progress) and a 4×4 *Full day* checklist
- **Works offline** as an installable web app

## Install on Android

1. Download **[MajorsDiary.apk](https://github.com/Zahid-56/Major-s-diary/releases/latest/download/MajorsDiary.apk)** and open it (allow installs from your browser if asked).
2. Long-press your home screen → **Widgets** → **Major's Diary · Mini** or **Full day**.
3. Open the app and tap **Edit** to build your own routine — the widgets update automatically.

## For students

1. Install the app (above) and allow notifications.
2. Open the **Class** tab and enter the class code shared by your CR.
3. Notes, quizzes and reminders now arrive automatically.

## For the CR

Open **[CR Panel](https://zahid-56.github.io/Major-s-diary/admin.html)** (also linked at the bottom of the Class tab). The first time, choose *Create your CR account* with the CR email and confirm it from your inbox.

## Project structure

```
docs/          Web app and CR panel (served by GitHub Pages)
android/       Android app and widgets (Java)
.github/       Workflow that builds the APK and publishes a release
```

## Development

- **Web:** plain HTML, CSS and JavaScript in `docs/` — no build step. Class data lives in Supabase (`docs/config.js`), protected by row-level security.
- **Android:** `cd android && gradle assembleDebug` (JDK 17, Android SDK 34).
- Every push to `android/` builds the APK and publishes it under **Releases**, named after `versionName` in `android/app/build.gradle` (e.g. *Major's Diary 1.0*).

<sub>Background photos from [Unsplash](https://unsplash.com).</sub>
