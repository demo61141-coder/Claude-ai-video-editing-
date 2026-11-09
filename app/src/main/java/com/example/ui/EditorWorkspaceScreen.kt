package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.BroadcastFrameRenderer
import com.example.model.CanvasElementType
import com.example.model.KeyframeConfig
import com.example.model.LowerThirdStyle
import com.example.model.MotionGraphicOverlay
import com.example.model.MotionPreset
import com.example.model.SoundEffectType
import com.example.model.StudioBackdropPreset
import com.example.model.TextAnimationStyle
import com.example.ui.theme.BroadcastAmber
import com.example.ui.theme.BroadcastCrimson
import com.example.ui.theme.BroadcastEmerald
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.InspectorTab
import com.example.viewmodel.StudioUiState

@Composable
fun EditorWorkspaceScreen(
    uiState: StudioUiState,
    frameRenderer: BroadcastFrameRenderer,
    onBackToDashboard: () -> Unit,
    onOpenPasteCodeSheet: () -> Unit,
    onOpenExportSettingsClick: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onRestartPlayback: () -> Unit,
    onToggleMute: () -> Unit,
    onSeekToSeconds: (Float) -> Unit,
    onAdvancePlayhead: (Float) -> Unit,
    onSelectShot: (Int) -> Unit,
    onSelectInspectorTab: (InspectorTab) -> Unit,
    onCanvasTappedAt: (Float, Float) -> Unit,
    onOpenCanvasQuickEdit: () -> Unit,
    onDeleteSelectedCanvasElement: () -> Unit,
    onAddNewCustomText: () -> Unit,
    onUpdateCustomTextLayer: (Int, String?, Float?, Float?, Float?) -> Unit,
    onRemoveCustomTextLayer: (Int) -> Unit,
    onDismissStatusBanner: () -> Unit,
    onUpdateCategory: (String) -> Unit,
    onUpdateHeadline: (String) -> Unit,
    onUpdateSubheadline: (String) -> Unit,
    onUpdateReporterLocation: (String) -> Unit,
    onUpdateChannelName: (String) -> Unit,
    onUpdateTickerText: (String) -> Unit,
    onDeleteOrRestoreElement: (CanvasElementType, Int?) -> Unit,
    onPickGalleryVideoOrPhoto: () -> Unit,
    onSelectBackdropPreset: (StudioBackdropPreset) -> Unit,
    onPickVoiceoverAudio: () -> Unit,
    onToggleStudioSynth: (Boolean) -> Unit,
    onUpdateDuration: (Float) -> Unit,
    onUpdateMotionPreset: (MotionPreset) -> Unit,
    onUpdateKeyframes: ((KeyframeConfig) -> KeyframeConfig) -> Unit,
    onUpdateSoundEffect: (SoundEffectType) -> Unit,
    onUpdateMotionGraphicOverlay: (MotionGraphicOverlay) -> Unit,
    onUpdateLowerThirdStyle: (LowerThirdStyle) -> Unit,
    onUpdateTextAnimation: (TextAnimationStyle) -> Unit,
    onAddNewShot: () -> Unit,
    onDuplicateShot: () -> Unit,
    onDeleteShot: () -> Unit,
    onMoveShotEarlier: () -> Unit,
    onMoveShotLater: () -> Unit,
    onSaveProjectJsonClick: () -> Unit
) {
    BackHandler(onBack = onBackToDashboard)

    var lastFrameNanos by remember { mutableLongStateOf(0L) }
    LaunchedEffect(uiState.isPlaying) {
        lastFrameNanos = 0L
        while (uiState.isPlaying) {
            withFrameNanos { frameTimeNanos ->
                if (lastFrameNanos != 0L) {
                    val deltaSec = ((frameTimeNanos - lastFrameNanos) / 1_000_000_000f)
                        .coerceIn(0f, 0.1f)
                    onAdvancePlayhead(deltaSec)
                }
                lastFrameNanos = frameTimeNanos
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = StudioObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Broadcast Studio Header Bar
            EditorTopStudioBar(
                projectName = uiState.project.projectName,
                shotCount = uiState.project.shots.size,
                resolutionLabel = uiState.exportConfig.resolution.label,
                onBackClick = onBackToDashboard,
                onCodeClick = onOpenPasteCodeSheet,
                onExportMp4Click = onOpenExportSettingsClick
            )

            // Status Notification Toast Banner
            AnimatedVisibility(visible = uiState.statusBannerMessage != null) {
                uiState.statusBannerMessage?.let { msg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF064E3B))
                            .clickable(onClick = onDismissStatusBanner)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.labelLarge,
                            color = BroadcastEmerald
                        )
                        Text(
                            text = "OK",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }

            // Main Scrollable Workspace
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 820.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 2. Interactive Live Video Preview Canvas (Tap any element on canvas to select, edit, or delete!)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black)
                        .border(1.5.dp, StudioCardBorder, RoundedCornerShape(14.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { tapOffset ->
                                val normX = (tapOffset.x / size.width.toFloat()).coerceIn(0f, 1f)
                                val normY = (tapOffset.y / size.height.toFloat()).coerceIn(0f, 1f)
                                onCanvasTappedAt(normX, normY)
                            }
                        }
                        .testTag("live_video_preview_canvas")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawIntoCanvas { composeCanvas ->
                            frameRenderer.renderFrame(
                                canvas = composeCanvas.nativeCanvas,
                                width = size.width,
                                height = size.height,
                                project = uiState.project,
                                globalTimeSec = uiState.playheadSeconds,
                                selectedCanvasElement = uiState.selectedCanvasElement,
                                selectedCustomTextIndex = uiState.selectedCustomTextIndex,
                                drawSelectionHighlight = !uiState.isPlaying
                            )
                        }
                    }

                    // Top-Center Interactive Hint Badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xCC080C14))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Tap any item on video to edit or delete",
                            tint = BroadcastAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "ভিডিওর যেকোনো লেখার ওপর ক্লিক করে এডিট বা ডিলিট করুন",
                            fontSize = 10.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }

                // Direct On-Canvas Quick Action Strip (Edit / Delete / + Add Text / + Video or Photo)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioSurface)
                        .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "সিলেক্টেড: ${uiState.selectedCanvasElement.banglaName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BroadcastAmber,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onOpenCanvasQuickEdit,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Selected",
                                tint = BroadcastAmber,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("এডিট", fontSize = 11.sp, color = BroadcastAmber)
                        }

                        OutlinedButton(
                            onClick = onDeleteSelectedCanvasElement,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Selected",
                                tint = BroadcastCrimson,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("ডিলিট", fontSize = 11.sp, color = BroadcastCrimson)
                        }

                        OutlinedButton(
                            onClick = onAddNewCustomText,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Text",
                                tint = BroadcastEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("টেক্সট", fontSize = 11.sp, color = BroadcastEmerald)
                        }

                        OutlinedButton(
                            onClick = onPickGalleryVideoOrPhoto,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Import Video/Photo",
                                tint = TextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("মিডিয়া", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }

                // 3. Playback & Transport Control Bar
                TransportControlBar(
                    isPlaying = uiState.isPlaying,
                    isMuted = uiState.isMuted,
                    selectedShotIndex = uiState.selectedShotIndex,
                    totalShots = uiState.project.shots.size,
                    playheadSeconds = uiState.playheadSeconds,
                    totalDurationSeconds = uiState.project.totalDurationSeconds,
                    onTogglePlayPause = onTogglePlayPause,
                    onRestartPlayback = onRestartPlayback,
                    onPrevShot = {
                        if (uiState.selectedShotIndex > 0) {
                            onSelectShot(uiState.selectedShotIndex - 1)
                        }
                    },
                    onNextShot = {
                        if (uiState.selectedShotIndex < uiState.project.shots.lastIndex) {
                            onSelectShot(uiState.selectedShotIndex + 1)
                        }
                    },
                    onToggleMute = onToggleMute,
                    onAddShot = onAddNewShot
                )

                // 4. Multi-Track Timeline & Shot Strip
                MultiTrackTimelineSection(
                    project = uiState.project,
                    selectedShotIndex = uiState.selectedShotIndex,
                    playheadSeconds = uiState.playheadSeconds,
                    onSeekToSeconds = onSeekToSeconds,
                    onSelectShot = onSelectShot,
                    onAddShotClick = onAddNewShot,
                    onImportVoiceoverClick = onPickVoiceoverAudio
                )

                // 5. Inline Shot, Keyframe, SFX & Media Inspector Panel
                InlineShotInspectorPanel(
                    project = uiState.project,
                    selectedShotIndex = uiState.selectedShotIndex,
                    selectedShot = uiState.selectedShot,
                    activeTab = uiState.activeInspectorTab,
                    onSelectTab = onSelectInspectorTab,
                    onUpdateCategory = onUpdateCategory,
                    onUpdateHeadline = onUpdateHeadline,
                    onUpdateSubheadline = onUpdateSubheadline,
                    onUpdateReporterLocation = onUpdateReporterLocation,
                    onUpdateChannelName = onUpdateChannelName,
                    onUpdateTickerText = onUpdateTickerText,
                    onDeleteOrRestoreElement = onDeleteOrRestoreElement,
                    onAddNewCustomText = onAddNewCustomText,
                    onUpdateCustomTextLayer = onUpdateCustomTextLayer,
                    onRemoveCustomTextLayer = onRemoveCustomTextLayer,
                    onPickGalleryVideoOrPhoto = onPickGalleryVideoOrPhoto,
                    onSelectBackdropPreset = onSelectBackdropPreset,
                    onPickVoiceoverAudio = onPickVoiceoverAudio,
                    onToggleStudioSynth = onToggleStudioSynth,
                    onUpdateDuration = onUpdateDuration,
                    onUpdateMotionPreset = onUpdateMotionPreset,
                    onUpdateKeyframes = onUpdateKeyframes,
                    onUpdateSoundEffect = onUpdateSoundEffect,
                    onUpdateMotionGraphicOverlay = onUpdateMotionGraphicOverlay,
                    onUpdateLowerThirdStyle = onUpdateLowerThirdStyle,
                    onUpdateTextAnimation = onUpdateTextAnimation,
                    onAddNewShot = onAddNewShot,
                    onDuplicateShot = onDuplicateShot,
                    onDeleteShot = onDeleteShot,
                    onMoveShotEarlier = onMoveShotEarlier,
                    onMoveShotLater = onMoveShotLater,
                    onSaveProjectJsonClick = onSaveProjectJsonClick
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun EditorTopStudioBar(
    projectName: String,
    shotCount: Int,
    resolutionLabel: String,
    onBackClick: () -> Unit,
    onCodeClick: () -> Unit,
    onExportMp4Click: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .border(width = 1.dp, color = StudioCardBorder)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("back_to_dashboard_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Import Screen",
                    tint = TextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$shotCount Shots • $resolutionLabel MP4 Ready",
                    style = MaterialTheme.typography.labelMedium,
                    color = BroadcastAmber
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = onCodeClick,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("top_import_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Input Project File or Code",
                    tint = BroadcastAmber,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("ইনপুট ফাইল", fontSize = 12.sp, color = BroadcastAmber)
            }

            Button(
                onClick = onExportMp4Click,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BroadcastCrimson,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("export_mp4_button")
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = "Export Video MP4",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "সেভ ভিডিও (MP4)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TransportControlBar(
    isPlaying: Boolean,
    isMuted: Boolean,
    selectedShotIndex: Int,
    totalShots: Int,
    playheadSeconds: Float,
    totalDurationSeconds: Float,
    onTogglePlayPause: () -> Unit,
    onRestartPlayback: () -> Unit,
    onPrevShot: () -> Unit,
    onNextShot: () -> Unit,
    onToggleMute: () -> Unit,
    onAddShot: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(StudioSurface)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(BroadcastCrimson)
                    .testTag("play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = onRestartPlayback,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Restart Playback",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onPrevShot,
                enabled = selectedShotIndex > 0,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous Shot",
                    tint = if (selectedShotIndex > 0) TextPrimary else TextSecondary.copy(alpha = 0.4f)
                )
            }

            IconButton(
                onClick = onNextShot,
                enabled = selectedShotIndex < totalShots - 1,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next Shot",
                    tint = if (selectedShotIndex < totalShots - 1) TextPrimary else TextSecondary.copy(alpha = 0.4f)
                )
            }

            IconButton(
                onClick = onToggleMute,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Toggle Audio Mute",
                    tint = if (isMuted) BroadcastCrimson else BroadcastEmerald,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "SHOT ${selectedShotIndex + 1} OF $totalShots",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = BroadcastAmber
            )
            Text(
                text = "${formatSeconds(playheadSeconds)} / ${formatSeconds(totalDurationSeconds)}",
                fontSize = 11.sp,
                color = TextSecondary
            )
        }

        OutlinedButton(
            onClick = onAddShot,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier.testTag("add_shot_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add New Shot",
                tint = BroadcastAmber,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("+ শট", fontSize = 12.sp, color = BroadcastAmber)
        }
    }
}
