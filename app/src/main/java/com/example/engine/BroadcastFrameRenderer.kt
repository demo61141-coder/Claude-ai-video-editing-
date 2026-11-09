package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.model.CanvasElementType
import com.example.model.LowerThirdStyle
import com.example.model.MotionGraphicOverlay
import com.example.model.MotionPreset
import com.example.model.NewsProject
import com.example.model.NewsShot
import com.example.model.ShotMediaType
import com.example.model.StudioBackdropPreset
import com.example.model.TextAnimationStyle
import com.example.model.ToonCharacterPose
import com.example.model.ToonCharacterWho
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

class BroadcastFrameRenderer(private val context: Context) {

    // Bangla Toon Palette (Exact colors from user's Bangla Toon — News Reel code)
    private val colToonWhite = Color.parseColor("#F6F1E7")
    private val colTerracotta = Color.parseColor("#C8643B")
    private val colMarigold = Color.parseColor("#F2A900")
    private val colToonRed = Color.parseColor("#B3202A")
    private val colTeal = Color.parseColor("#1F7A74")
    private val colJute = Color.parseColor("#D9B88F")
    private val colInk = Color.parseColor("#1B1A22")
    private val colSkin = Color.parseColor("#C98B5E")
    private val colSkinDark = Color.parseColor("#A66F47")
    private val colGreen = Color.parseColor("#4E7A3A")
    private val colWater = Color.parseColor("#2E8B9E")
    private val colMinHair = Color.parseColor("#E8E4DA")

    private val presetBitmaps = mutableMapOf<StudioBackdropPreset, Bitmap>()
    private val customBitmaps = mutableMapOf<String, Bitmap>()
    private val customVideoFrameCache = mutableMapOf<String, List<Bitmap>>()

