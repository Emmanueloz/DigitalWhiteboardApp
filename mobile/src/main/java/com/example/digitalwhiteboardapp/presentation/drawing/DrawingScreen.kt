package com.example.digitalwhiteboardapp.presentation.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.digitalwhiteboardapp.data.model.Circle
import com.example.digitalwhiteboardapp.data.model.FreePath
import com.example.digitalwhiteboardapp.data.model.Line
import com.example.digitalwhiteboardapp.data.model.Rectangle
import com.example.digitalwhiteboardapp.presentation.drawing.components.DrawingBottomBar
import com.example.digitalwhiteboardapp.presentation.drawing.components.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    viewModel: DrawingViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Show error dialog if there's an error
    if (!uiState.errorMessage.isNullOrEmpty()) {
        ErrorDialog(
            message = uiState.errorMessage!!,
            onDismiss = { viewModel.clearError() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Digital Whiteboard") },
                actions = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            DrawingBottomBar(
                selectedTool = uiState.selectedTool,
                selectedColor = uiState.selectedColor,
                isFilled = uiState.isFilled,
                onToolSelected = { viewModel.selectTool(it) },
                onColorSelected = { viewModel.setColor(it) },
                onToggleFill = { viewModel.toggleFill() },
                onUndo = { viewModel.onUndo() },
                onClear = { viewModel.onClear() },
                onStrokeWidthChange = { viewModel.setStrokeWidth(it) },
                currentStrokeWidth = uiState.strokeWidth
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            viewModel.onStartDrawing(offset)
                            viewModel.onEndDrawing()
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            viewModel.onStartDrawing(offset)
                        },
                        onDrag = { change, _ ->
                            viewModel.onDraw(change.position)
                        },
                        onDragEnd = {
                            viewModel.onEndDrawing()
                        }
                    )
                }
        ) {
            // Draw grid background
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Draw grid lines
                val gridSize = 20f
                for (x in 0..(size.width / gridSize).toInt()) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(x * gridSize, 0f),
                        end = Offset(x * gridSize, size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(size.height / gridSize).toInt()) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, y * gridSize),
                        end = Offset(size.width, y * gridSize),
                        strokeWidth = 1f
                    )
                }
            }

            // Draw all saved shapes
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                uiState.shapes.forEach { shape ->
                    shape.draw(this)
                }

                // Draw the current shape being drawn
                uiState.currentShape?.let { shape ->
                    shape.draw(this)
                }
            }
        }
    }
}