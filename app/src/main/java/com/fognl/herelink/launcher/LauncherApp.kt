package com.fognl.herelink.launcher

import android.app.Application
import com.fognl.herelink.launcher.model.AppLaunchStorage

class LauncherApp: Application() {
    companion object {
        private lateinit var _instance: LauncherApp

        fun get(): LauncherApp = _instance
    }

    init {
        _instance = this
    }


    override fun onCreate() {
        super.onCreate()

        AppLaunchStorage.init(this)
    }
}
