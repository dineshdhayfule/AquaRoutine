package com.example.alarm.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.data.UserPreferences
import com.example.alarm.data.dao.WaterLogDao
import com.example.alarm.data.entity.WaterLogEntity
import com.example.alarm.data.repository.WaterRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class BackfillViewModel(
    private val waterRepository: WaterRepository,
    private val waterLogDao: WaterLogDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now().minusDays(1))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _dailyGoal = MutableStateFlow(UserPreferences.DEFAULT_GOAL_ML)
    val dailyGoal: StateFlow<Int> = _dailyGoal.asStateFlow()

    private val _existingIntake = MutableStateFlow(0)
    val existingIntake: StateFlow<Int> = _existingIntake.asStateFlow()

    private val _amountToRestore = MutableStateFlow(0)
    val amountToRestore: StateFlow<Int> = _amountToRestore.asStateFlow()

    private val _previewLogs = MutableStateFlow<List<WaterLogEntity>>(emptyList())
    val previewLogs: StateFlow<List<WaterLogEntity>> = _previewLogs.asStateFlow()

    init {
        viewModelScope.launch {
            _selectedDate.collectLatest { date ->
                updateGoalAndIntakeForDate(date)
            }
        }
    }

    fun setDate(date: LocalDate) {
        if (date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now())) {
            _selectedDate.value = date
        }
    }

    fun setAmountToRestore(amount: Int) {
        _amountToRestore.value = maxOf(0, amount)
    }

    private suspend fun updateGoalAndIntakeForDate(date: LocalDate) {
        val goal = waterRepository.getDailyGoal(date)
        _dailyGoal.value = goal

        val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        
        val logs = waterLogDao.getLogsInRangeSuspend(start, end)
        val existing = logs.filter { !it.isDeleted }.sumOf { it.amountMl }
        _existingIntake.value = existing
        
        // Suggest missing amount, default to 0 if already hit goal
        _amountToRestore.value = maxOf(0, goal - existing)
    }

    fun addSingleEntry(amount: Int, time: LocalTime, note: String?) {
        if (amount <= 0) return
        
        val timestamp = ZonedDateTime.of(_selectedDate.value, time, ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val log = WaterLogEntity(
            amountMl = amount,
            timestamp = timestamp,
            note = note,
            source = "BACKFILLED"
        )
        
        viewModelScope.launch {
            waterLogDao.insertLog(log)
            updateGoalAndIntakeForDate(_selectedDate.value)
        }
    }

    fun generatePreview(startTime: LocalTime, endTime: LocalTime, entries: Int) {
        if (entries <= 0 || _amountToRestore.value <= 0) {
            _previewLogs.value = emptyList()
            return
        }

        val totalAmount = _amountToRestore.value
        val baseAmount = totalAmount / entries
        val remainder = totalAmount % entries

        // 1. Amount Distribution (Balanced)
        val amounts = IntArray(entries) { baseAmount }
        
        // Spread remainder evenly
        if (remainder > 0) {
            val step = entries.toFloat() / remainder.toFloat()
            for (i in 0 until remainder) {
                val index = (i * step).toInt().coerceIn(0, entries - 1)
                amounts[index]++
            }
        }

        // 2. Time Distribution
        val startMins = startTime.hour * 60 + startTime.minute
        val endMins = endTime.hour * 60 + endTime.minute
        
        val durationMins = if (endMins >= startMins) {
            endMins - startMins
        } else {
            // End time is next day, shouldn't happen based on UI constraints but handle gracefully
            (24 * 60 - startMins) + endMins
        }

        val intervalMins = if (entries > 1) durationMins / (entries - 1).coerceAtLeast(1) else 0

        val generatedLogs = mutableListOf<WaterLogEntity>()
        
        // To avoid collisions, we need existing logs for the day
        viewModelScope.launch {
            val startTs = _selectedDate.value.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endTs = _selectedDate.value.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            val existingLogs = waterLogDao.getLogsInRangeSuspend(startTs, endTs).filter { !it.isDeleted }

            for (i in 0 until entries) {
                var currentMins = startMins + (i * intervalMins)
                if (currentMins >= 24 * 60) currentMins = 23 * 60 + 59 // Cap at 11:59 PM
                
                var time = LocalTime.of(currentMins / 60, currentMins % 60)
                var timestamp = ZonedDateTime.of(_selectedDate.value, time, ZoneId.systemDefault()).toInstant().toEpochMilli()
                
                // Collision Detection (Avoid putting logs within 5 minutes of existing ones)
                var attempts = 0
                while (existingLogs.any { Math.abs(it.timestamp - timestamp) < 5 * 60 * 1000 } && attempts < 5) {
                    time = time.plusMinutes(7) // Shift slightly
                    timestamp = ZonedDateTime.of(_selectedDate.value, time, ZoneId.systemDefault()).toInstant().toEpochMilli()
                    attempts++
                }

                generatedLogs.add(
                    WaterLogEntity(
                        amountMl = amounts[i],
                        timestamp = timestamp,
                        source = "BACKFILLED"
                    )
                )
            }
            
            _previewLogs.value = generatedLogs
        }
    }

    fun savePreview() {
        val logs = _previewLogs.value
        if (logs.isEmpty()) return

        viewModelScope.launch {
            waterLogDao.insertLogs(logs)
            _previewLogs.value = emptyList() // Clear preview after save
            updateGoalAndIntakeForDate(_selectedDate.value)
        }
    }
}
