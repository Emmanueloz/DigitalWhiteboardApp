package com.example.digitalwhiteboardapp.data.repository


import com.example.shared.model.Circle
import com.example.shared.model.DrawingShape
import com.example.shared.model.FreePath
import com.example.shared.model.Line
import com.example.shared.model.Rectangle
import com.example.shared.model.ShapeType
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import timber.log.Timber

private const val SHAPES_PATH = "shapes"

class FirebaseDrawingRepository(
    private val database: FirebaseDatabase
) : DrawingRepository {
    
    private val shapesRef = database.getReference(SHAPES_PATH)

    override suspend fun loadShapes(): List<DrawingShape> {
        return try {
            val snapshot = shapesRef
                .orderByChild("createdAt") // Ordenar por timestamp de creación
                .get()
                .await()

            Timber.tag("FirebaseDrawingRepository").d("Snapshot: $snapshot")

            snapshot.children
                .mapNotNull { child ->
                    val map = child.value as? Map<String, Any> ?: return@mapNotNull null
                    // Skip shapes marked as removed
                    if (map["isRemoved"] == true) return@mapNotNull null
                    toShape(map)
                }
                .sortedBy { shape ->
                    // Ordenamiento adicional por timestamp para garantizar el orden correcto
                    val map = shape.toFirebaseMap()
                    (map["createdAt"] as? Number)?.toLong() ?: 0L
                }

        } catch (e: Exception) {
            Timber.e(e, "Error loading shapes from Firebase")
            emptyList()
        }
    }

    override suspend fun saveShape(shape: DrawingShape) {
        try {
            val shapeMap = shape.toFirebaseMap().toMutableMap()

            // Agregar timestamp si no existe
            if (!shapeMap.containsKey("createdAt")) {
                shapeMap["createdAt"] = System.currentTimeMillis()
            }

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
            when (type.uppercase()) {
                ShapeType.LINE.name -> Line.fromFirebaseMap(map)
                ShapeType.RECTANGLE.name -> Rectangle.fromFirebaseMap(map)
                ShapeType.CIRCLE.name -> Circle.fromFirebaseMap(map)
                ShapeType.FREE_PATH.name -> FreePath.fromFirebaseMap(map)
                else -> {
                    Timber.e("Unknown shape type: $type")
                    null
                }
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
