package com.example.digitalwhiteboardapp.data.repository

import com.example.shared.model.DrawingShape

interface DrawingRepository {
    suspend fun loadShapes(): List<DrawingShape>
}
