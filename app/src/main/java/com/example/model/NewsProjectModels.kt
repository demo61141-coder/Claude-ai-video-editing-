package com.example.model

import androidx.annotation.DrawableRes
import com.example.R
import java.util.UUID

enum class MotionPreset(
    val label: String,
    val banglaLabel: String,
    val defaultStartScale: Float,
    val defaultEndScale: Float,
    val defaultStartX: Float,
    val defaultEndX: Float
) {
    STATIC_STUDIO("Static Frame (Toon Default)", "স্থির ফ্রেম (কার্টুন ডিফল্ট)", 1.0f, 1.0f, 0f, 0f),
    KEN_BURNS_IN("Ken Burns Zoom In", "ধীরে জুম ইন", 1.0f, 1.15f, 0f, 0f),
    KEN_BURNS_OUT("Ken Burns Zoom Out", "ধীরে জুম আউট", 1.16f, 1.0f, 0f, 0f),
    PAN_LEFT_RIGHT("Pan Left → Right", "বাম থেকে ডানে প্যান", 1.12f, 1.12f, -50f, 50f),
    PAN_RIGHT_LEFT("Pan Right → Left", "ডান থেকে বামে প্যান", 1.12f, 1.12f, 50f, -50f),
    DRAMATIC_PUSH("Dramatic Keyframe Push", "দ্রুত ফোকাস জুম", 1.0f, 1.24f, 0f, -12f),
    CUSTOM_KEYFRAME("Custom Keyframes", "কাস্টম কী-ফ্রেম", 1.0f, 1.12f, 0f, 0f)
}

enum class LowerThirdStyle(
    val label: String,
    val banglaLabel: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val badgeHex: Long
) {
    TOON_CAPTION_PILL(
        label = "Bangla Toon Caption Pill",
        banglaLabel = "কার্টুন ক্যাপশন পিল (হলুদ হাইলাইট)",
        primaryHex = 0xFF1B1A22,
        secondaryHex = 0xFF1B1A22,
        badgeHex = 0xFFF2A900
    ),
    CRIMSON_BREAKING(
        label = "Breaking Crimson TV",
        banglaLabel = "টিভি ব্রেকিং লাল ব্যানার",
        primaryHex = 0xFFE11D48,
        secondaryHex = 0xFF0F172A,
        badgeHex = 0xFFF59E0B
    ),
    NAVY_GOLD_PRIME(
        label = "Prime Time Gold TV",
        banglaLabel = "প্রাইম টাইম গোল্ড ব্যানার",
        primaryHex = 0xFF1E3A8A,
        secondaryHex = 0xFF090D16,
        badgeHex = 0xFFF59E0B
    ),
    EMERALD_ECONOMY(
        label = "Broadcast Emerald TV",
        banglaLabel = "সবুজ অর্থনীতি ব্যানার",
        primaryHex = 0xFF047857,
        secondaryHex = 0xFF091512,
        badgeHex = 0xFF10B981
    )
}

enum class TextAnimationStyle(val label: String, val banglaLabel: String) {
    SLIDE_UP_REVEAL("Slide Up Reveal", "নিচ থেকে স্লাইড"),
    TYPEWRITER_BANGLA("Typewriter Reveal", "টাইপরাইটার ইফেক্ট"),
    POP_AND_GLOW("Pop & Scale", "পপ ও জুম"),
    SMOOTH_FADE("Smooth Fade", "স্মুথ ফেইড")
}

enum class SoundEffectType(
    val label: String,
    val banglaLabel: String,
    val freqStartHz: Double,
    val freqEndHz: Double,
    val durationMs: Int
) {
    NONE("No SFX", "কোনো সাউন্ড নেই", 0.0, 0.0, 0),
    BREAKING_SWOOSH("Breaking Swoosh", "ব্রেকিং সুশ সাউন্ড", 180.0, 720.0, 320),
    NEWS_CHIME("Broadcast Chime", "সংবাদ চাইম বেল", 523.25, 1046.5, 420),
    HEADLINE_IMPACT("Headline Impact", "হেডলাইন ইমপ্যাক্ট", 140.0, 65.0, 350),
    TICKER_BEEP("Digital Telemetry", "ডিজিটাল বিপ", 880.0, 1320.0, 220),
    URGENT_ALERT("Urgent Alert Pulse", "জরুরি অ্যালার্ট", 660.0, 880.0, 480)
}

