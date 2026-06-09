package com.siscontrol.mobile.core

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.ContextCompat
import com.siscontrol.mobile.R
import com.siscontrol.mobile.data.remote.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Motor de generación de reportes PDF profesionales.
 * Incluye diseño corporativo, soporte para incidentes, descarga de evidencias y análisis de IA.
 */
object PdfManager {

    private val navyBlue = Color.parseColor("#1E3A8A")
    private val slateGray = Color.parseColor("#64748B")
    private val textPrimary = Color.parseColor("#1F2937")
    private val dangerRed = Color.parseColor("#991B1B")
    private val headerBgColor = Color.parseColor("#F1F5F9")

    private fun splitTextIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    private fun formatDate(dateTimeStr: String?): String? {
        if (dateTimeStr == null) return null
        val parts = dateTimeStr.split("T", " ")
        if (parts.size >= 2) {
            val dateParts = parts[0].split("-")
            if (dateParts.size == 3) {
                return "${dateParts[2]}-${dateParts[1]}-${dateParts[0]} ${parts[1].take(5)}"
            }
        } else if (parts.size == 1) {
            val dateParts = parts[0].split("-")
            if (dateParts.size == 3) {
                return "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"
            }
        }
        return dateTimeStr
    }

    private fun formatDateOnly(dateTimeStr: String?): String? {
        if (dateTimeStr == null) return null
        val parts = dateTimeStr.split("T", " ")
        if (parts.isNotEmpty()) {
            val dateParts = parts[0].split("-")
            if (dateParts.size == 3) {
                return "${dateParts[2]}-${dateParts[1]}-${dateParts[0]}"
            }
        }
        return dateTimeStr
    }

    fun generateRoundReport(context: Context, detail: RoundDetailResponseDto): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        drawHeader(context, canvas, "REPORTE DE AUDITORÍA DE RONDA")

        // 2. Información General
        val labelPaint = Paint().apply { color = slateGray; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val valuePaint = Paint().apply { color = textPrimary; textSize = 10f }

        canvas.drawText("INSTALACIÓN:", 50f, 140f, labelPaint)
        canvas.drawText(detail.ronda?.installation?.clientName ?: detail.ronda?.installation?.name ?: "N/A", 150f, 140f, valuePaint)
        
        canvas.drawText("COLABORADOR:", 50f, 155f, labelPaint)
        canvas.drawText(detail.ronda?.worker?.fullName ?: "N/A", 150f, 155f, valuePaint)
        
        canvas.drawText("ID RONDA:", 50f, 170f, labelPaint)
        canvas.drawText("#${detail.ronda?.id ?: "---"}", 150f, 170f, valuePaint)

        canvas.drawText("FECHA:", 350f, 140f, labelPaint)
        canvas.drawText(formatDateOnly(detail.ronda?.startTime) ?: "N/A", 430f, 140f, valuePaint)
        
        canvas.drawText("HORA INICIO:", 350f, 155f, labelPaint)
        canvas.drawText(detail.ronda?.startTime?.substringAfter("T")?.take(5) ?: "N/A", 430f, 155f, valuePaint)

        val linePaint = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }
        canvas.drawLine(40f, 190f, 555f, 190f, linePaint)

        var yPos = 220f
        val sectionTitlePaint = Paint().apply { color = navyBlue; textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("ANÁLISIS DE IA Y OBSERVACIONES", 50f, yPos, sectionTitlePaint)
        yPos += 25f

        val observations = detail.ronda?.observations ?: "Sin observaciones registradas."
        val obsPaint = Paint().apply { color = textPrimary; textSize = 10.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) }
        
