package com.example.digitalwhiteboardapp.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.builtins.ListSerializer
import java.util.UUID
import androidx.core.graphics.toColorInt


// Extension function to convert hex string to Color
fun String.hexToColor(): Color {
    return try {
        val color = this.toColorInt()
        Color(color)
    } catch (e: Exception) {
        Color.Black
    }
}

// Extension function to convert Color to hex string
fun Color.toHex(): String {
    val a = (alpha * 255.999f).toInt()
    val r = (red * 255.999f).toInt()
    val g = (green * 255.999f).toInt()
    val b = (blue * 255.999f).toInt()
    return String.format("#%02X%02X%02X%02X", a, r, g, b)
}

/**
 * Base class for all drawable shapes on the canvas.
 */
@Serializable
abstract class DrawingShape {
    abstract val id: String
    abstract val type: ShapeType
    abstract val color: Color
    abstract val strokeWidth: Float
    abstract val isFilled: Boolean
    abstract val isRemoved: Boolean

    abstract val createdAt: Long

    abstract fun toSvg(): String
    abstract fun draw(scope: DrawScope)

    open fun toFirebaseMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "type" to type.name,
            "color" to color.toHex(),
            "strokeWidth" to strokeWidth,
            "isFilled" to isFilled,
            "isRemoved" to isRemoved,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromFirebaseMap(map: Map<String, Any>): DrawingShape? {
            return when (map["type"]) {
                ShapeType.LINE.name -> Line.fromFirebaseMap(map)
                ShapeType.RECTANGLE.name -> Rectangle.fromFirebaseMap(map)
                ShapeType.CIRCLE.name -> Circle.fromFirebaseMap(map)
                ShapeType.FREE_PATH.name -> FreePath.fromFirebaseMap(map)
                else -> null
            }
        }
    }
}

/**
 * Represents a line drawn on the canvas.
 */
@Serializable
data class Line(
    override val id: String = UUID.randomUUID().toString(),
    @Serializable(with = OffsetSerializer::class)
    val start: Offset,
    @Serializable(with = OffsetSerializer::class)
    val end: Offset,
    @Serializable(with = ColorSerializer::class)
    override val color: Color,
    override val strokeWidth: Float,
    override val isFilled: Boolean = false,
    override val isRemoved: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis()
) : DrawingShape() {
    override val type: ShapeType = ShapeType.LINE

    override fun toFirebaseMap(): Map<String, Any> {
        return super.toFirebaseMap().toMutableMap().apply {
            putAll(mapOf(
                "startX" to start.x,
                "startY" to start.y,
                "endX" to end.x,
                "endY" to end.y
            ))
        }
    }

    companion object {
        fun fromFirebaseMap(map: Map<String, Any>): Line {
            return Line(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                start = Offset(
                    (map["startX"] as? Number)?.toFloat() ?: 0f,
                    (map["startY"] as? Number)?.toFloat() ?: 0f
                ),
                end = Offset(
                    (map["endX"] as? Number)?.toFloat() ?: 0f,
                    (map["endY"] as? Number)?.toFloat() ?: 0f
                ),
                color = (map["color"] as? String)?.hexToColor() ?: Color.Black,
                strokeWidth = (map["strokeWidth"] as? Number)?.toFloat() ?: 5f,
                isFilled = map["isFilled"] as? Boolean ?: false,
                isRemoved = map["isRemoved"] as? Boolean ?: false,

            )
        }
    }

    override fun toSvg(): String {
        return """<line x1="${start.x}" y1="${start.y}" x2="${end.x}" y2="${end.y}" 
               |stroke="${color.toHex()}" stroke-width="$strokeWidth" fill="none"/>""".trimMargin()
    }

    override fun draw(scope: DrawScope) {
        scope.drawLine(
            color = color,
            start = start,
            end = end,
        )
    }
}

/**
 * Represents a rectangle drawn on the canvas.
 */
