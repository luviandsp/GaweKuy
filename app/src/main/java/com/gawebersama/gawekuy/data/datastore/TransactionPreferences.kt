package com.gawebersama.gawekuy.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first


private val Context.datastore by preferencesDataStore(name = "transaction_preferences")

class TransactionPreferences(private val context: Context) {

    companion object {
        private val TRANSACTION_ID_KEY = stringPreferencesKey("transaction_id")
        private val GROSS_AMOUNT = intPreferencesKey("gross_amount")
        private val SERVICE_NAME = stringPreferencesKey("service_name")
        private val SELECTED_SERVICE = stringPreferencesKey("selected_service")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val TRANSACTION_STATUS = stringPreferencesKey("transaction_status")
    }

    suspend fun saveTransactionId(transactionId: String) {
        context.datastore.edit { preferences ->
            preferences[TRANSACTION_ID_KEY] = transactionId
        }
    }

    suspend fun saveGrossAmount(grossAmount: Int) {
        context.datastore.edit { preferences ->
            preferences[GROSS_AMOUNT] = grossAmount
        }
    }

    suspend fun saveServiceName(serviceName: String) {
        context.datastore.edit { preferences ->
            preferences[SERVICE_NAME] = serviceName
        }
    }

    suspend fun saveSelectedService(selectedService: String) {
        context.datastore.edit { preferences ->
            preferences[SELECTED_SERVICE] = selectedService
        }
    }

    suspend fun saveToken(token: String) {
        context.datastore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun saveTransactionStatus(status: String) {
        context.datastore.edit { preferences ->
            preferences[TRANSACTION_STATUS] = status
        }
    }

    suspend fun getTransactionId(): String? {
        val preferences = context.datastore.data.first()
        return preferences[TRANSACTION_ID_KEY]
    }

    suspend fun getGrossAmount(): Int? {
        val preferences = context.datastore.data.first()
        return preferences[GROSS_AMOUNT]
    }

    suspend fun getServiceName(): String? {
        val preferences = context.datastore.data.first()
        return preferences[SERVICE_NAME]
    }

    suspend fun getSelectedService(): String? {
        val preferences = context.datastore.data.first()
        return preferences[SELECTED_SERVICE]
    }

    suspend fun getToken(): String? {
        val preferences = context.datastore.data.first()
        return preferences[TOKEN_KEY]
    }

    suspend fun getTransactionStatus(): String? {
        val preferences = context.datastore.data.first()
        return preferences[TRANSACTION_STATUS]
    }

    suspend fun clearTransactionData() {
        context.datastore.edit { preferences ->
            preferences.remove(TRANSACTION_ID_KEY)
            preferences.remove(TOKEN_KEY)
            preferences.remove(TRANSACTION_STATUS)
            preferences.remove(GROSS_AMOUNT)
            preferences.remove(SERVICE_NAME)
            preferences.remove(SELECTED_SERVICE)
        }
    }

}