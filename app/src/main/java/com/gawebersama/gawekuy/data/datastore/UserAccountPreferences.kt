package com.gawebersama.gawekuy.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gawebersama.gawekuy.data.datamodel.TempUser
import kotlinx.coroutines.flow.first

private val Context.datastore by preferencesDataStore(name = "user_account_temp_preferences")

class UserAccountPreferences(private val context: Context) {

    companion object {
        private val EMAIL_KEY = stringPreferencesKey("email")
        private val NAME_KEY = stringPreferencesKey("name")
        private val PHONE_KEY = stringPreferencesKey("phone")
        private val ROLE_KEY = stringPreferencesKey("role")
        private val REGISTERED_KEY = booleanPreferencesKey("registered")
        const val TAG = "UserAccountTempPreferences"
    }

    suspend fun setRegistered(isRegistered: Boolean) {
        context.datastore.edit { preferences ->
            preferences[REGISTERED_KEY] = isRegistered
        }
    }

    suspend fun isRegistered(): Boolean {
        val preferences = context.datastore.data.first()
        return preferences[REGISTERED_KEY] ?: false
    }

    suspend fun saveTempUser(email: String, name: String, phone: String, role: String) {
        context.datastore.edit { preferences ->
            preferences[EMAIL_KEY] = email
            preferences[NAME_KEY] = name
            preferences[PHONE_KEY] = phone
            preferences[ROLE_KEY] = role
        }

        Log.d(TAG, "email=$email, name=$name, phone=$phone, role=$role")
    }

    suspend fun getTempUser(): TempUser? {
        val preferences = context.datastore.data.first()
        val email = preferences[EMAIL_KEY]
        val name = preferences[NAME_KEY]
        val phone = preferences[PHONE_KEY]
        val role = preferences[ROLE_KEY]

        Log.d(TAG, "email=$email, name=$name, phone=$phone, role=$role")

        return if (email!= null && name != null && phone != null && role != null) {
            TempUser(email, name, phone, role)
        } else null
    }

    suspend fun clearTempUser() {
        context.datastore.edit { it.clear() }
    }
}