enum class MotionGraphicOverlay(val label: String, val banglaLabel: String) {
    TOON_CLEAN_REEL("Toon News Reel (No TV Bar)", "কার্টুন নিউজ রিল (ক্লিন)"),
    FULL_BROADCAST_PACK("Full TV Broadcast Graphics", "সম্পূর্ণ টিভি গ্রাফিক্স"),
    BREAKING_PERIMETER_GLOW("Breaking Alert Frame", "ব্রেকিং বর্ডার ফ্রেম"),
    MINIMAL_LOWER_THIRD("Minimal Lower-Third", "মিনিমাল ব্যানার"),
    CINEMATIC_CLEAN("Clean Feed (No Caption)", "ক্যাপশন ছাড়া ক্লিন ফিড")
}

/**
 * Supports both Bangla Toon 2D Vector Animated Scenes (canal, stage, house)
 * AND Studio Photo Backdrops!
 */
enum class StudioBackdropPreset(
    val id: String,
    val title: String,
    val banglaTitle: String,
    val isToonVectorScene: Boolean,
    @DrawableRes val drawableRes: Int,
    val fallbackColorHex: Long
) {
    TOON_CANAL(
        id = "canal",
        title = "Toon Canal (খাল ও নদী)",
        banglaTitle = "খাল ও নদী (কার্টুন দৃশ্য)",
        isToonVectorScene = true,
        drawableRes = R.drawable.img_padma_bridge_news_1794205421041,
        fallbackColorHex = 0xFFF6F1E7
    ),
    TOON_STAGE(
        id = "stage",
        title = "Toon Stage (সমবায় মঞ্চ)",
        banglaTitle = "সমবায় মঞ্চ (কার্টুন দৃশ্য)",
        isToonVectorScene = true,
        drawableRes = R.drawable.img_news_studio_bg_1794205289445,
        fallbackColorHex = 0xFFF6F1E7
    ),
    TOON_HOUSE(
        id = "house",
        title = "Toon Village (ফসলি জমি ও বাড়ি)",
        banglaTitle = "ফসলি জমি ও বাড়ি (কার্টুন)",
        isToonVectorScene = true,
        drawableRes = R.drawable.img_dhaka_metro_news_1794205289844,
        fallbackColorHex = 0xFFF6F1E7
    ),
    NEWS_STUDIO_DESK(
        id = "studio_desk",
        title = "Dhaka Newsroom Desk",
        banglaTitle = "ঢাকা নিউজরুম স্টুডিও",
        isToonVectorScene = false,
        drawableRes = R.drawable.img_news_studio_bg_1794205289445,
        fallbackColorHex = 0xFF1E1B4B
    ),
    DHAKA_METRO_RAIL(
        id = "dhaka_metro",
        title = "Dhaka Metro Skyline",
        banglaTitle = "ঢাকা মেট্রোরেল",
        isToonVectorScene = false,
        drawableRes = R.drawable.img_dhaka_metro_news_1794205289844,
        fallbackColorHex = 0xFF3B1D1A
    ),
    PADMA_BRIDGE_AERIAL(
        id = "padma_bridge",
        title = "Padma Bridge Sunrise",
        banglaTitle = "পদ্মা সেতু দৃশ্য",
        isToonVectorScene = false,
        drawableRes = R.drawable.img_padma_bridge_news_1794205421041,
        fallbackColorHex = 0xFF0F2942
    )
}

enum class ToonCharacterWho(val id: String, val banglaLabel: String) {
    HOST("host", "উপস্থাপক (Host)"),
    MINISTER("minister", "মন্ত্রী ও উপস্থাপক (Minister + Host)"),
    MINISTER_SOLO("minister_solo", "শুধু মন্ত্রী (Minister Only)"),
    NONE("none", "কোনো চরিত্র নেই (No Character)")
}

enum class ToonCharacterPose(val id: String, val banglaLabel: String) {
    POINT("point", "হাত তুলে ইশারা (Point)"),
    TALK("talk", "কথা বলার ভঙ্গি (Talk)")
}

enum class ShotMediaType {
    PRESET_SCENE,
    GALLERY_PHOTO,
    GALLERY_VIDEO
}

data class CustomTextLayer(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "নতুন টেক্সট লেয়ার",
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.25f,
    val fontSizePx: Float = 36f,
    val textColorHex: Long = 0xFFFFFFFF,
    val bgColorHex: Long = 0xCCE11D48
)

