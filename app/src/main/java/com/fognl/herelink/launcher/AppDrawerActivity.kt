package com.fognl.herelink.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.fognl.herelink.launcher.adapter.AppLaunchAdapter
import com.fognl.herelink.launcher.model.AppLaunch
import com.fognl.herelink.launcher.model.AppLaunchStorage
import kotlinx.android.synthetic.main.activity_app_drawer.*


class AppDrawerActivity : AppCompatActivity() {

    private val launchAdapterListener = object: AppLaunchAdapter.ItemListener {
        override fun onItemClicked(position: Int, item: AppLaunch) {
            launchItem(item)
        }

        override fun onItemLongPressed(position: Int, item: AppLaunch) {
            showOptionsFor(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_drawer)

        layout_progress.visibility = View.GONE

        val layoutManager = GridLayoutManager(this, 7)
        rv_installed_apps.layoutManager = layoutManager
    }

    override fun onResume() {
        super.onResume()
        loadApps()
    }

    private fun loadApps() {
        layout_progress.visibility = View.VISIBLE
        AppLaunchStorage.instance.loadInstalledApps { items ->
            val adapter = AppLaunchAdapter(items, launchAdapterListener)
            rv_installed_apps.adapter = adapter
            layout_progress.visibility = View.GONE
        }
    }

    private fun launchItem(item: AppLaunch) {
        AppLaunch.fire(item) { intent ->
            try {
                startActivity(intent)
            } catch(ex: Throwable) {
                Toast.makeText(applicationContext, ex.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showOptionsFor(item: AppLaunch) {
        val names = arrayOf(
            getString(R.string.context_launch),
            getString(R.string.context_add_to_launcher),
            getString(R.string.context_uninstall)
            )

        AlertDialog.Builder(this)
            .setItems(names) { dlg, which ->
                when(which) {
                    0 -> launchItem(item)
                    1 -> onAddToLauncher(item)
                    2 -> onDeleteApp(item)
                }
            }
            .create()
            .show()
    }

    private fun onAddToLauncher(item: AppLaunch) {
        AppLaunchStorage.instance.addToFavorites(item) { success ->
            val resId = if(success) R.string.toast_added_app_success else R.string.toast_added_app_fail
            Toast.makeText(applicationContext, getString(resId, item.title), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onDeleteApp(item: AppLaunch) {
        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.dlg_delete_app_title)
            .setMessage(getString(R.string.dlg_delete_app_s, item.title))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                doUninstallApp(item)
            }
            .create()
            .show()
    }

    private fun doUninstallApp(item: AppLaunch) {
        val packageURI: Uri = Uri.parse("package:${item.packageName}")
        val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
        startActivity(uninstallIntent)
    }
}
