# Implement Data Model and ViewModel for Routine Reminder App

Implement the core data structures and state management for the water intake reminder app.

## Proposed Changes

### [Model]

#### [NEW] [AlarmItem.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/model/AlarmItem.kt)
Create a data class to represent an individual alarm.

### [ViewModel]

#### [NEW] [WaterIntakeViewModel.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/viewmodel/WaterIntakeViewModel.kt)
Create a ViewModel to manage the list of alarms and handle user interactions (toggle, add, delete, master toggle).

## Verification Plan

### Automated Tests
- Create unit tests for `WaterIntakeViewModel` to verify:
    - Initialization with 8 default alarms.
    - `toggleAlarm` updates the state correctly.
    - `addCustomAlarm` adds a new alarm with the correct time string.
    - `toggleMaster` updates all alarms.
    - `deleteAlarm` removes the alarm.

### Manual Verification
- N/A (UI is not part of this task).
