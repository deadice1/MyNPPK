package com.example.nppk.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {
    private const val PREFS_NAME = "secure_nppk_prefs"

    private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveCredentials(context: Context, login: String, pass: String) {
        getPrefs(context).edit()
            .putString("saved_login", login)
            .putString("saved_pass", pass)
            .apply()
    }

    fun getSavedLogin(context: Context): String? {
        return getPrefs(context).getString("saved_login", null)
    }

    fun getSavedPassword(context: Context): String? {
        return getPrefs(context).getString("saved_pass", null)
    }

    fun clearCredentials(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
