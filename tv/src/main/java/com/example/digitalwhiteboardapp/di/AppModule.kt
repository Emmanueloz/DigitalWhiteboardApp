package com.example.digitalwhiteboardapp.di

import com.example.digitalwhiteboardapp.data.repository.FirebaseDrawingRepository
import com.example.digitalwhiteboardapp.presentation.viewer.WhiteboardViewerViewModel
import com.google.firebase.database.FirebaseDatabase

object AppModule {
    
    private val firebaseDatabase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }
    
    val drawingRepository: FirebaseDrawingRepository by lazy {
        FirebaseDrawingRepository(firebaseDatabase)
    }
    
    fun provideWhiteboardViewerViewModel(): WhiteboardViewerViewModel {
        return WhiteboardViewerViewModel(drawingRepository)
    }
}
