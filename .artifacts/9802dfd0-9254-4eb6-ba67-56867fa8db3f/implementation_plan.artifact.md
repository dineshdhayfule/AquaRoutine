# UI Audit & Refinement Plan

Audit and refine the UI code in `WaterStatsScreen`, `RoutineReminderScreen`, `AlarmItemRow`, and `WaterBarChart` for adherence to Material 3 standards.

## Proposed Changes

### [UI Components]

#### [MODIFY] [AlarmItemRow.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/components/AlarmItemRow.kt)
- Reduce internal padding from 24dp to 16dp for a tighter, more professional look.
- Change time text style from `displaySmall` (36sp) to `headlineMedium` (28sp) to prevent the row from being excessively tall.
- Update corner radius from 24dp to 16dp to align with M3 Elevated Card standards.

#### [MODIFY] [WaterStatsScreen.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/WaterStatsScreen.kt)
- Standardize horizontal padding to 16dp (currently 24dp).
- Reduce section spacers from 32dp to 24dp.
- Ensure consistent left alignment.

#### [MODIFY] [RoutineReminderScreen.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/RoutineReminderScreen.kt)
- Fix the bottom Spacer to use `height(88.dp)` instead of `padding(40.dp)`.
- (Optional) Adjust section header padding if needed for consistency.

## Verification Plan

### Automated Tests
- Run `gradlew :app:assembleDebug` to ensure no build regressions.

### Manual Verification
- Review Compose Previews for all modified screens to verify the "good standard shape".
