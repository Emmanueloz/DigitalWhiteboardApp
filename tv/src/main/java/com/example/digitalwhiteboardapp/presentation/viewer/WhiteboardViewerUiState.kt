package com.example.digitalwhiteboardapp.presentation.viewer

import com.example.shared.model.DrawingShape

data class WhiteboardViewerUiState(
    val shapes: List<DrawingShape> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isConnected: Boolean = false
)
