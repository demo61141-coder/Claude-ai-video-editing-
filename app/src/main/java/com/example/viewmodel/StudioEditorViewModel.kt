package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectCodeParserAndGenerator
import com.example.engine.BroadcastFrameRenderer
import com.example.engine.Mp4VideoExporter
import com.example.model.CanvasElementType
import com.example.model.CustomTextLayer
import com.example.model.ExportMp4State
import com.example.model.KeyframeConfig
import com.example.model.LowerThirdStyle
import com.example.model.MotionGraphicOverlay
import com.example.model.MotionPreset
import com.example.model.NewsProject
import com.example.model.NewsShot
import com.example.model.SampleNewsProjects
import com.example.model.ShotMediaType
import com.example.model.SoundEffectType
import com.example.model.StudioBackdropPreset
import com.example.model.TextAnimationStyle
import com.example.model.VideoBitratePreset
import com.example.model.VideoExportConfig
import com.example.model.VideoResolutionPreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

enum class StudioDestination {
    IMPORT_DASHBOARD,
    EDITOR_WORKSPACE
}

enum class InspectorTab(val label: String, val banglaSubtitle: String) {
    TEXT_SCRIPT("Text & Layers", "টেক্সট এডিট/ডিলিট"),
    MEDIA_GALLERY("Video & Photo", "ভিডিও/ফটো ও অডিও"),
    MOTION_STYLE("Keyframe & SFX", "কী-ফ্রেম ও সাউন্ড"),
    SCENE_ORDER("Clips & Order", "শট ও টাইমলাইন")
}

data class StudioUiState(
    val destination: StudioDestination = StudioDestination.IMPORT_DASHBOARD,
    val project: NewsProject = SampleNewsProjects.primeTimeBulletin(),
    val selectedShotIndex: Int = 0,
    val isPlaying: Boolean = true,
    val playheadSeconds: Float = 0f,
    val isMuted: Boolean = false,
    val activeInspectorTab: InspectorTab = InspectorTab.TEXT_SCRIPT,
    val selectedCanvasElement: CanvasElementType = CanvasElementType.HEADLINE,
    val selectedCustomTextIndex: Int? = null,
    val showPasteCodeSheet: Boolean = false,
    val showCanvasQuickEditDialog: Boolean = false,
    val codeEditorDraft: String = "",
    val statusBannerMessage: String? = null,
    val exportConfig: VideoExportConfig = VideoExportConfig(),
    val exportState: ExportMp4State = ExportMp4State.Idle
) {
    val selectedShot: NewsShot
        get() = project.shots.getOrElse(selectedShotIndex) {
            project.shots.firstOrNull() ?: NewsShot(
                headline = "সংবাদ শিরোনাম",
                subheadline = "উপ-শিরোনাম"
            )
        }
}

class StudioEditorViewModel(application: Application) : AndroidViewModel(application) {

    val frameRenderer = BroadcastFrameRenderer(application.applicationContext)
    private val mp4Exporter = Mp4VideoExporter(application.applicationContext, frameRenderer)

    private val _uiState = MutableStateFlow(
        StudioUiState(
            codeEditorDraft = ProjectCodeParserAndGenerator.projectToJsonString(
                SampleNewsProjects.primeTimeBulletin()
            )
        )
    )
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private var voiceoverPlayer: MediaPlayer? = null
    private var exportJob: Job? = null
    private var lastSfxPlayedShotIndex: Int = -1

    init {
        frameRenderer.preloadBitmaps(_uiState.value.project)
    }

    fun openTemplateInEditor(templateProject: NewsProject) {
        frameRenderer.preloadBitmaps(templateProject)
        lastSfxPlayedShotIndex = -1
        _uiState.update {
            it.copy(
                destination = StudioDestination.EDITOR_WORKSPACE,
                project = templateProject,
                selectedShotIndex = 0,
                playheadSeconds = 0f,
                isPlaying = true,
                codeEditorDraft = ProjectCodeParserAndGenerator.projectToJsonString(templateProject),
                statusBannerMessage = "প্রজেক্ট লোড হয়েছে: ${templateProject.projectName} (${templateProject.shots.size}টি শট)"
            )
        }
    }

    fun navigateToDashboard() {
        pausePlayback()
        _uiState.update {
            it.copy(
                destination = StudioDestination.IMPORT_DASHBOARD,
                isPlaying = false
            )
        }
    }

