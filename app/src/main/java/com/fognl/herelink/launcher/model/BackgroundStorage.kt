package com.fognl.herelink.launcher.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import com.fognl.herelink.launcher.util.AppPrefs
import java.io.*

class BackgroundStorage {
    companion object {
        private val TAG = BackgroundStorage::class.java.simpleName

        private fun getImagesDir(context: Context): File = File(context.getExternalFilesDir(null), "backgrounds")

        private fun copyAndClose(input: InputStream, output: OutputStream) {
            try {
                try {
                    val buf = ByteArray(4096)
                    var read = input.read(buf)
                    while(read != -1) {
                        output.write(buf)
                        read = input.read(buf)
                    }
                } finally {
                    input.close()
                    output.flush()
                    output.close()
                }

            } catch(ex: IOException) {
                Log.e(TAG, ex.message, ex)
            }
        }

        fun dumpImages(context: Context) {
            val dir = getImagesDir(context)

            if(!dir.exists() && !dir.mkdirs()) {
                Log.w(TAG,"Unable to create dir ${dir}")
                return
            }

            val assMan = context.assets
            assMan.list("backgrounds")?.let { images ->
                images.forEach { img ->
                    val file = File(dir, img)
                    if(!file.exists()) {
                        try {
                            val input = assMan.open("backgrounds/${img}")
                            val output = FileOutputStream(file)
                            copyAndClose(input, output)
                        } catch(ex: Throwable) {
                            Log.e(TAG, ex.message, ex)
                        }
                    }
                }
            }
        }

        fun getImages(context: Context): List<File> {
            val output = mutableListOf<File>()

            val dir = getImagesDir(context)
            dir.listFiles() { f -> f.isFile }?.let { files ->
                output.addAll(files)
            }

            return output
        }

        fun setBackground(view: View) {
            getBackgroundImage(view.context)?.let { file ->
                val drawable = Drawable.createFromPath(file.absolutePath)
                view.setBackgroundDrawable(drawable)
            }
        }

        private fun getBackgroundImage(context: Context): File? {
            val selectedFile = AppPrefs.instance.backgroundImage
            if(selectedFile != null) {
                return selectedFile
            } else {
                val dir = getImagesDir(context)
                if(dir.exists()) {
                    dir.listFiles() { f -> f.isFile() }?.let { files ->
                        return if(files.isEmpty()) null else files[0]
                    }
                }
            }

            return null
        }
    }
}