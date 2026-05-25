package com.siscontrol.mobile.presentation.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.siscontrol.mobile.core.FirebaseStorageManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileImageCropperDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Estados de transformación
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(imageUri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val original = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (original != null) {
                    // Corregir rotación EXIF antes de mostrar en el editor
                    bitmap = FirebaseStorageManager.rotateImageIfRequired(context, original, imageUri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Recortar Foto", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                    },
                    actions = {
                        if (bitmap != null) {
                            IconButton(onClick = {
                                // Lógica de recorte real
                                try {
                                    val cropped = createCroppedBitmap(bitmap!!, scale, offset)
                                    onConfirm(cropped)
                                } catch (e: Exception) {
                                    onConfirm(bitmap!!) // Fallback al original si falla el recorte
                                }
                            }) {
                                Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    offset += pan
                                }
                            }
                    ) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }

                    // Superposición de círculo con estrategia Offscreen para evitar el "hueco" transparente
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    ) {
                        val circleRadius = size.minDimension / 2.5f
                        val center = Offset(size.width / 2, size.height / 2)
                        
                        // 1. Dibujar el fondo oscuro semi-transparente
                        drawRect(color = Color.Black.copy(alpha = 0.7f))
                        
                        // 2. "Limpiar" el círculo (esto ahora solo afecta al rectángulo negro de arriba)
                        drawCircle(
                            color = Color.Transparent,
                            radius = circleRadius,
                            center = center,
                            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                        )
                        
                        // 3. Dibujar el borde blanco del círculo
                        drawCircle(
                            color = Color.White,
                            radius = circleRadius,
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                    }
                } else {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

/**
 * Función técnica para recortar el Bitmap basándose en las transformaciones de la UI.
 */
private fun createCroppedBitmap(original: Bitmap, scale: Float, offset: Offset): Bitmap {
    val size = Math.min(original.width, original.height)
    val cropSize = (size / scale).toInt().coerceIn(100, size)
    
    // Centramos el recorte considerando el offset
    var x = ((original.width - cropSize) / 2 - (offset.x / scale)).toInt()
    var y = ((original.height - cropSize) / 2 - (offset.y / scale)).toInt()
    
    // Validar límites
    x = x.coerceIn(0, Math.max(0, original.width - cropSize))
    y = y.coerceIn(0, Math.max(0, original.height - cropSize))
    
    return Bitmap.createBitmap(original, x, y, cropSize, cropSize)
}