    fun openPasteCodeSheet(prefillWithCurrent: Boolean = true) {
        val currentCode = if (prefillWithCurrent) {
            ProjectCodeParserAndGenerator.projectToJsonString(_uiState.value.project)
        } else {
            _uiState.value.codeEditorDraft
        }
        _uiState.update {
            it.copy(
                showPasteCodeSheet = true,
                codeEditorDraft = currentCode
            )
        }
    }

    fun closePasteCodeSheet() {
        _uiState.update { it.copy(showPasteCodeSheet = false) }
    }

    fun updateCodeEditorDraft(newCode: String) {
        _uiState.update { it.copy(codeEditorDraft = newCode) }
    }

    fun loadSampleJsonIntoDraft() {
        val sampleJson = ProjectCodeParserAndGenerator.projectToJsonString(_uiState.value.project)
        _uiState.update { it.copy(codeEditorDraft = sampleJson) }
    }

    fun loadSampleHtmlIntoDraft() {
        val sampleHtml = ProjectCodeParserAndGenerator.projectToHtmlProjectCode(_uiState.value.project)
        _uiState.update { it.copy(codeEditorDraft = sampleHtml) }
    }

    fun importCodeStringAndOpenEditor(rawCode: String) {
        val result = ProjectCodeParserAndGenerator.parseCodeToProject(rawCode)
        result.onSuccess { parsedProject ->
            frameRenderer.preloadBitmaps(parsedProject)
            lastSfxPlayedShotIndex = -1
            _uiState.update {
                it.copy(
                    destination = StudioDestination.EDITOR_WORKSPACE,
                    project = parsedProject,
                    selectedShotIndex = 0,
                    playheadSeconds = 0f,
                    isPlaying = true,
                    showPasteCodeSheet = false,
                    codeEditorDraft = ProjectCodeParserAndGenerator.projectToJsonString(parsedProject),
                    statusBannerMessage = "প্রজেক্ট ফাইল ইমপোর্ট সম্পন্ন! (${parsedProject.shots.size}টি দৃশ্য যুক্ত হয়েছে)"
                )
            }
        }.onFailure { err ->
            _uiState.update {
                it.copy(
                    statusBannerMessage = "ইমপোর্ট ত্রুটি: ${err.localizedMessage ?: "সঠিক JSON/SRT/প্রজেক্ট ফাইল দিন"}"
                )
            }
        }
    }

    fun importCodeFileFromUri(uri: Uri) {
        val context = getApplication<Application>().applicationContext
        runCatching {
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                ?: throw IllegalArgumentException("ফাইলটি পড়া যায়নি।")
            importCodeStringAndOpenEditor(content)
        }.onFailure { err ->
            _uiState.update {
                it.copy(statusBannerMessage = "ফাইল রিড ত্রুটি: ${err.localizedMessage}")
            }
        }
    }

    /**
     * Called when the user clicks directly on the Live Preview Canvas at normalized coordinates (0..1, 0..1).
     * Pauses playback, highlights the clicked element on canvas, switches to the matching editor tab,
     * and opens the instant element editor!
     */
    fun onCanvasTappedAt(normX: Float, normY: Float) {
        val state = _uiState.value
        val (hitElement, customIdx) = frameRenderer.hitTestCanvasElement(
            normX = normX,
            normY = normY,
            project = state.project,
            shot = state.selectedShot
        )
        pausePlayback()
        val targetTab = when (hitElement) {
            CanvasElementType.HEADLINE,
            CanvasElementType.SUBHEADLINE,
            CanvasElementType.CATEGORY_BADGE,
            CanvasElementType.LIVE_CHANNEL_BUG,
            CanvasElementType.CUSTOM_TEXT_LAYER,
            CanvasElementType.NEWS_TICKER -> InspectorTab.TEXT_SCRIPT
            CanvasElementType.BACKDROP_MEDIA -> InspectorTab.MOTION_STYLE
        }
        _uiState.update {
            it.copy(
                selectedCanvasElement = hitElement,
                selectedCustomTextIndex = customIdx,
                activeInspectorTab = targetTab,
                showCanvasQuickEditDialog = true,
                statusBannerMessage = "সিলেক্ট করা হয়েছে: ${hitElement.banglaName} — এডিট বা ডিলিট করুন"
            )
        }
    }

