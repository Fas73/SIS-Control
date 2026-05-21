package com.siscontrol.mobile.core

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseStorageManager {

    /**
     * Sube una imagen a Firebase Storage y retorna la URL pública de descarga.
     * @param uri URI local del archivo (desde cámara o galería)
     * @param folder Carpeta de destino ("evidencias" o "perfiles")
     */
    suspend fun uploadImage(uri: Uri, folder: String): Result<String> {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileName = "${folder}/${UUID.randomUUID()}.jpg"
            val fileRef = storageRef.child(fileName)

            // Subir archivo
            fileRef.putFile(uri).await()

            // Obtener URL de descarga
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            android.util.Log.e("FIREBASE_UPLOAD", "Error al subir imagen", e)
            Result.failure(e)
        }
    }
}
