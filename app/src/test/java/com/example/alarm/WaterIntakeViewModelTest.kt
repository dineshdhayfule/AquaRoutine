package com.example.alarm

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.room.Room
import com.example.alarm.data.AppDatabase
import com.example.alarm.viewmodel.WaterIntakeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class WaterIntakeViewModelTest {

    private lateinit var viewModel: WaterIntakeViewModel
    private lateinit var application: Application
    private lateinit var database: AppDatabase
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        
        // Inject the in-memory database into the singleton field
        val instanceField = AppDatabase::class.java.getDeclaredField("INSTANCE")
        instanceField.isAccessible = true
        instanceField.set(null, database)
        
        viewModel = WaterIntakeViewModel(application)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization should create 8 default alarms`() = runTest {
        // Wait for flow to emit exactly 8 alarms
        val alarms = viewModel.alarms.filter { it.size == 8 }.first()
        assertEquals(8, alarms.size)
        assertEquals("09:00 AM", alarms[0].timeString)
        assertEquals("11:00 PM", alarms[7].timeString)
        alarms.forEach { assertFalse(it.isCustom) }
    }

    @Test
    fun `toggleAlarm should flip isActive state`() = runTest {
        val firstAlarm = viewModel.alarms.filter { it.isNotEmpty() }.first()[0]
        assertTrue(firstAlarm.isActive)

        viewModel.toggleAlarm(firstAlarm)
        
        // Wait for update
        val updatedAlarms = viewModel.alarms.filter { it[0].isActive != firstAlarm.isActive }.first()
        assertFalse(updatedAlarms[0].isActive)
    }

    @Test
    fun `addCustomAlarm should add a new custom alarm`() = runTest {
        viewModel.alarms.filter { it.size == 8 }.first()
        
        viewModel.addCustomAlarm(14, 30) // 2:30 PM
        
        val updatedAlarms = viewModel.alarms.filter { it.size == 9 }.first()
        assertEquals(9, updatedAlarms.size)
        val customAlarm = updatedAlarms.find { it.isCustom }!!
        assertEquals("02:30 PM", customAlarm.timeString)
        assertEquals(14, customAlarm.hour)
        assertEquals(30, customAlarm.minute)
        assertTrue(customAlarm.isCustom)
    }

    @Test
    fun `toggleMaster should update all alarms`() = runTest {
        viewModel.alarms.filter { it.isNotEmpty() }.first()
        
        viewModel.toggleMaster(false)
        val allDisabled = viewModel.alarms.filter { it.all { !it.isActive } }.first()
        allDisabled.forEach { assertFalse(it.isActive) }

        viewModel.toggleMaster(true)
        val allEnabled = viewModel.alarms.filter { it.all { it.isActive } }.first()
        allEnabled.forEach { assertTrue(it.isActive) }
    }

    @Test
    fun `deleteAlarm should remove only custom alarms`() = runTest {
        val initialAlarms = viewModel.alarms.filter { it.size == 8 }.first()
        val initialSize = initialAlarms.size
        
        val defaultAlarm = initialAlarms[0]
        viewModel.deleteAlarm(defaultAlarm)
        // Wait a bit to ensure it's NOT deleted
        advanceUntilIdle()
        assertEquals(initialSize, viewModel.alarms.value.size)

        viewModel.addCustomAlarm(10, 0)
        val withCustom = viewModel.alarms.filter { it.size == 9 }.first()
        val customAlarm = withCustom.find { it.isCustom }!!
        assertEquals(9, withCustom.size)

        viewModel.deleteAlarm(customAlarm)
        val backToDefault = viewModel.alarms.filter { it.size == 8 }.first()
        assertEquals(8, backToDefault.size)
        assertFalse(backToDefault.any { it.isCustom })
    }
}
