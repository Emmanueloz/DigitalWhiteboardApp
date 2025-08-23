package com.example.digitalwhiteboardapp.data.repository

import com.example.shared.model.Circle
import com.example.shared.model.DrawingShape
import com.example.shared.model.FreePath
import com.example.shared.model.Line
import com.example.shared.model.Rectangle
import com.example.shared.model.ShapeType
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import android.util.Log

private const val SHAPES_PATH = "shapes"

class FirebaseDrawingRepository(
    private val database: FirebaseDatabase
) : DrawingRepository {
    
    private val shapesRef = database.getReference(SHAPES_PATH)

    override suspend fun loadShapes(): List<DrawingShape> {
        return try {
            val snapshot = shapesRef
                .orderByChild("createdAt")
                .get()
                .await()

            Log.d("FirebaseDrawingRepository", "Snapshot: $snapshot")

            snapshot.children
                .mapNotNull { child ->
                    val map = child.value as? Map<String, Any> ?: return@mapNotNull null
                    // Skip shapes marked as removed
                    if (map["isRemoved"] == true) return@mapNotNull null
                    toShape(map)
                }
                .sortedBy { shape ->
                    val map = shape.toFirebaseMap()
                    (map["createdAt"] as? Number)?.toLong() ?: 0L
                }

        } catch (e: Exception) {
            Log.e("FirebaseDrawingRepository", "Error loading shapes from Firebase", e)
            emptyList()
        }
    }

    fun observeShapes(): Flow<List<DrawingShape>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val shapes = snapshot.children
                        .mapNotNull { child ->
                            val map = child.value as? Map<String, Any> ?: return@mapNotNull null
                            // Skip shapes marked as removed
                            if (map["isRemoved"] == true) return@mapNotNull null
                            toShape(map)
                        }
                        .sortedBy { shape ->
                            val map = shape.toFirebaseMap()
                            (map["createdAt"] as? Number)?.toLong() ?: 0L
                        }
                    
                    trySend(shapes)
                } catch (e: Exception) {
                    Log.e("FirebaseDrawingRepository", "Error processing shapes update", e)
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseDrawingRepository", "Firebase listener cancelled: ${error.message}")
                trySend(emptyList())
            }
        }

        shapesRef.orderByChild("createdAt").addValueEventListener(listener)

        awaitClose {
            shapesRef.removeEventListener(listener)
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
                    Log.e("FirebaseDrawingRepository", "Unknown shape type: $type")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseDrawingRepository", "Error converting map to shape", e)
            null
        }
    }
}
