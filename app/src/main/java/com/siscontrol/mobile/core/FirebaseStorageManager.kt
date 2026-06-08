package com.siscontrol.mobile.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

object FirebaseStorageManager {
    private const val TAG = "FIREBASE_UPLOAD"

    /**
     * Sube una imagen COMPRIMIDA a Firebase Storage con un TIEMPO LÍMITE de 20 segundos.
     */
    suspend fun uploadImage(context: Context, uri: Uri, folder: String): Result<String> {
        val appContext = context.applicationContext
        return try {
            kotlinx.coroutines.withTimeout(20000) { 
                Log.d(TAG, "Iniciando subida a Firebase...")
                
                val storage = FirebaseStorage.getInstance("gs://vito-sis-control.firebasestorage.app")
                val storageRef = storage.reference
                
                // --- NOMBRE ACORTADO: Tiempo Hex + 4 caracteres aleatorios ---
                val shortId = java.lang.Long.toHexString(System.currentTimeMillis()) + 
                              UUID.randomUUID().toString().take(4)
                val fileName = "$shortId.jpg"
                
                val fileRef = storageRef.child("$folder/$fileName")

                val compressedData = compressImage(appContext, uri)
                
                Log.d(TAG, "Subiendo bytes (Nombre: $fileName)...")
                fileRef.putBytes(compressedData).await()
                
                // --- LIMPIEZA DE URL: Eliminamos el token para acortar el texto ---
                val rawUrl = fileRef.downloadUrl.await().toString()
                val shortUrl = rawUrl.split("?").first() + "?alt=media"
                
                Log.d(TAG, "✅ URL Acortada: $shortUrl")
                Result.success(shortUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ FALLA FIREBASE: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    /**
     * Sube un BITMAP (generalmente el resultado de un recorte) a Firebase.
     */
    suspend fun uploadBitmap(bitmap: Bitmap, folder: String): Result<String> {
        return try {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            val data = out.toByteArray()
            
            // --- NOMBRE ACORTADO ---
            val shortId = java.lang.Long.toHexString(System.currentTimeMillis()) + 
                          UUID.randomUUID().toString().take(4)
            val fileName = "$shortId.jpg"
            
            val storageRef = FirebaseStorage.getInstance().reference
            val fileRef = storageRef.child("$folder/$fileName")

            fileRef.putBytes(data).await()
            
            // --- LIMPIEZA DE URL ---
            val rawUrl = fileRef.downloadUrl.await().toString()
            val shortUrl = rawUrl.split("?").first() + "?alt=media"
            Result.success(shortUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sube un arreglo de BYTES directamente a Firebase.
     * Esto ahorra mucho tiempo y memoria.
     */
    suspend fun uploadBytes(data: ByteArray, folder: String): Result<String> {
        return try {
            kotlinx.coroutines.withTimeout(20000) {
                // --- NOMBRE ACORTADO ---
                val shortId = java.lang.Long.toHexString(System.currentTimeMillis()) + 
                              UUID.randomUUID().toString().take(4)
                val fileName = "$shortId.jpg"
                
                val storage = FirebaseStorage.getInstance("gs://vito-sis-control.firebasestorage.app")
                val fileRef = storage.reference.child("$folder/$fileName")

                fileRef.putBytes(data).await()
                
                // --- LIMPIEZA DE URL ---
                val rawUrl = fileRef.downloadUrl.await().toString()
                val shortUrl = rawUrl.split("?").first() + "?alt=media"
                
                Log.d(TAG, "✅ URL Acortada: $shortUrl")
                Result.success(shortUrl)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Falla subida bytes: ${e.message}")
            Result.failure(e)
        }
    }

    private fun compressImage(context: Context, uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri) 
            ?: throw Exception("No se pudo acceder al archivo local")
            
        val original = BitmapFactory.decodeStream(inputStream)
            ?: throw Exception("La imagen está corrupta o no es válida")
        
        // 1. CORREGIR ORIENTACIÓN (EXIF)
        val corrected = rotateImageIfRequired(context, original, uri)

        // 2. REDIMENSIONAR
        val width = 1024
        val ratio = corrected.width.toFloat() / corrected.height.toFloat()
        val height = (width / ratio).toInt()
        val scaled = Bitmap.createScaledBitmap(corrected, width, height, true)

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 75, out)
        
        val bytes = out.toByteArray()
        inputStream.close()
        original.recycle()
        if (corrected != original) corrected.recycle()
        if (scaled != corrected) scaled.recycle()
        
        return bytes
    }

    /**
     * Corrige la rotación de la imagen basándose en la información EXIF.
     */
    fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        val input = context.contentResolver.openInputStream(selectedImage) ?: return img
        val ei = ExifInterface(input)
        
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        
        val rotated = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
        
        input.close()
        return rotated
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        // No reciclamos img aquí porque podría ser la original y se maneja arriba
        return rotatedImg
    }
}