data class KeyframeConfig(
    val startScale: Float = 1.0f,
    val endScale: Float = 1.0f,
    val startOffsetX: Float = 0f,
    val endOffsetX: Float = 0f,
    val startOffsetY: Float = 0f,
    val endOffsetY: Float = 0f
)

enum class CanvasElementType(val displayName: String, val banglaName: String) {
    HEADLINE("Main Caption / Headline", "ক্যাপশন / প্রধান টেক্সট"),
    HIGHLIGHT_WORD("Highlighted Word (Marigold)", "হাইলাইট শব্দ (হলুদ)"),
    STAGE_BANNER_TEXT("Stage Banner Title", "মঞ্চের ব্যানার টেক্সট"),
    TOON_CHARACTER("Animated Character Rig", "কার্টুন চরিত্র ও পোজ"),
    SUBHEADLINE("Sub-headline", "উপ-শিরোনাম"),
    CATEGORY_BADGE("Category Tag", "ক্যাটাগরি ট্যাগ"),
    LIVE_CHANNEL_BUG("Channel & LIVE Bug", "চ্যানেল ও LIVE লোগো"),
    CUSTOM_TEXT_LAYER("Floating Text Layer", "অতিরিক্ত টেক্সট লেয়ার"),
    NEWS_TICKER("Scrolling Ticker", "স্ক্রলিং সংবাদ"),
    BACKDROP_MEDIA("Scene / Photo / Video & Keyframes", "ব্যাকগ্রাউন্ড দৃশ্য ও কী-ফ্রেম")
}

data class NewsShot(
    val id: String = UUID.randomUUID().toString(),
    val shotKey: String = "",
    val categoryTag: String = "সংবাদ রিল",
    val headline: String,
    val highlightWord: String = "",
    val subheadline: String = "",
    val stageBannerText: String = "৫৫তম জাতীয় সমবায় দিবস",
    val reporterLocation: String = "ঢাকা স্টুডিও • সরাসরি",
    val durationSeconds: Float = 6.5f,
    val toonWho: ToonCharacterWho = ToonCharacterWho.HOST,
    val toonPose: ToonCharacterPose = ToonCharacterPose.TALK,
    val motionPreset: MotionPreset = MotionPreset.STATIC_STUDIO,
    val motionIntensity: Float = 1.0f,
    val keyframes: KeyframeConfig = KeyframeConfig(),
    val lowerThirdStyle: LowerThirdStyle = LowerThirdStyle.TOON_CAPTION_PILL,
    val textAnimation: TextAnimationStyle = TextAnimationStyle.SLIDE_UP_REVEAL,
    val motionGraphicOverlay: MotionGraphicOverlay = MotionGraphicOverlay.TOON_CLEAN_REEL,
    val soundEffect: SoundEffectType = SoundEffectType.NONE,
    val backdropPreset: StudioBackdropPreset = StudioBackdropPreset.TOON_CANAL,
    val mediaType: ShotMediaType = ShotMediaType.PRESET_SCENE,
    val customImageUri: String? = null,
    val customVideoUri: String? = null,
    val customMediaName: String? = null,
    val showCategoryBadge: Boolean = false,
    val showHeadline: Boolean = true,
    val showSubheadline: Boolean = false,
    val showStageBanner: Boolean = true,
    val showLiveBug: Boolean = false,
    val showWaveformOverlay: Boolean = false,
    val customTextLayers: List<CustomTextLayer> = emptyList()
)

data class NewsProject(
    val id: String = UUID.randomUUID().toString(),
    val projectName: String = "Bangla Toon — News Reel (Full)",
    val channelName: String = "বাংলা টুন নিউজ",
    val tickerText: String = "দেশে কুড়ি হাজার কিলোমিটার খাল খনন হবে • ৫৫তম জাতীয় সমবায় দিবসে মন্ত্রী আসাদুল হাবিব দুলুর বক্তব্য • নতুন ধারণা: মডেল খাল",
    val showTicker: Boolean = false,
    val shots: List<NewsShot> = emptyList(),
    val customAudioUri: String? = null,
    val customAudioName: String? = null,
    val useStudioSynthAudio: Boolean = true,
    val tickerSpeedMultiplier: Float = 1.0f
) {
    val totalDurationSeconds: Float
        get() = shots.sumOf { it.durationSeconds.toDouble() }.toFloat().coerceAtLeast(1f)
}

