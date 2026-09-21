package com.example.alarm.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alarm.data.UserPreferences
import com.example.alarm.data.entity.WaterLogEntity
import com.example.alarm.ui.components.*
import com.example.alarm.ui.theme.AlarmTheme
import com.example.alarm.viewmodel.StatisticsPeriod
import com.example.alarm.viewmodel.WaterStatsViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

@Composable
fun WaterStatsScreen(
    viewModel: WaterStatsViewModel,
    onNavigateToBackfill: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dailyLogs by viewModel.dailyLogs.collectAsStateWithLifecycle()
    val dailyTotal by viewModel.dailyTotal.collectAsStateWithLifecycle()
    val dailyGoalMl by viewModel.dailyGoalMl.collectAsStateWithLifecycle()
    val chartData by viewModel.chartData.collectAsStateWithLifecycle()
    val hydrationStatus by viewModel.hydrationStatus.collectAsStateWithLifecycle()
    val hydrationStreak by viewModel.hydrationStreak.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val chartAnchorDate by viewModel.chartAnchorDate.collectAsStateWithLifecycle()
    val isOwnerModeActive by viewModel.isOwnerModeActive.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<WaterLogEntity?>(null) }
    
    var showPinManagement by remember { mutableStateOf(false) }
    
    val isToday = remember(selectedDate) { selectedDate == LocalDate.now() }
    val isEditable = isToday || isOwnerModeActive
    val isPinSet by viewModel.isPinSet.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    if (showPinManagement) {
        PinManagementDialog(
            isPinSet = isPinSet,
            onVerifyPin = { viewModel.verifyPin(it) },
            onSavePin = { viewModel.saveOwnerPin(it) },
            onDisablePin = { viewModel.disableOwnerPin() },
            onDismissRequest = { showPinManagement = false }
        )
    }

    if (showAddDialog && isEditable) {
        AddWaterDialog(
            onDismissRequest = { showAddDialog = false },
            onSubmit = { amount -> viewModel.addWaterLog(amount) }, // Normal add flow (today)
            onUnlockOwnerMode = { pin -> viewModel.unlockOwnerMode(pin) },
            isPinSet = isPinSet,
            onOpenPinSetup = { showPinManagement = true }
        )
    }

    editingLog?.let { log ->
        if (isEditable) {
            EditWaterLogDialog(
                log = log,
                onDismissRequest = { editingLog = null },
                onDelete = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.deleteWaterLog(log)
                    editingLog = null
                },
                onUpdate = { newAmount ->
                    viewModel.updateWaterLog(log, newAmount)
                    editingLog = null
                }
            )
        } else {
            editingLog = null
        }
    }

    WaterStatsContent(
        selectedDate = selectedDate,
        dailyLogs = dailyLogs,
        dailyTotal = dailyTotal,
        dailyGoalMl = dailyGoalMl,
        chartData = chartData,
        chartAnchorDate = chartAnchorDate,
        hydrationStatus = hydrationStatus,
        hydrationStreak = hydrationStreak,
        selectedPeriod = selectedPeriod,
        dailySummary = dailySummary,
        isToday = isToday,
        isOwnerModeActive = isOwnerModeActive,
        isEditable = isEditable,
        onLockOwnerMode = { viewModel.lockOwnerMode() },
        onOpenPinManagement = { showPinManagement = true },
        onPeriodSelected = viewModel::selectPeriod,
        onPrevDay = viewModel::previousDay,
        onNextDay = viewModel::nextDay,
        onAddClick = { if (isEditable) showAddDialog = true },
        onDateSelected = viewModel::selectDate,
        onDeleteLog = { if (isEditable) viewModel.deleteWaterLog(it) },
        onLogClick = { if (isEditable) editingLog = it },
        onNavigateToBackfill = onNavigateToBackfill,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterStatsContent(
    selectedDate: LocalDate,
    dailyLogs: List<WaterLogEntity>,
    dailyTotal: Int,
    dailyGoalMl: Int,
    chartData: List<Triple<String, Int, Int>>,
    chartAnchorDate: LocalDate,
    hydrationStatus: String,
    hydrationStreak: Int,
    selectedPeriod: StatisticsPeriod,
    dailySummary: Pair<Float, Float>,
    isToday: Boolean,
    isOwnerModeActive: Boolean,
    isEditable: Boolean,
    onLockOwnerMode: () -> Unit,
    onOpenPinManagement: () -> Unit,
    onPeriodSelected: (StatisticsPeriod) -> Unit,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onAddClick: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onDeleteLog: (WaterLogEntity) -> Unit,
    onLogClick: (WaterLogEntity) -> Unit,
    onNavigateToBackfill: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val filteredLogs = remember(dailyLogs, selectedTab) {
        if (selectedTab == 0) {
            dailyLogs.filter { !it.isDeleted }
        } else {
            dailyLogs.filter { it.isDeleted }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Water Stats", fontWeight = FontWeight.Bold) },
                actions = {
                    if (isOwnerModeActive) {
                        IconButton(onClick = onNavigateToBackfill) {
                            Icon(
                                Icons.Rounded.AddCircleOutline, 
                                contentDescription = "Backfill Logs",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onOpenPinManagement) {
                            Icon(
                                Icons.Rounded.Settings, 
                                contentDescription = "Security Settings",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                        IconButton(onClick = onLockOwnerMode) {
                            Icon(
                                Icons.Rounded.Lock, 
                                contentDescription = "Lock Owner Mode",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isEditable) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Rounded.Add, 
                        contentDescription = "Add Water"
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressRing(
                    currentMl = dailyTotal,
                    goalMl = dailyGoalMl,
                    status = hydrationStatus,
                    modifier = Modifier.size(200.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                StreakCard(streak = hydrationStreak)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                DailySummaryCard(
                    currentMl = dailyTotal,
                    goalMl = dailyGoalMl
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Period Toggle
                PeriodToggle(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Bar Chart
                WaterBarChart(
                    data = chartData,
                    selectedDate = selectedDate,
                    chartAnchorDate = chartAnchorDate,
                    onDateSelected = { index ->
                        val daysCount = when (selectedPeriod) {
                            StatisticsPeriod.WEEK -> 7
                            StatisticsPeriod.MONTH -> 30
                            StatisticsPeriod.QUARTER -> 90
                        }
                        val startDate = chartAnchorDate.minusDays(daysCount.toLong() - 1)
                        
                        if (selectedPeriod == StatisticsPeriod.QUARTER) {
                            onDateSelected(startDate.plusDays(index.toLong() * 7))
                        } else {
                            onDateSelected(startDate.plusDays(index.toLong()))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Date Navigator
                DateNavigator(
                    selectedDate = selectedDate,
                    onPrevDay = onPrevDay,
                    onNextDay = onNextDay
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Log List Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Logs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "+${"%.1f".format(dailySummary.first)} / -${"%.1f".format(dailySummary.second)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Active") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Deleted") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            if (filteredLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val emptyText = if (selectedTab == 0) "No active logs for this day" else "No deleted logs for this day"
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                items(filteredLogs, key = { it.id }) { log ->
                    WaterLogItem(
                        log = log,
                        onDeleteClick = { if (isEditable) onDeleteLog(log) },
                        isEditable = isEditable,
                        modifier = Modifier
                            .animateItem()
                            .clickable(enabled = isEditable) { onLogClick(log) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(88.dp)) // Extra bottom spacing for FAB
            }
        }
    }
}

@Composable
fun StreakCard(streak: Int) {
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Whatshot,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = if (streak > 0) "$streak Day Streak!" else "Start your streak!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = if (streak > 0) "Keep it up! You're doing great." else "Meet your goal today to start a streak.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeriodToggle(
    selectedPeriod: StatisticsPeriod,
    onPeriodSelected: (StatisticsPeriod) -> Unit
) {
    val periods = StatisticsPeriod.entries
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                onClick = { onPeriodSelected(period) },
                selected = period == selectedPeriod,
                label = {
                    Text(period.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })
                }
            )
        }
    }
}

@Composable
fun CircularProgressRing(
    currentMl: Int,
    goalMl: Int,
    status: String,
    modifier: Modifier = Modifier
) {
    val progress = (currentMl.toFloat() / goalMl.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "progress"
    )

    Box(
        contentAlignment = Alignment.Center, 
        modifier = modifier.semantics {
            contentDescription = "Hydration progress: ${(progress * 100).toInt()}%. Total: ${currentMl}ml of ${goalMl}ml goal. Status: $status"
        }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            
            // Background track
            drawCircle(
                color = Color.LightGray.copy(alpha = 0.2f),
                style = Stroke(width = strokeWidth)
            )
            
            // Progress arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFF64B5F6),
                        Color(0xFF2196F3),
                        Color(0xFF1976D2),
                        Color(0xFF64B5F6)
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(currentMl / 1000f)} Ltr",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            if (status.isNotEmpty()) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (status) {
                        "Ahead", "Goal Achieved!" -> Color(0xFF4CAF50)
                        "On Track" -> Color(0xFF2196F3)
                        "Behind" -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
            }
        }
    }
}

@Composable
fun DailySummaryCard(
    currentMl: Int,
    goalMl: Int
) {
    val remaining = (goalMl - currentMl).coerceAtLeast(0)
    
    ElevatedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (remaining > 0) "Remaining today" else "Goal achieved!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = if (remaining > 0) "$remaining ml to reach your daily goal" else "You've reached your daily target. Well done!",
                style = MaterialTheme.typography.bodyLarge
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Info, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Goal: $goalMl ml (based on your profile)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DateNavigator(
    selectedDate: LocalDate,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit
) {
    val isToday = selectedDate == LocalDate.now()
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onPrevDay) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous Day")
            }
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(
                onClick = onNextDay,
                enabled = !isToday
            ) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Day")
            }
        }
    }
}

@Composable
fun WaterLogItem(
    log: WaterLogEntity,
    onDeleteClick: () -> Unit,
    isEditable: Boolean = true,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())
    val timeString = Instant.ofEpochMilli(log.timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(timeFormatter)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.isDeleted) {
                        Text(
                            text = "Deleted",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "${log.originalAmount} ml",
                            style = MaterialTheme.typography.titleLarge.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "+${log.amountMl} ml",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (log.source == "BACKFILLED") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "↩ Backfilled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                if (!log.note.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.note!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (log.isDeleted) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            if (!log.isDeleted && isEditable) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete Log",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            } else if (log.isDeleted) {
                Icon(
                    Icons.Rounded.Block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun WaterStatsScreenPreview() {
    val dummyLogs = listOf(
        WaterLogEntity(1, 250, System.currentTimeMillis() - 3600000),
        WaterLogEntity(2, 500, System.currentTimeMillis() - 7200000),
        WaterLogEntity(3, 250, System.currentTimeMillis())
    )
    val dummyWeeklyData = listOf(
        Triple("Sun", 1500, 2500),
        Triple("Mon", 2000, 2500),
        Triple("Tue", 3000, 2500),
        Triple("Wed", 1200, 2500),
        Triple("Thu", 2500, 2500),
        Triple("Fri", 2800, 2500),
        Triple("Sat", 1000, 2500)
    )
    val dummyDate = LocalDate.now()
    AlarmTheme(darkTheme = true) {
        WaterStatsContent(
            selectedDate = dummyDate,
            dailyLogs = dummyLogs,
            dailyTotal = 1000,
            dailyGoalMl = UserPreferences.DEFAULT_GOAL_ML,
            chartData = dummyWeeklyData,
            chartAnchorDate = dummyDate,
            hydrationStatus = "On Track",
            hydrationStreak = 5,
            selectedPeriod = StatisticsPeriod.WEEK,
            dailySummary = 1.0f to 0.0f,
            isToday = true,
            isOwnerModeActive = false,
            isEditable = true,
            onLockOwnerMode = {},
            onOpenPinManagement = {},
            onPeriodSelected = {},
            onPrevDay = {},
            onNextDay = {},
            onAddClick = {},
            onDateSelected = {},
            onDeleteLog = {},
            onLogClick = {},
            onNavigateToBackfill = {}
        )
    }
}