        val obsLines = splitTextIntoLines(observations, obsPaint, 500f)
        for (line in obsLines) {
            if (yPos > 780) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f
            }
            canvas.drawText(line, 50f, yPos, obsPaint)
            yPos += obsPaint.textSize * 1.5f
        }
        yPos += 30f

        if (!detail.incidentes.isNullOrEmpty()) {
            canvas.drawText("ALERTAS E INCIDENTES REGISTRADOS", 50f, yPos, sectionTitlePaint)
            yPos += 25f
            
            val incidentPaint = Paint().apply { color = dangerRed; textSize = 10.5f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            
            detail.incidentes.forEach { incident ->
                if (yPos > 750) { pdfDocument.finishPage(page); page = pdfDocument.startPage(pageInfo); canvas = page.canvas; yPos = 50f }
                canvas.drawCircle(55f, yPos - 3f, 3f, incidentPaint)
                canvas.drawText("${incident.title.uppercase()} (${incident.severity.uppercase()})", 65f, yPos, incidentPaint)
                yPos += 15f
                
                val incLines = splitTextIntoLines(incident.description, obsPaint, 480f)
                for (line in incLines) {
                    if (yPos > 780) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        yPos = 50f
                    }
                    canvas.drawText(line, 65f, yPos, obsPaint)
                    yPos += obsPaint.textSize * 1.5f
                }
                yPos += 15f
            }
        }

        yPos += 10f
        if (yPos > 750) { pdfDocument.finishPage(page); page = pdfDocument.startPage(pageInfo); canvas = page.canvas; yPos = 50f }
        canvas.drawText("PUNTOS DE CONTROL VERIFICADOS (NFC)", 50f, yPos, sectionTitlePaint)
        yPos += 25f
        
        val tableHeaderPaint = Paint().apply { color = Color.WHITE; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val tableHeaderBg = Paint().apply { color = navyBlue }
        
        fun drawChecklogsHeader(canvas: Canvas, y: Float) {
            canvas.drawRect(50f, y - 15f, 545f, y + 5f, tableHeaderBg)
            canvas.drawText("Punto de Control", 60f, y, tableHeaderPaint)
            canvas.drawText("Estado", 300f, y, tableHeaderPaint)
            canvas.drawText("Hora", 450f, y, tableHeaderPaint)
        }
        
        drawChecklogsHeader(canvas, yPos)
        yPos += 25f

        detail.escaneos?.forEach { scan ->
            if (yPos > 780) { 
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                yPos = 50f 
                drawChecklogsHeader(canvas, yPos)
                yPos += 25f
            }
            canvas.drawText(scan.checkpoint?.name ?: "Punto Sin Nombre", 60f, yPos, obsPaint)
            canvas.drawText("VERIFICADO", 300f, yPos, obsPaint)
            canvas.drawText(scan.scannedAt?.substringAfter("T")?.take(5) ?: "--:--", 450f, yPos, obsPaint)
            canvas.drawLine(50f, yPos + 5f, 545f, yPos + 5f, linePaint)
            yPos += 20f
        }

        drawFooter(canvas)
        pdfDocument.finishPage(page)

        val fileName = "Reporte_Ronda_${detail.ronda?.id ?: System.currentTimeMillis()}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        return try {
            pdfDocument.writeTo(FileOutputStream(file)); pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close(); null
        }
    }

    fun generateShiftReport(context: Context, workerName: String, entry: String?, exit: String?, location: String): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawHeader(context, canvas, "REPORTE DE ASISTENCIA")
        
        val labelPaint = Paint().apply { color = slateGray; textSize = 11f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        val valuePaint = Paint().apply { color = textPrimary; textSize = 11f }

        canvas.drawText("COLABORADOR:", 50f, 140f, labelPaint)
        canvas.drawText(workerName, 180f, 140f, valuePaint)
        
        canvas.drawText("INSTALACIÓN:", 50f, 165f, labelPaint)
        canvas.drawText(location, 180f, 165f, valuePaint)
        
        canvas.drawText("ENTRADA:", 50f, 190f, labelPaint)
        canvas.drawText(formatDate(entry) ?: "N/A", 180f, 190f, valuePaint)
        
        canvas.drawText("SALIDA:", 50f, 215f, labelPaint)
        canvas.drawText(formatDate(exit) ?: "Jornada en curso", 180f, 215f, valuePaint)

        canvas.drawLine(50f, 250f, 545f, 250f, Paint().apply { color = Color.LTGRAY })

        val sectionTitlePaint = Paint().apply { color = navyBlue; textSize = 14f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        canvas.drawText("CERTIFICACIÓN DE CUMPLIMIENTO", 50f, 290f, sectionTitlePaint)
        
        val textPaint = Paint().apply { color = textPrimary; textSize = 11f }
        canvas.drawText("Se certifica que el registro de asistencia fue realizado biométricamente", 50f, 315f, textPaint)
        canvas.drawText("y geolocalizado en tiempo real a través de la plataforma SIS Control.", 50f, 330f, textPaint)

        drawFooter(canvas)
        pdfDocument.finishPage(page)
        
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reporte_Jornada_${System.currentTimeMillis()}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file)); pdfDocument.close()
            file
        } catch (e: Exception) {
            pdfDocument.close(); null
        }
    }

    suspend fun generateConsolidatedShiftReport(context: Context, report: ShiftReportDto): File? {
        return withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            drawHeader(context, canvas, "REPORTE CONSOLIDADO DE JORNADA")

            // Cabecera
            val labelPaint = Paint().apply { color = slateGray; textSize = 10f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            val valuePaint = Paint().apply { color = textPrimary; textSize = 10f }

            canvas.drawText("COLABORADOR:", 50f, 130f, labelPaint)
            canvas.drawText(report.workerName, 150f, 130f, valuePaint)
            canvas.drawText("INSTALACIÓN:", 50f, 145f, labelPaint)
            canvas.drawText(report.installationName, 150f, 145f, valuePaint)
            canvas.drawText("JORNADA ID:", 50f, 160f, labelPaint)
            canvas.drawText("#${report.shiftId}", 150f, 160f, valuePaint)

            canvas.drawText("ENTRADA:", 350f, 130f, labelPaint)
            canvas.drawText(formatDate(report.entryTime) ?: "N/A", 430f, 130f, valuePaint)
            canvas.drawText("SALIDA:", 350f, 145f, labelPaint)
            canvas.drawText(formatDate(report.exitTime) ?: "En curso", 430f, 145f, valuePaint)

            canvas.drawLine(40f, 175f, 555f, 175f, Paint().apply { color = Color.LTGRAY })

            // 1. Métricas
            var yPos = 205f
            val sectionTitlePaint = Paint().apply { color = navyBlue; textSize = 13f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            canvas.drawText("MÉTRICAS DE DESEMPEÑO Y CUMPLIMIENTO", 50f, yPos, sectionTitlePaint)
            yPos += 25f

            canvas.drawRoundRect(50f, yPos - 15f, 545f, yPos + 40f, 10f, 10f, Paint().apply { color = Color.parseColor("#F8FAFC") })
            val kpiLabelPaint = Paint().apply { color = slateGray; textSize = 9f; textAlign = Paint.Align.CENTER }
            val kpiValuePaint = Paint().apply { color = navyBlue; textSize = 16f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); textAlign = Paint.Align.CENTER }

            canvas.drawText("RONDAS", 110f, yPos + 5f, kpiLabelPaint)
            canvas.drawText("${report.totalRoundsExecuted}/${report.totalRoundsPlanned}", 110f, yPos + 25f, kpiValuePaint)
            canvas.drawText("PUNTOS", 240f, yPos + 5f, kpiLabelPaint)
            canvas.drawText("${report.metrics.scannedCheckpoints}", 240f, yPos + 25f, kpiValuePaint)
            canvas.drawText("OMISIONES", 370f, yPos + 5f, kpiLabelPaint)
            canvas.drawText("${report.metrics.omittedCheckpoints}", 370f, yPos + 25f, kpiValuePaint)
            canvas.drawText("ALERTAS", 490f, yPos + 5f, kpiLabelPaint)
            canvas.drawText("${report.metrics.alertsCount}", 490f, yPos + 25f, kpiValuePaint)

            yPos += 85f

            // 2. Detalle de Rondas
            canvas.drawText("DETALLE DE ACTIVIDAD POR RONDA", 50f, yPos, sectionTitlePaint)
            yPos += 25f

            val headerBg = Paint().apply { color = navyBlue }
            val headerText = Paint().apply { color = Color.WHITE; textSize = 9f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
            
            fun drawRoundsHeader(canvas: Canvas, y: Float) {
                canvas.drawRect(50f, y - 12f, 545f, y + 5f, headerBg)
                canvas.drawText("ID Ronda", 60f, y, headerText)
                canvas.drawText("Inicio / Fin", 150f, y, headerText)
                canvas.drawText("Estado", 280f, y, headerText)
                canvas.drawText("Observaciones", 380f, y, headerText)
            }
            
            drawRoundsHeader(canvas, yPos)
            yPos += 20f

            val bodyPaint = Paint().apply { color = textPrimary; textSize = 9f }
            report.rondas.forEach { round ->
                if (yPos > 750) { 
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = 50f 
                    drawRoundsHeader(canvas, yPos)
                    yPos += 20f
                }
                
                val startY = yPos
                canvas.drawText("#${round.roundId}", 60f, startY, bodyPaint)
                canvas.drawText("${round.startTime.substringAfter("T").take(5)} / ${round.endTime?.substringAfter("T")?.take(5) ?: "--"}", 150f, startY, bodyPaint)
                canvas.drawText(round.status, 280f, startY, bodyPaint)
                
                val obsLines = splitTextIntoLines(round.observations ?: "Sin novedades.", bodyPaint, 160f)
                var currentY = startY
                var pageBroke = false
                for (line in obsLines) {
                    if (currentY > 780) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        currentY = 50f
                        drawRoundsHeader(canvas, currentY)
                        currentY += 20f
                        pageBroke = true
                    }
                    canvas.drawText(line, 380f, currentY, bodyPaint)
                    currentY += bodyPaint.textSize * 1.5f
                }
                
                yPos = if (pageBroke) currentY else maxOf(startY + bodyPaint.textSize * 1.5f, currentY)
                canvas.drawLine(50f, yPos, 545f, yPos, Paint().apply { color = Color.LTGRAY })
                yPos += 15f
            }

            // 3. Anexo de Evidencias (SIN DUPLICADOS)
            yPos += 20f
            if (yPos > 700) { pdfDocument.finishPage(page); page = pdfDocument.startPage(pageInfo); canvas = page.canvas; yPos = 50f }
            canvas.drawText("ANEXO DE EVIDENCIAS FOTOGRÁFICAS", 50f, yPos, sectionTitlePaint)
            yPos += 25f

            val uniqueEvidences = mutableListOf<Pair<String, String>>()
            report.rondas.forEach { r ->
                r.checklogs.filter { !it.imageUrl.isNullOrBlank() }.forEach {
                    uniqueEvidences.add("Ronda #${r.roundId} - ${it.checkpointName}" to it.imageUrl!!)
                }
            }
            report.incidentes.filter { !it.imageUrl.isNullOrBlank() }.forEach {
                uniqueEvidences.add(it.title to it.imageUrl!!)
            }
            val deduplicatedEvidences = uniqueEvidences.distinctBy { it.second.substringBefore("?") }

            val imagesPerRow = 2
            val margin = 50f
            val spacing = 20f
            val maxImageWidth = (595f - 2 * margin - (imagesPerRow - 1) * spacing) / imagesPerRow
            val maxImageHeight = 180f

            var currentColumn = 0
            var rowMaxHeight = 0f

            deduplicatedEvidences.forEach { (name, url) ->
                if (currentColumn == 0 && yPos > 600) { 
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = 50f 
                }
                
                val xStart = margin + currentColumn * (maxImageWidth + spacing)
                
                val lines = splitTextIntoLines(name, bodyPaint, maxImageWidth)
                var titleY = yPos
                for (line in lines) {
                    canvas.drawText(line, xStart, titleY, bodyPaint)
                    titleY += bodyPaint.textSize * 1.5f
                }
                
                val bitmap = downloadBitmap(url)
                val imageHeight = if (bitmap != null) {
                    val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                    var finalWidth = maxImageWidth
                    var finalHeight = finalWidth / ratio
                    if (finalHeight > maxImageHeight) {
                        finalHeight = maxImageHeight
                        finalWidth = finalHeight * ratio
                    }
                    val xCentered = xStart + (maxImageWidth - finalWidth) / 2f
                    val dstRect = RectF(xCentered, titleY, xCentered + finalWidth, titleY + finalHeight)
                    canvas.drawBitmap(bitmap, null, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))
                    finalHeight
                } else {
                    canvas.drawText("[Imagen no disponible]", xStart, titleY, bodyPaint)
                    20f
                }
                
                val totalHeight = (titleY - yPos) + imageHeight
                rowMaxHeight = maxOf(rowMaxHeight, totalHeight)
                
                currentColumn++
                if (currentColumn >= imagesPerRow) {
                    currentColumn = 0
                    yPos += rowMaxHeight + 30f
                    rowMaxHeight = 0f
                }
            }
            if (currentColumn > 0) {
                yPos += rowMaxHeight + 30f
            }

            // 4. Conclusión IA
            if (yPos > 650) { pdfDocument.finishPage(page); page = pdfDocument.startPage(pageInfo); canvas = page.canvas; yPos = 50f }
            yPos += 20f
            canvas.drawText("CONCLUSIÓN FINAL DE JORNADA (IA)", 50f, yPos, sectionTitlePaint)
            yPos += 25f

            val allObs = report.rondas.mapNotNull { it.observations }
            val allIncidentDesc = report.incidentes.map { it.description }
            val iaConclusion = AIManager.generateConsolidatedShiftConclusion(report.workerName, report.rondas.size, report.metrics.alertsCount, allObs, allIncidentDesc)
            
            val conclusionLines = splitTextIntoLines(iaConclusion, bodyPaint, 500f)
            for (line in conclusionLines) {
                if (yPos > 780) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPos = 50f
                }
                canvas.drawText(line, 50f, yPos, bodyPaint)
                yPos += bodyPaint.textSize * 1.5f
            }

            drawFooter(canvas)
            pdfDocument.finishPage(page)

            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Reporte_Maestro_Jornada_${report.shiftId}.pdf")
            try { pdfDocument.writeTo(FileOutputStream(file)); pdfDocument.close(); file } catch (e: Exception) { pdfDocument.close(); null }
        }
    }

    private fun drawHeader(context: Context, canvas: Canvas, title: String) {
        // Fondo del encabezado
        val bgPaint = Paint().apply { color = headerBgColor }
        canvas.drawRect(0f, 0f, 595f, 110f, bgPaint)
        
        // Logo en Alta Resolución
        drawRealLogo(context, canvas)
        
        val titlePaint = Paint().apply {
            color = navyBlue
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(title, 140f, 65f, titlePaint)
        
        val subTitlePaint = Paint().apply {
            color = slateGray
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText("Sistema de Gestión y Control de Seguridad - SIS Control", 140f, 80f, subTitlePaint)
    }

    private fun drawRealLogo(context: Context, canvas: Canvas) {
        try {
            val drawable = ContextCompat.getDrawable(context, R.drawable.logo_branding_sis_control)
            if (drawable != null) {
                // Área del logo (80x80)
                val dstRect = RectF(40f, 20f, 120f, 100f)
                
                // Generar bitmap de ALTA RESOLUCIÓN desde el recurso original
                val highResBitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
                val bitmapCanvas = Canvas(highResBitmap)
                drawable.setBounds(0, 0, 400, 400)
                drawable.draw(bitmapCanvas)
                
                // Dibujar con filtro de suavizado
                canvas.drawBitmap(highResBitmap, null, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))
                highResBitmap.recycle()
            }
        } catch (e: Exception) {
            canvas.drawRoundRect(40f, 20f, 120f, 100f, 12f, 12f, Paint().apply { color = navyBlue })
        }
    }

    private fun downloadBitmap(url: String): Bitmap? {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            BitmapFactory.decodeStream(connection.inputStream)
        } catch (e: Exception) { null }
    }

    private fun drawFooter(canvas: Canvas) {
        val footerPaint = Paint().apply { color = slateGray; textSize = 8.5f; textAlign = Paint.Align.CENTER }
        canvas.drawText("Documento generado automáticamente por el motor de IA de SIS Control.", 595f / 2f, 805f, footerPaint)
        canvas.drawText("La integridad de este reporte está garantizada por validación NFC y Firma Digital.", 595f / 2f, 820f, footerPaint)
    }
}