enum class VideoResolutionPreset(
    val label: String,
    val subLabel: String,
    val width: Int,
    val height: Int,
    val recommendedBitrateMbps: Float
) {
    RES_144P("144p", "256×144 • Ultra Fast", 256, 144, 0.8f),
    RES_360P("360p", "640×360 • Fast Share", 640, 360, 1.8f),
    RES_480P("480p", "854×480 • Standard SD", 854, 480, 3.0f),
    RES_720P("720p HD", "1280×720 • Broadcast HD", 1280, 720, 5.0f),
    RES_1080P("1080p FHD", "1920×1080 • Full HD", 1920, 1080, 8.0f),
    RES_1480P("1480p QHD+", "2560×1440 • Ultra Master", 2560, 1440, 14.0f)
}

enum class VideoBitratePreset(
    val label: String,
    val bitrateBps: Int,
    val mbpsValue: Float
) {
    LOW_1M("1 Mbps (Fast)", 1_000_000, 1.0f),
    MED_2_5M("2.5 Mbps (Balanced)", 2_500_000, 2.5f),
    STD_5M("5 Mbps (HD Standard)", 5_000_000, 5.0f),
    HIGH_8M("8 Mbps (Broadcast)", 8_000_000, 8.0f),
    ULTRA_12M("12 Mbps (High Detail)", 12_000_000, 12.0f),
    MASTER_16M("16 Mbps (Master Quality)", 16_000_000, 16.0f)
}

data class VideoExportConfig(
    val resolution: VideoResolutionPreset = VideoResolutionPreset.RES_720P,
    val bitratePreset: VideoBitratePreset = VideoBitratePreset.STD_5M,
    val fps: Int = 30,
    val ultraFastHardwareTurbo: Boolean = true
)

sealed class ExportMp4State {
    data object Idle : ExportMp4State()
    data object ConfiguringSettings : ExportMp4State()
    data class Rendering(
        val currentFrame: Int,
        val totalFrames: Int,
        val resolutionLabel: String,
        val bitrateLabel: String,
        val statusMessage: String
    ) : ExportMp4State() {
        val progress: Float
            get() = if (totalFrames > 0) (currentFrame.toFloat() / totalFrames).coerceIn(0f, 1f) else 0f
    }
    data class Completed(
        val filePath: String,
        val fileName: String,
        val resolutionLabel: String,
        val bitrateLabel: String,
        val durationSeconds: Float,
        val fileSizeKb: Long,
        val elapsedMs: Long
    ) : ExportMp4State()
    data class Error(val message: String) : ExportMp4State()
}

object SampleNewsProjects {

