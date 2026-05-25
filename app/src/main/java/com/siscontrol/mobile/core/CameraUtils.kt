package com.siscontrol.mobile.core

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CameraUtils {
    private const val TAG = "CameraUtils"
    private const val AUTHORITY = "com.siscontrol.mobile.fileprovider"

    fun createTempImageUri(context: Context): Uri? {
        return try {
            val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = context.getExternalFilesDir("Pictures")
            
            if (storageDir == null) {
                Log.e(TAG, "External storage directory is null")
                return null
            }

            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }
            
            val file = File(storageDir, "IMG_$ts.jpg")
            // Ensure the file is created so the Uri is valid for the camera app
            if (file.exists()) file.delete()
            file.createNewFile()
            
            val uri = FileProvider.getUriForFile(context, AUTHORITY, file)
            Log.d(TAG, "URI successfully created: $uri")
            uri
        } catch (e: Exception) {
            Log.e(TAG, "Error creating temp image URI: ${e.message}")
            null
        }
    }
}
