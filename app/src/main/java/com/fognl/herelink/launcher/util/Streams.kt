package com.fognl.herelink.launcher.util

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object Streams {
    @Throws(IOException::class)
    fun <T : OutputStream>copyAndClose(input: InputStream, output: T): T {
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

        return output
    }
}
