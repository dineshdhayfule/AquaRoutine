# Premium Material 3 UI/UX Refactor for Water Intake Tracker

This plan aims to elevate the Water Intake Tracker's UI to a professional, premium Material 3 aesthetic, focusing on typography, surface elevation, micro-interactions, and a refactored bar chart.

## Proposed Changes

### [Component] Typography & Theme

#### [MODIFY] [Type.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/theme/Type.kt)
- Ensure a complete M3 typography scale is defined.
- Optimize `displayLarge` for the massive daily total.

#### [MODIFY] [Theme.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/theme/Theme.kt)
- Ensure dynamic color support is robust and fallback colors are expressive.

---

### [Component] Bar Chart

#### [NEW] [WaterBarChart.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/components/WaterBarChart.kt)
- Rewrite the bar chart using `Canvas` or `Row` with `Box`es.
- Implement vertical scaling (Max Goal: 3000ml = 100% height).
- Use `RoundedCornerShape` with rounded tops for bars.
- Highlight the bar corresponding to the `selectedDate`.
- Add vertical grid lines/labels for "0ml", "1500ml", "3000ml".

---

### [Component] Surfaces & Spacing

#### [MODIFY] [WaterStatsScreen.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/WaterStatsScreen.kt)
- Use the new `WaterBarChart`.
- Refactor `WaterLogItem` to use `ElevatedCard` with large corners (24dp-32dp) and tonal elevation.
- Apply generous spacing (16dp/24dp) throughout.
- Add haptic feedback when adding water logs.

#### [MODIFY] [AlarmItemRow.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/components/AlarmItemRow.kt)
- Use `ElevatedCard` or `OutlinedCard` with M3 tonal elevation.
- Increase corner radius and apply consistent padding.

#### [MODIFY] [RoutineReminderScreen.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/ui/RoutineReminderScreen.kt)
- Add haptic feedback when toggling alarms.
- Ensure consistent spacing and layout.

---

### [Component] Main Navigation & Layout

#### [MODIFY] [MainActivity.kt](file:///C:/Users/DELL/AndroidStudioProjects/Alarm/app/src/main/java/com/example/alarm/MainActivity.kt)
- Ensure full edge-to-edge implementation with proper window insets handling for the `Scaffold` and `NavigationBar`.

## Verification Plan

### Automated Tests
- Run existing unit tests to ensure no regressions in logic.
- `./gradlew testDebugUnitTest`

### Manual Verification
- Visual inspection of the new Bar Chart: verify scaling, rounding, and highlighting.
- Verify haptic feedback on physical device or emulator (if supported).
- Check edge-to-edge rendering in both light and dark modes.
- Verify Typography scale and weight for the daily total.