    private val banglaBoldTypeface: Typeface by lazy {
        runCatching {
            Typeface.create(ResourcesCompat.getFont(context, R.font.hind_siliguri), Typeface.BOLD)
        }.getOrDefault(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD))
    }

    private val banglaRegularTypeface: Typeface by lazy {
        runCatching {
            Typeface.create(ResourcesCompat.getFont(context, R.font.hind_siliguri), Typeface.NORMAL)
        }.getOrDefault(Typeface.SANS_SERIF)
    }

    private val studioMonoTypeface: Typeface by lazy {
        runCatching {
            Typeface.create(ResourcesCompat.getFont(context, R.font.space_grotesk), Typeface.BOLD)
        }.getOrDefault(Typeface.MONOSPACE)
    }

    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        color = Color.parseColor("#F59E0B")
        pathEffect = DashPathEffect(floatArrayOf(20f, 12f), 0f)
    }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val matrix = Matrix()
    private val rectF = RectF()
    private val path = Path()

    private data class ToonCharConfig(
        val skin: Int,
        val hair: Int,
        val shirt: Int,
        val accent: Int?,
        val glasses: Boolean,
        val mustache: Boolean,
        val hairStyle: String,
        val cap: Int?,
        val phase: Float
    )

    private val hostConfig = ToonCharConfig(
        skin = colSkin,
        hair = colInk,
        shirt = colTeal,
        accent = colToonWhite,
        glasses = true,
        mustache = false,
        hairStyle = "side",
        cap = null,
        phase = 0f
    )

    private val ministerConfig = ToonCharConfig(
        skin = colSkinDark,
        hair = colMinHair,
        shirt = colToonWhite,
        accent = colTerracotta,
        glasses = false,
        mustache = true,
        hairStyle = "combed",
        cap = colToonWhite,
        phase = 1.4f
    )

    fun preloadBitmaps(project: NewsProject) {
        StudioBackdropPreset.entries.filter { !it.isToonVectorScene }.forEach { preset ->
            getPresetBitmap(preset)
        }
        project.shots.forEach { shot ->
            shot.customImageUri?.let { getCustomBitmap(it) }
            shot.customVideoUri?.let { getVideoFrames(it) }
        }
    }

    private fun getPresetBitmap(preset: StudioBackdropPreset): Bitmap? {
        if (preset.isToonVectorScene) return null
        presetBitmaps[preset]?.let { return it }
        return runCatching {
            val opts = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeResource(context.resources, preset.drawableRes, opts)?.also {
                presetBitmaps[preset] = it
            }
        }.getOrNull()
    }

    private fun getCustomBitmap(uriString: String): Bitmap? {
        customBitmaps[uriString]?.let { return it }
        return runCatching {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val opts = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeStream(stream, null, opts)?.also { decoded ->
                    customBitmaps[uriString] = decoded
                }
            }
        }.getOrNull()
    }

    private fun getVideoFrames(uriString: String): List<Bitmap> {
        customVideoFrameCache[uriString]?.let { return it }
        val frames = mutableListOf<Bitmap>()
        runCatching {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, Uri.parse(uriString))
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 4000L
            val steps = 8
            for (i in 0 until steps) {
                val timeUs = ((durationMs * 1000L) * i) / steps
                val bmp = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (bmp != null) {
                    val scaled = Bitmap.createScaledBitmap(bmp, 960, 540, true)
                    frames.add(scaled)
                }
            }
            retriever.release()
        }
        if (frames.isNotEmpty()) {
            customVideoFrameCache[uriString] = frames
        }
        return frames
    }

    fun resolveShotAtTime(project: NewsProject, globalTimeSec: Float): Triple<Int, NewsShot, Float> {
        val shots = project.shots
        if (shots.isEmpty()) {
            val fallback = NewsShot(
                headline = "সংবাদ প্রজেক্ট প্রস্তুত",
                subheadline = "নতুন শট যোগ করুন বা ফাইল ইমপোর্ট করুন"
            )
            return Triple(0, fallback, 0f)
        }
        var accumulated = 0f
        for (i in shots.indices) {
            val shot = shots[i]
            val dur = shot.durationSeconds.coerceAtLeast(1f)
            if (globalTimeSec <= accumulated + dur || i == shots.lastIndex) {
                val localSec = (globalTimeSec - accumulated).coerceIn(0f, dur)
                return Triple(i, shot, localSec)
            }
            accumulated += dur
        }
        return Triple(0, shots.first(), 0f)
    }

    /**
     * Hit-tests normalized coordinates (0..1, 0..1) on the 1920x1080 virtual canvas.
     */
    fun hitTestCanvasElement(
        normX: Float,
        normY: Float,
        project: NewsProject,
        shot: NewsShot
    ): Pair<CanvasElementType, Int?> {
        val vx = normX.coerceIn(0f, 1f) * 1920f
        val vy = normY.coerceIn(0f, 1f) * 1080f

        // 1. Custom Floating Text Layers
        shot.customTextLayers.forEachIndexed { idx, layer ->
            val lx = layer.xPercent * 1920f
            val ly = layer.yPercent * 1080f
            if (abs(vx - lx) <= 380f && abs(vy - ly) <= 68f) {
                return CanvasElementType.CUSTOM_TEXT_LAYER to idx
            }
        }

        // 2. Bottom Ticker
        if (project.showTicker && vy >= 995f) {
            return CanvasElementType.NEWS_TICKER to null
        }

        // 3. Caption Pill or Lower-Third Headline (y = 700..1000)
        if (shot.showHeadline && vy in 740f..990f && vx in 140f..1780f) {
            return CanvasElementType.HEADLINE to null
        }

        // 4. Stage Banner (when Toon Stage is active, y = 210..445)
        if (shot.mediaType == ShotMediaType.PRESET_SCENE &&
            shot.backdropPreset == StudioBackdropPreset.TOON_STAGE &&
            shot.showStageBanner &&
            vy in 210f..445f && vx in 200f..1720f
        ) {
            return CanvasElementType.STAGE_BANNER_TEXT to null
        }

        // 5. Animated Character Rig (Host or Minister in center/stage area)
        if (shot.toonWho != ToonCharacterWho.NONE && vy in 280f..890f && vx in 420f..1520f) {
            return CanvasElementType.TOON_CHARACTER to null
        }

        // 6. Top Broadcast Bug
        if (shot.showLiveBug && vy in 30f..135f && vx in 50f..780f) {
            return CanvasElementType.LIVE_CHANNEL_BUG to null
        }

        return CanvasElementType.BACKDROP_MEDIA to null
    }

    /**
     * Renders a complete 1920x1080 virtual broadcast/cartoon frame onto [canvas].
     */
    fun renderFrame(
        canvas: Canvas,
        width: Float,
        height: Float,
        project: NewsProject,
        globalTimeSec: Float,
        selectedCanvasElement: CanvasElementType? = null,
        selectedCustomTextIndex: Int? = null,
        drawSelectionHighlight: Boolean = false
    ) {
        if (width <= 0f || height <= 0f) return

        val virtualW = 1920f
        val virtualH = 1080f
        val scaleX = width / virtualW
        val scaleY = height / virtualH

        canvas.save()
        canvas.scale(scaleX, scaleY)

        val (shotIndex, activeShot, localSec) = resolveShotAtTime(project, globalTimeSec)
        val shotDuration = activeShot.durationSeconds.coerceAtLeast(1f)
        val shotProgress = (localSec / shotDuration).coerceIn(0f, 1f)

        // Apply Camera / Keyframe Motion Transform to Scene + Characters
        val ease = smoothStep(shotProgress)
        val intensity = activeShot.motionIntensity.coerceIn(0.4f, 2.0f)
        val kf = activeShot.keyframes
        val camScale: Float
        val camTx: Float
        val camTy: Float

        if (activeShot.motionPreset == MotionPreset.STATIC_STUDIO) {
            camScale = lerp(kf.startScale, kf.endScale, ease)
            camTx = lerp(kf.startOffsetX, kf.endOffsetX, ease)
            camTy = lerp(kf.startOffsetY, kf.endOffsetY, ease)
        } else {
            val s0 = kf.startScale
            val s1 = kf.endScale
            camScale = lerp(s0, 1f + (s1 - 1f) * intensity, ease).coerceIn(0.8f, 2.2f)
            camTx = lerp(kf.startOffsetX, kf.endOffsetX, ease) * intensity
            camTy = lerp(kf.startOffsetY, kf.endOffsetY, ease) * intensity
        }

        canvas.save()
        if (camScale != 1.0f || camTx != 0f || camTy != 0f) {
            canvas.translate(virtualW / 2f + camTx, virtualH / 2f + camTy)
            canvas.scale(camScale, camScale)
            canvas.translate(-virtualW / 2f, -virtualH / 2f)
        }

        // 1. Draw Background Scene (Bangla Toon 2D Vector Scene OR Gallery Photo/Video/Studio)
        drawSceneBackdrop(canvas, virtualW, virtualH, activeShot, globalTimeSec, shotProgress)

        // 2. Draw Animated 2D Character Rig (Host / Minister)
        if (activeShot.toonWho != ToonCharacterWho.NONE) {
            val blink = (globalTimeSec % 4.2f) < 0.11f
            val talk = if (localSec > 0.15f && localSec < (shotDuration - 0.18f)) {
                (0.45f + 0.45f * abs(sin(globalTimeSec * 15f)))
            } else {
                0f
            }
            when (activeShot.toonWho) {
                ToonCharacterWho.HOST -> {
                    drawToonCharacter(
                        canvas = canvas,
                        cfg = hostConfig,
                        x = virtualW * 0.50f,
                        y = 900f,
                        scale = 1.02f,
                        t = globalTimeSec,
                        blink = blink,
                        talk = talk,
                        pose = activeShot.toonPose
                    )
                }
                ToonCharacterWho.MINISTER -> {
                    drawToonCharacter(
                        canvas = canvas,
                        cfg = hostConfig,
                        x = virtualW * 0.30f,
                        y = 900f,
                        scale = 0.98f,
                        t = globalTimeSec,
                        blink = blink,
                        talk = 0f,
                        pose = ToonCharacterPose.TALK
                    )
                    drawToonCharacter(
                        canvas = canvas,
                        cfg = ministerConfig,
                        x = virtualW * 0.68f,
                        y = 880f,
                        scale = 1.05f,
                        t = globalTimeSec,
                        blink = blink,
                        talk = talk,
                        pose = activeShot.toonPose
                    )
                }
                ToonCharacterWho.MINISTER_SOLO -> {
                    drawToonCharacter(
                        canvas = canvas,
                        cfg = ministerConfig,
                        x = virtualW * 0.50f,
                        y = 890f,
                        scale = 1.05f,
                        t = globalTimeSec,
                        blink = blink,
                        talk = talk,
                        pose = activeShot.toonPose
                    )
                }
                ToonCharacterWho.NONE -> Unit
            }
        }

        canvas.restore()

        // 3. Optional Broadcast Atmosphere / Breaking Frame
        if (activeShot.motionGraphicOverlay == MotionGraphicOverlay.BREAKING_PERIMETER_GLOW) {
            val pulse = ((sin(globalTimeSec * 7.0) * 0.5 + 0.5) * 180 + 55).toInt().coerceIn(50, 240)
            strokePaint.color = Color.argb(pulse, 225, 29, 72)
            strokePaint.strokeWidth = 14f
            rectF.set(14f, 14f, virtualW - 14f, virtualH - 14f)
            canvas.drawRoundRect(rectF, 20f, 20f, strokePaint)
        }

        // 4. Optional Top Broadcast Bug
        if (activeShot.showLiveBug || activeShot.motionGraphicOverlay == MotionGraphicOverlay.FULL_BROADCAST_PACK) {
            drawTopBroadcastBar(
                canvas = canvas,
                virtualW = virtualW,
                project = project,
                activeShot = activeShot,
                shotIndex = shotIndex,
                totalShots = project.shots.size.coerceAtLeast(1),
                globalTimeSec = globalTimeSec
            )
        }

        // 5. Caption Pill (Bangla Toon Style with Marigold Highlight Word) OR TV Lower-Third Banner
        if (activeShot.motionGraphicOverlay != MotionGraphicOverlay.CINEMATIC_CLEAN && activeShot.showHeadline) {
            if (activeShot.lowerThirdStyle == LowerThirdStyle.TOON_CAPTION_PILL &&
                activeShot.motionGraphicOverlay != MotionGraphicOverlay.FULL_BROADCAST_PACK
            ) {
                if (localSec >= 0.15f && localSec <= shotDuration - 0.10f) {
                    drawToonCaptionPill(
                        canvas = canvas,
                        virtualW = virtualW,
                        text = activeShot.headline,
                        hiWord = activeShot.highlightWord
                    )
                }
            } else {
                drawTvLowerThirdGraphics(
                    canvas = canvas,
                    virtualW = virtualW,
                    shot = activeShot,
                    localSec = localSec,
                    shotDuration = shotDuration,
                    shotProgress = shotProgress
                )
            }
        }

        // 6. Custom Added Floating Text Layers
        drawCustomTextLayers(canvas, virtualW, virtualH, activeShot)

        // 7. Optional Bottom Scrolling News Ticker
        if (project.showTicker) {
            drawScrollingTicker(
                canvas = canvas,
                virtualW = virtualW,
                virtualH = virtualH,
                project = project,
                globalTimeSec = globalTimeSec
            )
        }

        // 8. Interactive Selection Highlight Box on Live Preview Canvas
        if (drawSelectionHighlight && selectedCanvasElement != null) {
            drawElementSelectionBox(
                canvas = canvas,
                virtualW = virtualW,
                virtualH = virtualH,
                shot = activeShot,
                element = selectedCanvasElement,
                customTextIdx = selectedCustomTextIndex
            )
        }

        canvas.restore()
    }

    private fun drawSceneBackdrop(
        canvas: Canvas,
        virtualW: Float,
        virtualH: Float,
        shot: NewsShot,
        globalTimeSec: Float,
        progress: Float
    ) {
        if (shot.mediaType == ShotMediaType.PRESET_SCENE && shot.backdropPreset.isToonVectorScene) {
            when (shot.backdropPreset) {
                StudioBackdropPreset.TOON_STAGE -> drawToonStageBg(canvas, virtualW, virtualH, shot)
                StudioBackdropPreset.TOON_CANAL -> drawToonCanalBg(canvas, virtualW, virtualH, globalTimeSec)
                StudioBackdropPreset.TOON_HOUSE -> drawToonHouseBg(canvas, virtualW, virtualH)
                else -> drawToonCanalBg(canvas, virtualW, virtualH, globalTimeSec)
            }
            return
        }

        // Photo or Video Backdrop
        fillPaint.shader = null
        fillPaint.color = shot.backdropPreset.fallbackColorHex.toInt() or -0x1000000
        canvas.drawRect(0f, 0f, virtualW, virtualH, fillPaint)

        val bitmap: Bitmap? = when (shot.mediaType) {
            ShotMediaType.GALLERY_VIDEO -> {
                val frames = shot.customVideoUri?.let { getVideoFrames(it) }.orEmpty()
                if (frames.isNotEmpty()) {
                    val idx = ((progress * frames.size).toInt()).coerceIn(0, frames.lastIndex)
                    frames[idx]
                } else {
                    getPresetBitmap(shot.backdropPreset)
                }
            }
            ShotMediaType.GALLERY_PHOTO -> {
                shot.customImageUri?.let { getCustomBitmap(it) }
                    ?: getPresetBitmap(shot.backdropPreset)
            }
            ShotMediaType.PRESET_SCENE -> {
                shot.customImageUri?.let { getCustomBitmap(it) }
                    ?: getPresetBitmap(shot.backdropPreset)
            }
        }

        if (bitmap != null && !bitmap.isRecycled) {
            val bw = bitmap.width.toFloat().coerceAtLeast(1f)
            val bh = bitmap.height.toFloat().coerceAtLeast(1f)
            val scale = maxOf(virtualW / bw, virtualH / bh)
            matrix.reset()
            matrix.postTranslate(-bw / 2f, -bh / 2f)
            matrix.postScale(scale, scale)
            matrix.postTranslate(virtualW / 2f, virtualH / 2f)
            canvas.drawBitmap(bitmap, matrix, bitmapPaint)
        }
    }

    /**
     * Exact 1:1 implementation of `bgStage(t)` from user's Bangla Toon code.
     */
    private fun drawToonStageBg(
        canvas: Canvas,
        virtualW: Float,
        virtualH: Float,
        shot: NewsShot
    ) {
        fillPaint.shader = null
        fillPaint.color = colToonWhite
        canvas.drawRect(0f, 0f, virtualW, virtualH, fillPaint)

        // Back teal pillars (alpha 0.35)
        fillPaint.color = colTeal
        fillPaint.alpha = 89
        rectF.set(80f, 380f, 480f, 900f)
        canvas.drawRoundRect(rectF, 20f, 20f, fillPaint)
        rectF.set(1440f, 380f, 1840f, 900f)
        canvas.drawRoundRect(rectF, 20f, 20f, fillPaint)
        fillPaint.alpha = 255

        // Stage Banner
        if (shot.showStageBanner && shot.stageBannerText.isNotBlank()) {
            fillPaint.color = colTerracotta
            rectF.set(200f, 220f, 1720f, 440f)
            canvas.drawRoundRect(rectF, 24f, 24f, fillPaint)

            strokePaint.color = colInk
            strokePaint.strokeWidth = 8f
            canvas.drawRoundRect(rectF, 24f, 24f, strokePaint)

            textPaint.typeface = banglaBoldTypeface
            textPaint.color = colToonWhite
            textPaint.textSize = if (shot.stageBannerText.length > 26) 68f else 84f
            val textW = textPaint.measureText(shot.stageBannerText)
            canvas.drawText(shot.stageBannerText, (virtualW - textW) / 2f, 358f, textPaint)
        }

        // Front Jute Stage Floor
        fillPaint.color = colJute
        canvas.drawRect(0f, 860f, virtualW, virtualH, fillPaint)
        strokePaint.color = colInk
        strokePaint.strokeWidth = 10f
        canvas.drawLine(0f, 860f, virtualW, 860f, strokePaint)
    }

    /**
     * Exact 1:1 implementation of `bgCanal(t)` from user's Bangla Toon code.
     */
    private fun drawToonCanalBg(
        canvas: Canvas,
        virtualW: Float,
        virtualH: Float,
        t: Float
    ) {
        fillPaint.shader = null
        fillPaint.color = colToonWhite
        canvas.drawRect(0f, 0f, virtualW, virtualH, fillPaint)

        // Sun in sky
        fillPaint.color = colMarigold
        canvas.drawCircle(1550f, 200f, 90f, fillPaint)

        // Back trees (alpha 0.4)
        fillPaint.color = colGreen
        fillPaint.alpha = 102
        for (i in 0 until 5) {
            canvas.drawCircle(120f + i * 180f, 420f, 70f, fillPaint)
        }
        fillPaint.alpha = 255

        // Middle green bank
        fillPaint.color = colGreen
        canvas.drawRect(0f, 520f, virtualW, 720f, fillPaint)

        // Canal water curve
        path.reset()
        path.moveTo(0f, 700f)
        path.quadTo(960f, 640f, 1920f, 700f)
        path.lineTo(1920f, 860f)
        path.lineTo(0f, 860f)
        path.close()
        fillPaint.color = colWater
        canvas.drawPath(path, fillPaint)

        // Animated water waves
        strokePaint.color = Color.argb(128, 255, 255, 255)
        strokePaint.strokeWidth = 4f
        for (i in 0 until 6) {
            val y = 740f + i * 20f
            val off = sin(t * 1.2f + i) * 20f
            path.reset()
            path.moveTo(100f + off, y)
            path.quadTo(300f + off, y - 10f, 500f + off, y)
            canvas.drawPath(path, strokePaint)
        }

        // Front dark ground
        fillPaint.color = colInk
        canvas.drawRect(0f, 860f, virtualW, virtualH, fillPaint)
    }

    /**
     * Exact 1:1 implementation of `bgHouse(t)` from user's Bangla Toon code.
     */
    private fun drawToonHouseBg(
        canvas: Canvas,
        virtualW: Float,
        virtualH: Float
    ) {
        fillPaint.shader = null
        fillPaint.color = colToonWhite
        canvas.drawRect(0f, 0f, virtualW, virtualH, fillPaint)

        // Sun
        fillPaint.color = colMarigold
        canvas.drawCircle(300f, 180f, 80f, fillPaint)

        // Paddy field
        fillPaint.color = colGreen
        canvas.drawRect(0f, 600f, virtualW, 920f, fillPaint)

        strokePaint.color = Color.argb(38, 0, 0, 0)
        strokePaint.strokeWidth = 4f
        for (i in 0 until 20) {
            val x = 60f + i * 95f
            canvas.drawLine(x, 620f, x, 900f, strokePaint)
        }

        // House wall (terracotta)
        fillPaint.color = colTerracotta
        rectF.set(1280f, 480f, 1700f, 780f)
        canvas.drawRoundRect(rectF, 20f, 20f, fillPaint)
        strokePaint.color = colInk
        strokePaint.strokeWidth = 8f
        canvas.drawRoundRect(rectF, 20f, 20f, strokePaint)

        // House roof (jute triangle)
        path.reset()
        path.moveTo(1260f, 480f)
        path.lineTo(1490f, 340f)
        path.lineTo(1720f, 480f)
        path.close()
        fillPaint.color = colJute
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, strokePaint)

        // Marigold windows
        fillPaint.color = colMarigold
        strokePaint.strokeWidth = 4f
        rectF.set(1340f, 560f, 1430f, 650f)
        canvas.drawRoundRect(rectF, 10f, 10f, fillPaint)
        canvas.drawRoundRect(rectF, 10f, 10f, strokePaint)

        rectF.set(1550f, 560f, 1640f, 650f)
        canvas.drawRoundRect(rectF, 10f, 10f, fillPaint)
        canvas.drawRoundRect(rectF, 10f, 10f, strokePaint)

        // Front dark ground
        fillPaint.color = colInk
        canvas.drawRect(0f, 900f, virtualW, virtualH, fillPaint)
    }

    /**
     * Exact 1:1 implementation of `drawChar(cfg, x, y, s, t, blink, talk, pose)` from user's Bangla Toon code!
     */
    private fun drawToonCharacter(
        canvas: Canvas,
        cfg: ToonCharConfig,
        x: Float,
        y: Float,
        scale: Float,
        t: Float,
        blink: Boolean,
        talk: Float,
        pose: ToonCharacterPose
    ) {
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(scale, scale)

        val breath = 1f + 0.012f * sin(t * 2.1f + cfg.phase)

        // Legs
        for (legX in listOf(-50f, 50f)) {
            canvas.save()
            canvas.translate(legX, 0f)
            fillPaint.color = colInk
            strokePaint.color = colInk
            strokePaint.strokeWidth = 8f
            rectF.set(-26f, -15f, 26f, 100f)
            canvas.drawRoundRect(rectF, 24f, 24f, fillPaint)
            canvas.drawRoundRect(rectF, 24f, 24f, strokePaint)
            canvas.restore()
        }

        // Torso
        canvas.save()
        canvas.translate(0f, -160f)
        canvas.scale(1f, breath)
        fillPaint.color = cfg.shirt
        strokePaint.color = colInk
        strokePaint.strokeWidth = 8f
        rectF.set(-105f, -165f, 105f, 130f)
        canvas.drawRoundRect(rectF, 44f, 44f, fillPaint)
        canvas.drawRoundRect(rectF, 44f, 44f, strokePaint)

        // Collar / Placket Accent
        if (cfg.accent != null) {
            fillPaint.color = cfg.accent
            strokePaint.strokeWidth = 4f
            rectF.set(-34f, -155f, 34f, -45f)
            canvas.drawRoundRect(rectF, 16f, 16f, fillPaint)
            canvas.drawRoundRect(rectF, 16f, 16f, strokePaint)
        }

        // 3 Buttons
        fillPaint.color = colInk
        for (i in 0 until 3) {
            canvas.drawCircle(0f, -130f + i * 36f, 5f, fillPaint)
        }
        canvas.restore()

        // Neck
        fillPaint.color = cfg.skin
        strokePaint.color = colInk
        strokePaint.strokeWidth = 8f
        rectF.set(-28f, -355f, 28f, -300f)
        canvas.drawRoundRect(rectF, 16f, 16f, fillPaint)
        canvas.drawRoundRect(rectF, 16f, 16f, strokePaint)

        // Left Arm
        canvas.save()
        canvas.translate(-115f, -295f)
        fillPaint.color = cfg.skin
        strokePaint.strokeWidth = 8f
        rectF.set(-26f, 0f, 26f, 195f)
        canvas.drawRoundRect(rectF, 24f, 24f, fillPaint)
        canvas.drawRoundRect(rectF, 24f, 24f, strokePaint)
        canvas.drawCircle(0f, 200f, 32f, fillPaint)
        canvas.drawCircle(0f, 200f, 32f, strokePaint)
        canvas.restore()

        // Right Arm (with Point / Talk pose rotation)
        val raiseRad = if (pose == ToonCharacterPose.POINT) -0.9f else 0f
        val armAngleDeg = Math.toDegrees((raiseRad + sin(t * 1.5f) * 0.1f * talk).toDouble()).toFloat()
        canvas.save()
        canvas.translate(115f, -295f)
        canvas.rotate(armAngleDeg)
        fillPaint.color = cfg.skin
        strokePaint.strokeWidth = 8f
        rectF.set(-26f, 0f, 26f, 195f)
        canvas.drawRoundRect(rectF, 24f, 24f, fillPaint)
        canvas.drawRoundRect(rectF, 24f, 24f, strokePaint)
        canvas.drawCircle(0f, 200f, 32f, fillPaint)
        canvas.drawCircle(0f, 200f, 32f, strokePaint)
        canvas.restore()

        // Head & Ears
        val hy = -460f
        fillPaint.color = cfg.skin
        strokePaint.strokeWidth = 8f
        rectF.set(-102f, hy - 116f, 102f, hy + 116f)
        canvas.drawOval(rectF, fillPaint)
        canvas.drawOval(rectF, strokePaint)

        canvas.drawCircle(-105f, hy + 10f, 22f, fillPaint)
        canvas.drawCircle(-105f, hy + 10f, 22f, strokePaint)
        canvas.drawCircle(105f, hy + 10f, 22f, fillPaint)
        canvas.drawCircle(105f, hy + 10f, 22f, strokePaint)

        // Hair
        fillPaint.color = cfg.hair
        strokePaint.strokeWidth = 6f
        path.reset()
        if (cfg.hairStyle == "side") {
            path.moveTo(-98f, hy - 28f)
            path.quadTo(-108f, hy - 128f, 0f, hy - 132f)
            path.quadTo(108f, hy - 128f, 98f, hy - 28f)
            path.quadTo(55f, hy - 78f, 0f, hy - 82f)
            path.quadTo(-55f, hy - 78f, -98f, hy - 28f)
            path.close()
        } else {
            path.moveTo(-100f, hy - 20f)
            path.quadTo(-115f, hy - 135f, 10f, hy - 138f)
            path.quadTo(120f, hy - 130f, 102f, hy - 20f)
            path.quadTo(60f, hy - 100f, -10f, hy - 90f)
            path.quadTo(-70f, hy - 85f, -100f, hy - 20f)
            path.close()
        }
        canvas.drawPath(path, fillPaint)
        canvas.drawPath(path, strokePaint)

        // Cap (Minister)
        if (cfg.cap != null) {
            fillPaint.color = cfg.cap
            strokePaint.strokeWidth = 6f
            rectF.set(-105f, hy - 150f, 105f, hy - 66f)
            canvas.drawArc(rectF, 180f, 180f, true, fillPaint)
            canvas.drawArc(rectF, 180f, 180f, true, strokePaint)

            rectF.set(-95f, hy - 112f, 95f, hy - 86f)
            canvas.drawRoundRect(rectF, 12f, 12f, fillPaint)
            canvas.drawRoundRect(rectF, 12f, 12f, strokePaint)
        }

        // Eyebrows
        strokePaint.color = colInk
        strokePaint.strokeWidth = 7f
        path.reset()
        path.moveTo(-66f, hy - 38f)
        path.quadTo(-44f, hy - 52f, -22f, hy - 40f)
        canvas.drawPath(path, strokePaint)

        path.reset()
        path.moveTo(22f, hy - 40f)
        path.quadTo(44f, hy - 52f, 66f, hy - 38f)
        canvas.drawPath(path, strokePaint)

        // Eyes (blinking)
        val eyeH = if (blink) 2f else 14f
        fillPaint.color = colInk
        rectF.set(-59f, hy - 4f - eyeH, -29f, hy - 4f + eyeH)
        canvas.drawOval(rectF, fillPaint)
        rectF.set(29f, hy - 4f - eyeH, 59f, hy - 4f + eyeH)
        canvas.drawOval(rectF, fillPaint)

        // Nose
        strokePaint.strokeWidth = 6f
        path.reset()
        path.moveTo(0f, hy + 6f)
        path.quadTo(-7f, hy + 24f, 0f, hy + 28f)
        canvas.drawPath(path, strokePaint)

        // Mustache (Minister)
        if (cfg.mustache) {
            fillPaint.color = cfg.hair
            strokePaint.strokeWidth = 5f
            rectF.set(-44f, hy + 30f, 44f, hy + 54f)
            canvas.drawArc(rectF, 180f, 180f, true, fillPaint)
            canvas.drawArc(rectF, 180f, 180f, true, strokePaint)
        }

        // Animated Talking Mouth
        val mo = if (talk > 0.15f) 12f + talk * 12f else 3f
        fillPaint.color = colToonRed
        strokePaint.strokeWidth = 7f
        rectF.set(-34f, hy + 62f - mo, 34f, hy + 62f + mo)
        canvas.drawOval(rectF, fillPaint)
        canvas.drawOval(rectF, strokePaint)

        // Cheeks
        fillPaint.color = Color.argb(56, 179, 32, 42)
        canvas.drawCircle(-62f, hy + 44f, 14f, fillPaint)
        canvas.drawCircle(62f, hy + 44f, 14f, fillPaint)

        // Glasses (Host)
        if (cfg.glasses) {
            strokePaint.color = colInk
            strokePaint.strokeWidth = 5f
            rectF.set(-68f, hy - 24f, -20f, hy + 16f)
            canvas.drawOval(rectF, strokePaint)
            rectF.set(20f, hy - 24f, 68f, hy + 16f)
            canvas.drawOval(rectF, strokePaint)
            canvas.drawLine(-20f, hy - 4f, 20f, hy - 4f, strokePaint)
        }

        canvas.restore()
    }

    /**
     * Exact 1:1 implementation of `drawCaption(text, hiWord)` from user's Bangla Toon code!
     * Draws the rounded dark pill at y = 880 with white text and marigold highlighted word.
     */
    private fun drawToonCaptionPill(
        canvas: Canvas,
        virtualW: Float,
        text: String,
        hiWord: String
    ) {
        if (text.isBlank()) return
        textPaint.typeface = banglaBoldTypeface
        textPaint.textSize = when {
            text.length > 52 -> 42f
            text.length > 38 -> 48f
            else -> 54f
        }

        val padX = 54f
        val h = 94f
        val fullTextW = textPaint.measureText(text)
        val pillW = (fullTextW + padX * 2f).coerceIn(420f, 1740f)
        val x = (virtualW - pillW) / 2f
        val y = 880f

        fillPaint.color = Color.argb(232, 27, 26, 34)
        rectF.set(x, y, x + pillW, y + h)
        canvas.drawRoundRect(rectF, h / 2f, h / 2f, fillPaint)

        val baselineY = y + h / 2f + 18f
        val cleanHi = hiWord.trim()

        if (cleanHi.isEmpty() || !text.contains(cleanHi)) {
            textPaint.color = Color.WHITE
            val fitted = ellipsizeText(text, textPaint, pillW - 60f)
            val fw = textPaint.measureText(fitted)
            canvas.drawText(fitted, (virtualW - fw) / 2f, baselineY, textPaint)
        } else {
            val parts = text.split(cleanHi)
            var cursorX = (virtualW - fullTextW) / 2f
            for (i in parts.indices) {
                textPaint.color = Color.WHITE
                canvas.drawText(parts[i], cursorX, baselineY, textPaint)
                cursorX += textPaint.measureText(parts[i])
                if (i < parts.lastIndex) {
                    textPaint.color = colMarigold
                    canvas.drawText(cleanHi, cursorX, baselineY, textPaint)
                    cursorX += textPaint.measureText(cleanHi)
                }
            }
        }
    }

    private fun drawTopBroadcastBar(
        canvas: Canvas,
        virtualW: Float,
        project: NewsProject,
        activeShot: NewsShot,
        shotIndex: Int,
        totalShots: Int,
        globalTimeSec: Float
    ) {
        val topY = 44f
        val barH = 60f
        var cursorX = 64f

        if (activeShot.showLiveBug) {
            fillPaint.color = Color.parseColor("#E11D48")
            rectF.set(cursorX, topY, cursorX + 160f, topY + barH)
            canvas.drawRoundRect(rectF, 12f, 12f, fillPaint)

            val pulseAlpha = ((sin(globalTimeSec * 6.0) * 0.4 + 0.6) * 255).toInt().coerceIn(90, 255)
            fillPaint.color = Color.argb(pulseAlpha, 255, 255, 255)
            canvas.drawCircle(cursorX + 34f, topY + barH / 2f, 9f, fillPaint)

            textPaint.typeface = studioMonoTypeface
            textPaint.textSize = 28f
            textPaint.color = Color.WHITE
            canvas.drawText("LIVE", cursorX + 58f, topY + 40f, textPaint)
            cursorX += 174f

            val channelText = project.channelName.ifBlank { "বাংলা নিউজ ২৪" }
            textPaint.typeface = banglaBoldTypeface
            textPaint.textSize = 30f
            val channelTextW = textPaint.measureText(channelText)
            val pillW = channelTextW + 56f

            fillPaint.color = Color.argb(225, 15, 23, 42)
            rectF.set(cursorX, topY, cursorX + pillW, topY + barH)
            canvas.drawRoundRect(rectF, 12f, 12f, fillPaint)

            textPaint.color = Color.parseColor("#F8FAFC")
            canvas.drawText(channelText, cursorX + 28f, topY + 41f, textPaint)
        }

        val rightEdge = virtualW - 64f
        val locationText = "SHOT ${shotIndex + 1}/$totalShots"
        textPaint.typeface = studioMonoTypeface
        textPaint.textSize = 24f
        val locW = textPaint.measureText(locationText)
        val rightBoxW = locW + 48f

        fillPaint.color = Color.argb(200, 15, 23, 42)
        rectF.set(rightEdge - rightBoxW, topY, rightEdge, topY + barH)
        canvas.drawRoundRect(rectF, 12f, 12f, fillPaint)

        textPaint.color = Color.parseColor("#F2A900")
        canvas.drawText(locationText, rightEdge - rightBoxW + 24f, topY + 39f, textPaint)
    }

    private fun drawTvLowerThirdGraphics(
        canvas: Canvas,
        virtualW: Float,
        shot: NewsShot,
        localSec: Float,
        shotDuration: Float,
        shotProgress: Float
    ) {
        val style = shot.lowerThirdStyle
        val primaryColor = style.primaryHex.toInt() or -0x1000000
        val secondaryColor = style.secondaryHex.toInt() or -0x1000000
        val badgeColor = style.badgeHex.toInt() or -0x1000000

        val enterFactor = smoothStep((localSec / 0.40f).coerceIn(0f, 1f))
        val exitFactor = if (shotDuration - localSec < 0.28f) {
            smoothStep(((shotDuration - localSec) / 0.28f).coerceIn(0f, 1f))
        } else {
            1f
        }
        val visibilityFactor = min(enterFactor, exitFactor)
        val slideOffsetY = (1f - visibilityFactor) * 75f

        val leftX = 72f
        val bannerW = virtualW - 144f
        val baseTopY = 710f + slideOffsetY

        canvas.save()

        if (shot.showCategoryBadge && shot.categoryTag.isNotBlank()) {
            val catText = shot.categoryTag
            textPaint.typeface = banglaBoldTypeface
            textPaint.textSize = 32f
            val catW = (textPaint.measureText(catText) + 72f).coerceAtLeast(240f)
            val catH = 56f

            path.reset()
            path.moveTo(leftX, baseTopY)
            path.lineTo(leftX + catW, baseTopY)
            path.lineTo(leftX + catW - 22f, baseTopY + catH)
            path.lineTo(leftX, baseTopY + catH)
            path.close()

            fillPaint.color = badgeColor
            canvas.drawPath(path, fillPaint)

            textPaint.color = Color.parseColor("#090D16")
            canvas.drawText(catText, leftX + 28f, baseTopY + 39f, textPaint)
        }

        val headlineTopY = baseTopY + 56f
        val headlineH = 116f

        if (shot.showHeadline && shot.headline.isNotBlank()) {
            fillPaint.shader = LinearGradient(
                leftX, headlineTopY, leftX + bannerW, headlineTopY,
                intArrayOf(primaryColor, darkenColor(primaryColor, 0.72f)),
                null,
                Shader.TileMode.CLAMP
            )
            rectF.set(leftX, headlineTopY, leftX + bannerW, headlineTopY + headlineH)
            canvas.drawRoundRect(rectF, 10f, 10f, fillPaint)
            fillPaint.shader = null

            fillPaint.color = badgeColor
            rectF.set(leftX, headlineTopY, leftX + 14f, headlineTopY + headlineH)
            canvas.drawRect(rectF, fillPaint)

            val rawHeadline = shot.headline
            val displayedHeadline = if (shot.textAnimation == TextAnimationStyle.TYPEWRITER_BANGLA) {
                val charProgress = ((localSec - 0.12f) / 1.1f).coerceIn(0f, 1f)
                val count = (rawHeadline.length * charProgress).toInt().coerceIn(1, rawHeadline.length)
                rawHeadline.substring(0, count)
            } else {
                rawHeadline
            }

            textPaint.typeface = banglaBoldTypeface
            textPaint.color = Color.WHITE
            textPaint.textSize = when {
                rawHeadline.length > 60 -> 40f
                rawHeadline.length > 44 -> 46f
                else -> 52f
            }
            val fittedHeadline = ellipsizeText(displayedHeadline, textPaint, bannerW - 80f)
            canvas.drawText(fittedHeadline, leftX + 40f, headlineTopY + 76f, textPaint)
        }

        val subTopY = headlineTopY + headlineH
        val subH = 72f
        if (shot.showSubheadline && shot.subheadline.isNotBlank()) {
            fillPaint.color = Color.argb(
                240,
                Color.red(secondaryColor),
                Color.green(secondaryColor),
                Color.blue(secondaryColor)
            )
            rectF.set(leftX, subTopY, leftX + bannerW, subTopY + subH)
            canvas.drawRoundRect(rectF, 10f, 10f, fillPaint)

            textPaint.typeface = banglaRegularTypeface
            textPaint.textSize = 34f
            textPaint.color = Color.parseColor("#E2E8F0")
            val fittedSub = ellipsizeText(shot.subheadline, textPaint, bannerW - 80f)
            canvas.drawText(fittedSub, leftX + 40f, subTopY + 47f, textPaint)

            fillPaint.color = badgeColor
            canvas.drawRect(
                leftX,
                subTopY + subH - 6f,
                leftX + bannerW * shotProgress,
                subTopY + subH,
                fillPaint
            )
        }

        canvas.restore()
    }

    private fun drawCustomTextLayers(
        canvas: Canvas,
        virtualW: Float,
        virtualH: Float,
        shot: NewsShot
    ) {
        shot.customTextLayers.forEach { layer ->
            if (layer.text.isBlank()) return@forEach
            val scaledFont = (layer.fontSizePx * 1.45f).coerceIn(28f, 92f)
            textPaint.typeface = banglaBoldTypeface
            textPaint.textSize = scaledFont
            val textW = textPaint.measureText(layer.text)
            val cx = (layer.xPercent.coerceIn(0.1f, 0.9f) * virtualW)
            val cy = (layer.yPercent.coerceIn(0.1f, 0.85f) * virtualH)
            val boxW = textW + 64f
            val boxH = scaledFont + 36f

            fillPaint.color = layer.bgColorHex.toInt()
            rectF.set(cx - boxW / 2f, cy - boxH / 2f, cx + boxW / 2f, cy + boxH / 2f)
            canvas.drawRoundRect(rectF, 16f, 16f, fillPaint)

            strokePaint.color = Color.WHITE
            strokePaint.strokeWidth = 2.5f
            canvas.drawRoundRect(rectF, 16f, 16f, strokePaint)

            textPaint.color = layer.textColorHex.toInt() or -0x1000000
            canvas.drawText(layer.text, cx - textW / 2f, cy + scaledFont * 0.34f, textPaint)
        }
    }

    private fun drawScrollingTicker(
        canvas: Canvas,
        virtualW: Float,
        virtualH: Float,
        project: NewsProject,
        globalTimeSec: Float
    ) {
        val tickerH = 76f
        val topY = virtualH - tickerH

        fillPaint.color = Color.parseColor("#060911")
        canvas.drawRect(0f, topY, virtualW, virtualH, fillPaint)

        fillPaint.color = Color.parseColor("#E11D48")
        canvas.drawRect(0f, topY, virtualW, topY + 4f, fillPaint)

        val tickerStr = project.tickerText.ifBlank {
            "ব্রেকিং নিউজ: সরাসরি স্টুডিও থেকে সম্প্রচারিত সংবাদ বুলেটিন"
        }
        val repeatedTicker = "$tickerStr   •   $tickerStr   •   "
        textPaint.typeface = banglaBoldTypeface
        textPaint.textSize = 34f
        textPaint.color = Color.parseColor("#F8FAFC")

        val singleCycleWidth = textPaint.measureText("$tickerStr   •   ").coerceAtLeast(600f)
        val speed = 190f * project.tickerSpeedMultiplier.coerceIn(0.5f, 2.5f)
        val scrollOffset = (globalTimeSec * speed) % singleCycleWidth

        canvas.save()
        canvas.clipRect(260f, topY, virtualW, virtualH)
        canvas.drawText(repeatedTicker, 280f - scrollOffset, topY + 50f, textPaint)
        canvas.restore()

        fillPaint.color = Color.parseColor("#E11D48")
        canvas.drawRect(0f, topY, 260f, virtualH, fillPaint)

        textPaint.typeface = banglaBoldTypeface
        textPaint.textSize = 34f
        textPaint.color = Color.WHITE
        canvas.drawText("শিরোনাম", 62f, topY + 50f, textPaint)
    }

    private fun drawElementSelectionBox(
        canvas: Canvas,
        virtualW: Float,
        virtualH: Float,
        shot: NewsShot,
        element: CanvasElementType,
        customTextIdx: Int?
    ) {
        when (element) {
            CanvasElementType.HEADLINE,
            CanvasElementType.HIGHLIGHT_WORD -> rectF.set(160f, 868f, virtualW - 160f, 984f)
            CanvasElementType.STAGE_BANNER_TEXT -> rectF.set(190f, 210f, 1730f, 450f)
            CanvasElementType.TOON_CHARACTER -> rectF.set(420f, 280f, 1500f, 960f)
            CanvasElementType.SUBHEADLINE -> rectF.set(66f, 875f, virtualW - 66f, 960f)
            CanvasElementType.CATEGORY_BADGE -> rectF.set(66f, 700f, 500f, 770f)
            CanvasElementType.LIVE_CHANNEL_BUG -> rectF.set(56f, 38f, 720f, 114f)
            CanvasElementType.NEWS_TICKER -> rectF.set(8f, virtualH - 82f, virtualW - 8f, virtualH - 6f)
            CanvasElementType.CUSTOM_TEXT_LAYER -> {
                val layer = customTextIdx?.let { shot.customTextLayers.getOrNull(it) }
                    ?: shot.customTextLayers.firstOrNull()
                if (layer != null) {
                    val cx = layer.xPercent * virtualW
                    val cy = layer.yPercent * virtualH
                    rectF.set(cx - 320f, cy - 56f, cx + 320f, cy + 56f)
                } else {
                    return
                }
            }
            CanvasElementType.BACKDROP_MEDIA -> rectF.set(24f, 24f, virtualW - 24f, 850f)
        }
        canvas.drawRoundRect(rectF, 16f, 16f, selectionPaint)
    }

    private fun ellipsizeText(text: String, paint: TextPaint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 4 && paint.measureText(text.substring(0, end) + "…") > maxWidth) {
            end--
        }
        return text.substring(0, end) + "…"
    }

    private fun smoothStep(x: Float): Float {
        val t = x.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    private fun darkenColor(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
}
