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
        // Drawing tools
        ShapeType.entries.forEach { shape ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (shape) {
                            LINE -> Icons.Default.HorizontalRule
                            RECTANGLE -> Icons.Default.CropSquare
                            CIRCLE -> Icons.Default.Circle
                            FREE_PATH -> Icons.Default.Edit
                        },
                        contentDescription = shape.name
                    )
                },
                selected = selectedTool == shape,
                onClick = { onToolSelected(shape) }
            )
        }

        // Color selection with dropdown
        var showColorPicker by remember { mutableStateOf(false) }
        val colors = listOf(
            Color.Black,
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.Yellow,
            Color.Magenta,
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
