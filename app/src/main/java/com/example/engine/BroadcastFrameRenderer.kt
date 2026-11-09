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
import com.example.model.MotionGraphicOverlay
import com.example.model.MotionPreset
import com.example.model.NewsProject
import com.example.model.NewsShot
import com.example.model.ShotMediaType
import com.example.model.StudioBackdropPreset
import com.example.model.TextAnimationStyle
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin

class BroadcastFrameRenderer(private val context: Context) {

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
    private val shapePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        color = Color.parseColor("#F59E0B")
        pathEffect = DashPathEffect(floatArrayOf(14f, 8f), 0f)
    }
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private val matrix = Matrix()
    private val rectF = RectF()
    private val badgePath = Path()

    fun preloadBitmaps(project: NewsProject) {
        StudioBackdropPreset.entries.forEach { preset ->
            getPresetBitmap(preset)
        }
        project.shots.forEach { shot ->
            shot.customImageUri?.let { getCustomBitmap(it) }
            shot.customVideoUri?.let { getVideoFrames(it) }
        }
    }

    private fun getPresetBitmap(preset: StudioBackdropPreset): Bitmap? {
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
                    inSampleSize = 1
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
     * Hit-tests normalized canvas coordinates (0..1, 0..1) so when the user clicks directly
     * on the video preview, we identify the exact element they clicked (Headline, Subheadline,
     * Category Badge, Custom Text Layer, Ticker, Live Bug, or Backdrop Media).
     */
    fun hitTestCanvasElement(
        normX: Float,
        normY: Float,
        project: NewsProject,
        shot: NewsShot
    ): Pair<CanvasElementType, Int?> {
        val vx = normX.coerceIn(0f, 1f) * 1280f
        val vy = normY.coerceIn(0f, 1f) * 720f

        // 1. Check Custom Floating Text Layers first
        shot.customTextLayers.forEachIndexed { idx, layer ->
            val lx = layer.xPercent * 1280f
            val ly = layer.yPercent * 720f
            if (abs(vx - lx) <= 260f && abs(vy - ly) <= 46f) {
                return CanvasElementType.CUSTOM_TEXT_LAYER to idx
            }
        }

        // 2. Check Bottom Ticker
        if (project.showTicker && vy >= 660f) {
            return CanvasElementType.NEWS_TICKER to null
        }

        // 3. Check Lower-Third Banner Regions
        if (shot.motionGraphicOverlay != MotionGraphicOverlay.CINEMATIC_CLEAN) {
            if (shot.showSubheadline && vy in 588f..652f && vx in 40f..1240f) {
                return CanvasElementType.SUBHEADLINE to null
            }
            if (shot.showHeadline && vy in 506f..588f && vx in 40f..1240f) {
                return CanvasElementType.HEADLINE to null
            }
            if (shot.showCategoryBadge && vy in 460f..508f && vx in 40f..420f) {
                return CanvasElementType.CATEGORY_BADGE to null
            }
        }

        // 4. Check Top Broadcast Channel / LIVE Bug
        if (shot.showLiveBug && vy in 24f..90f && vx in 40f..520f) {
            return CanvasElementType.LIVE_CHANNEL_BUG to null
        }

        // 5. Otherwise user clicked the photo/video/keyframe backdrop
        return CanvasElementType.BACKDROP_MEDIA to null
    }

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

        val virtualW = 1280f
        val virtualH = 720f
        val scaleX = width / virtualW
        val scaleY = height / virtualH

        canvas.save()
        canvas.scale(scaleX, scaleY)

        val (shotIndex, activeShot, localSec) = resolveShotAtTime(project, globalTimeSec)
        val shotDuration = activeShot.durationSeconds.coerceAtLeast(1f)
        val shotProgress = (localSec / shotDuration).coerceIn(0f, 1f)

        // 1. Background Photo / Video with Keyframe Interpolation
        drawAnimatedBackdrop(canvas, virtualW, virtualH, activeShot, shotProgress)

        // 2. Studio Atmosphere & Motion Graphic Overlays
        drawStudioAtmosphereOverlay(canvas, virtualW, virtualH, activeShot, globalTimeSec)

        // 3. Top Broadcast Bug
        drawTopBroadcastBar(
            canvas = canvas,
            virtualW = virtualW,
            project = project,
            activeShot = activeShot,
            shotIndex = shotIndex,
            totalShots = project.shots.size.coerceAtLeast(1),
            globalTimeSec = globalTimeSec
        )

        // 4. Lower-Third Broadcast Graphics
        if (activeShot.motionGraphicOverlay != MotionGraphicOverlay.CINEMATIC_CLEAN) {
            drawLowerThirdGraphics(
                canvas = canvas,
                virtualW = virtualW,
                shot = activeShot,
                localSec = localSec,
                shotDuration = shotDuration,
                shotProgress = shotProgress
            )
        }

        // 5. Custom Added Text Layers
        drawCustomTextLayers(canvas, virtualW, virtualH, activeShot)

        // 6. Bottom Scrolling News Ticker
        if (project.showTicker) {
            drawScrollingTicker(
                canvas = canvas,
                virtualW = virtualW,
                virtualH = virtualH,
                project = project,
                globalTimeSec = globalTimeSec
            )
        }

        // 7. Interactive Selection Highlight Box (only on Live Preview Canvas)
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

    private fun drawAnimatedBackdrop(
        canvas: Canvas,
        virtualW: Float,
        virtualH: Float,
        shot: NewsShot,
        progress: Float
    ) {
        shapePaint.shader = null
        shapePaint.color = shot.backdropPreset.fallbackColorHex.toInt() or -0x1000000
        canvas.drawRect(0f, 0f, virtualW, virtualH, shapePaint)

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
            ShotMediaType.PRESET_PHOTO -> {
                shot.customImageUri?.let { getCustomBitmap(it) }
                    ?: getPresetBitmap(shot.backdropPreset)
            }
        }

        val ease = smoothStep(progress)
        val intensity = shot.motionIntensity.coerceIn(0.4f, 2.0f)
        val kf = shot.keyframes

        val currentScale: Float
        val translateX: Float
        val translateY: Float

        if (shot.motionPreset == MotionPreset.CUSTOM_KEYFRAME) {
            currentScale = lerp(kf.startScale, kf.endScale, ease)
            translateX = lerp(kf.startOffsetX, kf.endOffsetX, ease)
            translateY = lerp(kf.startOffsetY, kf.endOffsetY, ease)
        } else {
            val s0 = kf.startScale
            val s1 = kf.endScale
            currentScale = lerp(s0, 1f + (s1 - 1f) * intensity, ease).coerceIn(0.8f, 2.2f)
            translateX = lerp(kf.startOffsetX, kf.endOffsetX, ease) * intensity
            translateY = lerp(kf.startOffsetY, kf.endOffsetY, ease) * intensity
        }

        if (bitmap != null && !bitmap.isRecycled) {
            canvas.save()
            val bw = bitmap.width.toFloat().coerceAtLeast(1f)
            val bh = bitmap.height.toFloat().coerceAtLeast(1f)
            val baseScale = maxOf(virtualW / bw, virtualH / bh)
            val totalScale = baseScale * currentScale

            matrix.reset()
            matrix.postTranslate(-bw / 2f, -bh / 2f)
            matrix.postScale(totalScale, totalScale)
            matrix.postTranslate(virtualW / 2f + translateX, virtualH / 2f + translateY)

            canvas.drawBitmap(bitmap, matrix, bitmapPaint)
            canvas.restore()
        } else {
            shapePaint.shader = LinearGradient(
                0f, 0f, virtualW, virtualH,
                intArrayOf(
                    Color.parseColor("#0F172A"),
                    Color.parseColor("#31102F"),
                    Color.parseColor("#090D16")
                ),
                null,
                Shader.TileMode.CLAMP
            )
            canvas.drawRect(0f, 0f, virtualW, virtualH, shapePaint)
            shapePaint.shader = null
        }
    }

    private fun drawStudioAtmosphereOverlay(
        canvas: Canvas,
        virtualW: Float,
        virtualH: Float,
        shot: NewsShot,
        globalTimeSec: Float
    ) {
        if (shot.motionGraphicOverlay == MotionGraphicOverlay.FULL_BROADCAST_PACK ||
            shot.motionGraphicOverlay == MotionGraphicOverlay.BREAKING_PERIMETER_GLOW
        ) {
            strokePaint.color = Color.argb(22, 148, 163, 184)
            strokePaint.strokeWidth = 1f
            val step = 80f
            var x = (globalTimeSec * 12f) % step
            while (x < virtualW) {
                canvas.drawLine(x, 0f, x, virtualH, strokePaint)
                x += step
            }
        }

        // Vignette gradient
        shapePaint.shader = LinearGradient(
            0f, 0f, 0f, virtualH,
            intArrayOf(
                Color.argb(165, 6, 9, 17),
                Color.argb(25, 6, 9, 17),
                Color.argb(115, 6, 9, 17),
                Color.argb(230, 6, 9, 17)
            ),
            floatArrayOf(0f, 0.28f, 0.62f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, virtualW, virtualH, shapePaint)
        shapePaint.shader = null

        // Breaking Perimeter Alert Glow Frame
        if (shot.motionGraphicOverlay == MotionGraphicOverlay.BREAKING_PERIMETER_GLOW) {
            val pulse = ((sin(globalTimeSec * 7.0) * 0.5 + 0.5) * 180 + 55).toInt().coerceIn(50, 240)
            strokePaint.color = Color.argb(pulse, 225, 29, 72)
            strokePaint.strokeWidth = 10f
            rectF.set(10f, 10f, virtualW - 10f, virtualH - 60f)
            canvas.drawRoundRect(rectF, 14f, 14f, strokePaint)
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
        val topY = 34f
        val barH = 44f
        var cursorX = 48f

        if (activeShot.showLiveBug) {
            shapePaint.color = Color.parseColor("#E11D48")
            rectF.set(cursorX, topY, cursorX + 116f, topY + barH)
            canvas.drawRoundRect(rectF, 8f, 8f, shapePaint)

            val pulseAlpha = ((sin(globalTimeSec * 6.0) * 0.4 + 0.6) * 255).toInt().coerceIn(90, 255)
            shapePaint.color = Color.argb(pulseAlpha, 255, 255, 255)
            canvas.drawCircle(cursorX + 24f, topY + barH / 2f, 6.5f, shapePaint)

            textPaint.typeface = studioMonoTypeface
            textPaint.textSize = 20f
            textPaint.color = Color.WHITE
            canvas.drawText("LIVE", cursorX + 42f, topY + 29f, textPaint)
            cursorX += 126f

            val channelText = project.channelName.ifBlank { "বাংলা নিউজ ২৪" }
            textPaint.typeface = banglaBoldTypeface
            textPaint.textSize = 22f
            val channelTextW = textPaint.measureText(channelText)
            val pillW = channelTextW + 44f

            shapePaint.color = Color.argb(225, 15, 23, 42)
            rectF.set(cursorX, topY, cursorX + pillW, topY + barH)
            canvas.drawRoundRect(rectF, 8f, 8f, shapePaint)

            strokePaint.color = Color.argb(120, 245, 158, 11)
            strokePaint.strokeWidth = 1.5f
            canvas.drawRoundRect(rectF, 8f, 8f, strokePaint)

            textPaint.color = Color.parseColor("#F8FAFC")
            canvas.drawText(channelText, cursorX + 22f, topY + 30f, textPaint)
        }

        val rightEdge = virtualW - 48f
        val locationText = "${activeShot.reporterLocation}  •  SHOT ${shotIndex + 1}/$totalShots"
        textPaint.typeface = banglaRegularTypeface
        textPaint.textSize = 18f
        val locW = textPaint.measureText(locationText)
        val waveformW = if (activeShot.showWaveformOverlay) 78f else 0f
        val rightBoxW = locW + waveformW + 44f

        shapePaint.color = Color.argb(215, 15, 23, 42)
        rectF.set(rightEdge - rightBoxW, topY, rightEdge, topY + barH)
        canvas.drawRoundRect(rectF, 8f, 8f, shapePaint)

        textPaint.color = Color.parseColor("#E2E8F0")
        canvas.drawText(locationText, rightEdge - rightBoxW + 20f, topY + 28f, textPaint)

        if (activeShot.showWaveformOverlay) {
            val waveStartX = rightEdge - waveformW - 10f
            val centerY = topY + barH / 2f
            shapePaint.color = Color.parseColor("#10B981")
            for (b in 0 until 8) {
                val amp = (sin(globalTimeSec * 10f + b * 0.9f) * 0.5 + 0.5).toFloat()
                val barHeight = 6f + amp * 20f
                val bx = waveStartX + b * 8.5f
                rectF.set(bx, centerY - barHeight / 2f, bx + 4.5f, centerY + barHeight / 2f)
                canvas.drawRoundRect(rectF, 2f, 2f, shapePaint)
            }
        }
    }

    private fun drawLowerThirdGraphics(
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

        val slideOffsetY = when (shot.textAnimation) {
            TextAnimationStyle.SLIDE_UP_REVEAL -> (1f - visibilityFactor) * 60f
            TextAnimationStyle.POP_AND_GLOW -> (1f - visibilityFactor) * 32f
            else -> (1f - visibilityFactor) * 16f
        }

        val leftX = 48f
        val bannerW = virtualW - 96f
        val baseTopY = 468f + slideOffsetY

        canvas.save()

        // 1. Category Badge
        if (shot.showCategoryBadge && shot.categoryTag.isNotBlank()) {
            val catText = shot.categoryTag
            textPaint.typeface = banglaBoldTypeface
            textPaint.textSize = 22f
            val catW = (textPaint.measureText(catText) + 52f).coerceAtLeast(180f)
            val catH = 40f

            badgePath.reset()
            badgePath.moveTo(leftX, baseTopY)
            badgePath.lineTo(leftX + catW, baseTopY)
            badgePath.lineTo(leftX + catW - 16f, baseTopY + catH)
            badgePath.lineTo(leftX, baseTopY + catH)
            badgePath.close()

            shapePaint.color = badgeColor
            canvas.drawPath(badgePath, shapePaint)

            textPaint.color = Color.parseColor("#090D16")
            canvas.drawText(catText, leftX + 20f, baseTopY + 28f, textPaint)
        }

        val headlineTopY = baseTopY + 40f
        val headlineH = 84f

        // 2. Main Headline Banner
        if (shot.showHeadline && shot.headline.isNotBlank()) {
            shapePaint.shader = LinearGradient(
                leftX, headlineTopY, leftX + bannerW, headlineTopY,
                intArrayOf(primaryColor, darkenColor(primaryColor, 0.72f)),
                null,
                Shader.TileMode.CLAMP
            )
            rectF.set(leftX, headlineTopY, leftX + bannerW, headlineTopY + headlineH)
            canvas.drawRoundRect(rectF, 6f, 6f, shapePaint)
            shapePaint.shader = null

            shapePaint.color = badgeColor
            rectF.set(leftX, headlineTopY, leftX + 10f, headlineTopY + headlineH)
            canvas.drawRect(rectF, shapePaint)

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
                rawHeadline.length > 62 -> 28f
                rawHeadline.length > 45 -> 32f
                else -> 36f
            }
            val maxTextWidth = bannerW - 56f
            val fittedHeadline = ellipsizeText(displayedHeadline, textPaint, maxTextWidth)
            canvas.drawText(fittedHeadline, leftX + 28f, headlineTopY + 54f, textPaint)
        }

        // 3. Subheadline Bar
        val subTopY = headlineTopY + headlineH
        val subH = 52f
        if (shot.showSubheadline && shot.subheadline.isNotBlank() &&
            shot.motionGraphicOverlay != MotionGraphicOverlay.MINIMAL_LOWER_THIRD
        ) {
            shapePaint.color = Color.argb(
                240,
                Color.red(secondaryColor),
                Color.green(secondaryColor),
                Color.blue(secondaryColor)
            )
            rectF.set(leftX, subTopY, leftX + bannerW, subTopY + subH)
            canvas.drawRoundRect(rectF, 6f, 6f, shapePaint)

            textPaint.typeface = banglaRegularTypeface
            textPaint.textSize = 23f
            textPaint.color = Color.parseColor("#E2E8F0")
            val fittedSub = ellipsizeText(shot.subheadline, textPaint, bannerW - 56f)
            canvas.drawText(fittedSub, leftX + 28f, subTopY + 34f, textPaint)

            shapePaint.color = badgeColor
            canvas.drawRect(
                leftX,
                subTopY + subH - 4f,
                leftX + bannerW * shotProgress,
                subTopY + subH,
                shapePaint
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
            textPaint.typeface = banglaBoldTypeface
            textPaint.textSize = layer.fontSizePx.coerceIn(18f, 64f)
            val textW = textPaint.measureText(layer.text)
            val cx = (layer.xPercent.coerceIn(0.1f, 0.9f) * virtualW)
            val cy = (layer.yPercent.coerceIn(0.1f, 0.85f) * virtualH)
            val boxW = textW + 44f
            val boxH = layer.fontSizePx + 26f

            shapePaint.color = layer.bgColorHex.toInt()
            rectF.set(cx - boxW / 2f, cy - boxH / 2f, cx + boxW / 2f, cy + boxH / 2f)
            canvas.drawRoundRect(rectF, 10f, 10f, shapePaint)

            strokePaint.color = Color.WHITE
            strokePaint.strokeWidth = 1.5f
            canvas.drawRoundRect(rectF, 10f, 10f, strokePaint)

            textPaint.color = layer.textColorHex.toInt() or -0x1000000
            canvas.drawText(layer.text, cx - textW / 2f, cy + layer.fontSizePx * 0.34f, textPaint)
        }
    }

    private fun drawScrollingTicker(
        canvas: Canvas,
        virtualW: Float,
        virtualH: Float,
        project: NewsProject,
        globalTimeSec: Float
    ) {
        val tickerH = 54f
        val topY = virtualH - tickerH

        shapePaint.color = Color.parseColor("#060911")
        canvas.drawRect(0f, topY, virtualW, virtualH, shapePaint)

        shapePaint.color = Color.parseColor("#E11D48")
        canvas.drawRect(0f, topY, virtualW, topY + 3f, shapePaint)

        val tickerStr = project.tickerText.ifBlank {
            "ব্রেকিং নিউজ: সরাসরি স্টুডিও থেকে সম্প্রচারিত সংবাদ বুলেটিন"
        }
        val repeatedTicker = "$tickerStr   •   $tickerStr   •   "
        textPaint.typeface = banglaBoldTypeface
        textPaint.textSize = 23f
        textPaint.color = Color.parseColor("#F8FAFC")

        val singleCycleWidth = textPaint.measureText("$tickerStr   •   ").coerceAtLeast(400f)
        val speed = 135f * project.tickerSpeedMultiplier.coerceIn(0.5f, 2.5f)
        val scrollOffset = (globalTimeSec * speed) % singleCycleWidth

        canvas.save()
        canvas.clipRect(190f, topY, virtualW, virtualH)
        canvas.drawText(repeatedTicker, 200f - scrollOffset, topY + 36f, textPaint)
        canvas.restore()

        shapePaint.color = Color.parseColor("#E11D48")
        canvas.drawRect(0f, topY, 186f, virtualH, shapePaint)

        textPaint.typeface = banglaBoldTypeface
        textPaint.textSize = 23f
        textPaint.color = Color.WHITE
        canvas.drawText("শিরোনাম", 42f, topY + 36f, textPaint)
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
            CanvasElementType.HEADLINE -> rectF.set(44f, 504f, virtualW - 44f, 594f)
            CanvasElementType.SUBHEADLINE -> rectF.set(44f, 590f, virtualW - 44f, 648f)
            CanvasElementType.CATEGORY_BADGE -> rectF.set(44f, 462f, 340f, 510f)
            CanvasElementType.LIVE_CHANNEL_BUG -> rectF.set(44f, 28f, 490f, 84f)
            CanvasElementType.NEWS_TICKER -> rectF.set(4f, virtualH - 58f, virtualW - 4f, virtualH - 4f)
            CanvasElementType.CUSTOM_TEXT_LAYER -> {
                val layer = customTextIdx?.let { shot.customTextLayers.getOrNull(it) }
                    ?: shot.customTextLayers.firstOrNull()
                if (layer != null) {
                    val cx = layer.xPercent * virtualW
                    val cy = layer.yPercent * virtualH
                    rectF.set(cx - 210f, cy - 38f, cx + 210f, cy + 38f)
                } else {
                    return
                }
            }
            CanvasElementType.BACKDROP_MEDIA -> rectF.set(16f, 16f, virtualW - 16f, 452f)
        }
        canvas.drawRoundRect(rectF, 8f, 8f, selectionPaint)
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
