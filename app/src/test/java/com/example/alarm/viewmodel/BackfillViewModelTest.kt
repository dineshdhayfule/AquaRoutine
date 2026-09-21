package com.example.alarm.viewmodel

import com.example.alarm.data.UserPreferences
import com.example.alarm.data.dao.WaterLogDao
import com.example.alarm.data.entity.WaterLogEntity
import com.example.alarm.data.repository.WaterRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class BackfillViewModelTest {

    private val waterRepository = mockk<WaterRepository>()
    private val waterLogDao = mockk<WaterLogDao>()
    private val userPreferences = mockk<UserPreferences>()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: BackfillViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        coEvery { waterRepository.getDailyGoal(any()) } returns 3000
        coEvery { waterLogDao.getLogsInRangeSuspend(any(), any()) } returns emptyList()
        
        viewModel = BackfillViewModel(waterRepository, waterLogDao, userPreferences)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test exact division distribution`() = runTest {
        viewModel.setAmountToRestore(3000)
        viewModel.generatePreview(LocalTime.of(9, 0), LocalTime.of(15, 0), 6)
        
        val logs = viewModel.previewLogs.value
        assertEquals(6, logs.size)
        assertTrue(logs.all { it.amountMl == 500 })
        assertEquals(3000, logs.sumOf { it.amountMl })
    }

    @Test
    fun `test uneven division distribution`() = runTest {
        viewModel.setAmountToRestore(1750)
        viewModel.generatePreview(LocalTime.of(9, 0), LocalTime.of(15, 0), 4)
        
        val logs = viewModel.previewLogs.value
        assertEquals(4, logs.size)
        // 1750 / 4 = 437 with remainder 2
        // Distribution should be like [438, 437, 438, 437]
        assertEquals(1750, logs.sumOf { it.amountMl })
        val count438 = logs.count { it.amountMl == 438 }
        val count437 = logs.count { it.amountMl == 437 }
        assertEquals(2, count438)
        assertEquals(2, count437)
    }

    @Test
    fun `test small division distribution`() = runTest {
        viewModel.setAmountToRestore(250)
        viewModel.generatePreview(LocalTime.of(9, 0), LocalTime.of(15, 0), 3)
        
        val logs = viewModel.previewLogs.value
        assertEquals(3, logs.size)
        // 250 / 3 = 83 with remainder 1
        assertEquals(250, logs.sumOf { it.amountMl })
        assertTrue(logs.any { it.amountMl == 84 })
    }

    @Test
    fun `test daily goal minus existing calculation`() = runTest {
        val date = LocalDate.now().minusDays(1)
        
        coEvery { waterRepository.getDailyGoal(any()) } returns 3000
        coEvery { waterLogDao.getLogsInRangeSuspend(any(), any()) } returns listOf(
            WaterLogEntity(amountMl = 1250, timestamp = System.currentTimeMillis())
        )
        
        viewModel.setDate(date)
        
        assertEquals(3000, viewModel.dailyGoal.value)
        assertEquals(1250, viewModel.existingIntake.value)
        assertEquals(1750, viewModel.amountToRestore.value)
    }
}
