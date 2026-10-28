<div align="center">

  <img width="110" height="110" alt="Dead Sleep Logo" src="https://github.com/user-attachments/assets/b950bf83-a9a2-49c9-b33c-46b7746e4d79" />
  <h2><b>Dead Sleep</b></h2>
  <h4><i>"Sleep on time. Wake up on time."</i></h4>

  <p align="center">
    A production-grade, 100% offline Android bedtime companion designed to eliminate bedtime phone distraction through automated app blocking, proactive bedtime nudges, and cognitive puzzle wake-up alarms.
  </p>

  <br />

  <a href="https://github.com/tanjeem180hz/Dead-Sleep/raw/main/release/DeadSleep-v1.0.0.apk">
    <img src="https://img.shields.io/badge/📥_Download_Direct_APK_(v1.0.0)-22c55e?style=for-the-badge&logo=android&logoColor=white" alt="Download APK" height="38" />
  </a>
  &nbsp;&nbsp;
  <a href="https://tanjeem180hz.github.io/">
    <img src="https://img.shields.io/badge/🚀_Launch_Live_Web_Demo-8C7CFB?style=for-the-badge&logo=googlechrome&logoColor=white" alt="Live Demo" height="38" />
  </a>

  <br /><br />

  [![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://www.java.com)
  [![Android SDK](https://img.shields.io/badge/Android%20SDK-min%2026%20%7C%20target%2035-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
  [![Architecture](https://img.shields.io/badge/Architecture-Clean%20OOP%20%7C%20MVVM-8C7CFB?style=flat-square)](https://en.wikipedia.org/wiki/Object-oriented_programming)
  [![Database](https://img.shields.io/badge/Database-Room%202.6.1-4285F4?style=flat-square)](https://developer.android.com/training/data-storage/room)
  [![Offline](https://img.shields.io/badge/Offline-100%25%20No%20Internet-brightgreen?style=flat-square)](https://github.com/tanjeem180hz/Dead-Sleep)
  [![Build](https://img.shields.io/badge/Build-Gradle%208.11%20Passing-success?style=flat-square)](https://github.com/tanjeem180hz/Dead-Sleep)

</div>

---

## 📖 Table of Contents
1. [Core Product Concept](#-core-product-concept)
2. [Architecture & Design Principles](#-architecture--design-principles)
3. [Package Structure](#-package-structure)
4. [Complete Feature Breakdown](#-complete-feature-breakdown)
   - [User Setup & Guided Permission Wizard](#1-user-setup--permission-wizard)
   - [Sleep Schedule Management](#2-sleep-schedule-management)
   - [Bedtime Nudge System](#3-bedtime-nudge-system)
   - [App Restriction & Automated Sleep Mode](#4-app-restriction--automated-sleep-mode)
   - [Tamper Protection & Kiosk Defense](#5-tamper-protection--kiosk-defense)
   - [Smart Wake Alarm & Audio Engine](#6-smart-wake-alarm--audio-engine)
   - [Cognitive Wake Challenges (5 Types)](#7-cognitive-wake-challenges)
   - [Statistics & Consistency Scoring](#8-statistics--consistency-scoring)
   - [Intelligent Routine Advisor](#9-intelligent-routine-advisor)
   - [Whitelist & Emergency Exit](#10-whitelist--emergency-exit)
5. [Permissions & OEM Optimization Guide](#-permissions--oem-optimization-guide)
6. [How to Run in Android Studio](#-how-to-run-in-android-studio)
7. [Testing & Verification](#-testing--verification)

---

## 🌌 Core Product Concept

Dead Sleep fights bedtime phone addiction in three synchronized phases:

```
  ┌─────────────────────────────────────────────────────────────┐
  │ 1. PLAN: Configure sleep schedule & select distracting apps  │
  └──────────────────────────────┬──────────────────────────────┘
                                 │ 30 min before bedtime: Nudge alert
                                 ▼
  ┌─────────────────────────────────────────────────────────────┐
  │ 2. RESTRICT: Sleep Mode locks apps & defends uninstallation │
  └──────────────────────────────┬──────────────────────────────┘
                                 │ At wake time: High-priority alarm
                                 ▼
  ┌─────────────────────────────────────────────────────────────┐
  │ 3. AWAKEN: Dismiss alarm only by solving cognitive puzzles   │
  └─────────────────────────────────────────────────────────────┘
```

1. **Plan:** Choose your bedtime, wake time, and weekly active days. Select which apps to lock (social media, games, video streaming, browsers).
2. **Restrict:** When sleep mode begins, selected apps are instantly locked with a full-screen overlay. The application protects itself from being force-stopped or uninstalled via Device Admin and Accessibility Service guards.
3. **Awaken:** In the morning, you cannot silence the alarm with a tap. You must complete interactive cognitive challenges (Math, Memory, Pattern Memory, Reaction, Shake) to prove full consciousness.

---

## 🏛️ Architecture & Design Principles

Dead Sleep is engineered using **pure Java 17** with a clean, layered, object-oriented design:

- **Abstraction & Polymorphism:** The `Challenge` abstract base class defines common lifecycle contracts (`generate()`, `validate()`, `renderView()`, `cleanup()`). Subclasses (`MathChallenge`, `MemoryChallenge`, `PatternMemoryChallenge`, `ReactionChallenge`, `ShakeChallenge`) implement specialized game logic. The `ChallengeFactory` dynamically instantiates the appropriate challenge.
- **Single Responsibility Principle (SRP):** Each manager handles an isolated domain:
  - `ScheduleManager`: Single source of truth for all `AlarmManager` schedules.
  - `SleepModeManager`: Sleep session lifecycle, state machine, and edit-locking.
  - `RestrictionManager`: App categorisation, package blocking, and system app immunity.
  - `DeadSleepAlarmManager`: Ringing state, volume ramping, snooze limits, and 10s test alarms.
  - `StatsManager`: Aggregates sessions, blocked attempts, streaks, and consistency scores.
  - `RoutineAdvisor`: 14-day heuristic recommendation engine.
- **Repository Pattern & Reactive UI:** `DeadSleepRepository` coordinates Room DAOs with background executor threads and exposes observable `LiveData` to UI components.
- **100% Offline by Design:** Zero internet permissions declared in `AndroidManifest.xml`. All data is stored locally in Room SQLite (`dead_sleep.db`) and encrypted SharedPreferences.
- **Custom Canvas Rendering:** `SleepDurationChartView` and `ConsistencyGaugeView` render custom charts without external charting dependencies.

---

## 📁 Package Structure

```
com.deadsleep.app/
├── challenge/                  # Cognitive wake puzzle engine
│   ├── Challenge.java          # Base abstract class
│   ├── ChallengeFactory.java   # Dynamic factory pattern
│   ├── MathChallenge.java      # Dynamic arithmetic with custom numeric keypad
│   ├── MemoryChallenge.java    # Card-flip matching game (12 to 20 cards)
│   ├── PatternMemoryChallenge.java # Simon-style sequence memory (4 to 8 steps)
│   ├── ReactionChallenge.java  # Millisecond reflex test with false-start detection
│   └── ShakeChallenge.java     # Accelerometer shake counter with progress ring
├── data/                       # Local persistence layer
│   ├── AppDatabase.java        # Room database singleton with default seeding
│   ├── DeadSleepRepository.java# Repository coordinating Room DAOs & executors
│   ├── PreferencesManager.java # Type-safe wrapper for SharedPreferences
│   └── dao/                    # Room Data Access Objects
│       ├── BlockedAttemptDao.java
│       ├── ChallengeResultDao.java
│       ├── RestrictedAppDao.java
│       ├── SleepScheduleDao.java
│       ├── SleepSessionDao.java
│       └── WhitelistDao.java
├── manager/                    # Core business logic managers
│   ├── ChallengeManager.java   # Snooze auto-escalation & challenge preparation
│   ├── DeadSleepAlarmManager.java # Audio focus, volume lock & alarm trigger
│   ├── ReminderManager.java    # Bedtime lead nudges (15-60m) and 5m final alerts
│   ├── RestrictionManager.java # Package lockdown, auto-scanning & immunity
│   ├── RoutineAdvisor.java     # Rule-based sleep pattern recommendations
│   ├── ScheduleManager.java    # Master scheduling via Android AlarmManager
│   ├── SettingsManager.java    # CSV statistics export, JSON backup/restore
│   ├── SleepModeManager.java   # Sleep mode activation, edit-lock & emergency exit
│   └── StatsManager.java       # Consistency score (0-100) & streak tracking
├── model/                      # POJOs and Room database entities
│   ├── AlarmConfig.java
│   ├── BlockedAttempt.java
│   ├── ChallengeResult.java
│   ├── RestrictedApp.java
│   ├── RoutineSuggestion.java
│   ├── SleepSchedule.java
│   ├── SleepSession.java
│   ├── UserProfile.java
│   └── WhitelistEntry.java
├── receiver/                   # BroadcastReceivers for system events
│   ├── AlarmReceiver.java      # Triggers morning alarm
│   ├── BootReceiver.java       # Reschedules alarms on boot / timezone / time change
│   ├── DeadSleepDeviceAdminReceiver.java # Defends against device admin deactivation
│   ├── NudgeReceiver.java      # Dispatches bedtime nudge notifications
│   ├── PackageChangeReceiver.java # Updates restricted app catalogue on app install
│   ├── SleepEndReceiver.java   # Concludes sleep session
│   └── SleepStartReceiver.java # Initiates scheduled sleep mode
├── service/                    # Background & foreground services
│   ├── AlarmForegroundService.java # High-priority alarm audio player with wake lock
│   ├── AppLockAccessibilityService.java # Window state tracker & tamper defense
│   ├── SleepModeForegroundService.java  # Persistent sleep mode notification
│   └── UsageMonitorService.java         # Fallback usage stats poller
├── ui/                         # Activities, Fragments, Adapters, Custom Views
│   ├── AboutActivity.java
│   ├── AlarmRingingActivity.java
│   ├── AlarmTestActivity.java
│   ├── AppPickerActivity.java
│   ├── AppsFragment.java
│   ├── ChallengeActivity.java
│   ├── ConsistencyGaugeView.java
│   ├── GoodMorningActivity.java
│   ├── HomeFragment.java
│   ├── LockOverlayActivity.java
│   ├── MainActivity.java
│   ├── OnboardingActivity.java
│   ├── PermissionWizardActivity.java
│   ├── RoutineAdvisorActivity.java
│   ├── ScheduleEditActivity.java
│   ├── ScheduleListFragment.java
│   ├── SettingsFragment.java
│   ├── SleepDurationChartView.java
│   ├── SleepModeActiveActivity.java
│   ├── SplashActivity.java
│   ├── StatsActivity.java
│   ├── StatsFragment.java
│   ├── WhitelistActivity.java
│   └── adapter/
│       ├── AppAdapter.java
│       ├── OnboardingAdapter.java
│       ├── RoutineAdapter.java
│       ├── ScheduleAdapter.java
│       └── WhitelistAdapter.java
└── util/                       # Static helpers and utilities
    ├── Constants.java          # App constants, actions, channel IDs
    ├── NotificationHelper.java # Channels, ongoing notifications, full-screen intents
    ├── PermissionUtils.java    # Permission validation & health checklist
    ├── SoundPlayer.java        # Audio engine with fade-in volume ramp & bundled PCM
    ├── TimeUtils.java          # Midnight math, day bitmasks, countdown formatters
    └── VibrationHelper.java    # Haptic feedback & alarm vibration waveforms
```

---

## 🚀 Complete Feature Breakdown

### 1. User Setup & Permission Wizard
- **Android 12+ Splash Screen API:** Custom animated night moon logo.
- **Onboarding (ViewPager2):** 4 interactive slides explaining the Plan, Restrict, and Awaken phases.
- **Guided Permission Wizard:** 8-step permission configuration with real-time status check:
  1. Notification Permission (`POST_NOTIFICATIONS` on Android 13+)
  2. Usage Access (`PACKAGE_USAGE_STATS`)
  3. Accessibility Service (`AppLockAccessibilityService`)
  4. Display Over Other Apps (`SYSTEM_ALERT_WINDOW`)
  5. Exact Alarms (`SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM`)
  6. Device Administrator (`BIND_DEVICE_ADMIN`)
  7. Battery Optimizations Exemption (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`)
  8. Full-Screen Intent Permission (Android 14+)
- **Setup Health Checklist:** Surfaces real-time missing permission warnings on the Home screen with 1-tap resolution.

### 2. Sleep Schedule Management
- **Material Time Pickers:** Set sleep and wake times with duration computation.
- **Midnight-Crossing Math:** Full support for overnight schedules (e.g., 23:30 to 07:00).
- **Weekly Bitmask Repeat Rules:** Independent chips for Mon–Sun, weekday vs. weekend profiles.
- **Duration Safety Warnings:** Highlights warnings if scheduled sleep is under 6 hours or over 10 hours.
- **Schedule Edit Lock:** Modifying or disabling schedules while sleep mode is active is strictly locked.

### 3. Bedtime Nudge System
- **Lead Time Nudges:** Exact notification fired 15, 30, 45, or 60 minutes before bedtime (default: 30 minutes).
- **Interactive Actions:** "Start Sleep Now" (initiates sleep mode early) or "Snooze 10 min".
- **Final 5-Minute Warning:** Optional secondary countdown nudge.
- **Reboot Resilience:** `BootReceiver` restores all pending nudges upon reboot, time change, or app update.

### 4. App Restriction & Automated Sleep Mode
- **Smart App Picker:** Installed launchable apps categorized into Social, Games, Video/Streaming, Browsers, and Other.
- **One-Tap Category Selection:** Quickly lock all apps in a specific category.
- **Real-Time Lockout:** `AppLockAccessibilityService` detects foreground app transitions. If restricted, it instantly redirects to the `LockOverlayActivity`.
- **Blocked Attempt Logging:** Every blocked access attempt is logged to the Room database with timestamp and package name.

### 5. Tamper Protection & Kiosk Defense
- **Device Admin Guard:** `DeadSleepDeviceAdminReceiver.onDisableRequested()` blocks removal while sleep mode is active.
- **Settings & Installer Shield:** Accessibility service automatically closes system App Info screens, uninstallation dialogs, and accessibility toggle menus targeting Dead Sleep during active sleep mode.
- **Safe Mode Detection:** Detects reboot into Safe Mode on next launch and records an incident flag.

### 6. Smart Wake Alarm & Audio Engine
- **Audio Ramp Engine:** Linear or exponential volume fade-in over 10 to 60 seconds to prevent morning shock.
- **5 Bundled 16-Bit PCM Ringtones:**
  - `tone_gentle_chime.wav`
  - `tone_morning_dawn.wav`
  - `tone_digital_beep.wav`
  - `tone_cosmic_echo.wav`
  - `tone_wake_bell.wav`
- **Volume Lock:** Re-asserts maximum alarm volume via an observer if hardware volume keys are pressed during ringing.
- **Snooze Discipline:** Configurable maximum snoozes (0 to 3); once exhausted, snooze buttons are disabled.
- **Alarm Sandbox:** Dedicated "Test Alarm in 10s" screen for safe verification.

### 7. Cognitive Wake Challenges
The alarm cannot be silenced by tapping. The user must solve one of 5 cognitive challenges:
1. **Math Challenge:** Dynamic arithmetic (Easy: 2-term addition; Medium: multiplication/subtraction; Hard: 3-term operations) with a custom in-app numeric keypad.
2. **Memory Challenge:** Card-flip matching game (12 cards on Easy, 16 on Medium, 20 on Hard).
3. **Pattern Memory Challenge:** Simon-style colored sequence memory (4 to 8 steps).
4. **Reaction Challenge:** Millisecond reflex test requiring tap within 300–600ms of visual signal with false-start detection.
5. **Shake Challenge:** Accelerometer physical movement counter (15 to 45 shakes) with an interactive progress ring.
- **Snooze Escalation:** Each snooze automatically increases puzzle difficulty by one tier.
- **Anti-Entrapment Emergency Exit:** 10-second hold + confirmation math problem to prevent lockouts in case of hardware sensor faults.

### 8. Statistics & Consistency Scoring
- **Sleep Consistency Score (0–100):** Evaluated against schedule adherence (40%), wake-time punctuality (30%), blocked night temptations (15%), and alarm response speed (15%).
- **Custom Canvas Charts:**
  - `SleepDurationChartView`: 7-day bar chart with target bedtime markers.
  - `ConsistencyGaugeView`: Radial speedometer gauge for health scores.
- **Data Export & Backup:** Export history to CSV; full JSON settings backup and restore.

### 9. Intelligent Routine Advisor
- **14-Day Pattern Analyzer:** Detects chronic snooze habits, consistent early/late sleep variations, and weekend schedule drift.
- **Actionable Suggestions:** Generates actionable cards with 1-tap "Apply Adjustment" buttons.

### 10. Whitelist & Emergency Exit
- **App Immunity:** Essential dialer, emergency calls, navigation, and system alarms are permanently immune.
- **Custom Contact & App Whitelist:** Users can whitelist specific applications and phone numbers.
- **Emergency Sleep Exit:** 10-second button hold with mandatory confirmation prompt allows emergency sleep termination.

---

## 🛡️ Permissions & OEM Optimization Guide

Dead Sleep relies on standard Android platform APIs to function reliably without internet access.

| Permission | Purpose |
| :--- | :--- |
| `POST_NOTIFICATIONS` | Nudge reminders, active sleep notification, and wake alarms |
| `PACKAGE_USAGE_STATS` | Secondary foreground app detection and usage statistics |
| `BIND_ACCESSIBILITY_SERVICE` | Real-time app window locking and uninstallation defense |
| `SYSTEM_ALERT_WINDOW` | Full-screen lock overlay over restricted apps |
| `SCHEDULE_EXACT_ALARM` | Precise wake-up alarm and nudge firing in Doze mode |
| `BIND_DEVICE_ADMIN` | Prevents app uninstallation during active sleep mode |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Ensures services survive Android Doze |
| `USE_FULL_SCREEN_INTENT` | Displays alarm ringing screen over the lock screen |

### OEM Autostart & Battery Settings:
Aggressive OEM battery managers may kill background services if not whitelisted:
- **Xiaomi (MIUI / HyperOS):** Settings → Apps → Manage Apps → Dead Sleep → Enable **Autostart**. Set Battery Saver to **No restrictions**.
- **Samsung (OneUI):** Settings → Battery and device care → Battery → Background usage limits → Add Dead Sleep to **Never sleeping apps**.
- **Oppo / Realme (ColorOS):** Settings → App Management → Dead Sleep → Allow Auto-launch and Background activity.
- **Vivo (FuntouchOS):** Settings → Battery → High background power consumption → Enable Dead Sleep.
- **Huawei (EMUI):** Settings → Battery → App Launch → Dead Sleep → Manage manually (Enable Auto-launch, Secondary launch, Run in background).

---

## 💻 How to Run in Android Studio

1. **Prerequisites:**
   - Android Studio Meerkat or newer.
   - Java Development Kit: **JDK 17** (configured as Gradle JDK).
   - Android SDK Platform **API 35** and Build-Tools **34.0.0+**.

2. **Open the Project:**
   - Launch Android Studio.
   - Select **Open** and browse to the repository root: `e:\GitHubProject\DeadSleep`.
   - Android Studio will automatically recognize the Gradle project and trigger a sync.

3. **Verify Gradle Sync:**
   - File → **Sync Project with Gradle Files**. The sync will complete with 0 errors.

4. **Run on Emulator or Device:**
   - Select an emulator or connected physical device (Android 8.0 / API 26 or higher).
   - Click the green **Run (Shift + F10)** button.
   - Complete the onboarding and guided permission wizard to activate sleep protection.

---

## 🧪 Testing & Verification

### Running Unit Tests via CLI:
```bash
./gradlew test
```
*All test suites (`TimeUtilsTest`, `ChallengeValidationTest`, `StatsLogicTest`, `RoutineAdvisorTest`) execute with 100% pass rate.*

### Building the Debug APK:
```bash
./gradlew assembleDebug
```
*Outputs the compiled APK at `app/build/outputs/apk/debug/app-debug.apk`.*

### Running Instrumented UI Tests:
```bash
./gradlew connectedAndroidTest
```
*Executes `MainActivityTest` across connected Android devices or emulators.*

---

## 🔒 Offline & Privacy Guarantee

- **Zero Network Permissions:** The app manifest contains no `android.permission.INTERNET` or `android.permission.ACCESS_NETWORK_STATE`.
- **Zero Third-Party Trackers:** No analytics, crash reporters, or advertising SDKs.
- **Local Storage:** All preferences, schedules, and sleep records remain strictly on your physical device.

---

<div align="center">
  <sub>🌙 <b>Dead Sleep</b> · Sleep on time. Wake up on time.</sub><br/>
  <sub>Engineered with care for healthier bedtime habits & restful mornings.</sub>
</div>
