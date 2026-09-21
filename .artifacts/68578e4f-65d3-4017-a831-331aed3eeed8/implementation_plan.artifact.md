# Implementation Plan - Support Daily vs. Global Goals in EditGoalDialog

Update the goal editing flow to allow users to choose between overriding the goal for "Today Only" or updating the "Global Goal" (All Days).

## Proposed Changes

### [Component] ViewModel

#### [MODIFY] [WaterStatsViewModel.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/viewmodel/WaterStatsViewModel.kt)
- Rename `updateDailyGoal(goal: Int)` to `updateGlobalGoal(goal: Int)` to match the requirement and improve clarity.
- Ensure `updateDailyGoalOverride(goal: Int)` correctly uses the `_selectedDate`.

### [Component] UI Components

#### [MODIFY] [EditGoalDialog.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/components/EditGoalDialog.kt)
- Update `EditGoalDialog` to include selection for "Today Only" vs "All Days".
- Change `onSubmit` callback signature to `(Int, Boolean) -> Unit` where the Boolean indicates if it's a global update.
- Implement UI using `RadioButton`s or a `Switch`.

### [Component] Screens

#### [MODIFY] [WaterStatsScreen.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/WaterStatsScreen.kt)
- Update the `EditGoalDialog` call to handle the new `onSubmit` signature.
- Wire the result to `viewModel.updateGlobalGoal` or `viewModel.updateDailyGoalOverride`.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.
- (Optional) Add unit tests for `WaterStatsViewModel` to verify it calls the correct DAO/Preferences method.

### Manual Verification
1. Launch the app and go to Water Stats.
2. Click the edit icon in the "Daily Summary" card.
3. Verify the dialog shows "Apply to Today Only" and "Apply to All Days" options.
4. Test "Apply to Today Only" for a specific date and verify it doesn't change other days.
5. Test "Apply to All Days" and verify it updates the goal for all days (except those with overrides).