    fun selectCanvasElement(element: CanvasElementType, customIdx: Int? = null) {
        _uiState.update {
            it.copy(
                selectedCanvasElement = element,
                selectedCustomTextIndex = customIdx
            )
        }
    }

    /**
     * Deletes or toggles the currently selected text/graphic element on the canvas.
     */
    fun deleteOrRestoreCanvasElement(element: CanvasElementType, customIdx: Int? = null) {
        when (element) {
            CanvasElementType.HEADLINE -> {
                updateSelectedShot {
                    val nextShow = !it.showHeadline
                    it.copy(showHeadline = nextShow)
                }
            }
            CanvasElementType.SUBHEADLINE -> {
                updateSelectedShot {
                    val nextShow = !it.showSubheadline
                    it.copy(showSubheadline = nextShow)
                }
            }
            CanvasElementType.CATEGORY_BADGE -> {
                updateSelectedShot {
                    val nextShow = !it.showCategoryBadge
                    it.copy(showCategoryBadge = nextShow)
                }
            }
            CanvasElementType.LIVE_CHANNEL_BUG -> {
                updateSelectedShot {
                    val nextShow = !it.showLiveBug
                    it.copy(showLiveBug = nextShow)
                }
            }
            CanvasElementType.NEWS_TICKER -> {
                _uiState.update { state ->
                    state.copy(
                        project = state.project.copy(showTicker = !state.project.showTicker)
                    )
                }
            }
            CanvasElementType.CUSTOM_TEXT_LAYER -> {
                val idx = customIdx ?: _uiState.value.selectedCustomTextIndex
                if (idx != null) {
                    removeCustomTextLayer(idx)
                }
            }
            CanvasElementType.BACKDROP_MEDIA -> {
                updateSelectedShot {
                    it.copy(
                        mediaType = ShotMediaType.PRESET_PHOTO,
                        customImageUri = null,
                        customVideoUri = null,
                        customMediaName = null
                    )
                }
            }
        }
    }

    fun addNewCustomTextLayer(initialText: String = "নতুন ব্রেকিং টেক্সট") {
        updateSelectedShot { shot ->
            val offset = (shot.customTextLayers.size * 0.10f).coerceAtMost(0.4f)
            val newLayer = CustomTextLayer(
                text = initialText,
                xPercent = 0.5f,
                yPercent = (0.24f + offset).coerceIn(0.15f, 0.70f),
                fontSizePx = 34f
            )
            shot.copy(customTextLayers = shot.customTextLayers + newLayer)
        }
        val newIndex = (_uiState.value.selectedShot.customTextLayers.size - 1).coerceAtLeast(0)
        _uiState.update {
            it.copy(
                selectedCanvasElement = CanvasElementType.CUSTOM_TEXT_LAYER,
                selectedCustomTextIndex = newIndex,
                statusBannerMessage = "নতুন টেক্সট লেয়ার যুক্ত হয়েছে! পজিশন ও লেখা পরিবর্তন করুন"
            )
        }
    }

    fun updateCustomTextLayer(
        index: Int,
        text: String? = null,
        xPercent: Float? = null,
        yPercent: Float? = null,
        fontSizePx: Float? = null
    ) {
        updateSelectedShot { shot ->
            if (index !in shot.customTextLayers.indices) return@updateSelectedShot shot
            val list = shot.customTextLayers.toMutableList()
            val cur = list[index]
            list[index] = cur.copy(
                text = text ?: cur.text,
                xPercent = (xPercent ?: cur.xPercent).coerceIn(0.1f, 0.9f),
                yPercent = (yPercent ?: cur.yPercent).coerceIn(0.1f, 0.85f),
                fontSizePx = (fontSizePx ?: cur.fontSizePx).coerceIn(18f, 60f)
            )
            shot.copy(customTextLayers = list)
        }
    }

    fun removeCustomTextLayer(index: Int) {
        updateSelectedShot { shot ->
            if (index !in shot.customTextLayers.indices) return@updateSelectedShot shot
            val list = shot.customTextLayers.toMutableList()
            list.removeAt(index)
            shot.copy(customTextLayers = list)
        }
        _uiState.update {
            it.copy(
                selectedCanvasElement = CanvasElementType.HEADLINE,
                selectedCustomTextIndex = null,
                statusBannerMessage = "টেক্সট লেয়ার ডিলিট করা হয়েছে"
            )
        }
    }

