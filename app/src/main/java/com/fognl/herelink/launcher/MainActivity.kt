package com.fognl.herelink.launcher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.fognl.herelink.launcher.adapter.AppLaunchAdapter
import com.fognl.herelink.launcher.model.AppLaunch
import com.fognl.herelink.launcher.model.AppLaunchStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_main)

        val layoutManager = GridLayoutManager(applicationContext, 7)
        rv_installed_apps.layoutManager = layoutManager

        btn_app_drawer.setOnClickListener { onAppDrawerClick() }
    }

    override fun onResume() {
        super.onResume()
        loadApps()
    }

    private fun loadApps() {
        val adapter = AppLaunchAdapter(AppLaunchStorage.instance.launcherItems, launchAdapterListener)
        rv_installed_apps.adapter = adapter

        if(adapter.isEmpty()) {
            rv_installed_apps.visibility = View.GONE
            txt_empty.visibility = View.VISIBLE
        } else {
            rv_installed_apps.visibility = View.VISIBLE
            txt_empty.visibility = View.GONE
        }
    }

    private fun onAppDrawerClick() {
        startActivity(Intent(this, AppDrawerActivity::class.java))
    }

    private fun showOptionsFor(item: AppLaunch) {
        val names = arrayOf(
            getString(R.string.context_launch),
            getString(R.string.context_remove_from_launcher)
        )

        AlertDialog.Builder(this)
            .setItems(names) { dlg, which ->
                when(which) {
                    0 -> launchItem(item)
                    1 -> onRemoveFromLauncher(item)
                }
            }
            .create()
            .show()
    }

    private fun onRemoveFromLauncher(item: AppLaunch) {
        AppLaunchStorage.instance.removeFromFavorites(item) { success ->
            if(success) {
                loadApps()
            } else {
                Toast.makeText(this, R.string.toast_remove_app_fail, Toast.LENGTH_SHORT).show()
            }
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
}
