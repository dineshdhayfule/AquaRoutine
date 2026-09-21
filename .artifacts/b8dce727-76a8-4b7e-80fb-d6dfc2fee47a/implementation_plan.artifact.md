# Implementation Plan - Dynamic Goal Increase and Logging

Implement a dynamic goal increase logic where the daily water goal is automatically increased by 1000ml when the user reaches or exceeds their current goal. Also, add support for notes in water logs to document these goal increases.

## User Review Required

> [!IMPORTANT]
> The database version will be incremented to 4, and `fallbackToDestructiveMigration()` is already enabled in `AppDatabase.kt`. This will clear all existing data (Alarms, Water Logs, etc.) in the local development database.

## Proposed Changes

### Data Layer

#### [MODIFY] [WaterLogEntity.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/data/entity/WaterLogEntity.kt)
- Add `val note: String? = null` field to the entity.

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/data/AppDatabase.kt)
- Increment database version to 4.

#### [NEW] [WaterRepository.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/data/repository/WaterRepository.kt)
- Create a new repository to centralize the water logging logic.
- Implement `addWaterLog(amount: Int)` which:
    - Calculates today's current intake.
    - Determines today's current goal (from override or global prefs).
    - Checks if `currentTotal + amount >= currentGoal`.
    - If yes, inserts/updates `DailyGoalOverride` with `currentGoal + 1000`.
    - Inserts the `WaterLogEntity` with a note if the goal was reached.

---

### UI Layer

#### [MODIFY] [WaterStatsViewModel.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/viewmodel/WaterStatsViewModel.kt)
- Inject `WaterRepository`.
- Update `addWaterLog` to use `WaterRepository.addWaterLog`.

#### [MODIFY] [WaterStatsScreen.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/WaterStatsScreen.kt)
- Update `WaterLogItem` to display the `note` if it's not null.
- Use a small subtext or tag style for the note.

---

### Widget

#### [MODIFY] [AddWaterAction.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/widget/AddWaterAction.kt)
- Update `onAction` to use `WaterRepository.addWaterLog` to ensure the dynamic goal logic is applied when adding water from the widget.

## Verification Plan

### Automated Tests
- Create a unit test for `WaterRepository` to verify:
    - Goal is NOT increased if the intake is below the goal.
    - Goal IS increased by 1000ml if the intake reaches or exceeds the goal.
    - A note is added to the log when the goal is reached.

### Manual Verification
1. Open the app.
2. Note the current goal (e.g., 2500ml).
3. Add water until the total reaches or exceeds 2500ml.
4. Verify that the goal automatically increases to 3500ml.
5. Verify that the last log entry shows the note: "Goal reached! +1L extra added to your target."
6. Try adding water from the widget and verify the same behavior.
