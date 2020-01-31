package com.fognl.herelink.launcher

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fognl.herelink.launcher.adapter.WallpaperAdapter
import com.fognl.herelink.launcher.model.BackgroundStorage
import kotlinx.android.synthetic.main.activity_wallpaper.*
import java.io.File

class WallpaperActivity : AppCompatActivity() {

    private val adapterListener = object : WallpaperAdapter.ItemListener {
        override fun onItemSelected(position: Int, file: File) {
            selectedFile = file
            val d = Drawable.createFromPath(file.absolutePath)
            image.setImageDrawable(d)
        }
    }

    var selectedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)

        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv_files.layoutManager = layoutManager

        btn_save.setOnClickListener { onSaveClick() }
    }

    override fun onResume() {
        super.onResume()

        val adapter = WallpaperAdapter(BackgroundStorage.getImages(this), adapterListener)
        rv_files.adapter = adapter
    }

    private fun onSaveClick() {
        selectedFile?.let { file ->
            setResult(RESULT_OK, Intent().setData(Uri.fromFile(file)))
            finish()
        } ?: run {
            finish()
        }
    }
}
