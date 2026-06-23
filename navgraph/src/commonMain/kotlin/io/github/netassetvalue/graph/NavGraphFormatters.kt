package io.github.netassetvalue.graph

/** Formats a UTC epoch-millis timestamp as `MMM 'YY`, for example `Jan '26`. */
fun defaultNavAxisLabelFormatter(timestampMillis: Long): String {
    val ymd = navMillisToYmd(timestampMillis)
    val year2 = (ymd.year % Hundred).toString().padStart(2, '0')
    return "${MonthNames[ymd.month - 1]} '$year2"
}

/** Formats a UTC epoch-millis timestamp as `MMM DD`, for example `Jan 09`. */
fun defaultNavTooltipFormatter(timestampMillis: Long): String {
    val ymd = navMillisToYmd(timestampMillis)
    val day = ymd.day.toString().padStart(2, '0')
    return "${MonthNames[ymd.month - 1]} $day"
}

/** Builds evenly spaced x-axis labels from a NAV point list. */
fun navAxisLabels(
    points: List<NavPoint>,
    count: Int = DefaultAxisLabelCount,
    formatter: (Long) -> String = ::defaultNavAxisLabelFormatter,
): List<String> {
    if (points.isEmpty() || count <= 0) return emptyList()
    if (points.size <= count) return points.map { formatter(it.timestampMillis) }
    return List(count) { index ->
        val fraction = index.toFloat() / (count - 1).coerceAtLeast(1)
        val pointIndex = (fraction * points.lastIndex).toInt().coerceIn(0, points.lastIndex)
        formatter(points[pointIndex].timestampMillis)
    }
}

/** Percentage change of the point at [index] relative to the first point. */
fun List<NavPoint>.returnPercentageFromFirst(index: Int): Double {
    val first = firstOrNull()?.value?.takeIf { it != 0.0 } ?: return 0.0
    val current = getOrNull(index)?.value ?: return 0.0
    return (current - first) / first * PercentageMultiplier
}

internal data class Ymd(val year: Int, val month: Int, val day: Int)

/** Converts UTC epoch millis to a Gregorian date without requiring platform date APIs. */
internal fun navMillisToYmd(millis: Long): Ymd {
    val daysFromEpoch = millis / MillisPerDay
    val z = daysFromEpoch + DaysFromCivilEpoch
    val era = (if (z >= 0) z else z - DaysPerEraMinusOne) / DaysPerEra
    val doe = (z - era * DaysPerEra).toInt()
    val yoe = (doe - doe / DaysPerFourYears + doe / DaysPerCentury - doe / DaysPerEraMinusOne) / DaysPerYear
    val y = yoe + era.toInt() * YearsPerEra
    val doy = doe - (DaysPerYear * yoe + yoe / YearsPerLeapCycle - yoe / YearsPerCentury)
    val mp = (MonthsFormulaMultiplier * doy + MonthsFormulaOffset) / DaysPerMonthFormula
    val day = doy - (DaysPerMonthFormula * mp + MonthsFormulaOffset) / MonthsFormulaMultiplier + 1
    val month = if (mp < MarchBasedMonths) mp + 3 else mp - 9
    val year = if (month <= February) y + 1 else y
    return Ymd(year = year.toInt(), month = month.toInt(), day = day.toInt())
}

private const val DefaultAxisLabelCount = 5
private const val PercentageMultiplier = 100.0
private const val Hundred = 100
private const val MillisPerDay = 86_400_000L
private const val DaysFromCivilEpoch = 719_468L
private const val DaysPerEra = 146_097L
private const val DaysPerEraMinusOne = 146_096L
private const val DaysPerFourYears = 1_460
private const val DaysPerCentury = 36_524
private const val DaysPerYear = 365
private const val YearsPerEra = 400
private const val YearsPerLeapCycle = 4
private const val YearsPerCentury = 100
private const val MonthsFormulaMultiplier = 5
private const val MonthsFormulaOffset = 2
private const val DaysPerMonthFormula = 153
private const val MarchBasedMonths = 10
private const val February = 2

private val MonthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)
