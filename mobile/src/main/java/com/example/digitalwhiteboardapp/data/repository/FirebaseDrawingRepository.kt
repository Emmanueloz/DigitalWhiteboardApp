package com.example.digitalwhiteboardapp.data.repository

import com.example.digitalwhiteboardapp.data.model.Circle
import com.example.digitalwhiteboardapp.data.model.DrawingShape
import com.example.digitalwhiteboardapp.data.model.FreePath
import com.example.digitalwhiteboardapp.data.model.Line
import com.example.digitalwhiteboardapp.data.model.Rectangle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

private const val SHAPES_PATH = "shapes"

class FirebaseDrawingRepository(
    private val database: FirebaseDatabase
) : DrawingRepository {
    
    private val shapesRef = database.getReference(SHAPES_PATH)
    
    override suspend fun loadShapes(): List<DrawingShape> {
        return try {
            val snapshot = shapesRef.get().await()
            snapshot.children.mapNotNull { child ->
                val map = child.value as? Map<String, Any> ?: return@mapNotNull null
                // Skip shapes marked as removed
                if (map["isRemoved"] == true) return@mapNotNull null
                toShape(map)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading shapes from Firebase")
            emptyList()
        }
    }
    
    override suspend fun saveShape(shape: DrawingShape) {
        try {
            val shapeMap = shape.toFirebaseMap().toMutableMap()
            // If shape is marked as removed, set isRemoved flag and update
            if (shape.isRemoved) {
                shapeMap["isRemoved"] = true
                shapesRef.child(shape.id).setValue(shapeMap).await()
            } else {
                // Only save if not marked as removed
                shapesRef.child(shape.id).setValue(shapeMap).await()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error saving shape to Firebase")
            throw e
        }
    }
    
    override suspend fun updateShape(shape: DrawingShape) {
        try {
            val shapeMap = shape.toFirebaseMap()
            shapesRef.child(shape.id).updateChildren(shapeMap).await()
        } catch (e: Exception) {
            Timber.e(e, "Error updating shape in Firebase")
            throw e
        }
    }
    
    override suspend fun removeShape(shapeId: String) {
        try {
            shapesRef.child(shapeId).removeValue().await()
        } catch (e: Exception) {
            Timber.e(e, "Error removing shape from Firebase")
            throw e
        }
    }
    
    override suspend fun clearDrawing() {
        try {
            shapesRef.removeValue().await()
        } catch (e: Exception) {
            Timber.e(e, "Error clearing drawing from Firebase")
            throw e
        }
    }
    
    private fun toShape(map: Map<String, Any>): DrawingShape? {
        return try {
            val type = map["type"] as? String ?: return null
            when (type) {
                "line" -> Line.fromFirebaseMap(map)
                "rectangle" -> Rectangle.fromFirebaseMap(map)
                "circle" -> Circle.fromFirebaseMap(map)
                "freePath" -> FreePath.fromFirebaseMap(map)
                else -> null
            }
        } catch (e: Exception) {
            Timber.e(e, "Error converting map to shape")
            null
        }
    }
    
    companion object {
        private const val TAG = "FirebaseDrawingRepository"
    }
}
