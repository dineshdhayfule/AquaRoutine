# AquaRoutine

A modern Android application built with Jetpack Compose designed to help users track their hydration levels and manage routine reminders effectively.

## 🚀 Features


### 💧 Water Tracking
- **Smart Hydration Logging**: Easily log water intake. Includes strict validation to prevent 0ml entries and ensures historical data integrity by locking past logs as read-only.
- **Dynamic Goals**: Daily water goals are automatically calculated based on your weight and activity level, with the option to set manual overrides.
- **Premium Visual Statistics**: 
  - Circular progress ring showing daily intake and goal status.
  - **Dynamic Bar Charts**: View Weekly, Monthly, and Quarterly (Weekly Averages) trends with honest, dynamic scaling and clear goal reference lines. The chart seamlessly anchors to your selected date.
  - **Interactive Tooltips**: Tap any bar to see exact intake amounts and goal completion percentages.
  - **Hydration Streaks** to keep you motivated.
- **Log Management**: Separate tabs for **Active** and **Deleted** logs with a daily summary (+Added / -Deleted). Past data is protected from accidental modifications.
- **Home Screen Widget**: Quick-log water and see your current progress directly from your home screen.

### ⏰ Alarms & Reminders
- **Routine Alarms**: Schedule and manage reminders for your daily routines.
- **Custom Sounds**: Personalize your alarms by selecting any custom ringtone, alarm, or notification sound from your device.
- **Snooze Support**: Configurable snooze durations to suit your needs.

### ⚙️ Settings & Data
- **Profile Management**: Set your weight and activity level for personalized hydration targets.
- **Data Portability**: Export and Import your data via JSON files for easy backup and restoration.
- **System Integration**: Support for exact alarms and modern Android notification permissions.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3.
- **Navigation**: [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) for seamless screen transitions.
- **Database**: [Room](https://developer.android.com/training/data-storage/room) for persistent local storage of logs and alarms.
- **Preferences**: [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for lightweight user settings.
- **App Widgets**: [Jetpack Glance](https://developer.android.com/jetpack/compose/glance) for building modern home screen widgets.
- **Architecture**: MVVM (Model-View-ViewModel) with Repositories and Coroutines/Flow for reactive data handling.

## 🏗 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/alarm-hydrate.git
   ```
2. **Open in Android Studio**: Use Android Studio Ladybug (2024.2.1) or newer.
3. **Build and Run**: Deploy to an emulator or physical device running Android 8.0 (API 26) or higher.

## 📸 Screenshots

| Dashboard | Statistics | Settings |
| :---: | :---: | :---: |
| ![Dashboard](https://via.placeholder.com/200x400?text=Dashboard) | ![Statistics](https://via.placeholder.com/200x400?text=Statistics) | ![Settings](https://via.placeholder.com/200x400?text=Settings) |

*(Note: Replace placeholders with actual screenshots for a better preview)*

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
