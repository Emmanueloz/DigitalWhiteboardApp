package com.example.digitalwhiteboardapp.data.repository

import com.example.digitalwhiteboardapp.data.model.DrawingShape
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing the drawing board.
 * Handles loading and saving the current drawing state.
 */
interface DrawingRepository {
    /**
     * Loads all shapes from the current drawing.
     * @return List of shapes in the current drawing.
     */
    suspend fun loadShapes(): List<DrawingShape>

    /**
     * Saves a new shape to the drawing.
     * @param shape The shape to save.
     */
    suspend fun saveShape(shape: DrawingShape)

    /**
     * Updates an existing shape in the drawing.
     * @param shape The shape to update.
     */
    suspend fun updateShape(shape: DrawingShape)

    /**
     * Clears all shapes from the current drawing.
     */
    suspend fun clearDrawing()
    
    /**
     * Removes a specific shape from the drawing.
     * @param shapeId The ID of the shape to remove.
     */
    suspend fun removeShape(shapeId: String)
}