    /**
     * Exact 8-shot Bangla Toon — News Reel (Full) project from the user's code!
     */
    fun banglaToonCanalNewsReel(): NewsProject {
        val rawShots = listOf(
            Triple("khál_20k", "দেশে কুড়ি হাজার কিলোমিটার খাল খনন হবে", "কুড়ি হাজার কিলোমিটার") to
                Triple(StudioBackdropPreset.TOON_CANAL, ToonCharacterWho.HOST, ToonCharacterPose.POINT),
            Triple("minister", "মন্ত্রী আসাদুল হাবিব দুলু", "আসাদুল হাবিব দুলু") to
                Triple(StudioBackdropPreset.TOON_STAGE, ToonCharacterWho.MINISTER, ToonCharacterPose.TALK),
            Triple("coopday", "৫৫তম জাতীয় সমবায় দিবসে বক্তব্য", "৫৫তম") to
                Triple(StudioBackdropPreset.TOON_STAGE, ToonCharacterWho.MINISTER, ToonCharacterPose.TALK),
            Triple("ministry", "মন্ত্রণালয়ের মাধ্যমে ৭ হাজার কিলোমিটার খাল", "৭ হাজার কিলোমিটার") to
                Triple(StudioBackdropPreset.TOON_CANAL, ToonCharacterWho.HOST, ToonCharacterPose.TALK),
            Triple("model", "নতুন ধারণা: মডেল খাল", "মডেল খাল") to
                Triple(StudioBackdropPreset.TOON_CANAL, ToonCharacterWho.HOST, ToonCharacterPose.POINT),
            Triple("maintain", "রক্ষণাবেক্ষণ ও সুরক্ষা নিশ্চিত করা হবে", "সুরক্ষা") to
                Triple(StudioBackdropPreset.TOON_CANAL, ToonCharacterWho.HOST, ToonCharacterPose.TALK),
            Triple("occupy", "পুনরায় ভরাট করে দখল রোধে নজর", "দখল রোধ") to
                Triple(StudioBackdropPreset.TOON_HOUSE, ToonCharacterWho.HOST, ToonCharacterPose.TALK),
            Triple("mohendro", "মহেন্দ্রনগর খাল: ১৯৮০-তে খনন, এখন প্রায় বিলুপ্ত", "প্রায় বিলুপ্ত") to
                Triple(StudioBackdropPreset.TOON_HOUSE, ToonCharacterWho.HOST, ToonCharacterPose.TALK)
        )

        val totalSec = 57f
        val totalChars = rawShots.sumOf { it.first.second.length }.coerceAtLeast(1)

        val shots = rawShots.map { (textInfo, sceneInfo) ->
            val (key, text, hi) = textInfo
            val (bg, who, pose) = sceneInfo
            val dur = ((text.length.toFloat() / totalChars.toFloat()) * totalSec).coerceIn(3.5f, 12f)
            NewsShot(
                shotKey = key,
                categoryTag = "সংবাদ রিল",
                headline = text,
                highlightWord = hi,
                subheadline = "হাইলাইট: $hi • দৃশ্য: ${bg.banglaTitle}",
                stageBannerText = "৫৫তম জাতীয় সমবায় দিবস",
                durationSeconds = dur,
                toonWho = who,
                toonPose = pose,
                backdropPreset = bg,
                lowerThirdStyle = LowerThirdStyle.TOON_CAPTION_PILL,
                motionGraphicOverlay = MotionGraphicOverlay.TOON_CLEAN_REEL,
                motionPreset = MotionPreset.STATIC_STUDIO
            )
        }

        return NewsProject(
            projectName = "Bangla Toon — News Reel (Full)",
            channelName = "বাংলা টুন নিউজ",
            showTicker = false,
            shots = shots
        )
    }

