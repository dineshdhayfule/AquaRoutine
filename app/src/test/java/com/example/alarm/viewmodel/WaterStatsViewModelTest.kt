package com.example.alarm.viewmodel

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.alarm.data.UserPreferences
import com.example.alarm.data.dao.DailyGoalOverrideDao
import com.example.alarm.data.dao.WaterLogDao
import com.example.alarm.data.entity.WaterLogEntity
import com.example.alarm.data.repository.WaterRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WaterStatsViewModelTest {

    private val waterLogDao = mockk<WaterLogDao>()
    private val dailyGoalOverrideDao = mockk<DailyGoalOverrideDao>()
    private val userPreferences = mockk<UserPreferences>()
    private val waterRepository = mockk<WaterRepository>()
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: WaterStatsViewModel
    
    private val dailyGoalFlow = MutableStateFlow(2000)
    private val weightFlow = MutableStateFlow(70f)
    private val activityFlow = MutableStateFlow("Sedentary")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        every { userPreferences.dailyGoalMl } returns dailyGoalFlow
        every { userPreferences.userWeightKg } returns weightFlow
        every { userPreferences.activityLevel } returns activityFlow
        every { dailyGoalOverrideDao.getOverrideForDate(any()) } returns flowOf(null)
        every { dailyGoalOverrideDao.getOverridesInRange(any(), any()) } returns flowOf(emptyList())
        
        viewModel = WaterStatsViewModel(
            waterLogDao,
            dailyGoalOverrideDao,
            userPreferences,
            waterRepository,
            context
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `chartData for WEEK should return 7 days of daily logs`() = runTest {
        val today = LocalDate.now()
        val logs = listOf(
            WaterLogEntity(1, 500, today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
        )
        
        every { waterLogDao.getLogsInRange(any(), any()) } returns flowOf(logs)
        
        // Start collecting to activate stateIn
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chartData.collect {}
        }
        
        viewModel.selectPeriod(StatisticsPeriod.WEEK)
        
        val data = viewModel.chartData.value
        assertEquals(7, data.size)
        assertEquals(500, data.last().second)
        assertEquals(2000, data.last().third)
        
        collectJob.cancel()
    }

    @Test
    fun `chartData for QUARTER should aggregate by week`() = runTest {
        val today = LocalDate.now()
        val logs = (0..6).map { i ->
            WaterLogEntity(
                id = i,
                amountMl = 1000,
                timestamp = today.minusDays(i.toLong()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
        }
        
        every { waterLogDao.getLogsInRange(any(), any()) } returns flowOf(logs)
        
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.chartData.collect {}
        }
        
        viewModel.selectPeriod(StatisticsPeriod.QUARTER)
        
        val data = viewModel.chartData.value
        assertEquals(13, data.size)
        
        val lastWeekData = data.last()
        assertEquals(1000, lastWeekData.second)
        assertEquals(2000, lastWeekData.third)
        
        collectJob.cancel()
    }

    @Test
    fun `dailySummary should calculate added and deleted water correctly`() = runTest {
        val logs = listOf(
            WaterLogEntity(1, 500, System.currentTimeMillis()),
            WaterLogEntity(2, 0, System.currentTimeMillis(), isDeleted = true, originalAmount = 250)
        )
        
        every { waterLogDao.getLogsForDay(any(), any()) } returns flowOf(logs)
        
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.dailySummary.collect {}
        }
        
        val today = LocalDate.now()
        viewModel.selectDate(today)
        
        val summary = viewModel.dailySummary.value
        assertEquals(0.5f, summary.first, 0.01f)
        assertEquals(0.25f, summary.second, 0.01f)
        
        collectJob.cancel()
    }
}
