package com.example

import com.example.model.SampleNewsProjects
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun sampleNewsProjects_haveValidShotsAndDurations() {
        val prime = SampleNewsProjects.primeTimeBulletin()
        assertEquals(4, prime.shots.size)
        assertTrue(prime.totalDurationSeconds >= 15f)
    }
}