    fun togglePlayPause() {
        val nextPlaying = !_uiState.value.isPlaying
        _uiState.update { it.copy(isPlaying = nextPlaying) }
        syncVoiceoverPlayback(nextPlaying, _uiState.value.playheadSeconds)
    }

    fun pausePlayback() {
        _uiState.update { it.copy(isPlaying = false) }
        runCatching {
            if (voiceoverPlayer?.isPlaying == true) {
                voiceoverPlayer?.pause()
            }
        }
    }

    fun restartPlayback() {
        lastSfxPlayedShotIndex = -1
        _uiState.update {
            it.copy(
                playheadSeconds = 0f,
                selectedShotIndex = 0,
                isPlaying = true
            )
        }
        syncVoiceoverPlayback(true, 0f)
    }

    fun seekToSeconds(seconds: Float) {
        val total = _uiState.value.project.totalDurationSeconds
        val clamped = seconds.coerceIn(0f, total)
        val (shotIdx, _, _) = frameRenderer.resolveShotAtTime(_uiState.value.project, clamped)
        _uiState.update {
            it.copy(
                playheadSeconds = clamped,
                selectedShotIndex = shotIdx
            )
        }
    }

    fun advancePlayheadByDelta(deltaSeconds: Float) {
        val state = _uiState.value
        if (!state.isPlaying) return
        val total = state.project.totalDurationSeconds
        var nextTime = state.playheadSeconds + deltaSeconds
        if (nextTime >= total) {
            nextTime = 0f
            lastSfxPlayedShotIndex = -1
            syncVoiceoverPlayback(true, 0f)
        }
        val (resolvedIndex, activeShot, _) = frameRenderer.resolveShotAtTime(state.project, nextTime)
        if (resolvedIndex != lastSfxPlayedShotIndex && !state.isMuted) {
            lastSfxPlayedShotIndex = resolvedIndex
            if (activeShot.soundEffect != SoundEffectType.NONE) {
                mp4Exporter.playSoundEffectPreview(viewModelScope, activeShot.soundEffect)
            }
        }
        _uiState.update {
            it.copy(
                playheadSeconds = nextTime,
                selectedShotIndex = resolvedIndex
            )
        }
    }

    fun selectShot(index: Int, jumpPlayhead: Boolean = true) {
        val shots = _uiState.value.project.shots
        if (index !in shots.indices) return
        var startSec = 0f
        for (i in 0 until index) {
            startSec += shots[i].durationSeconds
        }
        _uiState.update {
            it.copy(
                selectedShotIndex = index,
                playheadSeconds = if (jumpPlayhead) startSec + 0.4f else it.playheadSeconds
            )
        }
    }

    fun selectInspectorTab(tab: InspectorTab) {
        _uiState.update { it.copy(activeInspectorTab = tab) }
    }

    fun setShowCanvasQuickEditDialog(show: Boolean) {
        _uiState.update { it.copy(showCanvasQuickEditDialog = show) }
    }

    fun clearStatusBanner() {
        _uiState.update { it.copy(statusBannerMessage = null) }
    }

    fun updateSelectedShot(transform: (NewsShot) -> NewsShot) {
        _uiState.update { state ->
            val idx = state.selectedShotIndex
            if (idx !in state.project.shots.indices) return@update state
            val updatedShots = state.project.shots.toMutableList()
            updatedShots[idx] = transform(updatedShots[idx])
            state.copy(project = state.project.copy(shots = updatedShots))
        }
    }

    fun updateHeadline(headline: String) = updateSelectedShot {
        it.copy(headline = headline, showHeadline = true)
    }

    fun updateSubheadline(sub: String) = updateSelectedShot {
        it.copy(subheadline = sub, showSubheadline = true)
    }

    fun updateCategoryTag(tag: String) = updateSelectedShot {
        it.copy(categoryTag = tag, showCategoryBadge = true)
    }

    fun updateReporterLocation(loc: String) = updateSelectedShot {
        it.copy(reporterLocation = loc)
    }

    fun updateShotDuration(seconds: Float) = updateSelectedShot {
        it.copy(durationSeconds = seconds.coerceIn(1.5f, 20.0f))
    }