@Serializable
data class Rectangle(
    override val id: String = UUID.randomUUID().toString(),
    @Serializable(with = OffsetSerializer::class)
    val topLeft: Offset,
    @Serializable(with = OffsetSerializer::class)
    val bottomRight: Offset,
    @Serializable(with = ColorSerializer::class)
    override val color: Color,
    override val strokeWidth: Float,
    override val isFilled: Boolean = false,
    override val isRemoved: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis()
) : DrawingShape() {
    override val type: ShapeType = ShapeType.RECTANGLE

    override fun toFirebaseMap(): Map<String, Any> {
        return super.toFirebaseMap().toMutableMap().apply {
            putAll(mapOf(
                "topLeftX" to topLeft.x,
                "topLeftY" to topLeft.y,
                "bottomRightX" to bottomRight.x,
                "bottomRightY" to bottomRight.y
            ))
        }
    }

    companion object {
        fun fromFirebaseMap(map: Map<String, Any>): Rectangle {
            return Rectangle(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                topLeft = Offset(
                    (map["topLeftX"] as? Number)?.toFloat() ?: 0f,
                    (map["topLeftY"] as? Number)?.toFloat() ?: 0f
                ),
                bottomRight = Offset(
                    (map["bottomRightX"] as? Number)?.toFloat() ?: 0f,
                    (map["bottomRightY"] as? Number)?.toFloat() ?: 0f
                ),
                color = (map["color"] as? String)?.hexToColor() ?: Color.Black,
                strokeWidth = (map["strokeWidth"] as? Number)?.toFloat() ?: 5f,
                isFilled = map["isFilled"] as? Boolean ?: false,
                isRemoved = map["isRemoved"] as? Boolean ?: false
            )
        }
    }

    override fun toSvg(): String {
        val width = bottomRight.x - topLeft.x
        val height = bottomRight.y - topLeft.y
        val fill = if (isFilled) "fill=\"${color.toHex()}\"" else "fill=\"none\""
        val stroke =
            if (isFilled) "" else "stroke=\"${color.toHex()}\" stroke-width=\"$strokeWidth\""

        return """<rect x="${topLeft.x}" y="${topLeft.y}" width="$width" height="$height" 
               |$fill $stroke/>""".trimMargin()
    }

    override fun draw(scope: DrawScope) {
        if (isFilled) {
            scope.drawRect(
                color = color,
                topLeft = topLeft,
                size = (bottomRight - topLeft).toSize()
            )
        } else {
            scope.drawRect(
                color = color,
                topLeft = topLeft,
                size = (bottomRight - topLeft).toSize(),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
        }
    }
}

/**
 * Represents a circle drawn on the canvas.
 */
@Serializable
data class Circle(
    override val id: String = UUID.randomUUID().toString(),
    @Serializable(with = OffsetSerializer::class)
    val center: Offset,
    val radius: Float,
    @Serializable(with = ColorSerializer::class)
    override val color: Color,
    override val strokeWidth: Float,
    override val isFilled: Boolean = false,
    override val isRemoved: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis()
) : DrawingShape() {
    override val type: ShapeType = ShapeType.CIRCLE

    override fun toFirebaseMap(): Map<String, Any> {
        return super.toFirebaseMap().toMutableMap().apply {
            putAll(mapOf(
                "centerX" to center.x,
                "centerY" to center.y,
                "radius" to radius
            ))
        }
    }

    companion object {
        fun fromFirebaseMap(map: Map<String, Any>): Circle {
            return Circle(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                center = Offset(
                    (map["centerX"] as? Number)?.toFloat() ?: 0f,
                    (map["centerY"] as? Number)?.toFloat() ?: 0f
                ),
                radius = (map["radius"] as? Number)?.toFloat() ?: 0f,
                color = (map["color"] as? String)?.hexToColor() ?: Color.Black,
                strokeWidth = (map["strokeWidth"] as? Number)?.toFloat() ?: 5f,
                isFilled = map["isFilled"] as? Boolean ?: false,
                isRemoved = map["isRemoved"] as? Boolean ?: false
            )
        }
    }

    override fun toSvg(): String {
        val fill = if (isFilled) "fill=\"${color.toHex()}\"" else "fill=\"none\""
        val stroke =
            if (isFilled) "" else "stroke=\"${color.toHex()}\" stroke-width=\"$strokeWidth\""

        return """<circle cx="${center.x}" cy="${center.y}" r="$radius" $fill $stroke/>"""
    }

    override fun draw(scope: DrawScope) {
        if (isFilled) {
            scope.drawCircle(
                color = color,
                center = center,
                radius = radius
            )
        } else {
            scope.drawCircle(
                color = color,
                center = center,
                radius = radius,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
        }
    }
}

/**
 * Represents a freehand path drawn on the canvas.
 */
@Serializable
data class FreePath(
    override val id: String = UUID.randomUUID().toString(),
    @Serializable(with = OffsetListSerializer::class)
    val points: List<Offset>,
    @Serializable(with = ColorSerializer::class)
    override val color: Color,
    override val strokeWidth: Float,
    override val isFilled: Boolean = false,
    override val isRemoved: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis()
) : DrawingShape() {
    override val type: ShapeType = ShapeType.FREE_PATH

    override fun toFirebaseMap(): Map<String, Any> {
        val pointsMap = points.flatMapIndexed { index, offset ->
            listOf(
                "x$index" to offset.x,
                "y$index" to offset.y
            )
        }.toMap()

        return super.toFirebaseMap().toMutableMap().apply {
            putAll(pointsMap)
            put("pointCount", points.size)
        }
    }

    companion object {
        fun fromFirebaseMap(map: Map<String, Any>): FreePath {
            val pointCount = (map["pointCount"] as? Number)?.toInt() ?: 0
            val points = (0 until pointCount).mapNotNull { index ->
                val x = (map["x$index"] as? Number)?.toFloat()
                val y = (map["y$index"] as? Number)?.toFloat()
                if (x != null && y != null) Offset(x, y) else null
            }.filterNotNull()

            return FreePath(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                points = points,
                color = (map["color"] as? String)?.hexToColor() ?: Color.Black,
                strokeWidth = (map["strokeWidth"] as? Number)?.toFloat() ?: 5f,
                isFilled = map["isFilled"] as? Boolean ?: false,
                isRemoved = map["isRemoved"] as? Boolean ?: false
            )
        }
    }

    override fun toSvg(): String {
        if (points.size < 2) return ""

        val pathData = buildString {
            append("M ${points[0].x},${points[0].y} ")
            points.drop(1).forEach { point ->
                append("L ${point.x},${point.y} ")
            }
        }

        return """<path d="$pathData" fill="none" stroke="${color.toHex()}" 
               |stroke-width="$strokeWidth" stroke-linecap="round" stroke-linejoin="round"/>""".trimMargin()
    }

    override fun draw(scope: DrawScope) {
        if (points.size < 2) return

        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            points.drop(1).forEach { point ->
                lineTo(point.x, point.y)
            }
        }

        if (isFilled) {
            scope.drawPath(
                path = path,
                color = color
            )
        } else {
            scope.drawPath(
                path = path,
                color = color,
                style = Stroke(width = strokeWidth)
            )
        }
    }
}


// Extension function to convert Offset to Size
fun Offset.toSize() = Size(x, y)

// Extension operator function to subtract two Offsets.
operator fun Offset.minus(other: Offset) = Offset(x - other.x, y - other.y)

// Serializer for List<Offset>
@OptIn(InternalSerializationApi::class)
object OffsetListSerializer : KSerializer<List<Offset>> {
    private val delegate = ListSerializer(OffsetSerializer)

    override val descriptor: SerialDescriptor = buildSerialDescriptor("List", StructureKind.LIST) {
        element("Offset", OffsetSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: List<Offset>) {
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<Offset> {
        return delegate.deserialize(decoder)
    }
}

// Serializers for Compose UI types that don't have built-in serialization
@Serializable
data class OffsetSurrogate(val x: Float, val y: Float) {
    constructor(offset: Offset) : this(offset.x, offset.y)

    fun toOffset() = Offset(x, y)
}

@Serializer(forClass = Offset::class)
object OffsetSerializer : KSerializer<Offset> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Offset") {
        element<Float>("x")
        element<Float>("y")
    }

    override fun serialize(encoder: Encoder, value: Offset) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeFloatElement(descriptor, 0, value.x)
        composite.encodeFloatElement(descriptor, 1, value.y)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Offset {
        val composite = decoder.beginStructure(descriptor)
        var x = 0f
        var y = 0f

        loop@ while (true) {
            when (val i = composite.decodeElementIndex(descriptor)) {
                CompositeDecoder.DECODE_DONE -> break@loop
                0 -> x = composite.decodeFloatElement(descriptor, 0)
                1 -> y = composite.decodeFloatElement(descriptor, 1)
                else -> throw SerializationException("Unknown index $i")
            }
        }

        composite.endStructure(descriptor)
        return Offset(x, y)
    }

}


@Serializer(forClass = Color::class)
object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeString(value.toHex())
    }

    override fun deserialize(decoder: Decoder): Color {
        val hex = decoder.decodeString()
        return hex.hexToColor()
    }
}
