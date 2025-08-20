package com.example.digitalwhiteboardapp.presentation.drawing

import androidx.compose.ui.graphics.Color
import com.example.digitalwhiteboardapp.data.model.DrawingShape
import com.example.digitalwhiteboardapp.data.model.ShapeType

/**
 * Represents the UI state for the drawing screen.
 */
data class DrawingUiState(
    val shapes: List<DrawingShape> = emptyList(),
    val currentShape: DrawingShape? = null,
    val selectedTool: ShapeType = ShapeType.RECTANGLE,
    val selectedColor: Color = Color.Black,
    val strokeWidth: Float = 5f,
    val isFilled: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showExportDialog: Boolean = false,
    val exportSvg: String? = null,
    val isErasing: Boolean = false,
    val selectedShapeIndex: Int = -1
)
