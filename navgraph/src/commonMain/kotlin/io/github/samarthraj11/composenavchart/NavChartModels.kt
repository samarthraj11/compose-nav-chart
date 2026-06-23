package io.github.samarthraj11.composenavchart

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect

/** A NAV data point at a UTC epoch-millis timestamp. */
data class NavPoint(
    val timestampMillis: Long,
    val value: Double,
)

/** A named NAV line rendered by [ComposeNavChart]. */
data class NavSeries(
    val name: String,
    val points: List<NavPoint>,
)

/** Visual styling for one NAV line. */
data class NavLineStyle(
    val color: Color,
    val strokeWidth: Float = DefaultStrokeWidth,
    val pathEffect: PathEffect? = null,
    val smooth: Boolean = true,
) {
    companion object {
        const val DefaultStrokeWidth: Float = 5f

        val Primary = NavLineStyle(color = Color(0xFF3B66EC))
        val Benchmark = NavLineStyle(color = Color(0xFFEF9F27))
    }
}

/** A scrubbed chart point reported while the user drags across the graph. */
data class NavScrubPoint(
    val seriesIndex: Int,
    val pointIndex: Int,
    val series: NavSeries,
    val point: NavPoint,
)