    fun primeTimeBulletin(): NewsProject = NewsProject(
        projectName = "সন্ধ্যা ৭টার প্রধান সংবাদ (Prime Bulletin)",
        channelName = "বাংলা নিউজ ২৪",
        showTicker = true,
        tickerText = "শিরোনাম: ঢাকা মেট্রোরেলের সান্ধ্যকালীন বিশেষ সার্ভিস চালু • পদ্মা সেতু দিয়ে একদিনে সর্বোচ্চ যানবাহন পারাপার • আইটি খাতে তরুণ উদ্যোক্তাদের নতুন সাফল্য",
        shots = listOf(
            NewsShot(
                categoryTag = "প্রধান সংবাদ",
                headline = "সন্ধ্যা ৭টার বিশেষ বুলেটিনে স্বাগতম",
                highlightWord = "বিশেষ বুলেটিনে",
                subheadline = "দেশ ও জনগণের সর্বশেষ সংবাদ নিয়ে সরাসরি ঢাকা স্টুডিও থেকে",
                reporterLocation = "ঢাকা কেন্দ্রীয় স্টুডিও • LIVE",
                durationSeconds = 4.5f,
                toonWho = ToonCharacterWho.NONE,
                motionPreset = MotionPreset.KEN_BURNS_IN,
                keyframes = KeyframeConfig(1.0f, 1.16f, 0f, 0f),
                lowerThirdStyle = LowerThirdStyle.CRIMSON_BREAKING,
                motionGraphicOverlay = MotionGraphicOverlay.FULL_BROADCAST_PACK,
                textAnimation = TextAnimationStyle.SLIDE_UP_REVEAL,
                soundEffect = SoundEffectType.NEWS_CHIME,
                backdropPreset = StudioBackdropPreset.NEWS_STUDIO_DESK,
                showCategoryBadge = true,
                showSubheadline = true,
                showLiveBug = true,
                showWaveformOverlay = true
            ),
            NewsShot(
                categoryTag = "বিশেষ প্রতিবেদন",
                headline = "ঢাকা মেট্রোরেলে যুক্ত হলো নতুন আধুনিক কোচ ও বাড়তি ট্রিপ",
                highlightWord = "নতুন আধুনিক কোচ",
                subheadline = "যাত্রীদের ভোগান্তি কমাতে পিক আওয়ারে প্রতি ৪ মিনিট পরপর ট্রেন চলাচল শুরু",
                reporterLocation = "আগারগাঁও স্টেশন, ঢাকা",
                durationSeconds = 5.0f,
                toonWho = ToonCharacterWho.NONE,
                motionPreset = MotionPreset.PAN_LEFT_RIGHT,
                keyframes = KeyframeConfig(1.14f, 1.14f, -50f, 50f),
                lowerThirdStyle = LowerThirdStyle.NAVY_GOLD_PRIME,
                motionGraphicOverlay = MotionGraphicOverlay.FULL_BROADCAST_PACK,
                textAnimation = TextAnimationStyle.POP_AND_GLOW,
                soundEffect = SoundEffectType.BREAKING_SWOOSH,
                backdropPreset = StudioBackdropPreset.DHAKA_METRO_RAIL,
                showCategoryBadge = true,
                showSubheadline = true,
                showLiveBug = true,
                showWaveformOverlay = true
            ),
            NewsShot(
                categoryTag = "অর্থনীতি ও উন্নয়ন",
                headline = "পদ্মা সেতু ঘিরে দক্ষিণবঙ্গের অর্থনীতিতে নতুন গতির সঞ্চার",
                highlightWord = "পদ্মা সেতু",
                subheadline = "কৃষি ও শিল্পপণ্য পরিবহনে সময় কমেছে অর্ধেকের বেশি, বাড়ছে নতুন বিনিয়োগ",
                reporterLocation = "মাওয়া প্রান্ত, মুন্সীগঞ্জ",
                durationSeconds = 5.0f,
                toonWho = ToonCharacterWho.NONE,
                motionPreset = MotionPreset.KEN_BURNS_OUT,
                keyframes = KeyframeConfig(1.18f, 1.0f, 0f, 0f),
                lowerThirdStyle = LowerThirdStyle.EMERALD_ECONOMY,
                motionGraphicOverlay = MotionGraphicOverlay.FULL_BROADCAST_PACK,
                textAnimation = TextAnimationStyle.SLIDE_UP_REVEAL,
                soundEffect = SoundEffectType.HEADLINE_IMPACT,
                backdropPreset = StudioBackdropPreset.PADMA_BRIDGE_AERIAL,
                showCategoryBadge = true,
                showSubheadline = true,
                showLiveBug = true,
                showWaveformOverlay = true
            )
        )
    )

    fun breakingNewsAlert(): NewsProject = NewsProject(
        projectName = "ব্রেকিং নিউজ স্পেশাল (Breaking Alert)",
        channelName = "সময় এক্সপ্রেস টিভি",
        showTicker = true,
        tickerText = "জরুরি সংবাদ: রাজধানীতে মেট্রোরেল ও এলিভেটেড এক্সপ্রেসওয়ের সংযোগ প্রকল্পের চূড়ান্ত অনুমোদন • সরাসরি সম্প্রচার",
        shots = listOf(
            NewsShot(
                categoryTag = "ব্রেকিং নিউজ",
                headline = "রাজধানীর যোগাযোগ ব্যবস্থায় আসছে যুগান্তকারী পরিবর্তন",
                highlightWord = "যুগান্তকারী পরিবর্তন",
                subheadline = "নতুন এলিভেটেড করিডোর নির্মাণের ঘোষণা দিল কর্তৃপক্ষ",
                reporterLocation = "ব্রেকিং নিউজ ডেস্ক • সরাসরি",
                durationSeconds = 4.5f,
                toonWho = ToonCharacterWho.NONE,
                motionPreset = MotionPreset.DRAMATIC_PUSH,
                keyframes = KeyframeConfig(1.0f, 1.24f, 0f, 0f),
                motionGraphicOverlay = MotionGraphicOverlay.BREAKING_PERIMETER_GLOW,
                soundEffect = SoundEffectType.URGENT_ALERT,
                lowerThirdStyle = LowerThirdStyle.CRIMSON_BREAKING,
                textAnimation = TextAnimationStyle.POP_AND_GLOW,
                backdropPreset = StudioBackdropPreset.DHAKA_METRO_RAIL,
                showCategoryBadge = true,
                showSubheadline = true,
                showLiveBug = true
            )
        )
    )
}
