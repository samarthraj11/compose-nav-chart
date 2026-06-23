package io.github.samarthraj11.composenavchart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Renders an animated Compose Multiplatform NAV line graph with optional scrub interaction.
 *
 * All series should generally contain the same number of points ordered by timestamp.
 */
@Composable
fun ComposeNavChart(
    series: List<NavSeries>,
    modifier: Modifier = Modifier,
    lineStyles: List<NavLineStyle> = DefaultLineStyles,
    axisLabels: List<String> = navAxisLabels(series.firstOrNull()?.points.orEmpty()),
    graphHeight: Dp = DefaultGraphHeight,
    backgroundColor: Color = Color.Transparent,
    axisTextColor: Color = Color.Black.copy(alpha = AxisTextAlpha),
    axisTextStyle: TextStyle = TextStyle.Default,
    interactive: Boolean = true,
    animationDurationMillis: Int = DefaultRevealMillis,
    tooltipFormatter: (NavPoint) -> String = { defaultNavTooltipFormatter(it.timestampMillis) },
    emptyContent: @Composable () -> Unit = { Box(modifier = Modifier.fillMaxWidth().height(graphHeight)) },
    onScrubChange: (List<NavScrubPoint>) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor),
    ) {
        if (series.any { it.points.isNotEmpty() }) {
            ChartContent(
                series = series.filter { it.points.isNotEmpty() },
                lineStyles = lineStyles,
                axisLabels = axisLabels,
                graphHeight = graphHeight,
                interactive = interactive,
                animationDurationMillis = animationDurationMillis,
                axisTextColor = axisTextColor,
                axisTextStyle = axisTextStyle,
                tooltipFormatter = tooltipFormatter,
                onScrubChange = onScrubChange,
            )
        } else {
            emptyContent()
        }
    }
}

@Composable
private fun ChartContent(
    series: List<NavSeries>,
    lineStyles: List<NavLineStyle>,
    axisLabels: List<String>,
    graphHeight: Dp,
    interactive: Boolean,
    animationDurationMillis: Int,
    axisTextColor: Color,
    axisTextStyle: TextStyle,
    tooltipFormatter: (NavPoint) -> String,
    onScrubChange: (List<NavScrubPoint>) -> Unit,
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(series) {
        progress.snapTo(0f)
        progress.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = animationDurationMillis))
    }

    var touchIndex by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(touchIndex, series) {
        val index = touchIndex
        onScrubChange(
            if (index == null) {
                emptyList()
            } else {
                series.mapIndexedNotNull { seriesIndex, navSeries ->
                    navSeries.points.getOrNull(index)?.let { point ->
                        NavScrubPoint(
                            seriesIndex = seriesIndex,
                            pointIndex = index,
                            series = navSeries,
                            point = point,
                        )
                    }
                }
            },
        )
    }

    val textMeasurer = rememberTextMeasurer()
    val dataMinMax by remember(series) {
        derivedStateOf {
            val allValues = series.flatMap { navSeries -> navSeries.points.map { it.value } }
            val min = allValues.minOrNull() ?: 0.0
            val max = allValues.maxOrNull() ?: 0.0
            val span = (max - min).coerceAtLeast(1.0)
            Pair(min - span * YPaddingRatio, max + span * YPaddingRatio)
        }
    }
    val maxPointCount = remember(series) {
        series.maxOfOrNull { it.points.size }.orZero()
    }

    val canvasModifier = Modifier
        .fillMaxWidth()
        .height(graphHeight)
        .then(
            if (interactive) {
                Modifier.pointerInput(series) {
                    trackTouches { position, released ->
                        touchIndex = if (released || maxPointCount == 0) {
                            null
                        } else {
                            val width = size.width.toFloat()
                            if (width <= 0f) {
                                null
                            } else {
                                val fraction = (position.x / width).coerceIn(0f, 1f)
                                (fraction * (maxPointCount - 1)).roundToInt().coerceIn(0, maxPointCount - 1)
                            }
                        }
                    }
                }
            } else {
                Modifier
            },
        )

    Canvas(modifier = canvasModifier) {
        val canvasWidth = size.width
        val plotHeight = size.height
        val (yMin, yMax) = dataMinMax
        val ySpan = (yMax - yMin).coerceAtLeast(1.0)

        fun xAt(index: Int, lastIndex: Int): Float =
            if (lastIndex == 0) 0f else canvasWidth * (index.toFloat() / lastIndex)

        fun yAt(value: Double): Float {
            val fraction = ((value - yMin) / ySpan).toFloat()
            return plotHeight * (1f - fraction)
        }

        val plottedSeries = series.mapIndexed { index, navSeries ->
            val lineStyle = lineStyles.getOrNull(index) ?: DefaultLineStyles[index % DefaultLineStyles.size]
            val points = navSeries.points.mapIndexed { pointIndex, point ->
                Offset(xAt(pointIndex, navSeries.points.lastIndex), yAt(point.value))
            }
            val path = if (lineStyle.smooth) buildSmoothPath(points) else buildStraightPath(points)
            PlottedSeries(navSeries = navSeries, style = lineStyle, points = points, path = path)
        }

        clipRect(right = canvasWidth * progress.value) {
            plottedSeries.forEach { plotted ->
                drawPath(
                    path = plotted.path,
                    color = plotted.style.color,
                    style = Stroke(width = plotted.style.strokeWidth, pathEffect = plotted.style.pathEffect),
                )
            }
        }

        val markerIndex = if (interactive) touchIndex else null
        if (markerIndex != null) {
            val markerX = canvasWidth * (markerIndex.toFloat() / (maxPointCount - 1).coerceAtLeast(1))
            val markerPoint = series.firstNotNullOfOrNull { it.points.getOrNull(markerIndex) }
            if (markerPoint != null) {
                drawTooltip(
                    markerX = markerX,
                    plotHeight = plotHeight,
                    canvasWidth = canvasWidth,
                    text = tooltipFormatter(markerPoint),
                    textStyle = axisTextStyle,
                    textColor = Color.Black,
                    textMeasurer = textMeasurer,
                )
            }

            plottedSeries.forEach { plotted ->
                plotted.points.getOrNull(markerIndex)?.let { marker ->
                    drawCircle(color = Color.White, radius = DotRadiusPx, center = marker)
                    drawCircle(color = plotted.style.color, radius = DotInnerRadiusPx, center = marker)
                }
            }
        }
    }

    if (axisLabels.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            axisLabels.forEach { label ->
                BasicText(
                    text = label,
                    style = axisTextStyle.copy(color = axisTextColor),
                )
            }
        }
    }
}

