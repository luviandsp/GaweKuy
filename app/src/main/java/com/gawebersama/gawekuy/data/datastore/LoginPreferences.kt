package com.gawebersama.gawekuy.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.datastore: DataStore<Preferences> by preferencesDataStore(name = "app")

class LoginPreferences(context: Context) {
    private val dataStore = context.datastore

    companion object {
        private val LOGIN_STATUS_KEY = booleanPreferencesKey("loginStatus")
        private val ADMIN_LOGIN_STATUS_KEY = booleanPreferencesKey("adminLoginStatus")
    }

    suspend fun setAdminLoginStatus(loginStatus: Boolean) {
        dataStore.edit { preferences ->
            preferences[ADMIN_LOGIN_STATUS_KEY] = loginStatus
        }
    }

    suspend fun getAdminLoginStatus(): Boolean? {
        return dataStore.data.first()[ADMIN_LOGIN_STATUS_KEY] ?: false
    }

    suspend fun setLoginStatus(loginStatus: Boolean) {
        dataStore.edit { preferences ->
            preferences[LOGIN_STATUS_KEY] = loginStatus
        }
    }

    suspend fun getLoginStatus(): Boolean? {
        return dataStore.data.first()[LOGIN_STATUS_KEY] ?: false
    }
}