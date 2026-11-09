package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProjectCodeParserAndGenerator
import com.example.model.SampleNewsProjects
import com.example.model.VideoResolutionPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExampleRobolectricTest {
    @Test
    fun readStringFromContext_AndVerifyProjectCodeRoundTrip() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("Bangla News Studio Pro", context.getString(R.string.app_name))

        val sample = SampleNewsProjects.primeTimeBulletin()
        val jsonCode = ProjectCodeParserAndGenerator.projectToJsonString(sample)
        val parsedFromJson = ProjectCodeParserAndGenerator.parseCodeToProject(jsonCode).getOrThrow()

        assertEquals(sample.projectName, parsedFromJson.projectName)
        assertEquals(sample.shots.size, parsedFromJson.shots.size)
        assertEquals(sample.shots.first().headline, parsedFromJson.shots.first().headline)

        val resolutions = VideoResolutionPreset.entries.map { it.label }
        assertTrue(resolutions.contains("144p"))
        assertTrue(resolutions.contains("360p"))
        assertTrue(resolutions.contains("480p"))
        assertTrue(resolutions.contains("720p HD"))
        assertTrue(resolutions.contains("1480p QHD+"))
    }
}
