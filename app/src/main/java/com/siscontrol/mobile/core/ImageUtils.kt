package com.siscontrol.mobile.core

import android.content.Context
import android.graphics.*
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*

object ImageUtils {

    /**
     * Estampa una marca de agua con fecha, hora y el nombre de la app en la imagen.
     * Versión compatible que guarda en disco.
     */
    fun applyWatermark(context: Context, imageUri: Uri, extraText: String? = null): Uri {
        val bytes = processImageForUpload(context, imageUri, extraText) ?: return imageUri
        return try {
            val outputStream = context.contentResolver.openOutputStream(imageUri)
            outputStream?.use { it.write(bytes) }
            imageUri
        } catch (e: Exception) {
            imageUri
        }
    }

    /**
     * Procesa la imagen (Redimensión + Rotación + Marca de Agua + Compresión)
     * Genera un archivo liviano (< 300KB) para evitar errores de tamaño en el servidor.
     */
    fun processImageForUpload(context: Context, imageUri: Uri, extraText: String? = null): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            
            // 1. Decodificación inteligente para ahorrar RAM
            val options = BitmapFactory.Options().apply { inSampleSize = 1 } 
            val originalBitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            if (originalBitmap == null) return null

            // 2. REDIMENSIÓN PROFESIONAL (Máximo 1024px de ancho/alto)
            val maxWidth = 1024
            val maxHeight = 1024
            val scale = Math.min(maxWidth.toFloat() / originalBitmap.width, maxHeight.toFloat() / originalBitmap.height)
            
            val matrix = Matrix()
            if (scale < 1.0) matrix.postScale(scale, scale)

            // 3. CORRECCIÓN DE ROTACIÓN (EXIF)
            val exifInputStream = context.contentResolver.openInputStream(imageUri)
            exifInputStream?.use {
                val exif = androidx.exifinterface.media.ExifInterface(it)
                val rotation = when (exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, 1)) {
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
                if (rotation != 0) matrix.postRotate(rotation.toFloat())
            }

            val resizedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)

            // 4. MARCA DE AGUA SOBRE IMAGEN REDIMENSIONADA
            val workingBitmap = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(workingBitmap)
            val paint = Paint().apply {
                color = Color.WHITE
                textSize = (workingBitmap.height / 50).toFloat().coerceAtLeast(14f) // Reducido para mayor legibilidad
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(3f, 1f, 1f, Color.BLACK) // Sombra optimizada
            }

            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Posicionamiento en la esquina inferior izquierda con mejor margen
            val margin = 20f
            val spacing = 5f
            
            canvas.drawText("SIS CONTROL - $dateStr", margin, workingBitmap.height - margin, paint)
            extraText?.let { 
                canvas.drawText(it.uppercase(), margin, workingBitmap.height - margin - paint.textSize - spacing, paint) 
            }

            // 5. COMPRESIÓN AGRESIVA (60% es perfecto para reportes técnicos)
            val out = java.io.ByteArrayOutputStream()
            workingBitmap.compress(Bitmap.CompressFormat.JPEG, 60, out)
            val result = out.toByteArray()
            
            // Liberar memoria inmediatamente
            originalBitmap.recycle()
            resizedBitmap.recycle()
            workingBitmap.recycle()
            
            android.util.Log.d("IMAGE_UTILS", "Imagen optimizada. Tamaño final: ${result.size / 1024} KB")
            result
        } catch (e: Exception) {
            android.util.Log.e("IMAGE_UTILS", "Error optimizando imagen: ${e.message}")
            null
        }
    }
}
