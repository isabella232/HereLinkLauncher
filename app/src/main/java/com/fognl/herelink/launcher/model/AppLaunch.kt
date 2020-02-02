package com.fognl.herelink.launcher.model

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.util.Log
import com.fognl.herelink.launcher.LauncherApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileReader
import java.io.FileWriter

private const val TAG = "AppLaunch"

/** An app that can be launched by the launcher */
class AppLaunch {
    companion object {
        fun fire(input: AppLaunch, callback: (intent: Intent) -> Unit) {
            val manager = LauncherApp.get().packageManager
            input.packageName?.let { name ->
                val intent = manager.getLaunchIntentForPackage(name.toString())
                intent?.let { _ ->
                    callback(intent)
                }
            }
        }
    }

    var title: CharSequence? = null
    var packageName: CharSequence? = null
    var icon: Drawable? = null
}

class AppLaunchStorage private constructor(val context: Context) {
    companion object {
        private lateinit var _instance: AppLaunchStorage

        val instance: AppLaunchStorage get() = _instance

        fun init(context: Context) {
            _instance = AppLaunchStorage(context)
            _instance.loadPackages()
        }

        private fun getFavoritesFile(context: Context): File {
            return File(context.getExternalFilesDir(null), "favorites")
        }
    }

    private val packageNames = mutableSetOf<CharSequence>()

    fun addToFavorites(item: AppLaunch, callback: (success: Boolean) -> Unit) {
        item.packageName?.let { name ->
            packageNames.add(name)

            val success = savePackageNames()
            callback(success)
        }
    }

    fun removeFromFavorites(item: AppLaunch, callback: (success: Boolean) -> Unit) {
        item.packageName?.let { name ->
            packageNames.remove(name)

            val success = savePackageNames()
            callback(success)
        }
    }

    val launcherItems: List<AppLaunch>
        get() {
            val output = mutableListOf<AppLaunch>()

            with(LauncherApp.get().packageManager) {
                packageNames.forEach { name ->
                    val launch = Intent(Intent.ACTION_MAIN).setPackage(name.toString()).addCategory(Intent.CATEGORY_LAUNCHER)

                    resolveActivity(launch, 0)?.let { info ->
                        output.add(toAppLaunch(info, this))
                    }
                }
            }

            return output
        }


    fun loadInstalledApps(callback: (List<AppLaunch>) -> Unit) {
        fun retrieveApps(): List<AppLaunch> {
            val output = mutableListOf<AppLaunch>()

            val launch = Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER)

            with(LauncherApp.get().packageManager) {
                queryIntentActivities(launch, 0).forEach { ri ->
                    output.add(toAppLaunch(ri, this))
                }
            }

            return output
        }

        GlobalScope.launch(Dispatchers.Main) {
            val list = async(Dispatchers.IO) { retrieveApps() }
            callback(list.await())
        }
    }

    private fun loadPackages() {
        val file = getFavoritesFile(LauncherApp.get())

        try {
            val reader = FileReader(file)
            try {
                val lines = reader.readLines()
                packageNames.addAll(lines)
                Log.v(TAG, "packages=${packageNames}")
            } finally {
                reader.close()
            }
        } catch(ex: Throwable) {
            Log.e(TAG, ex.message, ex)
        }
    }

    private fun savePackageNames(): Boolean {
        var strings = ""
        packageNames.forEach {
            strings += it
            strings += "\n"
        }

        val file = getFavoritesFile(LauncherApp.get())

        try {
            val writer = FileWriter(file)
            try {
                writer.write(strings)
            } finally {
                writer.flush()
                writer.close()
            }

            return true
        } catch(ex: Throwable) {
            Log.e(TAG, ex.message, ex)
            return false
        }
    }

    private fun toAppLaunch(info: ResolveInfo, manager: PackageManager): AppLaunch {
        val app = AppLaunch()
        app.title = info.loadLabel(manager)
        app.packageName = info.activityInfo.packageName
        app.icon = info.activityInfo.loadIcon(manager)
        return app
    }
}
