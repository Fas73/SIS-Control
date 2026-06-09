package com.siscontrol.mobile.core

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object MapUtils {

    /**
     * Crea un marcador con forma de Gota/Pin de Google Maps que contiene la inicial
     */
    fun createCustomMarker(context: Context, name: String, color: Int): BitmapDescriptor {
        val width = 100
        val height = 140
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // 1. Dibujar la forma del Pin (Gota)
        val path = Path()
        val centerX = width / 2f
        val radius = width / 2f
        
        // Parte circular superior
        path.addCircle(centerX, radius, radius, Path.Direction.CW)
        
        // Punta inferior (Triángulo que conecta)
        path.moveTo(0f, radius)
        path.lineTo(centerX, height.toFloat())
        path.lineTo(width.toFloat(), radius)
        path.close()
        
        // Rellenar con el color de estado
        paint.color = color
        canvas.drawPath(path, paint)

        // 2. Círculo blanco interior (donde va la letra)
        paint.color = Color.WHITE
        canvas.drawCircle(centerX, radius, radius * 0.75f, paint)

        // 3. Texto (Inicial)
        paint.color = color
        paint.textSize = 45f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        val initial = name.take(1).uppercase()
        val textBounds = Rect()
        paint.getTextBounds(initial, 0, 1, textBounds)
        val textHeight = textBounds.height()
        canvas.drawText(initial, centerX, radius + textHeight / 2f, paint)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
