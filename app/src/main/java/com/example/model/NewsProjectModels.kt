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
    KEN_BURNS_IN("Ken Burns Zoom In", "ধীরে জুম ইন", 1.0f, 1.18f, 0f, 0f),
    KEN_BURNS_OUT("Ken Burns Zoom Out", "ধীরে জুম আউট", 1.20f, 1.0f, 0f, 0f),
    PAN_LEFT_RIGHT("Pan Left → Right", "বাম থেকে ডানে প্যান", 1.15f, 1.15f, -60f, 60f),
    PAN_RIGHT_LEFT("Pan Right → Left", "ডান থেকে বামে প্যান", 1.15f, 1.15f, 60f, -60f),
    DRAMATIC_PUSH("Dramatic Keyframe Push", "দ্রুত ফোকাস জুম", 1.0f, 1.28f, 0f, -15f),
    CUSTOM_KEYFRAME("Custom Keyframes", "কাস্টম কী-ফ্রেম", 1.0f, 1.15f, 0f, 0f),
    STATIC_STUDIO("Static Frame", "স্থির ফ্রেম", 1.0f, 1.0f, 0f, 0f)
}

enum class LowerThirdStyle(
    val label: String,
    val banglaLabel: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val badgeHex: Long
) {
    CRIMSON_BREAKING(
        label = "Breaking Crimson",
        banglaLabel = "ব্রেকিং লাল থিম",
        primaryHex = 0xFFE11D48,
        secondaryHex = 0xFF0F172A,
        badgeHex = 0xFFF59E0B
    ),
    NAVY_GOLD_PRIME(
        label = "Prime Time Gold",
        banglaLabel = "প্রাইম টাইম গোল্ড",
        primaryHex = 0xFF1E3A8A,
        secondaryHex = 0xFF090D16,
        badgeHex = 0xFFF59E0B
    ),
    EMERALD_ECONOMY(
        label = "Broadcast Emerald",
        banglaLabel = "সবুজ অর্থনীতি থিম",
        primaryHex = 0xFF047857,
        secondaryHex = 0xFF091512,
        badgeHex = 0xFF10B981
    ),
    CYBER_DARK_GLASS(
        label = "Cyber Dark Slate",
        banglaLabel = "ডার্ক গ্লাস স্টুডিও",
        primaryHex = 0xFF334155,
        secondaryHex = 0xFF090D16,
        badgeHex = 0xFF06B6D4
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
    FULL_BROADCAST_PACK("Full Broadcast Graphics", "সম্পূর্ণ টিভি গ্রাফিক্স"),
    BREAKING_PERIMETER_GLOW("Breaking Alert Frame", "ব্রেকিং বর্ডার ফ্রেম"),
    MINIMAL_LOWER_THIRD("Minimal Lower-Third", "মিনিমাল ব্যানার"),
    CINEMATIC_CLEAN("Clean Feed (No Banner)", "ক্লিন ভিডিও ফিড")
}

enum class StudioBackdropPreset(
    val id: String,
    val title: String,
    @DrawableRes val drawableRes: Int,
    val fallbackColorHex: Long
) {
    NEWS_STUDIO_DESK(
        id = "studio_desk",
        title = "Dhaka Newsroom Desk",
        drawableRes = R.drawable.img_news_studio_bg_1794205289445,
        fallbackColorHex = 0xFF1E1B4B
    ),
    DHAKA_METRO_RAIL(
        id = "dhaka_metro",
        title = "Dhaka Metro Skyline",
        drawableRes = R.drawable.img_dhaka_metro_news_1794205289844,
        fallbackColorHex = 0xFF3B1D1A
    ),
    PADMA_BRIDGE_AERIAL(
        id = "padma_bridge",
        title = "Padma Bridge Sunrise",
        drawableRes = R.drawable.img_padma_bridge_news_1794205421041,
        fallbackColorHex = 0xFF0F2942
    )
}

enum class ShotMediaType {
    PRESET_PHOTO,
    GALLERY_PHOTO,
    GALLERY_VIDEO
}

data class CustomTextLayer(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "নতুন টেক্সট লেয়ার",
    val xPercent: Float = 0.5f,
    val yPercent: Float = 0.30f,
    val fontSizePx: Float = 34f,
    val textColorHex: Long = 0xFFFFFFFF,
    val bgColorHex: Long = 0xCCE11D48
)

data class KeyframeConfig(
    val startScale: Float = 1.0f,
    val endScale: Float = 1.18f,
    val startOffsetX: Float = 0f,
    val endOffsetX: Float = 0f,
    val startOffsetY: Float = 0f,
    val endOffsetY: Float = -12f
)

enum class CanvasElementType(val displayName: String, val banglaName: String) {
    HEADLINE("Main Headline", "প্রধান শিরোনাম"),
    SUBHEADLINE("Sub-headline", "উপ-শিরোনাম"),
    CATEGORY_BADGE("Category Tag", "ক্যাটাগরি ট্যাগ"),
    LIVE_CHANNEL_BUG("Channel & LIVE Bug", "চ্যানেল ও LIVE লোগো"),
    CUSTOM_TEXT_LAYER("Floating Text Layer", "অতিরিক্ত টেক্সট লেয়ার"),
    NEWS_TICKER("Scrolling Ticker", "স্ক্রলিং সংবাদ"),
    BACKDROP_MEDIA("Photo / Video & Keyframes", "ছবি/ভিডিও ও কী-ফ্রেম")
}

data class NewsShot(
    val id: String = UUID.randomUUID().toString(),
    val categoryTag: String = "ব্রেকিং নিউজ",
    val headline: String,
    val subheadline: String,
    val reporterLocation: String = "ঢাকা স্টুডিও • সরাসরি",
    val durationSeconds: Float = 5.0f,
    val motionPreset: MotionPreset = MotionPreset.KEN_BURNS_IN,
    val motionIntensity: Float = 1.0f,
    val keyframes: KeyframeConfig = KeyframeConfig(
        startScale = MotionPreset.KEN_BURNS_IN.defaultStartScale,
        endScale = MotionPreset.KEN_BURNS_IN.defaultEndScale,
        startOffsetX = MotionPreset.KEN_BURNS_IN.defaultStartX,
        endOffsetX = MotionPreset.KEN_BURNS_IN.defaultEndX
    ),
    val lowerThirdStyle: LowerThirdStyle = LowerThirdStyle.CRIMSON_BREAKING,
    val textAnimation: TextAnimationStyle = TextAnimationStyle.SLIDE_UP_REVEAL,
    val motionGraphicOverlay: MotionGraphicOverlay = MotionGraphicOverlay.FULL_BROADCAST_PACK,
    val soundEffect: SoundEffectType = SoundEffectType.BREAKING_SWOOSH,
    val backdropPreset: StudioBackdropPreset = StudioBackdropPreset.NEWS_STUDIO_DESK,
    val mediaType: ShotMediaType = ShotMediaType.PRESET_PHOTO,
    val customImageUri: String? = null,
    val customVideoUri: String? = null,
    val customMediaName: String? = null,
    val showCategoryBadge: Boolean = true,
    val showHeadline: Boolean = true,
    val showSubheadline: Boolean = true,
    val showLiveBug: Boolean = true,
    val showWaveformOverlay: Boolean = true,
    val customTextLayers: List<CustomTextLayer> = emptyList()
)

data class NewsProject(
    val id: String = UUID.randomUUID().toString(),
    val projectName: String = "সন্ধ্যা ৭টার প্রধান সংবাদ",
    val channelName: String = "বাংলা নিউজ ২৪",
    val tickerText: String = "ব্রেকিং নিউজ: মেট্রোরেলের নতুন রুট উদ্বোধন • পদ্মা সেতুতে পণ্য পরিবহনে নতুন রেকর্ড • তথ্যপ্রযুক্তি খাতে রপ্তানি আয় বৃদ্ধি • স্টুডিও থেকে সরাসরি সম্প্রচার",
    val showTicker: Boolean = true,
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

    fun primeTimeBulletin(): NewsProject = NewsProject(
        projectName = "সন্ধ্যা ৭টার প্রধান সংবাদ (Prime Bulletin)",
        channelName = "বাংলা নিউজ ২৪",
        tickerText = "শিরোনাম: ঢাকা মেট্রোরেলের সান্ধ্যকালীন বিশেষ সার্ভিস চালু • পদ্মা সেতু দিয়ে একদিনে সর্বোচ্চ যানবাহন পারাপার • আইটি খাতে তরুণ উদ্যোক্তাদের নতুন সাফল্য • আবহাওয়া: আগামী ২৪ ঘণ্টায় ঢাকাসহ দেশের বিভিন্ন স্থানে বৃষ্টির সম্ভাবনা",
        shots = listOf(
            NewsShot(
                categoryTag = "প্রধান সংবাদ",
                headline = "সন্ধ্যা ৭টার বিশেষ বুলেটিনে স্বাগতম",
                subheadline = "দেশ ও জনগণের সর্বশেষ সংবাদ নিয়ে সরাসরি ঢাকা স্টুডিও থেকে",
                reporterLocation = "ঢাকা কেন্দ্রীয় স্টুডিও • LIVE",
                durationSeconds = 4.5f,
                motionPreset = MotionPreset.KEN_BURNS_IN,
                lowerThirdStyle = LowerThirdStyle.CRIMSON_BREAKING,
                textAnimation = TextAnimationStyle.SLIDE_UP_REVEAL,
                soundEffect = SoundEffectType.NEWS_CHIME,
                backdropPreset = StudioBackdropPreset.NEWS_STUDIO_DESK
            ),
            NewsShot(
                categoryTag = "বিশেষ প্রতিবেদন",
                headline = "ঢাকা মেট্রোরেলে যুক্ত হলো নতুন আধুনিক কোচ ও বাড়তি ট্রিপ",
                subheadline = "যাত্রীদের ভোগান্তি কমাতে পিক আওয়ারে প্রতি ৪ মিনিট পরপর ট্রেন চলাচল শুরু",
                reporterLocation = "আগারগাঁও স্টেশন, ঢাকা",
                durationSeconds = 5.0f,
                motionPreset = MotionPreset.PAN_LEFT_RIGHT,
                keyframes = KeyframeConfig(1.15f, 1.15f, -55f, 55f, 0f, 0f),
                lowerThirdStyle = LowerThirdStyle.NAVY_GOLD_PRIME,
                textAnimation = TextAnimationStyle.POP_AND_GLOW,
                soundEffect = SoundEffectType.BREAKING_SWOOSH,
                backdropPreset = StudioBackdropPreset.DHAKA_METRO_RAIL
            ),
            NewsShot(
                categoryTag = "অর্থনীতি ও উন্নয়ন",
                headline = "পদ্মা সেতু ঘিরে দক্ষিণবঙ্গের অর্থনীতিতে নতুন গতির সঞ্চার",
                subheadline = "কৃষি ও শিল্পপণ্য পরিবহনে সময় কমেছে অর্ধেকের বেশি, বাড়ছে নতুন বিনিয়োগ",
                reporterLocation = "মাওয়া প্রান্ত, মুন্সীগঞ্জ",
                durationSeconds = 5.0f,
                motionPreset = MotionPreset.KEN_BURNS_OUT,
                keyframes = KeyframeConfig(1.20f, 1.02f, 0f, 0f, 10f, 0f),
                lowerThirdStyle = LowerThirdStyle.EMERALD_ECONOMY,
                textAnimation = TextAnimationStyle.SLIDE_UP_REVEAL,
                soundEffect = SoundEffectType.HEADLINE_IMPACT,
                backdropPreset = StudioBackdropPreset.PADMA_BRIDGE_AERIAL
            ),
            NewsShot(
                categoryTag = "প্রযুক্তি ও ভবিষ্যৎ",
                headline = "কৃত্রিম বুদ্ধিমত্তা ও সফটওয়্যার রপ্তানিতে বাংলাদেশের নতুন মাইলফলক",
                subheadline = "বিশ্ববাজারে দেশীয় তরুণ প্রকৌশলীদের তৈরি প্রযুক্তির চাহিদা বাড়ছে",
                reporterLocation = "হাই-টেক পার্ক ডেস্ক",
                durationSeconds = 4.5f,
                motionPreset = MotionPreset.DRAMATIC_PUSH,
                keyframes = KeyframeConfig(1.0f, 1.25f, 0f, 0f, 0f, -15f),
                lowerThirdStyle = LowerThirdStyle.CYBER_DARK_GLASS,
                textAnimation = TextAnimationStyle.TYPEWRITER_BANGLA,
                soundEffect = SoundEffectType.TICKER_BEEP,
                backdropPreset = StudioBackdropPreset.NEWS_STUDIO_DESK
            )
        )
    )

    fun breakingNewsAlert(): NewsProject = NewsProject(
        projectName = "ব্রেকিং নিউজ স্পেশাল (Breaking Alert)",
        channelName = "সময় এক্সপ্রেস টিভি",
        tickerText = "জরুরি সংবাদ: রাজধানীতে মেট্রোরেল ও এলিভেটেড এক্সপ্রেসওয়ের সংযোগ প্রকল্পের চূড়ান্ত অনুমোদন • সরাসরি সম্প্রচার",
        shots = listOf(
            NewsShot(
                categoryTag = "ব্রেকিং নিউজ",
                headline = "রাজধানীর যোগাযোগ ব্যবস্থায় আসছে যুগান্তকারী পরিবর্তন",
                subheadline = "নতুন এলিভেটেড করিডোর নির্মাণের ঘোষণা দিল কর্তৃপক্ষ",
                reporterLocation = "ব্রেকিং নিউজ ডেস্ক • সরাসরি",
                durationSeconds = 4.0f,
                motionPreset = MotionPreset.DRAMATIC_PUSH,
                motionGraphicOverlay = MotionGraphicOverlay.BREAKING_PERIMETER_GLOW,
                soundEffect = SoundEffectType.URGENT_ALERT,
                lowerThirdStyle = LowerThirdStyle.CRIMSON_BREAKING,
                textAnimation = TextAnimationStyle.POP_AND_GLOW,
                backdropPreset = StudioBackdropPreset.DHAKA_METRO_RAIL
            ),
            NewsShot(
                categoryTag = "সরাসরি আপডেট",
                headline = "স্টুডিও থেকে যুক্ত হচ্ছেন বিশেষজ্ঞ প্রকৌশলী ও নগর পরিকল্পনাবিদ",
                subheadline = "যানজট নিরসনে সমন্বিত গণপরিবহন ব্যবস্থার ওপর গুরুত্বারোপ",
                reporterLocation = "ঢাকা স্টুডিও • LIVE",
                durationSeconds = 4.5f,
                motionPreset = MotionPreset.KEN_BURNS_IN,
                soundEffect = SoundEffectType.BREAKING_SWOOSH,
                lowerThirdStyle = LowerThirdStyle.CRIMSON_BREAKING,
                textAnimation = TextAnimationStyle.SLIDE_UP_REVEAL,
                backdropPreset = StudioBackdropPreset.NEWS_STUDIO_DESK
            )
        )
    )

    fun economyAndInfrastructureReport(): NewsProject = NewsProject(
        projectName = "অর্থনীতি ও বাণিজ্য সংবাদ (Economy Special)",
        channelName = "বাণিজ্য প্রতিদিন HD",
        tickerText = "শেয়ার বাজার আপডেট: সূচকের ঊর্ধ্বমুখী প্রবণতা • রেমিট্যান্স প্রবাহে ১৫ শতাংশ প্রবৃদ্ধি • রপ্তানি বাণিজ্যে নতুন সম্ভাবনা",
        shots = listOf(
            NewsShot(
                categoryTag = "বাণিজ্য সংবাদ",
                headline = "পদ্মা সেতু রেল সংযোগে দক্ষিণ-পশ্চিমাঞ্চলে শিল্পায়নের বিপ্লব",
                subheadline = "মোংলা বন্দরের সাথে সরাসরি পণ্যবাহী ট্রেন চলাচলে কমেছে ব্যয়",
                reporterLocation = "বাণিজ্য ডেস্ক • ঢাকা",
                durationSeconds = 5.0f,
                motionPreset = MotionPreset.PAN_RIGHT_LEFT,
                soundEffect = SoundEffectType.NEWS_CHIME,
                lowerThirdStyle = LowerThirdStyle.EMERALD_ECONOMY,
                textAnimation = TextAnimationStyle.SLIDE_UP_REVEAL,
                backdropPreset = StudioBackdropPreset.PADMA_BRIDGE_AERIAL
            ),
            NewsShot(
                categoryTag = "বিশেষ বিশ্লেষণ",
                headline = "ডিজিটাল ব্যাংকিং ও ফিনটেক খাতে গ্রাহকদের রেকর্ড লেনদেন",
                subheadline = "ক্যাশলেস বাংলাদেশ গড়ার লক্ষ্যে নতুন নীতিমালা বাস্তবায়ন",
                reporterLocation = "অর্থনীতি স্টুডিও • সরাসরি",
                durationSeconds = 5.0f,
                motionPreset = MotionPreset.KEN_BURNS_IN,
                soundEffect = SoundEffectType.HEADLINE_IMPACT,
                lowerThirdStyle = LowerThirdStyle.NAVY_GOLD_PRIME,
                textAnimation = TextAnimationStyle.TYPEWRITER_BANGLA,
                backdropPreset = StudioBackdropPreset.NEWS_STUDIO_DESK
            )
        )
    )
}
