package com.example

import com.example.model.SampleNewsProjects
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun sampleNewsProjects_haveValidShotsAndDurations() {
        val toonReel = SampleNewsProjects.banglaToonCanalNewsReel()
        assertEquals(8, toonReel.shots.size)
        assertTrue(toonReel.totalDurationSeconds >= 50f)

        val prime = SampleNewsProjects.primeTimeBulletin()
        assertEquals(3, prime.shots.size)
        assertTrue(prime.totalDurationSeconds >= 12f)
    }
}
