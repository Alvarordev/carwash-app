package com.example.carwash

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.OffsetDateTime

class OrderDateRangeSqliteCompatibilityTest {

    @Test
    fun lexicographicComparison_failsForEquivalentUtcRange() {
        val storedCreatedAt = "2026-03-17T01:25:01.07164Z"
        val startIso = "2026-03-16T00:00-05:00"
        val endIso = "2026-03-16T23:59:59-05:00"

        val lexicographicMatch = storedCreatedAt >= startIso && storedCreatedAt <= endIso

        assertFalse(lexicographicMatch)
    }

    @Test
    fun normalizedInstantComparison_acceptsEquivalentUtcRange() {
        val storedCreatedAt = OffsetDateTime.parse("2026-03-17T01:25:01.07164Z")
        val startIso = OffsetDateTime.parse("2026-03-16T00:00:00-05:00")
        val endIso = OffsetDateTime.parse("2026-03-16T23:59:59-05:00")

        val matchesRange = !storedCreatedAt.isBefore(startIso) && !storedCreatedAt.isAfter(endIso)

        assertTrue(matchesRange)
    }
}
