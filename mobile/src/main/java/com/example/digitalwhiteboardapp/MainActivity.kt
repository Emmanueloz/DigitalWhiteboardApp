package com.example.digitalwhiteboardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.digitalwhiteboardapp.presentation.drawing.DrawingScreen
import com.example.digitalwhiteboardapp.presentation.drawing.DrawingViewModel
import com.example.digitalwhiteboardapp.ui.theme.DigitalWhiteboardAppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var viewModel: DrawingViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            DigitalWhiteboardApp()
        }
    }
    
    @Composable
    fun DigitalWhiteboardApp() {
        DigitalWhiteboardAppTheme {
            // Set status bar color to match the app theme
            val systemUiController = rememberSystemUiController()
            
            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = true,
                    isNavigationBarContrastEnforced = false
                )
            }
            
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                DrawingScreen(
                    viewModel = viewModel,
                    onNavigateBack = { /* No navigation needed */ }
                )
            }
        }
    }
}