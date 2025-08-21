package com.example.digitalwhiteboardapp.presentation.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import com.example.digitalwhiteboardapp.presentation.drawing.components.DrawingBottomBar
import com.example.digitalwhiteboardapp.presentation.drawing.components.ErrorDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingScreen(
    viewModel: DrawingViewModel,
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
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                viewModel.onStartDrawing(offset)
                                viewModel.onEndDrawing()
                            }
                        )
                    }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
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
                    // Draw all saved shapes
                    uiState.shapes.forEach { shape ->
                        shape.draw(this)
                    }

                    // Draw current shape being drawn
                    uiState.currentShape?.let { shape ->
                        shape.draw(this)
                    }
                }
            }
        }
    }
}