    fun updateShotMotionPreset(preset: MotionPreset) = updateSelectedShot {
        it.copy(
            motionPreset = preset,
            keyframes = KeyframeConfig(
                startScale = preset.defaultStartScale,
                endScale = preset.defaultEndScale,
                startOffsetX = preset.defaultStartX,
                endOffsetX = preset.defaultEndX,
                startOffsetY = 0f,
                endOffsetY = -10f
            )
        )
    }

    fun updateShotKeyframes(transform: (KeyframeConfig) -> KeyframeConfig) = updateSelectedShot {
        it.copy(
            motionPreset = MotionPreset.CUSTOM_KEYFRAME,
            keyframes = transform(it.keyframes)
        )
    }

    fun updateShotMotionIntensity(intensity: Float) = updateSelectedShot {
        it.copy(motionIntensity = intensity.coerceIn(0.5f, 2.0f))
    }

    fun updateShotLowerThirdStyle(style: LowerThirdStyle) = updateSelectedShot {
        it.copy(lowerThirdStyle = style)
    }

    fun updateShotTextAnimation(anim: TextAnimationStyle) = updateSelectedShot {
        it.copy(textAnimation = anim)
    }

    fun updateShotMotionGraphicOverlay(overlay: MotionGraphicOverlay) = updateSelectedShot {
        it.copy(motionGraphicOverlay = overlay)
    }

    fun updateShotSoundEffect(sfx: SoundEffectType) {
        updateSelectedShot { it.copy(soundEffect = sfx) }
        if (!_uiState.value.isMuted && sfx != SoundEffectType.NONE) {
            mp4Exporter.playSoundEffectPreview(viewModelScope, sfx)
        }
    }

    fun updateShotBackdropPreset(preset: StudioBackdropPreset) = updateSelectedShot {
        it.copy(
            backdropPreset = preset,
            mediaType = ShotMediaType.PRESET_PHOTO,
            customImageUri = null,
            customVideoUri = null,
            customMediaName = null
        )
    }

    /**
     * Imports either a Photo OR a Video clip from the user's device gallery using PickVisualMedia!
     */
    fun importGalleryMediaForSelectedShot(uri: Uri) {
        val context = getApplication<Application>().applicationContext
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val mimeType = context.contentResolver.getType(uri) ?: ""
        val uriStr = uri.toString()
        val mediaName = uri.lastPathSegment?.substringAfterLast('/') ?: "Gallery_Media"

        if (mimeType.startsWith("video/")) {
            updateSelectedShot {
                it.copy(
                    mediaType = ShotMediaType.GALLERY_VIDEO,
                    customVideoUri = uriStr,
                    customImageUri = null,
                    customMediaName = "Video: $mediaName"
                )
            }
            viewModelScope.launch {
                frameRenderer.preloadBitmaps(_uiState.value.project)
            }
            _uiState.update {
                it.copy(statusBannerMessage = "গ্যালারি ভিডিও ক্লিপ শট #${it.selectedShotIndex + 1}-এ যুক্ত হয়েছে!")
            }
        } else {
            updateSelectedShot {
                it.copy(
                    mediaType = ShotMediaType.GALLERY_PHOTO,
                    customImageUri = uriStr,
                    customVideoUri = null,
                    customMediaName = "Photo: $mediaName"
                )
            }
            frameRenderer.preloadBitmaps(_uiState.value.project)
            _uiState.update {
                it.copy(statusBannerMessage = "গ্যালারি ফটো শট #${it.selectedShotIndex + 1}-এ যুক্ত হয়েছে!")
            }
        }
    }

