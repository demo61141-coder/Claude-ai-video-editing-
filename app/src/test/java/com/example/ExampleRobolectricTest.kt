package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ProjectCodeParserAndGenerator
import com.example.model.SampleNewsProjects
import com.example.model.StudioBackdropPreset
import com.example.model.ToonCharacterPose
import com.example.model.ToonCharacterWho
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

    @Test
    fun parseBanglaToonNewsReelHtmlJs_extractsAllEightShotsAccurately() {
        val banglaToonHtml = """
            <!DOCTYPE html>
            <html lang="bn">
            <head>
            <meta charset="UTF-8">
            <title>Bangla Toon — News Reel (Full)</title>
            </head>
            <body>
            <script>
            const TOTAL = 57;
            const SHOTS = [
              {key:'khál_20k',   text:'দেশে কুড়ি হাজার কিলোমিটার খাল খনন হবে',                     hi:'কুড়ি হাজার কিলোমিটার', bg:'canal', who:'host',     pose:'point', captionAt:0},
              {key:'inaug',      text:'মাননীয় প্রধানমন্ত্রী তারেক রহমান এই কর্মসূচি উদ্বোধন করবেন', hi:'তারেক রহমান',          bg:'stage', who:'minister', pose:'talk',  captionAt:null},
              {key:'tree_25c',   text:'আগামী পাঁচ বছরে পঁচিশ কোটি বৃক্ষরোপণ',                       hi:'পঁচিশ কোটি',           bg:'canal', who:'host',     pose:'point', captionAt:null},
              {key:'tree_5c_yr', text:'প্রতি বছর পাঁচ কোটি চারা রোপণ করা হবে',                       hi:'পাঁচ কোটি',            bg:'canal', who:'minister', pose:'talk',  captionAt:null},
              {key:'green_bd',   text:'আমরা একটি সবুজ বাংলাদেশ গড়তে চাই',                          hi:'সবুজ বাংলাদেশ',        bg:'canal', who:'host',     pose:'point', captionAt:null},
              {key:'river_save', text:'নদী ও খাল রক্ষা করা আমাদের দায়িত্ব',                        hi:'নদী ও খাল',            bg:'canal', who:'minister', pose:'talk',  captionAt:null},
              {key:'water_lvl',  text:'পানির স্তর ধরে রাখতে এই উদ্যোগ জরুরি',                       hi:'পানির স্তর',           bg:'canal', who:'host',     pose:'point', captionAt:null},
              {key:'mohendro',   text:'মহেন্দ্র খাল থেকেই শুরু হচ্ছে এই মহাপরিকল্পনা',               hi:'মহেন্দ্র খাল',          bg:'house', who:'minister', pose:'point', captionAt:null}
            ];
            </script>
            </body>
            </html>
        """.trimIndent()

        val parsed = ProjectCodeParserAndGenerator.parseCodeToProject(banglaToonHtml).getOrThrow()
        assertEquals("Bangla Toon — News Reel (Full)", parsed.projectName)
        assertEquals(8, parsed.shots.size)

        val shot0 = parsed.shots[0]
        assertEquals("দেশে কুড়ি হাজার কিলোমিটার খাল খনন হবে", shot0.headline)
        assertEquals("কুড়ি হাজার কিলোমিটার", shot0.highlightWord)
        assertEquals(StudioBackdropPreset.TOON_CANAL, shot0.backdropPreset)
        assertEquals(ToonCharacterWho.HOST, shot0.toonWho)
        assertEquals(ToonCharacterPose.POINT, shot0.toonPose)

        val shot1 = parsed.shots[1]
        assertEquals("মাননীয় প্রধানমন্ত্রী তারেক রহমান এই কর্মসূচি উদ্বোধন করবেন", shot1.headline)
        assertEquals("তারেক রহমান", shot1.highlightWord)
        assertEquals(StudioBackdropPreset.TOON_STAGE, shot1.backdropPreset)
        assertEquals(ToonCharacterWho.MINISTER, shot1.toonWho)
        assertEquals(ToonCharacterPose.TALK, shot1.toonPose)

        val shot7 = parsed.shots[7]
        assertEquals("মহেন্দ্র খাল থেকেই শুরু হচ্ছে এই মহাপরিকল্পনা", shot7.headline)
        assertEquals("মহেন্দ্র খাল", shot7.highlightWord)
        assertEquals(StudioBackdropPreset.TOON_HOUSE, shot7.backdropPreset)
        assertEquals(ToonCharacterWho.MINISTER, shot7.toonWho)
        assertEquals(ToonCharacterPose.POINT, shot7.toonPose)
    }
}
