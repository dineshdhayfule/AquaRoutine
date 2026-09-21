package com.example.alarm.data.repository

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.alarm.data.AppDatabase
import com.example.alarm.data.UserPreferences
import com.example.alarm.data.entity.WaterLogEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WaterRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: WaterRepository
    private lateinit var userPreferences: UserPreferences

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userPreferences = UserPreferences(context)
        repository = WaterRepository(
            database.waterLogDao(),
            database.dailyGoalOverrideDao(),
            userPreferences
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `addWaterLog should not increase goal if total is below goal`() = runTest {
        val today = LocalDate.now()
        val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        
        userPreferences.saveDailyGoalMl(2000)
        
        repository.addWaterLog(500)
        
        val logs = database.waterLogDao().getLogsInRangeSuspend(start, end)
        assertEquals(1, logs.size)
        assertEquals(500, logs[0].amountMl)
        assertNull(logs[0].note)
        
        val override = database.dailyGoalOverrideDao().getOverrideForDate(today.toString()).first()
        assertNull(override)
    }

    @Test
    fun `addWaterLog should increase goal if total reaches goal`() = runTest {
        val today = LocalDate.now()
        val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        
        userPreferences.saveDailyGoalMl(2000)
        
        // Add 2000ml (matches goal)
        repository.addWaterLog(2000)
        
        val logs = database.waterLogDao().getLogsInRangeSuspend(start, end)
        assertEquals(1, logs.size)
        assertEquals("Goal reached! +1L extra added to your target.", logs[0].note)
        
        val override = database.dailyGoalOverrideDao().getOverrideForDate(today.toString()).first()
        assertNotNull(override)
        assertEquals(3000, override?.goalMl)
    }

    @Test
    fun `addWaterLog should increase goal multiple times if goal is reached again`() = runTest {
        val today = LocalDate.now()
        userPreferences.saveDailyGoalMl(1000)
        
        // 1. Reach 1000ml goal -> goal becomes 2000ml
        repository.addWaterLog(1000)
        var override = database.dailyGoalOverrideDao().getOverrideForDate(today.toString()).first()
        assertEquals(2000, override?.goalMl)
        
        // 2. Reach 2000ml goal -> goal becomes 3000ml
        repository.addWaterLog(1000)
        override = database.dailyGoalOverrideDao().getOverrideForDate(today.toString()).first()
        assertEquals(3000, override?.goalMl)
        
        val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        val logs = database.waterLogDao().getLogsInRangeSuspend(start, end)
        assertEquals(2, logs.size)
        assertEquals("Goal reached! +1L extra added to your target.", logs[0].note)
        assertEquals("Goal reached! +1L extra added to your target.", logs[1].note)
    }
}
