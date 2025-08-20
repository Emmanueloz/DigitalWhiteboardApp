package com.example.digitalwhiteboardapp.presentation.drawing
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitalwhiteboardapp.data.model.Circle
import com.example.digitalwhiteboardapp.data.model.FreePath
import com.example.digitalwhiteboardapp.data.model.Line
import com.example.digitalwhiteboardapp.data.model.Rectangle
import com.example.digitalwhiteboardapp.data.model.ShapeType
import com.example.digitalwhiteboardapp.data.repository.DrawingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the drawing screen.
 * Manages the drawing state and handles user interactions.
 */
class DrawingViewModel(
    private val repository: DrawingRepository
) : ViewModel() {

    private var _uiState = MutableStateFlow(DrawingUiState())
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    init {
        loadDrawing()
    }

    private fun loadDrawing() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                // Load the drawing
                repository.observeShapes()
                    .collect { shapes ->
                        _uiState.value = _uiState.value.copy(
                            shapes = shapes,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load drawing: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun onStartDrawing(offset: Offset) {
        val currentState = _uiState.value
        // Handle eraser
        if (currentState.isErasing) {
            val shapesToRemove = mutableListOf<Int>()
            currentState.shapes.forEachIndexed { index, shape ->
                if (isPointOnShape(offset, shape, currentState.strokeWidth)) {
                    shapesToRemove.add(index)
                }
            }
            
            if (shapesToRemove.isNotEmpty()) {
                val updatedShapes = currentState.shapes.toMutableList()
                shapesToRemove.sortedDescending().forEach { index ->
                    updatedShapes.removeAt(index)
                }
                _uiState.value = currentState.copy(
                    shapes = updatedShapes,
                )
            }
        } else {
            // Start a new shape
            val newShape = when (currentState.selectedTool) {
                ShapeType.LINE -> Line(
                    start = offset,
                    end = offset,
                    color = currentState.selectedColor,
                    strokeWidth = currentState.strokeWidth,
                    isFilled = currentState.isFilled
                )
                ShapeType.RECTANGLE -> Rectangle(
                    topLeft = offset,
                    bottomRight = offset,
                    color = currentState.selectedColor,
                    strokeWidth = currentState.strokeWidth,
                    isFilled = currentState.isFilled
                )
                ShapeType.CIRCLE -> Circle(
                    center = offset,
                    radius = 0f,
                    color = currentState.selectedColor,
                    strokeWidth = currentState.strokeWidth,
                    isFilled = currentState.isFilled
                )
                ShapeType.FREE_PATH -> FreePath(
                    points = listOf(offset),
                    color = currentState.selectedColor,
                    strokeWidth = currentState.strokeWidth,
                    isFilled = currentState.isFilled
                )
                else -> return
            }
            _uiState.value = currentState.copy(
                currentShape = newShape
            )
        }
    }
    
    fun onDraw(offset: Offset) {
        val currentState = _uiState.value
        currentState.currentShape?.let { currentShape ->
            when (currentShape) {
                is Line -> {
                    _uiState.value = currentState.copy(
                        currentShape = currentShape.copy(
                            end = offset
                        )
                    )
                }
                is Rectangle -> {
                    _uiState.value = currentState.copy(
                        currentShape = currentShape.copy(
                            bottomRight = offset
                        )
                    )
                }
                is Circle -> {
                    val center = currentShape.center
                    val radius = (offset - center).getDistance()
                    _uiState.value = currentState.copy(
                        currentShape = currentShape.copy(
                            radius = radius
                        )
                    )
                }
                is FreePath -> {
                    val newPoints = currentShape.points.toMutableList().apply {
                        add(offset)
                    }
                    _uiState.value = currentState.copy(
                        currentShape = currentShape.copy(
                            points = newPoints
                        )
                    )
                }
            }
        }
    }
    
    fun onEndDrawing() {
        val currentState = _uiState.value
        currentState.currentShape?.let { currentShape ->
            val updatedShapes = currentState.shapes.toMutableList().apply {
                add(currentShape)
            }
            _uiState.value = currentState.copy(
                shapes = updatedShapes,
                currentShape = null,
                isErasing = false,
                selectedShapeIndex = -1
            )
            saveCurrentState()
        } ?: run {
            _uiState.value = currentState.copy(
                isErasing = false,
                selectedShapeIndex = -1
            )
        }
    }
    
    fun onUndo() {
        _uiState.value.let { currentState ->
            if (currentState.shapes.isNotEmpty()) {
                val updatedShapes = currentState.shapes.toMutableList()
                updatedShapes.removeLast()
                _uiState.value = currentState.copy(
                    shapes = updatedShapes,
                    selectedShapeIndex = -1,
                    currentShape = null
                )
                saveCurrentState()
            }
        }
    }
    
    fun onClear() {
        _uiState.value = _uiState.value.copy(
            shapes = mutableListOf(),
            selectedShapeIndex = -1,
            currentShape = null
        )
        saveCurrentState()
    }
    
    fun selectTool(tool: ShapeType) {
        _uiState.value = _uiState.value.copy(
            selectedTool = tool,
            selectedShapeIndex = -1,
            currentShape = null
        )
    }
    
    fun setColor(color: Color) {
        _uiState.value = _uiState.value.copy(
            selectedColor = color
        )
    }
    
    fun setStrokeWidth(width: Float) {
        _uiState.value = _uiState.value.copy(
            strokeWidth = width
        )
    }
    
    fun toggleFill() {
        _uiState.value = _uiState.value.copy(
            isFilled = !_uiState.value.isFilled
        )
    }
    
    private fun isPointOnShape(point: Offset, shape: Any, tolerance: Float): Boolean {
        return when (shape) {
            is Line -> {
                // Simple distance from point to line segment check
                val lineStart = shape.start
                val lineEnd = shape.end
                
                // Vector from start to end
                val lineVector = lineEnd - lineStart
                val lineLengthSquared = lineVector.x * lineVector.x + lineVector.y * lineVector.y
                
                // If line has zero length, check distance to the single point
                if (lineLengthSquared == 0f) {
                    return (point - lineStart).getDistanceSquared() <= tolerance * tolerance
                }
                
                // Project point onto the line
                val t = ((point.x - lineStart.x) * lineVector.x + 
                        (point.y - lineStart.y) * lineVector.y) / lineLengthSquared
                
                // Find closest point on the line segment
                val closestPoint = when {
                    t < 0 -> lineStart
                    t > 1 -> lineEnd
                    else -> Offset(
                        lineStart.x + t * lineVector.x,
                        lineStart.y + t * lineVector.y
                    )
                }
                
                // Check distance to the closest point
                (point - closestPoint).getDistanceSquared() <= tolerance * tolerance
            }
            is Rectangle -> {
                // Check if point is inside the rectangle with some tolerance
                val left = minOf(shape.topLeft.x, shape.bottomRight.x) - tolerance
                val top = minOf(shape.topLeft.y, shape.bottomRight.y) - tolerance
                val right = maxOf(shape.topLeft.x, shape.bottomRight.x) + tolerance
                val bottom = maxOf(shape.topLeft.y, shape.bottomRight.y) + tolerance
                
                point.x in left..right && point.y in top..bottom
            }
            is Circle -> {
                // Check if point is within the circle's radius + tolerance
                val distanceSquared = (point.x - shape.center.x) * (point.x - shape.center.x) +
                        (point.y - shape.center.y) * (point.y - shape.center.y)
                
                val radius = shape.radius
                distanceSquared <= (radius + tolerance) * (radius + tolerance) &&
                        distanceSquared >= (radius - tolerance) * (radius - tolerance)
            }
            is FreePath -> {
                // Check if point is close to any segment of the path
                for (i in 0 until shape.points.size - 1) {
                    val p1 = shape.points[i]
                    val p2 = shape.points[i + 1]
                    
                    // Simple distance from point to line segment check
                    val lineVector = p2 - p1
                    val lineLengthSquared = lineVector.x * lineVector.x + lineVector.y * lineVector.y
                    
                    // If line has zero length, check distance to the single point
                    if (lineLengthSquared == 0f) {
                        if ((point - p1).getDistanceSquared() <= tolerance * tolerance) {
                            return true
                        }
                        continue
                    }
                    
                    // Project point onto the line
                    val t = ((point.x - p1.x) * lineVector.x + 
                            (point.y - p1.y) * lineVector.y) / lineLengthSquared
                    
                    // Find closest point on the line segment
                    val closestPoint = when {
                        t < 0 -> p1
                        t > 1 -> p2
                        else -> Offset(
                            p1.x + t * lineVector.x,
                            p1.y + t * lineVector.y
                        )
                    }
                    
                    // Check distance to the closest point
                    if ((point - closestPoint).getDistanceSquared() <= tolerance * tolerance) {
                        return true
                    }
                }
                false
            }
            else -> false
        }
    }
    
    private fun Offset.getDistanceSquared(): Float {
        return x * x + y * y
    }
    
    private fun Offset.getDistance(): Float {
        return kotlin.math.sqrt(x * x + y * y)
    }
    
    private fun saveCurrentState() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                currentState.shapes.forEach { shape -> repository.saveShape(shape) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to save: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }
    



}
