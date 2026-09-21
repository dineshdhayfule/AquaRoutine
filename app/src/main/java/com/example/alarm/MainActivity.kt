package com.example.alarm

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.alarm.data.AppDatabase
import com.example.alarm.data.UserPreferences
import com.example.alarm.data.repository.WaterRepository
import com.example.alarm.ui.RoutineReminderRoute
import com.example.alarm.ui.RoutineReminderScreen
import com.example.alarm.ui.SettingsRoute
import com.example.alarm.ui.SettingsScreen
import com.example.alarm.ui.WaterStatsRoute
import com.example.alarm.ui.WaterStatsScreen
import com.example.alarm.ui.theme.AlarmTheme
import com.example.alarm.viewmodel.SettingsViewModel
import com.example.alarm.viewmodel.WaterIntakeViewModel
import com.example.alarm.viewmodel.WaterStatsViewModel
import com.example.alarm.widget.WaterWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val LocalSnackbarHostState = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            AlarmTheme {
                val context = LocalContext.current
                val snackbarHostState = remember { SnackbarHostState() }
                
                // Permission handling
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handle result if needed
                }

                LaunchedEffect(Unit) {
                    // Request Notification Permission for Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }

                    // Check Exact Alarm Permission for Android 12+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        if (!alarmManager.canScheduleExactAlarms()) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                            context.startActivity(intent)
                        }
                    }
                }

                CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val backStack = rememberNavBackStack(RoutineReminderRoute as NavKey)
                        
                        Scaffold(
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = {
                                NavigationBar {
                                    val currentRoute = backStack.lastOrNull()
                                    
                                    NavigationBarItem(
                                        selected = currentRoute is RoutineReminderRoute,
                                        onClick = {
                                            if (currentRoute !is RoutineReminderRoute) {
                                                backStack.clear()
                                                backStack.add(RoutineReminderRoute)
                                            }
                                        },
                                        icon = { 
                                            Icon(
                                                imageVector = if (currentRoute is RoutineReminderRoute) 
                                                    Icons.Rounded.Alarm else Icons.Outlined.Alarm, 
                                                contentDescription = "Alarms"
                                            ) 
                                        },
                                        label = { Text("Alarms") },
                                        alwaysShowLabel = true
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute is WaterStatsRoute,
                                        onClick = {
                                            if (currentRoute !is WaterStatsRoute) {
                                                backStack.clear()
                                                backStack.add(WaterStatsRoute)
                                            }
                                        },
                                        icon = { 
                                            Icon(
                                                imageVector = if (currentRoute is WaterStatsRoute) 
                                                    Icons.Rounded.WaterDrop else Icons.Outlined.WaterDrop, 
                                                contentDescription = "Water"
                                            ) 
                                        },
                                        label = { Text("Water") },
                                        alwaysShowLabel = true
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute is SettingsRoute,
                                        onClick = {
                                            if (currentRoute !is SettingsRoute) {
                                                backStack.clear()
                                                backStack.add(SettingsRoute)
                                            }
                                        },
                                        icon = { 
                                            Icon(
                                                imageVector = if (currentRoute is SettingsRoute) 
                                                    Icons.Rounded.Settings else Icons.Outlined.Settings, 
                                                contentDescription = "Settings"
                                            ) 
                                        },
                                        label = { Text("Settings") },
                                        alwaysShowLabel = true
                                    )
                                }
                            }
                        ) { innerPadding ->
                            val intentAction = intent.action
                            LaunchedEffect(intentAction) {
                                if (intentAction == "com.example.alarm.VIEW_STATS" || intentAction == "com.example.alarm.ADD_WATER") {
                                    if (backStack.lastOrNull() !is WaterStatsRoute) {
                                        backStack.clear()
                                        backStack.add(WaterStatsRoute)
                                    }
                                }
                            }

                            NavDisplay(
                                backStack = backStack,
                                onBack = { backStack.removeLastOrNull() },
                                modifier = Modifier.padding(innerPadding),
                                entryDecorators = listOf(
                                    rememberSaveableStateHolderNavEntryDecorator(),
                                    rememberViewModelStoreNavEntryDecorator()
                                ),
                                entryProvider = entryProvider {
                                    entry<RoutineReminderRoute> {
                                        val viewModel: WaterIntakeViewModel = viewModel()
                                        RoutineReminderScreen(viewModel = viewModel)
                                    }
                                    entry<WaterStatsRoute> {
                                        val waterStatsViewModel: WaterStatsViewModel = viewModel(
                                            factory = object : ViewModelProvider.Factory {
                                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                    val db = AppDatabase.getDatabase(context)
                                                    val waterLogDao = db.waterLogDao()
                                                    val dailyGoalOverrideDao = db.dailyGoalOverrideDao()
                                                    val userPrefs = UserPreferences(context)
                                                    val waterRepository = WaterRepository(waterLogDao, dailyGoalOverrideDao, userPrefs)
                                                    @Suppress("UNCHECKED_CAST")
                                                    return WaterStatsViewModel(waterLogDao, dailyGoalOverrideDao, userPrefs, waterRepository, context) as T
                                                }
                                            }
                                        )
                                        WaterStatsScreen(viewModel = waterStatsViewModel)
                                    }
                                    entry<SettingsRoute> {
                                        val settingsViewModel: SettingsViewModel = viewModel(
                                            factory = object : ViewModelProvider.Factory {
                                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                    val userPrefs = UserPreferences(context)
                                                    @Suppress("UNCHECKED_CAST")
                                                    return SettingsViewModel(application, userPrefs) as T
                                                }
                                            }
                                        )
                                        SettingsScreen(viewModel = settingsViewModel)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == "com.example.alarm.ADD_WATER") {
            val amount = intent.getIntExtra("amount", 250)
            if (amount <= 0) return
            
            CoroutineScope(Dispatchers.IO).launch {
                val dao = AppDatabase.getDatabase(this@MainActivity).waterLogDao()
                dao.insertLog(
                    com.example.alarm.data.entity.WaterLogEntity(
                        amountMl = amount,
                        timestamp = System.currentTimeMillis()
                    )
                )
                WaterWidget().updateAll(this@MainActivity)
            }
        }
    }
}
