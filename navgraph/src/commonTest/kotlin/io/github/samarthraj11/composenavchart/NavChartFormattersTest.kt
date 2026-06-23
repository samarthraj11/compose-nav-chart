package io.github.samarthraj11.composenavchart

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavChartFormattersTest {

    @Test
    fun axisLabelsAreEvenlySpaced() {
        val points = List(10) { index ->
            NavPoint(timestampMillis = JanFirst2026 + index * DayMillis, value = index.toDouble())
        }

        val labels = navAxisLabels(points = points, count = 3) { millis ->
            ((millis - JanFirst2026) / DayMillis).toString()
        }

        assertEquals(listOf("0", "4", "9"), labels)
    }

    @Test
    fun returnPercentageFromFirstHandlesValidIndex() {
        val points = listOf(
            NavPoint(timestampMillis = JanFirst2026, value = 100.0),
            NavPoint(timestampMillis = JanFirst2026 + DayMillis, value = 125.0),
        )

        assertEquals(25.0, points.returnPercentageFromFirst(index = 1))
    }

    @Test
    fun returnPercentageFromFirstHandlesZeroFirstPoint() {
        val points = listOf(
            NavPoint(timestampMillis = JanFirst2026, value = 0.0),
            NavPoint(timestampMillis = JanFirst2026 + DayMillis, value = 125.0),
        )

        assertEquals(0.0, points.returnPercentageFromFirst(index = 1))
    }

    @Test
    fun defaultFormattersUseUtcEpochMillis() {
        assertEquals("Jan '26", defaultNavAxisLabelFormatter(JanFirst2026))
        assertTrue(defaultNavTooltipFormatter(JanFirst2026).startsWith("Jan 01"))
    }

    private companion object {
        const val JanFirst2026 = 1_767_225_600_000L
        const val DayMillis = 86_400_000L
    }
}
