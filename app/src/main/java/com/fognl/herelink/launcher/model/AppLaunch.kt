package com.fognl.herelink.launcher.model

import android.content.Context
import android.content.Intent
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
                intent?.let { target ->
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
        // TODO: Make this async like the other method
        get() {
            val output = mutableListOf<AppLaunch>()

            val manager = LauncherApp.get().packageManager
            packageNames.forEach { name ->
                val launch = Intent(Intent.ACTION_MAIN).setPackage(name.toString()).addCategory(Intent.CATEGORY_LAUNCHER)
                val resolveInfo = manager.resolveActivity(launch, 0)

                resolveInfo?.let { info ->
                    val app = AppLaunch()
                    app.title = info.loadLabel(manager)
                    app.packageName = info.activityInfo.packageName
                    app.icon = info.activityInfo.loadIcon(manager)
                    output.add(app)
                }
            }

            return output
        }


    fun loadInstalledApps(callback: (List<AppLaunch>) -> Unit) {
        fun retrieveApps(): List<AppLaunch> {
            val output = mutableListOf<AppLaunch>()

            val launch = Intent(Intent.ACTION_MAIN, null)
                .addCategory(Intent.CATEGORY_LAUNCHER)

            val manager = LauncherApp.get().packageManager
            val availableActivities = manager.queryIntentActivities(launch, 0)

            availableActivities?.forEach { ri ->
                val app = AppLaunch()
                app.title = ri.loadLabel(manager)
                app.packageName = ri.activityInfo.packageName
                app.icon = ri.activityInfo.loadIcon(manager)
                output.add(app)
            }

            return output
        }

        GlobalScope.launch(Dispatchers.Main) {
            val list = async(Dispatchers.IO) { retrieveApps() }
            callback(list.await())
        }
    }

    private fun loadPackages() {
        val context = LauncherApp.get()
        val file = File(context.getExternalFilesDir(null), "favorites")

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

        val context = LauncherApp.get()
        val file = File(context.getExternalFilesDir(null), "favorites")

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

        return false
    }

}
