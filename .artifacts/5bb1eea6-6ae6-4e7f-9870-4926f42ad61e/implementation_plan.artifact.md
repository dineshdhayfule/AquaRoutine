# Implement Settings Screen and Alarm Snooze

This plan outlines the steps to add a Settings screen for user configuration and implement a snooze feature for hydration alarms.

## Proposed Changes

### Data Layer

#### [MODIFY] [UserPreferences.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/data/UserPreferences.kt)
- Add `SNOOZE_DURATION_MIN` key to DataStore.
- Add `snoozeDurationMin` Flow.
- Add `saveSnoozeDuration(minutes: Int)` function.

### Scheduler Layer

#### [MODIFY] [AlarmScheduler.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/scheduler/AlarmScheduler.kt)
- Add `scheduleSnooze(durationMinutes: Int)` to schedule a one-time alarm using `WaterAlarmReceiver`.
- Ensure it uses a unique request code for snooze.

### ViewModels

#### [NEW] [SettingsViewModel.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/viewmodel/SettingsViewModel.kt)
- Manage state for Settings: Daily Goal, Profile (Weight, Activity), Snooze Duration.
- Interact with `UserPreferences`.

### UI Layer

#### [NEW] [SettingsRoute.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/SettingsRoute.kt)
- Define `SettingsRoute` object for Navigation 3.

#### [NEW] [SettingsScreen.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/SettingsScreen.kt)
- Create the Settings UI with sections: "Hydration", "Notifications", "System".
- Include inputs for Goal, Weight, Activity Level, and Snooze Duration.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/MainActivity.kt)
- Add "Settings" tab to `NavigationBar`.
- Register `SettingsRoute` in `NavDisplay`.

#### [MODIFY] [AlarmActivity.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/AlarmActivity.kt)
- Add "Snooze" button to `AlarmScreen`.
- Implement snooze logic: retrieve duration from preferences, call `scheduleSnooze`, and finish activity.

## Verification Plan

### Automated Tests
- N/A (Unit tests could be added for logic if needed).

### Manual Verification
1. Open the app and navigate to the new Settings tab.
2. Change the Snooze Duration and other settings.
3. Wait for an alarm to trigger (or trigger manually).
4. Click the "Snooze" button on the Alarm screen.
5. Verify that the alarm is dismissed and triggers again after the specified snooze duration.
