package com.example.digitalwhiteboardapp.presentation.viewer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import android.util.Log

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WhiteboardViewerScreen(
    viewModel: WhiteboardViewerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Estado para zoom y pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.5f, 5f)
        offsetX += panChange.x
        offsetY += panChange.y
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main whiteboard canvas con zoom y scroll
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformableState)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            ) {
                // Draw all shapes from Firebase
                uiState.shapes.forEach { shape ->
                    shape.draw(this)
                }
            }
        }

        // Status indicators
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Connection status
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = SurfaceDefaults.colors(
                    containerColor = if (uiState.isConnected) Color.Green else Color.Red
                )
            ) {
                Text(
                    text = if (uiState.isConnected) "Conectado" else "Desconectado",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Shape count
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = SurfaceDefaults.colors(containerColor = Color.Blue)
            ) {
                Text(
                    text = "${uiState.shapes.size} elementos",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Zoom indicator
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                colors = SurfaceDefaults.colors(containerColor = Color.Gray)
            ) {
                Text(
                    text = "${(scale * 100).toInt()}%",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "⏳",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Conectando con la pizarra...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Error message
        uiState.errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    onClick = { },
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.colors(containerColor = Color.Red)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error de Conexión",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = error,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.retry() },
                            colors = ButtonDefaults.colors(containerColor = Color.White)
                        ) {
                            Text(
                                text = "Reintentar",
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }

        // Empty state
        if (!uiState.isLoading && uiState.shapes.isEmpty() && uiState.errorMessage == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📱 ➡️ 📺",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "Pizarra Digital TV",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.Black
                    )
                    Text(
                        text = "Esperando dibujos desde el móvil...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Los dibujos aparecerán aquí en tiempo real",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "🔍 Usa gestos para hacer zoom y mover la vista",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Control instructions overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                colors = SurfaceDefaults.colors(
                    containerColor = Color.Black.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Controles:",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Pellizcar: Zoom",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Arrastrar: Mover vista",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "• Zoom: 50% - 500%",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
