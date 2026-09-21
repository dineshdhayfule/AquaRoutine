package com.example.alarm.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.security.MessageDigest

private val Context.securityDataStore by preferencesDataStore("owner_security_prefs")

class SecurityManager(private val context: Context) {

    fun saveOwnerPin(pin: String) = runBlocking {
        val hash = hashPin(pin)
        context.securityDataStore.edit { prefs ->
            prefs[KEY_OWNER_PIN_HASH] = hash
        }
    }

    private fun getOwnerPinHash(): String? = runBlocking {
        context.securityDataStore.data.map { it[KEY_OWNER_PIN_HASH] }.first()
    }

    fun disableOwnerPin() = runBlocking {
        context.securityDataStore.edit { prefs ->
            prefs.remove(KEY_OWNER_PIN_HASH)
        }
    }
    
    fun verifyPin(pin: String): Boolean {
        val storedHash = getOwnerPinHash() ?: return false
        return storedHash == hashPin(pin)
    }
    
    fun isPinSet(): Boolean {
        return getOwnerPinHash() != null
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val KEY_OWNER_PIN_HASH = stringPreferencesKey("owner_pin_hash")
    }
}
