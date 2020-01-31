package com.fognl.herelink.launcher.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.io.File

class AppPrefs private constructor(val context: Context) {
    companion object {
        private lateinit var _instance: AppPrefs

        private const val PREF_BACKGROUND_IMAGE = "background_image"

        val instance: AppPrefs get() = _instance

        fun init(context: Context) {
            _instance = AppPrefs(context)
        }
    }

    private var prefs: SharedPreferences

    init {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    var backgroundImage: File?
        get() {
            prefs.getString(PREF_BACKGROUND_IMAGE, null)?.let { filename ->
                return File(filename)
            } ?: run {
                return null
            }
        }

        set(value) {
            value?.let { file ->
                prefs.edit().putString(PREF_BACKGROUND_IMAGE, file.absolutePath).apply()
            }
        }
}
