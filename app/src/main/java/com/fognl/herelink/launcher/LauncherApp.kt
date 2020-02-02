package com.fognl.herelink.launcher

import android.app.Application
import com.fognl.herelink.launcher.model.AppLaunchStorage
import com.fognl.herelink.launcher.model.BackgroundStorage
import com.fognl.herelink.launcher.util.AppPrefs

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

        AppPrefs.init(this)
        AppLaunchStorage.init(this)
        BackgroundStorage.dumpImages(this)

        AppLaunchStorage.instance.checkDefaults(this)
    }
}
