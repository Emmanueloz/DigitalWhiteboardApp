package com.example.digitalwhiteboardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.example.digitalwhiteboardapp.di.AppModule
import com.example.digitalwhiteboardapp.presentation.viewer.WhiteboardViewerScreen
import com.example.digitalwhiteboardapp.ui.theme.DigitalWhiteboardAppTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        
        setContent {
            DigitalWhiteboardAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    WhiteboardViewerApp()
                }
            }
        }
    }
}

@Composable
fun WhiteboardViewerApp() {
    val viewModel = AppModule.provideWhiteboardViewerViewModel()
    WhiteboardViewerScreen(viewModel = viewModel)
}