# Project Plan

Professional UI/UX overhaul of the Routine Reminder & Water Intake app. Transition from 'unprofessional/flat' to 'premium MD3'.

## Project Brief

muted `col`

# Project Brief: Hydrate & Routine - Premium MD3 Overhaul

## Features
- **Premium Hydration Tracking**: A sleek dashboard log with a Material 3 bar chart featuring normalized vertical scaling (3000ml goal), subtle gradients, and interactive tooltips for daily intake.
- **Smart Routine Reminders**: Modern scheduling system for routine tasks with integrated haptic feedback, expressive top app bars, and smooth state transitions.
- **Dynamic Material 3 UI**: Full implementation of the M3 type scale and modern sans-serif typography, utilizing Tonal/Elevated cards and dynamic color support for a premium feel.
- **Adaptive Edge-to-Edge Experience**: A high-quality layout using full edge-to-edge implementation and professional spacing, ensuring consistency across various device form factors.

## High-Level Technical Stack
- **Kotlin**: Primary language for modern, safe Android development.
- **Jetpack Compose (Material 3)**: Declarative UI toolkit for building the premium design overhaul and dynamic theming.
- **Jetpack Navigation 3**: State-driven navigation architecture for robust and predictable screen flows.
- **Compose Material Adaptive**: Essential library for creating adaptive layouts that scale gracefully across different window sizes.
- **Kotlin Coroutines**: For handling asynchronous tasks such as reminder scheduling and state management without blocking the UI thread.

## Implementation Steps

### Task_1_DataAndViewModel: Define AlarmItem data class and implement WaterIntakeViewModel. Initialize 8 default reminders (9 AM to 11 PM, every 2 hours) and manage custom alarm state.
- **Status:** COMPLETED
- **Updates:** Successfully created AlarmItem data class and WaterIntakeViewModel.
- **Acceptance Criteria:**
  - AlarmItem data class implemented
  - ViewModel initialized with 8 default alarms
  - Logic for toggling and adding alarms implemented in ViewModel

### Task_2_AdaptiveUI: Build the adaptive UI using Jetpack Compose and Compose Material Adaptive. Implement Active/Inactive sections, Master Toggle, and TimePicker for custom alarms. Set up Navigation 3 for app structure.
- **Status:** COMPLETED
- **Updates:** Built the Jetpack Compose UI with Material 3 and Navigation 3.
- **Acceptance Criteria:**
  - Adaptive dashboard implemented
  - Master Toggle functional
  - Active/Inactive sections correctly group alarms
  - TimePicker successfully adds new reminders

### Task_3_AlarmNotificationLogic: Implement AlarmReceiver for notifications and NotificationChannel. Integrate AlarmManager (setExactAndAllowWhileIdle) to schedule reminders. Handle POST_NOTIFICATIONS and SCHEDULE_EXACT_ALARM permissions.
- **Status:** COMPLETED
- **Updates:** Implemented AlarmScheduler using AlarmManager.setExactAndAllowWhileIdle for precise scheduling.
- **Acceptance Criteria:**
  - NotificationChannel configured
  - AlarmReceiver triggers notifications with specified text
  - AlarmManager correctly schedules/cancels alarms
  - Permissions handled for Android 12+ and 13+

### Task_4_EditAlarmAndFullScreen: Implement the ability to edit existing alarms by clicking them and create a Full-Screen Alarm Activity with sound and an 'I Drank' button.
- **Status:** COMPLETED
- **Updates:** Implemented full-screen alarm experience and editable alarm functionality.
- **Acceptance Criteria:**
  - Clicking an alarm opens time picker to modify its time
  - Updated time reflects in the list and triggers at new time
  - Full-screen activity appears when alarm triggers
  - Alarm sound plays during full-screen alert
  - 'I Drank' button dismisses the alert and stops sound

### Task_5_WaterTrackingDataLayer: Implement Room Database with WaterLogEntity and WaterLogDao. Develop WaterStatsViewModel to handle date navigation, daily logs, and 7-day weekly stats calculation.
- **Status:** COMPLETED
- **Updates:** Implemented Room Database tier:
- **Acceptance Criteria:**
  - WaterLogEntity and DAO implemented with day-based queries
  - Room database initialized/migrated
  - WaterStatsViewModel manages date state and fetches logs/stats correctly

### Task_6_WaterUIAndNavigation: Build WaterStatsScreen UI with a 'Digital Wellbeing' dark-mode aesthetic. Include a Weekly Bar Chart, Date Navigator, Log List (LazyColumn), and AddWaterDialog. Integrate with Navigation 3.
- **Status:** COMPLETED
- **Updates:** Implemented WaterStatsScreen UI with Digital Wellbeing aesthetic:
- **Acceptance Criteria:**
  - WaterStatsScreen UI implemented with Jetpack Compose
  - Weekly Bar Chart (Canvas/Box) displays 7-day trends
  - Navigation 3 updated to include the Water Stats screen
  - AddWaterDialog correctly saves intake to database

### Task_7_PremiumMD3RefinementAndVerify: Apply premium Material 3 refinements: normalized chart scaling (3000ml goal), subtle gradients, interactive tooltips, and haptic feedback. Ensure full edge-to-edge support and verify application stability.
- **Status:** COMPLETED
- **Updates:** Complete professional UI/UX refactor to Material 3:
- Typography: Implemented displayLarge (Bold, Primary) for daily totals.
- Bar Chart: Re-engineered with Canvas/Box vertical scaling based on a 3000ml goal, rounded tops, and selection highlighting.
- Surfaces: Transitioned all list items (Alarms, Water Logs) to ElevatedCards with 24dp rounded corners and tonal elevation.
- Date Navigator: Implemented a pill-shaped sleek Surface component for history browsing.
- Navigation: Updated Bottom Navigation with MD3 NavigationBar (selected/unselected icon states) and LargeFloatingActionButton.
- Polish: Added empty states for logs and ensured full edge-to-edge support with consistent MD3 spacing.
- **Acceptance Criteria:**
  - Chart features normalized scaling, gradients, and tooltips
  - Haptic feedback and edge-to-edge support implemented
  - build pass
  - app does not crash
  - make sure all existing tests pass
  - critic_agent verifies application stability and requirement alignment
- **Duration:** N/A

