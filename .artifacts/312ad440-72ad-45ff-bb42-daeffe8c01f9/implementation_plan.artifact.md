# Implementation Plan - Alarm Customization (Days of Week Selection)

Implement multi-day alarm scheduling by adding day-of-week selection to `AlarmItem`, updating the scheduling logic to find the next occurrence, and enhancing the UI with day selection indicators.

## User Review Required

> [!IMPORTANT]
> The current implementation of alarms is in-memory (in `WaterIntakeViewModel`). While this task focuses on adding multi-day support, these settings will persist only as long as the app process is alive (or re-initialized with defaults). Full persistence (Room) for alarm customization is a separate feature but would be highly recommended for a production alarm app.

## Proposed Changes

### Data Model

#### [MODIFY] [AlarmItem.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/model/AlarmItem.kt)
- Add `daysOfWeek: Set<Int>` property (default to all days 1-7).

### Scheduling Logic

#### [MODIFY] [AlarmScheduler.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/scheduler/AlarmScheduler.kt)
- Update `scheduleAlarm` to calculate the next occurrence based on `daysOfWeek`.
- Pass `hour`, `minute`, and `daysOfWeek` in the `PendingIntent` extras so the receiver can re-schedule.
- Add `calculateNextOccurrence` helper method.

#### [MODIFY] [WaterAlarmReceiver.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/receiver/WaterAlarmReceiver.kt)
- In `onReceive`, extract alarm details and call `AlarmScheduler` to schedule the next occurrence after firing.

### ViewModel

#### [MODIFY] [WaterIntakeViewModel.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/viewmodel/WaterIntakeViewModel.kt)
- Add `toggleDay(alarmId: Int, day: Int)` to update an alarm's selected days and re-schedule.
- Update `createAlarmItem` to initialize with all days selected.

### UI Components

#### [MODIFY] [AlarmItemRow.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/components/AlarmItemRow.kt)
- Add a row of 7 circular "Day" indicators (S, M, T, W, T, F, S).
- Indicators will be clickable and use `MaterialTheme.colorScheme.primary` when selected.

#### [MODIFY] [RoutineReminderScreen.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/RoutineReminderScreen.kt)
- Add `onToggleDay` parameter to `RoutineReminderContent`.
- Pass `viewModel::toggleDay` through to `AlarmItemRow`.

## Verification Plan

### Automated Tests
- N/A (Manual verification on device/emulator is preferred for alarm timing logic).

### Manual Verification
1. Open the app and see the day indicators in each alarm row.
2. Toggle a day and verify the UI updates (color changes).
3. Set an alarm for 1 minute from now on the current day.
4. Verify the alarm fires.
5. Verify (via logs if possible) that the next occurrence is scheduled for the next selected day.
6. Toggle off all days and verify the alarm becomes "Inactive" or effectively disabled.
