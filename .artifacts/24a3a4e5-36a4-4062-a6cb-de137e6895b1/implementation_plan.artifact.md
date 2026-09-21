# Refine Water Tracking Logic and UI

This plan covers updating the water tracking system to support soft deletion, refining goal adjustment logic, and improving the UI/Widget experience.

## Proposed Changes

### Data Layer

#### [MODIFY] [WaterLogEntity.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/data/entity/WaterLogEntity.kt)
- Add `isDeleted: Boolean = false`.
- Add `originalAmount: Int = 0` to persist the value before soft-deletion for UI display.

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/data/AppDatabase.kt)
- Increment version to 5.
- Add migration from version 4 to 5 to add `isDeleted` and `originalAmount` columns.

#### [MODIFY] [WaterRepository.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/data/repository/WaterRepository.kt)
- **addWaterLog**: Update goal increase condition to `totalIntake > currentGoal`.
- **deleteWaterLog**:
    - Implement soft delete: set `isDeleted = true`, `amountMl = 0`, `note = "Entry Removed"`, and store the old amount in `originalAmount`.
    - Maintain goal decrease logic using the updated total (which naturally excludes deleted logs as their amount is 0).

### UI Layer

#### [MODIFY] [WaterStatsScreen.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/WaterStatsScreen.kt)
- **WaterLogItem**:
    - Remove whole-item click-to-edit.
    - Add a Delete IconButton on the right side.
    - Style active logs with "+[Amount] ml" in primary color.
    - Style deleted logs with "Deleted" text and the original amount with a strikethrough.
    - Ensure the list displays both active and deleted logs.

### Widget Layer

#### [MODIFY] [WaterWidget.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/widget/WaterWidget.kt)
- Wrap the main widget content in a `clickable` modifier that triggers an action to open the app at the Water Stats screen.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.
- Run `gradlew :app:assembleDebug` to verify the build.

### Manual Verification
- Add a water log and verify it appears with "+[Amount] ml".
- Exceed the goal and verify it increases only when `total > goal`.
- Delete a log and verify:
    - It remains in the list but shows as "Deleted" with strikethrough amount.
    - The daily total decreases.
    - If the total falls below the threshold, the goal decreases.
- Tap the widget and verify it opens the app.
- Tap quick-add buttons on the widget and verify they still function independently.
