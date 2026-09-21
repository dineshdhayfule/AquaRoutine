package com.example.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.data.UserPreferences
import com.example.alarm.data.dao.DailyGoalOverrideDao
import com.example.alarm.data.dao.WaterLogDao
import com.example.alarm.data.entity.DailyGoalOverride
import com.example.alarm.data.entity.WaterLogEntity
import com.example.alarm.widget.WaterWidget
import androidx.glance.appwidget.updateAll
import android.content.Context
import java.time.Instant
import com.example.alarm.OwnerModeManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

import com.example.alarm.data.repository.WaterRepository

enum class StatisticsPeriod {
    WEEK, MONTH, QUARTER
}

class WaterStatsViewModel(
    private val waterLogDao: WaterLogDao,
    private val dailyGoalOverrideDao: DailyGoalOverrideDao,
    private val userPreferences: UserPreferences,
    private val waterRepository: WaterRepository,
    private val context: Context
) : ViewModel() {

    private val ownerModeManager = OwnerModeManager.getInstance(context)
    val isOwnerModeActive: StateFlow<Boolean> = ownerModeManager.isOwnerModeActive

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _chartAnchorDate = MutableStateFlow(LocalDate.now())
    val chartAnchorDate: StateFlow<LocalDate> = _chartAnchorDate.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(StatisticsPeriod.WEEK)
    val selectedPeriod: StateFlow<StatisticsPeriod> = _selectedPeriod.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyGoalMl: StateFlow<Int> = combine(
        _selectedDate,
        userPreferences.dailyGoalMl
    ) { date, globalGoal ->
        date to globalGoal
    }.flatMapLatest { (date, globalGoal) ->
        dailyGoalOverrideDao.getOverrideForDate(date.toString()).map { override ->
            override?.goalMl ?: globalGoal
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences.DEFAULT_GOAL_ML)

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyLogs: StateFlow<List<WaterLogEntity>> = _selectedDate
        .flatMapLatest { date ->
            val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            waterLogDao.getLogsForDay(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyTotal: StateFlow<Int> = _selectedDate
        .flatMapLatest { date ->
            val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            waterLogDao.getTotalConsumedForDay(start, end)
        }
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dailySummary: StateFlow<Pair<Float, Float>> = dailyLogs.map { logs ->
        val added = logs.sumOf { it.amountMl }.toFloat() / 1000f
        val deleted = logs.filter { it.isDeleted }.sumOf { it.originalAmount }.toFloat() / 1000f
        added to deleted
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f to 0f)

    val hydrationStatus: StateFlow<String> = combine(
        dailyTotal,
        dailyGoalMl,
        _selectedDate
    ) { total, goal, date ->
        if (date != LocalDate.now()) return@combine ""
        
        val now = LocalTime.now()
        val startHour = 7 // 7 AM
        val endHour = 23 // 11 PM
        val totalWakingHours = (endHour - startHour).toFloat()
        
        val currentHour = now.hour.coerceIn(startHour, endHour)
        val elapsedWakingHours = (currentHour - startHour).toFloat()
        
        val progressNeeded = (elapsedWakingHours / totalWakingHours) * goal
        
        when {
            total >= goal -> "Goal Achieved!"
            total >= progressNeeded + 200 -> "Ahead"
            total >= progressNeeded - 200 -> "On Track"
            else -> "Behind"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Calculating...")

    @OptIn(ExperimentalCoroutinesApi::class)
    val hydrationStreak: StateFlow<Int> = dailyGoalMl.flatMapLatest { goal ->
        val today = LocalDate.now()
        val start = today.minusDays(365).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        
        waterLogDao.getLogsInRange(start, end).map { logs ->
            var streak = 0
            var checkDate = today
            
            val todayStart = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val todayEnd = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            val todayTotal = logs.filter { it.timestamp in todayStart..todayEnd }.sumOf { it.amountMl }
            
            if (todayTotal >= goal) {
                streak++
                checkDate = today.minusDays(1)
            } else {
                checkDate = today.minusDays(1)
            }
            
            while (true) {
                val dayStart = checkDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val dayEnd = checkDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
                val dailyTotal = logs.filter { it.timestamp in dayStart..dayEnd }.sumOf { it.amountMl }
                
                if (dailyTotal >= goal) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }
            streak
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val chartData: StateFlow<List<Triple<String, Int, Int>>> = combine(
        _selectedPeriod,
        _chartAnchorDate,
        userPreferences.dailyGoalMl
    ) { period, anchorDate, globalGoal ->
        Triple(period, anchorDate, globalGoal)
    }.flatMapLatest { (period, anchorDate, globalGoal) ->
        val daysCount = when (period) {
            StatisticsPeriod.WEEK -> 7
            StatisticsPeriod.MONTH -> 30
            StatisticsPeriod.QUARTER -> 90
        }
        // Calculate range ending at anchorDate
        val startDate = anchorDate.minusDays(daysCount.toLong() - 1)
        val start = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = anchorDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        combine(
            waterLogDao.getLogsInRange(start, end),
            dailyGoalOverrideDao.getOverridesInRange(startDate.toString(), anchorDate.toString())
        ) { logs, overrides ->
            val overrideMap = overrides.associateBy { it.date }
            val data = mutableListOf<Triple<String, Int, Int>>()

            if (period == StatisticsPeriod.QUARTER) {
                // Quarter View: Aggregate by Week (Visual only)
                var currentStartDate = startDate
                while (currentStartDate.isBefore(anchorDate.plusDays(1))) {
                    val weekEnd = currentStartDate.plusDays(6).coerceAtMost(anchorDate)
                    val weekStartTs = currentStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val weekEndTs = weekEnd.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
                    
                    val weekLogs = logs.filter { it.timestamp in weekStartTs..weekEndTs }
                    val weekTotal = weekLogs.sumOf { it.amountMl }
                    
                    val daysInWeek = java.time.temporal.ChronoUnit.DAYS.between(currentStartDate, weekEnd).toInt() + 1
                    val avgIntake = if (daysInWeek > 0) weekTotal / daysInWeek else 0
                    
                    // Average goal for the week
                    var weekGoalSum = 0
                    var tempDate = currentStartDate
                    while (!tempDate.isAfter(weekEnd)) {
                        weekGoalSum += overrideMap[tempDate.toString()]?.goalMl ?: globalGoal
                        tempDate = tempDate.plusDays(1)
                    }
                    val avgGoal = if (daysInWeek > 0) weekGoalSum / daysInWeek else globalGoal
                    
                    val label = "W${(java.time.temporal.ChronoUnit.DAYS.between(startDate, currentStartDate) / 7 + 1)}"
                    data.add(Triple(label, avgIntake, avgGoal))
                    
                    currentStartDate = currentStartDate.plusDays(7)
                    if (currentStartDate.isAfter(anchorDate)) break
                }
            } else {
                // Week and Month Views: Daily
                for (i in 0 until daysCount) {
                    val date = startDate.plusDays(i.toLong())
                    val dayStart = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val dayEnd = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
                    
                    val dailySum = logs.filter { it.timestamp in dayStart..dayEnd }.sumOf { it.amountMl }
                    val dayGoal = overrideMap[date.toString()]?.goalMl ?: globalGoal
                    
                    val label = when (period) {
                        StatisticsPeriod.WEEK -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                        else -> date.dayOfMonth.toString()
                    }
                    data.add(Triple(label, dailySum, dayGoal))
                }
            }
            data
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun adjustAnchorDateIfNeeded(date: LocalDate, period: StatisticsPeriod) {
        val daysCount = when (period) {
            StatisticsPeriod.WEEK -> 7
            StatisticsPeriod.MONTH -> 30
            StatisticsPeriod.QUARTER -> 90
        }
        val windowEnd = _chartAnchorDate.value
        val windowStart = windowEnd.minusDays(daysCount.toLong() - 1)

        if (date.isAfter(windowEnd)) {
            _chartAnchorDate.value = date
        } else if (date.isBefore(windowStart)) {
            var newAnchor = date.plusDays(daysCount.toLong() - 1)
            if (newAnchor.isAfter(LocalDate.now())) {
                newAnchor = LocalDate.now()
            }
            _chartAnchorDate.value = newAnchor
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        adjustAnchorDateIfNeeded(date, _selectedPeriod.value)
    }

    fun selectPeriod(period: StatisticsPeriod) {
        _selectedPeriod.value = period
        adjustAnchorDateIfNeeded(_selectedDate.value, period)
    }

    fun nextDay() {
        val today = LocalDate.now()
        if (_selectedDate.value.isBefore(today)) {
            val newDate = _selectedDate.value.plusDays(1)
            _selectedDate.value = newDate
            adjustAnchorDateIfNeeded(newDate, _selectedPeriod.value)
        }
    }

    fun previousDay() {
        val newDate = _selectedDate.value.minusDays(1)
        _selectedDate.value = newDate
        adjustAnchorDateIfNeeded(newDate, _selectedPeriod.value)
    }

    private val securityManager = com.example.alarm.data.SecurityManager(context)
    
    private val _isPinSet = MutableStateFlow(securityManager.isPinSet())
    val isPinSet: StateFlow<Boolean> = _isPinSet.asStateFlow()

    fun isPinConfigured(): Boolean {
        return securityManager.isPinSet()
    }
    
    fun verifyPin(pin: String): Boolean {
        return securityManager.verifyPin(pin)
    }

    fun saveOwnerPin(pin: String) {
        securityManager.saveOwnerPin(pin)
        _isPinSet.value = true
    }

    fun disableOwnerPin() {
        securityManager.disableOwnerPin()
        _isPinSet.value = false
        lockOwnerMode()
    }

    fun unlockOwnerMode(pin: String): Boolean {
        return ownerModeManager.unlock(pin)
    }

    fun lockOwnerMode() {
        ownerModeManager.lock()
    }

    fun addWaterLog(amount: Int) {
        viewModelScope.launch {
            waterRepository.addWaterLog(amount)
            WaterWidget().updateAll(context)
        }
    }

    fun updateWaterLog(log: WaterLogEntity, newAmount: Int) {
        val logDate = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        if (logDate != LocalDate.now() && !isOwnerModeActive.value) return // Block if not today and not owner

        viewModelScope.launch {
            waterLogDao.updateLog(log.copy(amountMl = newAmount))
            ownerModeManager.resetTimeout() // Reset timeout on activity
            WaterWidget().updateAll(context)
        }
    }

    fun deleteWaterLog(log: WaterLogEntity) {
        val logDate = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
        if (logDate != LocalDate.now() && !isOwnerModeActive.value) return // Block if not today and not owner

        viewModelScope.launch {
            waterRepository.deleteWaterLog(log)
            ownerModeManager.resetTimeout() // Reset timeout on activity
            WaterWidget().updateAll(context)
        }
    }
}
