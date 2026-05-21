package com.siscontrol.mobile.core

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraUtils {
    fun createTempImageUri(context: Context): Uri? {
        return try {
            val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir("Pictures")
            
            if (storageDir?.exists() == false) {
                storageDir?.mkdirs()
            }
            
            val file = File(storageDir, "IMG_$ts.jpg")
            FileProvider.getUriForFile(
                context,
                "com.siscontrol.mobile.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
}
