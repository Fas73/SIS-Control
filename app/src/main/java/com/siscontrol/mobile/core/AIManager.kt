package com.siscontrol.mobile.core

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await

object AIManager {

    /**
     * Analiza una imagen y devuelve una lista de etiquetas detectadas por la IA.
     */
    suspend fun analyzeImage(context: Context, imageUri: Uri): List<String> {
        return try {
            val options = ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.5f) // Bajamos un poco el umbral para detectar más elementos
                .build()
            
            val labeler = ImageLabeling.getClient(options)
            val image = InputImage.fromFilePath(context, imageUri)
            
            val labels = labeler.process(image).await()
            // Filtramos etiquetas genéricas o poco útiles
            labels.filter { it.text.lowercase() !in listOf("hair", "smile", "joint", "hand", "finger") }
                  .map { it.text }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Valida si una imagen contiene un rostro humano claro (para selfies de registro).
     * Retorna un Pair(esValido: Boolean, mensajeError: String?)
     */
    suspend fun validateSelfie(context: Context, imageUri: Uri): Pair<Boolean, String?> {
        return try {
            val options = FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()

            val detector = FaceDetection.getClient(options)
            val image = InputImage.fromFilePath(context, imageUri)
            
            val faces = detector.process(image).await()

            when {
                faces.isEmpty() -> Pair(false, "No se detectó ningún rostro. Por favor, tómate una selfie clara.")
                faces.size > 1 -> Pair(false, "Se detectó más de un rostro. La foto debe ser individual.")
                else -> {
                    val face = faces.first()
                    // Verificar que el rostro sea lo suficientemente grande en la imagen
                    val boundingBox = face.boundingBox
                    if (boundingBox.width() < 100 || boundingBox.height() < 100) {
                        Pair(false, "El rostro está muy lejos. Acércate más a la cámara.")
                    } else {
                        Pair(true, null)
                    }
                }
            }
        } catch (e: Exception) {
            Pair(false, "Error al procesar la imagen: ${e.message}")
        }
    }

    /**
     * Genera una descripción profesional basada en la entrada del guardia, las etiquetas de la IA
     * y la existencia de evidencia fotográfica.
     */
    fun generateProfessionalDescription(
        userInput: String, 
        labels: List<String>, 
        hasPhoto: Boolean
    ): String {
        val sb = StringBuilder()
        
        // 1. Introducción formal
        if (userInput.isNotBlank()) {
            sb.append("Reporte de Auditoría: ${userInput.trim().replaceFirstChar { it.uppercase() }}. ")
        } else {
            sb.append("Se registra hallazgo durante la ronda de vigilancia. ")
        }

        // 2. Integración de visión artificial (Labels)
        if (labels.isNotEmpty()) {
            val translatedLabels = labels.take(3).map { translateLabel(it) }
            sb.append("El análisis visual automático identifica: ${translatedLabels.joinToString(", ")}. ")
        }

        // 3. Validación de evidencia
        if (hasPhoto) {
            sb.append("Se adjunta registro fotográfico como evidencia digital inalterable para respaldo de la jefatura. ")
            
            // Sugerencia contextual basada en labels + foto
            if (labels.any { it.contains("Fire", true) || it.contains("Smoke", true) }) {
                sb.append("La fotografía confirma situación de riesgo térmico/amago en el perímetro. ")
            } else if (labels.any { it.contains("Door", true) || it.contains("Gate", true) || it.contains("Lock", true) }) {
                sb.append("La imagen documenta el estado actual de los accesos revisados. ")
            }
        } else {
            sb.append("Nota: Reporte realizado sin captura de imagen (aviso verbal/presencial). ")
        }

        // 4. Cierre profesional
        sb.append("\n\nSugerencia IA: Se recomienda verificar protocolo de seguridad estándar.")

        return sb.toString()
    }

    /**
     * Realiza un análisis de riesgo profesional para la administración basado en el texto del reporte.
     */
    fun performRiskAnalysis(incidentText: String): String {
        val text = incidentText.lowercase()
        val sb = StringBuilder()
        
        sb.append("--- ANÁLISIS DE RIESGO IA ---\n")
        
        // 1. Evaluación de Nivel
        val riskLevel = when {
            text.contains("fuego") || text.contains("incendio") || text.contains("robo") || text.contains("pánico") -> "CRÍTICO"
            text.contains("forzado") || text.contains("daño") || text.contains("nfc") || text.contains("sospechoso") -> "ALTO"
            text.contains("mantenimiento") || text.contains("luz") || text.contains("agua") -> "MEDIO"
            else -> "BAJO / INFORMATIVO"
        }
        sb.append("Nivel de Amenaza: $riskLevel\n")

        // 2. Recomendación Estratégica
        sb.append("Recomendación: ")
        when (riskLevel) {
            "CRÍTICO" -> sb.append("Activar protocolo de emergencia inmediata, dar aviso a autoridades y evacuar si es necesario.")
            "ALTO" -> sb.append("Reforzar vigilancia en el perímetro afectado, revisar cámaras de seguridad y documentar accesos.")
            "MEDIO" -> sb.append("Programar visita técnica para reparación preventiva o revisión de infraestructura.")
            else -> sb.append("Continuar con monitoreo estándar de rutina y reporte en bitácora.")
        }

        // 3. Nota de Probabilidad
        if (text.contains("nfc") || text.contains("no escaneado")) {
            sb.append("\nAlerta de Integridad: El incumplimiento de punto NFC sugiere posible falla en la ronda o vulnerabilidad en el tag.")
        }

        return sb.toString()
    }

    /**
     * Genera un informe de "Inteligencia de Ronda" evaluando el desempeño del guardia.
     * Analiza tiempos, cobertura de puntos y calidad de reportes.
     */
    fun analyzeRoundPerformance(
        totalCheckpoints: Int,
        scannedCheckpoints: Int,
        incidentsCount: Int,
        durationMinutes: Long,
        observations: String
    ): String {
        val coverage = if (totalCheckpoints > 0) (scannedCheckpoints.toFloat() / totalCheckpoints * 100).toInt() else 0
        val sb = StringBuilder()

        sb.append("--- INTELIGENCIA DE RONDA (IA) ---\n\n")

        // 1. Evaluación de Cobertura
        sb.append("COBERTURA: $coverage%\n")
        when {
            coverage == 100 -> sb.append("Resultado: Excelente. Se cumplió con el 100% del recorrido establecido.\n")
            coverage >= 80 -> sb.append("Resultado: Satisfactorio. La mayoría de los puntos críticos fueron cubiertos.\n")
            else -> sb.append("Resultado: Alerta. Cobertura insuficiente. Se recomienda revisar causas de omisión.\n")
        }

        // 2. Análisis de Tiempo y Ritmo
        sb.append("\nEFICIENCIA TEMPORAL:\n")
        val expectedTimePerPoint = 3 // minutos asumiendo un estándar
        val idealDuration = scannedCheckpoints * expectedTimePerPoint
        
        when {
            durationMinutes < idealDuration / 2 -> sb.append("- Ritmo: Demasiado rápido. Riesgo de inspección superficial detectado.\n")
            durationMinutes > idealDuration * 2 -> sb.append("- Ritmo: Lento. Verificar posibles distracciones o incidentes no reportados.\n")
            else -> sb.append("- Ritmo: Óptimo. Desplazamiento adecuado entre puntos de control.\n")
        }

        // 3. Proactividad en Hallazgos
        sb.append("\nDETECCIÓN DE INCIDENTES:\n")
        if (incidentsCount > 0) {
            sb.append("- Se registraron $incidentsCount novedades. El guardia muestra alta atención al detalle.\n")
        } else {
            sb.append("- Sin novedades reportadas. Ronda informativa estándar.\n")
        }

        // 4. Calidad del Reporte Técnico
        sb.append("\nCALIDAD DE DOCUMENTACIÓN:\n")
        when {
            observations.length > 50 -> sb.append("- Redacción: Detallada y profesional. Facilita la auditoría administrativa.\n")
            observations.length > 10 -> sb.append("- Redacción: Concisa. Cumple con los requisitos mínimos de reporte.\n")
            else -> sb.append("- Redacción: Insuficiente. Se sugiere mayor detalle en las observaciones finales.\n")
        }

        // 5. Puntaje de Desempeño IA
        val score = (coverage * 0.6 + (if (incidentsCount > 0) 20 else 10) + (if (observations.length > 30) 20 else 10)).coerceAtMost(100.0)
        sb.append("\nÍNDICE DE DESEMPEÑO: ${score.toInt()}/100\n")
        
        sb.append("\nCONCLUSIÓN IA: ")
        if (score >= 90) sb.append("Operación de alta confianza. Recomendado para roles de supervisión.")
        else if (score >= 70) sb.append("Operación estándar cumplida. Mantener monitoreo.")
        else sb.append("Se requiere retroalimentación y posible re-capacitación en protocolos.")

        return sb.toString()
    }

    /**
     * Genera un párrafo de conclusión final para toda la jornada consolidada.
     * Analiza todas las observaciones e incidentes del turno.
     */
    fun generateConsolidatedShiftConclusion(
        workerName: String,
        roundsCount: Int,
        totalIncidents: Int,
        allObservations: List<String>,
        incidentDescriptions: List<String>
    ): String {
        val sb = StringBuilder()
        sb.append("Resumen Ejecutivo de Jornada - Auditoría IA\n\n")
        
        sb.append("Durante la jornada laboral, el colaborador $workerName realizó un total de $roundsCount rondas de vigilancia. ")
        
        if (totalIncidents > 0) {
            sb.append("Se detectaron $totalIncidents eventos de alerta que requieren atención administrativa. ")
        } else {
            sb.append("La jornada transcurrió sin incidentes críticos reportados, manteniendo un estándar de seguridad óptimo. ")
        }

        // Análisis cualitativo simple (Mock de lo que haría Gemini con el prompt)
        val consolidatedText = (allObservations + incidentDescriptions).joinToString(" ").lowercase()
        
        when {
            consolidatedText.contains("nfc") || consolidatedText.contains("omitido") -> {
                sb.append("Se identifica un patrón de omisión de puntos de control, lo que sugiere posibles vulnerabilidades técnicas en los Tags o bloqueos físicos en el perímetro. ")
            }
            consolidatedText.contains("puerta") || consolidatedText.contains("acceso") -> {
                sb.append("La actividad se centró en la verificación de accesos y puntos de entrada, documentando novedades en la infraestructura. ")
            }
        }

        sb.append("\n\nConclusión Final: Operación validada bajo protocolos SIS Control. ")
        if (totalIncidents > 2) {
            sb.append("Se recomienda revisión de bitácora y reforzamiento de perímetros críticos.")
        } else {
            sb.append("Continuar con monitoreo de rutina.")
        }

        return sb.toString()
    }

    /**
     * Sugiere un título profesional basado en las etiquetas detectadas en español.
     */
    fun suggestTitle(labels: List<String>): String {
        if (labels.isEmpty()) return "Incidente reportado"
        
        // Mapeo para traducir y profesionalizar las etiquetas detectadas por ML Kit
        return when {
            labels.any { it.contains("Fire", true) || it.contains("Smoke", true) || it.contains("Flame", true) } -> "Alerta de Incendio/Humo"
            labels.any { it.contains("Car", true) || it.contains("Vehicle", true) || it.contains("Truck", true) } -> "Novedad con Vehículo"
            labels.any { it.contains("Tool", true) || it.contains("Hammer", true) || it.contains("Screwdriver", true) } -> "Mantenimiento / Herramientas"
            labels.any { it.contains("Person", true) || it.contains("Man", true) || it.contains("Woman", true) || it.contains("Human", true) || it.contains("Face", true) } -> "Presencia de Personal / Individuo"
            labels.any { it.contains("Door", true) || it.contains("Gate", true) || it.contains("Entrance", true) } -> "Novedad en Puerta / Acceso"
            labels.any { it.contains("Water", true) || it.contains("Flood", true) || it.contains("Leak", true) } -> "Fuga de Agua / Inundación"
            labels.any { it.contains("Dog", true) || it.contains("Cat", true) || it.contains("Animal", true) } -> "Presencia de Animales"
            labels.any { it.contains("Lock", true) || it.contains("Security", true) || it.contains("Chain", true) } -> "Incidente de Seguridad / Candados"
            labels.any { it.contains("Glass", true) || it.contains("Window", true) } -> "Rotura de Vidrios / Ventanas"
            labels.any { it.contains("Laptop", true) || it.contains("Computer", true) || it.contains("Electronic", true) } -> "Equipamiento Electrónico"
            labels.any { it.contains("Musical", true) || it.contains("Instrument", true) } -> "Novedad en Área Común / Mobiliario"
            else -> {
                val translated = translateLabel(labels.first())
                "Hallazgo detectado: $translated"
            }
        }
    }

    /**
     * Traductor robusto de etiquetas comunes de ML Kit.
     */
    private fun translateLabel(label: String): String {
        val dictionary = mapOf(
            "building" to "Edificio",
            "wall" to "Pared",
            "window" to "Ventana",
            "tree" to "Árbol",
            "grass" to "Césped",
            "road" to "Camino / Calle",
            "sky" to "Cielo",
            "light" to "Luz / Iluminación",
            "box" to "Caja / Paquete",
            "electronic" to "Equipo Electrónico",
            "furniture" to "Mobiliario",
            "laptop" to "Computador",
            "phone" to "Teléfono",
            "clothing" to "Ropa / Vestimenta",
            "human" to "Persona",
            "man" to "Hombre",
            "woman" to "Mujer",
            "face" to "Rostro",
            "eye" to "Ojo",
            "hair" to "Cabello",
            "outdoor" to "Exterior",
            "indoor" to "Interior",
            "office" to "Oficina",
            "room" to "Habitación",
            "floor" to "Piso / Suelo",
            "ceiling" to "Techo",
            "door" to "Puerta",
            "car" to "Vehículo",
            "truck" to "Camión",
            "bicycle" to "Bicicleta",
            "wheel" to "Rueda",
            "plant" to "Planta",
            "flower" to "Flor",
            "table" to "Mesa",
            "chair" to "Silla",
            "computer" to "Computador",
            "screen" to "Pantalla",
            "keyboard" to "Teclado",
            "bag" to "Bolso / Mochila",
            "shoes" to "Zapatos",
            "watch" to "Reloj",
            "water" to "Agua",
            "fire" to "Fuego",
            "smoke" to "Humo",
            "tool" to "Herramienta",
            "poster" to "Cartel / Poster",
            "flesh" to "Piel / Persona",
            "musical instrument" to "Instrumento Musical",
            "instrument" to "Instrumento",
            "food" to "Alimento / Comida",
            "eating" to "Comiendo"
        )
        
        return dictionary[label.lowercase()] ?: label
    }
}
