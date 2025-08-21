package com.example.digitalwhiteboardapp.presentation.drawing.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.digitalwhiteboardapp.data.model.ShapeType
import com.example.digitalwhiteboardapp.data.model.ShapeType.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawingBottomBar(
    selectedTool: ShapeType,
    selectedColor: Color,
    isFilled: Boolean,
    onToolSelected: (ShapeType) -> Unit,
    onColorSelected: (Color) -> Unit,
    onToggleFill: () -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    currentStrokeWidth: Float
) {
    NavigationBar {
        // Tool selector with dropdown
        var showToolMenu by remember { mutableStateOf(false) }
        
        // Current tool icon
        NavigationBarItem(
            icon = {
                Box {
                    Icon(
                        imageVector = when (selectedTool) {
                            ShapeType.LINE -> Icons.Default.HorizontalRule
                            ShapeType.RECTANGLE -> Icons.Default.CropSquare
                            ShapeType.CIRCLE -> Icons.Default.Circle
                            ShapeType.FREE_PATH -> Icons.Default.Edit
                        },
                        contentDescription = "Tool: ${selectedTool.name}"
                    )
                    
                    // Tool selection dropdown
                    DropdownMenu(
                        expanded = showToolMenu,
                        onDismissRequest = { showToolMenu = false },
                        modifier = Modifier.width(200.dp)
                    ) {
                        ShapeType.entries.forEach { shape ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = shape.name.replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.bodyLarge
                                    ) 
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (shape) {
                                            ShapeType.LINE -> Icons.Default.HorizontalRule
                                            ShapeType.RECTANGLE -> Icons.Default.CropSquare
                                            ShapeType.CIRCLE -> Icons.Default.Circle
                                            ShapeType.FREE_PATH -> Icons.Default.Edit
                                        },
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    onToolSelected(shape)
                                    showToolMenu = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            },
            selected = false,
            onClick = { showToolMenu = true }
        )

        // Color selection with dropdown
        var showColorPicker by remember { mutableStateOf(false) }
        val colors = listOf(
            Color.Black,
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.Yellow,
            Color.White,
            Color.Cyan,
            Color.Gray
        )
        
        // Color picker item
        NavigationBarItem(
            icon = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                ) {
                    DropdownMenu(
                        expanded = showColorPicker,
                        onDismissRequest = { showColorPicker = false },
                        modifier = Modifier
                            .width(200.dp)
                            .padding(8.dp)
                    ) {
                        colors.chunked(4).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowColors.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(4.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .clickable {
                                                onColorSelected(color)
                                                showColorPicker = false
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            },
            selected = false,
            onClick = { showColorPicker = !showColorPicker }
        )

        // Stroke width selector
        var showStrokeWidthMenu by remember { mutableStateOf(false) }
        val strokeWidths = listOf(1f, 3f, 5f, 8f, 12f, 16f)
        
        NavigationBarItem(
            icon = {
                Box {
                    // Current stroke width indicator
                    Text("${currentStrokeWidth.roundToInt()}")
                    
                    // Stroke width dropdown menu
                    DropdownMenu(
                        expanded = showStrokeWidthMenu,
                        onDismissRequest = { showStrokeWidthMenu = false },
                        modifier = Modifier.width(120.dp)
                    ) {
                        strokeWidths.forEach { width ->
                            DropdownMenuItem(
                                text = { 
                                    Text("${width.roundToInt()}")
                                },

                                onClick = {
                                    onStrokeWidthChange(width)
                                    showStrokeWidthMenu = false
                                }
                            )
                        }
                    }
                }
            },
            selected = false,
            onClick = { showStrokeWidthMenu = true }
        )
        
        // Fill toggle
        NavigationBarItem(
            icon = {
                Icon(
                    imageVector = if (isFilled) Icons.Default.FormatColorFill else Icons.Default.FormatColorReset,
                    contentDescription = if (isFilled) "Filled" else "Outline"
                )
            },
            selected = isFilled,
            onClick = onToggleFill
        )

        // Undo
        NavigationBarItem(
            icon = { Icon(Icons.Default.Undo, "Undo") },
            selected = false,
            onClick = onUndo
        )

        // Clear
        NavigationBarItem(
            icon = { Icon(Icons.Default.Clear, "Clear") },
            selected = false,
            onClick = onClear
        )
    }


}
