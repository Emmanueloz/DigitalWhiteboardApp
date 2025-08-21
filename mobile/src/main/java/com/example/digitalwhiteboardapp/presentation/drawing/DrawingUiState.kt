package com.example.digitalwhiteboardapp.presentation.drawing

import androidx.compose.ui.graphics.Color
import com.example.shared.model.DrawingShape
import com.example.shared.model.ShapeType

/**
 * Represents the UI state for the drawing screen.
 */
/**
 * Represents the UI state for the drawing screen.
 * @property shapes List of all shapes in the drawing
 * @property currentShape The shape currently being drawn (if any)
 * @property selectedTool The currently selected drawing tool
 * @property selectedColor The currently selected color
 * @property strokeWidth The current stroke width
 * @property isFilled Whether shapes should be filled
 * @property isLoading Whether the app is currently loading data
 * @property errorMessage Current error message (if any)
 * @property showExportDialog Whether to show the export dialog
 * @property exportSvg The SVG string to export (if any)
 * @property selectedShapeIndex Index of the currently selected shape (-1 if none)
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
    val selectedShapeIndex: Int = -1
)