private data class PlottedSeries(
    val navSeries: NavSeries,
    val style: NavLineStyle,
    val points: List<Offset>,
    val path: Path,
)

private suspend fun PointerInputScope.trackTouches(
    onChange: (position: Offset, released: Boolean) -> Unit,
) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull() ?: continue
            val released = !change.pressed
            onChange(change.position, released)
            if (!released) change.consume()
        }
    }
}

private fun buildStraightPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    for (index in 1..points.lastIndex) path.lineTo(points[index].x, points[index].y)
    return path
}

private fun buildSmoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    if (points.size == 1) return path
    for (index in 0 until points.lastIndex) {
        val p0 = if (index == 0) points[index] else points[index - 1]
        val p1 = points[index]
        val p2 = points[index + 1]
        val p3 = if (index + 2 > points.lastIndex) points[index + 1] else points[index + 2]
        path.cubicTo(
            x1 = p1.x + (p2.x - p0.x) * SmoothTension,
            y1 = p1.y + (p2.y - p0.y) * SmoothTension,
            x2 = p2.x - (p3.x - p1.x) * SmoothTension,
            y2 = p2.y - (p3.y - p1.y) * SmoothTension,
            x3 = p2.x,
            y3 = p2.y,
        )
    }
    return path
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTooltip(
    markerX: Float,
    plotHeight: Float,
    canvasWidth: Float,
    text: String,
    textStyle: TextStyle,
    textColor: Color,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    val measured = textMeasurer.measure(text = text, style = textStyle.copy(color = textColor))
    val bubbleWidth = measured.size.width + TooltipPaddingHPx * 2
    val bubbleHeight = measured.size.height + TooltipPaddingVPx * 2
    val bubbleLeft = (markerX - bubbleWidth / 2f).coerceIn(0f, canvasWidth - bubbleWidth)
    val bubbleBottom = bubbleHeight
    val tailCenterX = markerX.coerceIn(
        bubbleLeft + TooltipCornerRadiusPx,
        bubbleLeft + bubbleWidth - TooltipCornerRadiusPx,
    )

    drawLine(
        color = Color.Black.copy(alpha = CrosshairAlpha),
        start = Offset(markerX, bubbleBottom + TooltipTailHeightPx),
        end = Offset(markerX, plotHeight),
        strokeWidth = CrosshairStrokePx,
    )
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(bubbleLeft, 0f),
        size = Size(bubbleWidth, bubbleHeight),
        cornerRadius = CornerRadius(TooltipCornerRadiusPx),
    )
    drawPath(
        path = Path().apply {
            moveTo(tailCenterX - TooltipTailWidthPx / 2f, bubbleBottom)
            lineTo(tailCenterX + TooltipTailWidthPx / 2f, bubbleBottom)
            lineTo(tailCenterX, bubbleBottom + TooltipTailHeightPx)
            close()
        },
        color = Color.White,
    )
    drawText(
        textLayoutResult = measured,
        topLeft = Offset(bubbleLeft + TooltipPaddingHPx, TooltipPaddingVPx),
    )
}

private fun Int?.orZero(): Int = this ?: 0

private val DefaultGraphHeight = 210.dp
private val DefaultLineStyles = listOf(
    NavLineStyle.Primary,
    NavLineStyle.Benchmark,
    NavLineStyle(color = Color(0xFF12B76A)),
    NavLineStyle(color = Color(0xFF7A5AF8)),
)

private const val CrosshairStrokePx = 2f
private const val CrosshairAlpha = 0.22f
private const val DotRadiusPx = 8f
private const val DotInnerRadiusPx = 5f
private const val AxisTextAlpha = 0.6f
private const val TooltipPaddingHPx = 16f
private const val TooltipPaddingVPx = 10f
private const val TooltipCornerRadiusPx = 12f
private const val TooltipTailWidthPx = 16f
private const val TooltipTailHeightPx = 8f
private const val YPaddingRatio = 0.20
private const val SmoothTension = 0.18f
private const val DefaultRevealMillis = 450
