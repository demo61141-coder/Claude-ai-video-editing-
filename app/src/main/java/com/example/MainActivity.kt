package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.EditorWorkspaceScreen
import com.example.ui.ExportMp4Modal
import com.example.ui.ImportDashboardScreen
import com.example.ui.PasteOrEditCodeDialog
import com.example.ui.QuickCanvasInlineEditDialog
import com.example.ui.theme.BanglaNewsStudioTheme
import com.example.viewmodel.StudioDestination
import com.example.viewmodel.StudioEditorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BanglaNewsStudioTheme {
                BanglaNewsStudioProApp()
            }
        }
    }
}

@Composable
fun BanglaNewsStudioProApp(
    viewModel: StudioEditorViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 1. Universal Video Project File Picker (.html, .js, .json, .srt, .xml, .txt)
    val codeFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importCodeFileFromUri(uri)
        }
    }

    // 2. Zero-permission Android Photo & Video Picker for importing Gallery Videos or Photos
    val galleryMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.importGalleryMediaForSelectedShot(uri)
        }
    }

    // 3. Audio file picker for importing custom voiceover tracks
    val voiceoverAudioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importVoiceoverAudioFromUri(uri)
        }
    }

    // 4. CreateDocument launcher for saving exported MP4 video to user storage
    val saveMp4Launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("video/mp4")
    ) { destinationUri ->
        if (destinationUri != null) {
            viewModel.saveExportedMp4ToDocumentUri(destinationUri)
        }
    }

    // 5. CreateDocument launcher for saving project JSON file
    val saveProjectJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { destinationUri ->
        if (destinationUri != null) {
            viewModel.saveProjectJsonToUri(destinationUri)
        }
    }

    when (uiState.destination) {
        StudioDestination.IMPORT_DASHBOARD -> {
            ImportDashboardScreen(
                currentProject = uiState.project,
                onPickCodeFileClick = {
                    codeFilePickerLauncher.launch(
                        arrayOf("text/html", "application/json", "text/*", "*/*")
                    )
                },
                onOpenPasteCodeSheet = {
                    viewModel.openPasteCodeSheet(prefillWithCurrent = true)
                },
                onOpenTemplateProject = { template ->
                    viewModel.openTemplateInEditor(template)
                },
                onResumeCurrentEditor = {
                    viewModel.openTemplateInEditor(uiState.project)
                }
            )
        }

        StudioDestination.EDITOR_WORKSPACE -> {
            EditorWorkspaceScreen(
                uiState = uiState,
                frameRenderer = viewModel.frameRenderer,
                onBackToDashboard = { viewModel.navigateToDashboard() },
                onOpenPasteCodeSheet = { viewModel.openPasteCodeSheet(prefillWithCurrent = true) },
                onOpenExportSettingsClick = { viewModel.openExportSettingsModal() },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onRestartPlayback = { viewModel.restartPlayback() },
                onToggleMute = { viewModel.toggleMute() },
                onSeekToSeconds = { viewModel.seekToSeconds(it) },
                onAdvancePlayhead = { viewModel.advancePlayheadByDelta(it) },
                onSelectShot = { viewModel.selectShot(it) },
                onSelectInspectorTab = { viewModel.selectInspectorTab(it) },
                onCanvasTappedAt = { normX, normY ->
                    viewModel.onCanvasTappedAt(normX, normY)
                },
                onOpenCanvasQuickEdit = { viewModel.setShowCanvasQuickEditDialog(true) },
                onDeleteSelectedCanvasElement = {
                    viewModel.deleteOrRestoreCanvasElement(
                        uiState.selectedCanvasElement,
                        uiState.selectedCustomTextIndex
                    )
                },
                onAddNewCustomText = { viewModel.addNewCustomTextLayer() },
                onUpdateCustomTextLayer = { idx, text, x, y, size ->
                    viewModel.updateCustomTextLayer(idx, text, x, y, size)
                },
                onRemoveCustomTextLayer = { idx ->
                    viewModel.removeCustomTextLayer(idx)
                },
                onDismissStatusBanner = { viewModel.clearStatusBanner() },
                onUpdateCategory = { viewModel.updateCategoryTag(it) },
                onUpdateHeadline = { viewModel.updateHeadline(it) },
                onUpdateHighlightWord = { viewModel.updateHighlightWord(it) },
                onUpdateSubheadline = { viewModel.updateSubheadline(it) },
                onUpdateStageBannerText = { viewModel.updateStageBannerText(it) },
                onUpdateToonWho = { viewModel.updateToonCharacterWho(it) },
                onUpdateToonPose = { viewModel.updateToonCharacterPose(it) },
                onUpdateReporterLocation = { viewModel.updateReporterLocation(it) },
                onUpdateChannelName = { viewModel.updateProjectMetadata(channelName = it) },
                onUpdateTickerText = { viewModel.updateProjectMetadata(tickerText = it) },
                onDeleteOrRestoreElement = { el, idx ->
                    viewModel.deleteOrRestoreCanvasElement(el, idx)
                },
                onPickGalleryVideoOrPhoto = {
                    galleryMediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                onSelectBackdropPreset = { viewModel.updateShotBackdropPreset(it) },
                onPickVoiceoverAudio = {
                    voiceoverAudioPickerLauncher.launch(arrayOf("audio/*"))
                },
                onToggleStudioSynth = { viewModel.toggleStudioSynthAudio(it) },
                onUpdateDuration = { viewModel.updateShotDuration(it) },
                onUpdateMotionPreset = { viewModel.updateShotMotionPreset(it) },
                onUpdateKeyframes = { viewModel.updateShotKeyframes(it) },
                onUpdateSoundEffect = { viewModel.updateShotSoundEffect(it) },
                onUpdateMotionGraphicOverlay = { viewModel.updateShotMotionGraphicOverlay(it) },
                onUpdateLowerThirdStyle = { viewModel.updateShotLowerThirdStyle(it) },
                onUpdateTextAnimation = { viewModel.updateShotTextAnimation(it) },
                onAddNewShot = { viewModel.addNewShot() },
                onDuplicateShot = { viewModel.duplicateSelectedShot() },
                onDeleteShot = { viewModel.deleteSelectedShot() },
                onMoveShotEarlier = { viewModel.moveSelectedShot(-1) },
                onMoveShotLater = { viewModel.moveSelectedShot(1) },
                onSaveProjectJsonClick = {
                    saveProjectJsonLauncher.launch("Bangla_News_Project.json")
                }
            )
        }
    }

    // Paste / Input Project Code Dialog
    if (uiState.showPasteCodeSheet) {
        PasteOrEditCodeDialog(
            codeDraft = uiState.codeEditorDraft,
            onCodeChange = { viewModel.updateCodeEditorDraft(it) },
            onLoadSampleJson = { viewModel.loadSampleJsonIntoDraft() },
            onLoadSampleHtml = { viewModel.loadSampleHtmlIntoDraft() },
            onPickFileFromDevice = {
                codeFilePickerLauncher.launch(
                    arrayOf("text/html", "application/json", "text/*", "*/*")
                )
            },
            onImportAndLaunch = { code ->
                viewModel.importCodeStringAndOpenEditor(code)
            },
            onDismiss = { viewModel.closePasteCodeSheet() }
        )
    }

    // Interactive On-Canvas Element Editor Dialog (opens when clicking any part of the video preview)
    if (uiState.showCanvasQuickEditDialog) {
        QuickCanvasInlineEditDialog(
            shotIndex = uiState.selectedShotIndex,
            shot = uiState.selectedShot,
            project = uiState.project,
            selectedElement = uiState.selectedCanvasElement,
            selectedCustomTextIndex = uiState.selectedCustomTextIndex,
            onSelectElement = { el, idx -> viewModel.selectCanvasElement(el, idx) },
            onUpdateCategory = { viewModel.updateCategoryTag(it) },
            onUpdateHeadline = { viewModel.updateHeadline(it) },
            onUpdateHighlightWord = { viewModel.updateHighlightWord(it) },
            onUpdateSubheadline = { viewModel.updateSubheadline(it) },
            onUpdateStageBannerText = { viewModel.updateStageBannerText(it) },
            onUpdateToonWho = { viewModel.updateToonCharacterWho(it) },
            onUpdateToonPose = { viewModel.updateToonCharacterPose(it) },
            onSelectBackdropPreset = { viewModel.updateShotBackdropPreset(it) },
            onUpdateTicker = { viewModel.updateProjectMetadata(tickerText = it) },
            onUpdateChannelName = { viewModel.updateProjectMetadata(channelName = it) },
            onDeleteOrRestoreElement = { el, idx ->
                viewModel.deleteOrRestoreCanvasElement(el, idx)
            },
            onAddNewCustomText = { viewModel.addNewCustomTextLayer() },
            onUpdateCustomTextLayer = { idx, text, x, y, size ->
                viewModel.updateCustomTextLayer(idx, text, x, y, size)
            },
            onUpdateSoundEffect = { viewModel.updateShotSoundEffect(it) },
            onUpdateMotionGraphicOverlay = { viewModel.updateShotMotionGraphicOverlay(it) },
            onPickGalleryVideoOrPhoto = {
                galleryMediaPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                )
            },
            onDismiss = { viewModel.setShowCanvasQuickEditDialog(false) }
        )
    }

    // Professional Video Export Settings (144p, 360p, 480p, 720p, 1080p, 1480p + Bitrate + FPS) & Progress Modal
    ExportMp4Modal(
        exportState = uiState.exportState,
        exportConfig = uiState.exportConfig,
        onSelectResolution = { viewModel.updateExportResolution(it) },
        onSelectBitrate = { viewModel.updateExportBitrate(it) },
        onSelectFps = { viewModel.updateExportFps(it) },
        onStartRenderClick = { viewModel.startMp4Export() },
        onSaveToDeviceClick = { fileName ->
            saveMp4Launcher.launch(fileName)
        },
        onShareVideoClick = {
            viewModel.shareExportedMp4File()
        },
        onDismiss = {
            viewModel.cancelOrDismissExportDialog()
        }
    )
}