    fun importVoiceoverAudioFromUri(uri: Uri) {
        val context = getApplication<Application>().applicationContext
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "Custom_Voiceover.mp3"
        runCatching {
            voiceoverPlayer?.release()
            voiceoverPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                isLooping = true
                prepare()
                if (_uiState.value.isPlaying && !_uiState.value.isMuted) {
                    start()
                }
            }
        }
        _uiState.update {
            it.copy(
                project = it.project.copy(
                    customAudioUri = uri.toString(),
                    customAudioName = fileName
                ),
                statusBannerMessage = "ভয়েসওভার অডিও যুক্ত হয়েছে: $fileName"
            )
        }
    }

    fun toggleStudioSynthAudio(enabled: Boolean) {
        _uiState.update {
            it.copy(project = it.project.copy(useStudioSynthAudio = enabled))
        }
    }

    fun toggleMute() {
        val nextMuted = !_uiState.value.isMuted
        _uiState.update { it.copy(isMuted = nextMuted) }
        runCatching {
            val vol = if (nextMuted) 0f else 1f
            voiceoverPlayer?.setVolume(vol, vol)
        }
    }

    fun updateProjectMetadata(
        projectName: String? = null,
        channelName: String? = null,
        tickerText: String? = null,
        showTicker: Boolean? = null,
        tickerSpeed: Float? = null
    ) {
        _uiState.update { state ->
            state.copy(
                project = state.project.copy(
                    projectName = projectName ?: state.project.projectName,
                    channelName = channelName ?: state.project.channelName,
                    tickerText = tickerText ?: state.project.tickerText,
                    showTicker = showTicker ?: state.project.showTicker,
                    tickerSpeedMultiplier = tickerSpeed ?: state.project.tickerSpeedMultiplier
                )
            )
        }
    }

    fun addNewShot() {
        _uiState.update { state ->
            val nextIdx = state.project.shots.size + 1
            val presets = StudioBackdropPreset.entries
            val styles = LowerThirdStyle.entries
            val newShot = NewsShot(
                categoryTag = "নতুন সংবাদ",
                headline = "নতুন সংবাদ দৃশ্য $nextIdx: এখানে শিরোনাম লিখুন",
                subheadline = "সরাসরি ঢাকা স্টুডিও থেকে সম্প্রচারিত বিশেষ প্রতিবেদন",
                reporterLocation = "ঢাকা স্টুডিও • LIVE",
                durationSeconds = 5.0f,
                motionPreset = MotionPreset.KEN_BURNS_IN,
                lowerThirdStyle = styles[nextIdx % styles.size],
                backdropPreset = presets[nextIdx % presets.size]
            )
            val updated = state.project.shots + newShot
            state.copy(
                project = state.project.copy(shots = updated),
                selectedShotIndex = updated.lastIndex,
                statusBannerMessage = "নতুন শট #${updated.size} টাইমলাইনে যোগ হয়েছে"
            )
        }
    }

    fun duplicateSelectedShot() {
        _uiState.update { state ->
            val idx = state.selectedShotIndex
            if (idx !in state.project.shots.indices) return@update state
            val original = state.project.shots[idx]
            val copyShot = original.copy(
                id = UUID.randomUUID().toString(),
                headline = "${original.headline} (কপি)"
            )
            val mutable = state.project.shots.toMutableList()
            mutable.add(idx + 1, copyShot)
            state.copy(
                project = state.project.copy(shots = mutable),
                selectedShotIndex = idx + 1,
                statusBannerMessage = "শট #${idx + 1} ডুপ্লিকেট করা হয়েছে"
            )
        }
    }

    fun deleteSelectedShot() {
        _uiState.update { state ->
            if (state.project.shots.size <= 1) {
                return@update state.copy(
                    statusBannerMessage = "টাইমলাইনে অন্তত ১টি শট থাকা আবশ্যক।"
                )
            }
            val idx = state.selectedShotIndex
            val mutable = state.project.shots.toMutableList()
            mutable.removeAt(idx)
            val nextSelected = idx.coerceAtMost(mutable.lastIndex)
            state.copy(
                project = state.project.copy(shots = mutable),
                selectedShotIndex = nextSelected,
                playheadSeconds = 0f,
                statusBannerMessage = "শট #${idx + 1} মুছে ফেলা হয়েছে"
            )
        }
    }

    fun moveSelectedShot(offset: Int) {
        _uiState.update { state ->
            val currentIdx = state.selectedShotIndex
            val targetIdx = currentIdx + offset
            val shots = state.project.shots
            if (currentIdx !in shots.indices || targetIdx !in shots.indices) return@update state
            val mutable = shots.toMutableList()
            val item = mutable.removeAt(currentIdx)
            mutable.add(targetIdx, item)
            state.copy(
                project = state.project.copy(shots = mutable),
                selectedShotIndex = targetIdx,
                statusBannerMessage = "শট পজিশন #${targetIdx + 1}-এ সরানো হয়েছে"
            )
        }
    }

    // Video Export Settings & Fast Hardware MP4 Export
    fun openExportSettingsModal() {
        pausePlayback()
        _uiState.update {
            it.copy(exportState = ExportMp4State.ConfiguringSettings)
        }
    }

    fun updateExportResolution(resolution: VideoResolutionPreset) {
        val matchingBitrate = VideoBitratePreset.entries.minByOrNull {
            kotlin.math.abs(it.mbpsValue - resolution.recommendedBitrateMbps)
        } ?: VideoBitratePreset.STD_5M
        _uiState.update {
            it.copy(
                exportConfig = it.exportConfig.copy(
                    resolution = resolution,
                    bitratePreset = matchingBitrate
                )
            )
        }
    }

    fun updateExportBitrate(bitratePreset: VideoBitratePreset) {
        _uiState.update {
            it.copy(exportConfig = it.exportConfig.copy(bitratePreset = bitratePreset))
        }
    }

    fun updateExportFps(fps: Int) {
        _uiState.update {
            it.copy(exportConfig = it.exportConfig.copy(fps = fps.coerceIn(24, 60)))
        }
    }

    fun startMp4Export() {
        if (_uiState.value.exportState is ExportMp4State.Rendering) return
        pausePlayback()

        val projectSnapshot = _uiState.value.project
        val configSnapshot = _uiState.value.exportConfig
        exportJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    exportState = ExportMp4State.Rendering(
                        currentFrame = 0,
                        totalFrames = (projectSnapshot.totalDurationSeconds * configSnapshot.fps).toInt(),
                        resolutionLabel = "${configSnapshot.resolution.width}×${configSnapshot.resolution.height} (${configSnapshot.resolution.label})",
                        bitrateLabel = configSnapshot.bitratePreset.label,
                        statusMessage = "Starting Turbo Hardware Encoder (${configSnapshot.resolution.label})..."
                    )
                )
            }

            val result = mp4Exporter.exportProjectToMp4(
                project = projectSnapshot,
                exportConfig = configSnapshot
            ) { progressState ->
                _uiState.update { it.copy(exportState = progressState) }
            }

            result.onSuccess { completedState ->
                _uiState.update {
                    it.copy(
                        exportState = completedState,
                        statusBannerMessage = "MP4 ভিডিও রেন্ডার সম্পন্ন! (${completedState.fileSizeKb} KB • ${"%.1f".format(completedState.elapsedMs / 1000f)}s)"
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        exportState = ExportMp4State.Error(
                            error.localizedMessage ?: "MP4 ভিডিও রেন্ডার করতে সমস্যা হয়েছে।"
                        )
                    )
                }
            }
        }
    }

    fun cancelOrDismissExportDialog() {
        exportJob?.cancel()
        _uiState.update { it.copy(exportState = ExportMp4State.Idle) }
    }

    fun saveExportedMp4ToDocumentUri(destinationUri: Uri) {
        val completed = _uiState.value.exportState as? ExportMp4State.Completed ?: return
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            runCatching {
                val srcFile = File(completed.filePath)
                context.contentResolver.openOutputStream(destinationUri)?.use { outStream ->
                    srcFile.inputStream().use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }
                _uiState.update {
                    it.copy(statusBannerMessage = "ভিডিও সেভ হয়েছে: ${completed.fileName}")
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(statusBannerMessage = "সেভ ত্রুটি: ${err.localizedMessage}")
                }
            }
        }
    }

    fun saveProjectJsonToUri(destinationUri: Uri) {
        val context = getApplication<Application>().applicationContext
        val jsonContent = ProjectCodeParserAndGenerator.projectToJsonString(_uiState.value.project)
        viewModelScope.launch {
            runCatching {
                context.contentResolver.openOutputStream(destinationUri)?.bufferedWriter()?.use { writer ->
                    writer.write(jsonContent)
                }
                _uiState.update {
                    it.copy(statusBannerMessage = "প্রজেক্ট ফাইল (.json) সেভ হয়েছে!")
                }
            }
        }
    }

    fun shareExportedMp4File() {
        val completed = _uiState.value.exportState as? ExportMp4State.Completed ?: return
        val context = getApplication<Application>().applicationContext
        runCatching {
            val file = File(completed.filePath)
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Share Exported MP4 Video").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    private fun syncVoiceoverPlayback(play: Boolean, positionSec: Float) {
        val player = voiceoverPlayer ?: return
        runCatching {
            if (play && !_uiState.value.isMuted) {
                player.seekTo((positionSec * 1000).toInt())
                player.start()
            } else if (player.isPlaying) {
                player.pause()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        runCatching { voiceoverPlayer?.release() }
    }
}
