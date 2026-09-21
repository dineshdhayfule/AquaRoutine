package com.example.alarm

import android.content.Context
import com.example.alarm.data.SecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OwnerModeManager private constructor(context: Context) {
    
    private val securityManager = SecurityManager(context)
    
    private val _isOwnerModeActive = MutableStateFlow(false)
    val isOwnerModeActive: StateFlow<Boolean> = _isOwnerModeActive.asStateFlow()

    private var timeoutJob: Job? = null
    private val TIMEOUT_MILLIS = 5 * 60 * 1000L // 5 minutes

    fun unlock(pin: String): Boolean {
        if (securityManager.verifyPin(pin)) {
            _isOwnerModeActive.value = true
            resetTimeout()
            return true
        }
        return false
    }

    fun lock() {
        _isOwnerModeActive.value = false
        timeoutJob?.cancel()
    }

    fun resetTimeout() {
        if (_isOwnerModeActive.value) {
            timeoutJob?.cancel()
            timeoutJob = CoroutineScope(Dispatchers.Main).launch {
                delay(TIMEOUT_MILLIS)
                lock()
            }
        }
    }
    
    fun isPinSet(): Boolean {
        return securityManager.isPinSet()
    }

    companion object {
        @Volatile
        private var instance: OwnerModeManager? = null

        fun getInstance(context: Context): OwnerModeManager {
            return instance ?: synchronized(this) {
                instance ?: OwnerModeManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
