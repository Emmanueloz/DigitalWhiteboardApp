package com.example.digitalwhiteboardapp.presentation.viewer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.digitalwhiteboardapp.data.repository.FirebaseDrawingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import android.util.Log

class WhiteboardViewerViewModel(
    private val repository: FirebaseDrawingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WhiteboardViewerUiState())
    val uiState: StateFlow<WhiteboardViewerUiState> = _uiState.asStateFlow()

    init {
        observeShapes()
    }

    private fun observeShapes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            repository.observeShapes()
                .catch { exception ->
                    Log.e("WhiteboardViewerViewModel", "Error observing shapes", exception)
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Error de conexión: ${exception.message}",
                        isLoading = false,
                        isConnected = false
                    )
                }
                .collect { shapes ->
                    _uiState.value = _uiState.value.copy(
                        shapes = shapes,
                        isLoading = false,
                        errorMessage = null,
                        isConnected = true
                    )
                    Log.d("WhiteboardViewerViewModel", "Received ${shapes.size} shapes from Firebase")
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun retry() {
        observeShapes()
    }
}
