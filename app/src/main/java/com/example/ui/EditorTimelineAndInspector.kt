package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CanvasElementType
import com.example.model.KeyframeConfig
import com.example.model.LowerThirdStyle
import com.example.model.MotionGraphicOverlay
import com.example.model.MotionPreset
import com.example.model.NewsProject
import com.example.model.NewsShot
import com.example.model.ShotMediaType
import com.example.model.SoundEffectType
import com.example.model.StudioBackdropPreset
import com.example.model.TextAnimationStyle
import com.example.model.ToonCharacterPose
import com.example.model.ToonCharacterWho
import com.example.ui.theme.BroadcastAmber
import com.example.ui.theme.BroadcastCrimson
import com.example.ui.theme.BroadcastEmerald
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.InspectorTab

@Composable
fun MultiTrackTimelineSection(
    project: NewsProject,
    selectedShotIndex: Int,
    playheadSeconds: Float,
    onSeekToSeconds: (Float) -> Unit,
    onSelectShot: (Int) -> Unit,
    onAddShotClick: () -> Unit,
    onImportVoiceoverClick: () -> Unit
) {
    val totalDuration = project.totalDurationSeconds

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Scrubber Bar & Time Ruler
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "TIMELINE (${project.shots.size} SHOTS)",
                    style = MaterialTheme.typography.labelMedium,
                    color = BroadcastAmber
                )
                Slider(
                    value = playheadSeconds.coerceIn(0f, totalDuration),
                    onValueChange = onSeekToSeconds,
                    valueRange = 0f..totalDuration,
                    modifier = Modifier
                        .weight(1f)
                        .height(26.dp)
                        .testTag("timeline_scrubber_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = BroadcastAmber,
                        activeTrackColor = BroadcastCrimson,
                        inactiveTrackColor = StudioSurfaceElevated
                    )
                )
                Text(
                    text = "${formatSeconds(playheadSeconds)} / ${formatSeconds(totalDuration)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary
                )
            }

            // Track 1: Video Shots Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                project.shots.forEachIndexed { index, shot ->
                    val isSelected = index == selectedShotIndex
                    TimelineShotBlock(
                        index = index,
                        shot = shot,
                        isSelected = isSelected,
                        onClick = { onSelectShot(index) }
                    )
                }

                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(76.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(StudioSurfaceElevated)
                        .border(1.dp, BroadcastAmber.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddShotClick)
                        .testTag("timeline_add_shot_card"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Shot",
                            tint = BroadcastAmber
                        )
                        Text(
                            text = "+ নতুন শট",
                            style = MaterialTheme.typography.labelMedium,
                            color = BroadcastAmber
                        )
                    }
                }
            }

            // Track 2: Audio & Sound Effects Track
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioObsidian)
                    .border(1.dp, StudioCardBorder, RoundedCornerShape(10.dp))
                    .clickable(onClick = onImportVoiceoverClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Audio Track",
                        tint = BroadcastEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = project.customAudioName?.let { "ভয়েসওভার: $it" }
                            ?: if (project.useStudioSynthAudio) {
                                "অডিও ট্র্যাক: নিউজরুম মিউজিক + শট সাউন্ড ইফেক্ট (SFX সক্রিয়)"
                            } else {
                                "অডিও ট্র্যাক: ভয়েসওভার অডিও ফাইল ইনপুট করতে এখানে ট্যাপ করুন"
                            },
                        style = MaterialTheme.typography.labelMedium,
                        color = BroadcastEmerald,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "+ অডিও ইনপুট",
                    style = MaterialTheme.typography.labelMedium,
                    color = BroadcastAmber
                )
            }
        }
    }
}

@Composable
private fun TimelineShotBlock(
    index: Int,
    shot: NewsShot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) BroadcastCrimson else StudioCardBorder
    val containerColor = if (isSelected) Color(0xFF2A1220) else StudioObsidian

    Row(
        modifier = Modifier
            .width(215.dp)
            .height(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag("timeline_shot_${index + 1}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (shot.backdropPreset.isToonVectorScene && shot.mediaType == ShotMediaType.PRESET_SCENE) {
                val sceneBgColor = when (shot.backdropPreset) {
                    StudioBackdropPreset.TOON_CANAL -> Color(0xFF1F7A74)
                    StudioBackdropPreset.TOON_STAGE -> Color(0xFF9F1C24)
                    StudioBackdropPreset.TOON_HOUSE -> Color(0xFF4A7856)
                    else -> Color(0xFF1E293B)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(sceneBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shot.backdropPreset.id.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF2A900)
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = shot.backdropPreset.drawableRes),
                    contentDescription = shot.headline,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(if (isSelected) BroadcastCrimson else Color(0xCC090D16))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "#${index + 1}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            if (shot.mediaType == ShotMediaType.GALLERY_VIDEO) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(BroadcastEmerald)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text("VID", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = shot.highlightWord.ifBlank { shot.categoryTag.ifBlank { "শট #${index + 1}" } },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = BroadcastAmber,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${"%.1f".format(shot.durationSeconds)}s",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
            }

            Text(
                text = shot.headline.ifBlank { "(হেডলাইন হাইড করা)" },
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${shot.backdropPreset.banglaTitle} • ${shot.toonWho.banglaLabel}",
                fontSize = 9.sp,
                color = BroadcastEmerald,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InlineShotInspectorPanel(
    project: NewsProject,
    selectedShotIndex: Int,
    selectedShot: NewsShot,
    activeTab: InspectorTab,
    onSelectTab: (InspectorTab) -> Unit,
    onUpdateCategory: (String) -> Unit,
    onUpdateHeadline: (String) -> Unit,
    onUpdateHighlightWord: (String) -> Unit,
    onUpdateSubheadline: (String) -> Unit,
    onUpdateStageBannerText: (String) -> Unit,
    onUpdateToonWho: (ToonCharacterWho) -> Unit,
    onUpdateToonPose: (ToonCharacterPose) -> Unit,
    onUpdateReporterLocation: (String) -> Unit,
    onUpdateChannelName: (String) -> Unit,
    onUpdateTickerText: (String) -> Unit,
    onDeleteOrRestoreElement: (CanvasElementType, Int?) -> Unit,
    onAddNewCustomText: () -> Unit,
    onUpdateCustomTextLayer: (Int, String?, Float?, Float?, Float?) -> Unit,
    onRemoveCustomTextLayer: (Int) -> Unit,
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioCardBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Inspector Tab Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioObsidian)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                InspectorTab.entries.forEach { tab ->
                    val isSelected = tab == activeTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) BroadcastCrimson else Color.Transparent)
                            .clickable { onSelectTab(tab) }
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                            .testTag("inspector_tab_${tab.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextSecondary,
                                maxLines = 1
                            )
                            Text(
                                text = tab.banglaSubtitle,
                                fontSize = 10.sp,
                                color = if (isSelected) Color(0xFFFFE4E6) else TextSecondary.copy(alpha = 0.75f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            when (activeTab) {
                InspectorTab.TEXT_SCRIPT -> {
                    TextAndLayersInspectorContent(
                        shotIndex = selectedShotIndex,
                        shot = selectedShot,
                        project = project,
                        onUpdateCategory = onUpdateCategory,
                        onUpdateHeadline = onUpdateHeadline,
                        onUpdateHighlightWord = onUpdateHighlightWord,
                        onUpdateSubheadline = onUpdateSubheadline,
                        onUpdateStageBannerText = onUpdateStageBannerText,
                        onUpdateToonWho = onUpdateToonWho,
                        onUpdateToonPose = onUpdateToonPose,
                        onUpdateReporterLocation = onUpdateReporterLocation,
                        onUpdateChannelName = onUpdateChannelName,
                        onUpdateTickerText = onUpdateTickerText,
                        onDeleteOrRestoreElement = onDeleteOrRestoreElement,
                        onAddNewCustomText = onAddNewCustomText,
                        onUpdateCustomTextLayer = onUpdateCustomTextLayer,
                        onRemoveCustomTextLayer = onRemoveCustomTextLayer
                    )
                }

                InspectorTab.MEDIA_GALLERY -> {
                    MediaAndGalleryInspectorContent(
                        shot = selectedShot,
                        project = project,
                        onPickGalleryVideoOrPhoto = onPickGalleryVideoOrPhoto,
                        onSelectBackdropPreset = onSelectBackdropPreset,
                        onPickVoiceoverAudio = onPickVoiceoverAudio,
                        onToggleStudioSynth = onToggleStudioSynth
                    )
                }

                InspectorTab.MOTION_STYLE -> {
                    KeyframeAndSfxInspectorContent(
                        shot = selectedShot,
                        onUpdateDuration = onUpdateDuration,
                        onUpdateMotionPreset = onUpdateMotionPreset,
                        onUpdateKeyframes = onUpdateKeyframes,
                        onUpdateSoundEffect = onUpdateSoundEffect,
                        onUpdateMotionGraphicOverlay = onUpdateMotionGraphicOverlay,
                        onUpdateLowerThirdStyle = onUpdateLowerThirdStyle,
                        onUpdateTextAnimation = onUpdateTextAnimation
                    )
                }

                InspectorTab.SCENE_ORDER -> {
                    SceneOrderInspectorContent(
                        project = project,
                        selectedShotIndex = selectedShotIndex,
                        onAddNewShot = onAddNewShot,
                        onDuplicateShot = onDuplicateShot,
                        onDeleteShot = onDeleteShot,
                        onMoveShotEarlier = onMoveShotEarlier,
                        onMoveShotLater = onMoveShotLater,
                        onSaveProjectJsonClick = onSaveProjectJsonClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TextAndLayersInspectorContent(
    shotIndex: Int,
    shot: NewsShot,
    project: NewsProject,
    onUpdateCategory: (String) -> Unit,
    onUpdateHeadline: (String) -> Unit,
    onUpdateHighlightWord: (String) -> Unit,
    onUpdateSubheadline: (String) -> Unit,
    onUpdateStageBannerText: (String) -> Unit,
    onUpdateToonWho: (ToonCharacterWho) -> Unit,
    onUpdateToonPose: (ToonCharacterPose) -> Unit,
    onUpdateReporterLocation: (String) -> Unit,
    onUpdateChannelName: (String) -> Unit,
    onUpdateTickerText: (String) -> Unit,
    onDeleteOrRestoreElement: (CanvasElementType, Int?) -> Unit,
    onAddNewCustomText: () -> Unit,
    onUpdateCustomTextLayer: (Int, String?, Float?, Float?, Float?) -> Unit,
    onRemoveCustomTextLayer: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TextFields,
                    contentDescription = "Text Layers",
                    tint = BroadcastAmber,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "শট #${shotIndex + 1} সংলাপ, হাইলাইট শব্দ ও ক্যারেক্টার এডিট",
                    style = MaterialTheme.typography.labelLarge,
                    color = BroadcastAmber
                )
            }

            Button(
                onClick = onAddNewCustomText,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BroadcastAmber,
                    contentColor = Color(0xFF090D16)
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("add_custom_text_layer_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ নতুন টেক্সট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Custom Floating Text Layers (if any added by user)
        shot.customTextLayers.forEachIndexed { idx, layer ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StudioObsidian)
                    .border(1.dp, BroadcastAmber.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = layer.text,
                        onValueChange = { onUpdateCustomTextLayer(idx, it, null, null, null) },
                        label = { Text("নতুন টেক্সট লেয়ার #${idx + 1}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("X:", fontSize = 11.sp, color = TextSecondary)
                        Slider(
                            value = layer.xPercent,
                            onValueChange = { onUpdateCustomTextLayer(idx, null, it, null, null) },
                            valueRange = 0.1f..0.9f,
                            modifier = Modifier.weight(1f)
                        )
                        Text("Y:", fontSize = 11.sp, color = TextSecondary)
                        Slider(
                            value = layer.yPercent,
                            onValueChange = { onUpdateCustomTextLayer(idx, null, null, it, null) },
                            valueRange = 0.1f..0.85f,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                IconButton(onClick = { onRemoveCustomTextLayer(idx) }) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Custom Text",
                        tint = BroadcastCrimson
                    )
                }
            }
        }

        // Main Headline / Caption with Delete/Hide Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = shot.headline,
                onValueChange = onUpdateHeadline,
                label = { Text("প্রধান সংলাপ / শিরোনাম (text / Headline)") },
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .testTag("shot_headline_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BroadcastCrimson,
                    unfocusedBorderColor = StudioCardBorder
                )
            )
            IconButton(
                onClick = { onDeleteOrRestoreElement(CanvasElementType.HEADLINE, null) }
            ) {
                Icon(
                    imageVector = if (shot.showHeadline) Icons.Default.DeleteOutline else Icons.Default.VisibilityOff,
                    contentDescription = "Delete or Restore Headline",
                    tint = if (shot.showHeadline) BroadcastCrimson else TextSecondary
                )
            }
        }

        // Highlight Word (`hi`) & Stage Banner (`৫৫তম জাতীয় সমবায় দিবস`)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = shot.highlightWord,
                onValueChange = onUpdateHighlightWord,
                label = { Text("হলুদ হাইলাইট শব্দ (hi)") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("shot_highlight_word_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BroadcastAmber,
                    unfocusedBorderColor = StudioCardBorder
                )
            )

            OutlinedTextField(
                value = shot.stageBannerText,
                onValueChange = onUpdateStageBannerText,
                label = { Text("মঞ্চ ব্যানার (Stage Banner)") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // 2D Animated Character (Host / Minister / Pose) Selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = "Toon Character",
                tint = BroadcastEmerald,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "২ডি অ্যানিমেটেড ক্যারেক্টার (who) ও হাত/কথার ভঙ্গি (pose):",
                style = MaterialTheme.typography.labelMedium,
                color = BroadcastEmerald
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToonCharacterWho.entries.forEach { whoOption ->
                val selected = shot.toonWho == whoOption
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) BroadcastCrimson else StudioObsidian)
                        .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                        .clickable { onUpdateToonWho(whoOption) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = whoOption.banglaLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            ToonCharacterPose.entries.forEach { poseOption ->
                val selected = shot.toonPose == poseOption
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) BroadcastAmber else StudioObsidian)
                        .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                        .clickable { onUpdateToonPose(poseOption) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = poseOption.banglaLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color(0xFF090D16) else TextPrimary
                    )
                }
            }
        }

        // Subheadline with Delete/Hide Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = shot.subheadline,
                onValueChange = onUpdateSubheadline,
                label = { Text("উপ-শিরোনাম (Sub-headline)") },
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .testTag("shot_subheadline_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BroadcastCrimson,
                    unfocusedBorderColor = StudioCardBorder
                )
            )
            IconButton(
                onClick = { onDeleteOrRestoreElement(CanvasElementType.SUBHEADLINE, null) }
            ) {
                Icon(
                    imageVector = if (shot.showSubheadline) Icons.Default.DeleteOutline else Icons.Default.VisibilityOff,
                    contentDescription = "Delete or Restore Subheadline",
                    tint = if (shot.showSubheadline) BroadcastCrimson else TextSecondary
                )
            }
        }

        // Category Tag & Location
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = shot.categoryTag,
                onValueChange = onUpdateCategory,
                label = { Text("ক্যাটাগরি ট্যাগ") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("shot_category_input")
            )
            IconButton(
                onClick = { onDeleteOrRestoreElement(CanvasElementType.CATEGORY_BADGE, null) }
            ) {
                Icon(
                    imageVector = if (shot.showCategoryBadge) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle Category Badge",
                    tint = if (shot.showCategoryBadge) BroadcastAmber else TextSecondary
                )
            }
            OutlinedTextField(
                value = shot.reporterLocation,
                onValueChange = onUpdateReporterLocation,
                label = { Text("লোকেশন টেক্সট") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Channel Name & Bottom Ticker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = project.channelName,
                onValueChange = onUpdateChannelName,
                label = { Text("চ্যানেল নাম") },
                singleLine = true,
                modifier = Modifier.weight(0.4f)
            )
            OutlinedTextField(
                value = project.tickerText,
                onValueChange = onUpdateTickerText,
                label = { Text("স্ক্রলিং সংবাদ (Ticker)") },
                singleLine = true,
                modifier = Modifier
                    .weight(0.6f)
                    .testTag("project_ticker_input")
            )
            IconButton(
                onClick = { onDeleteOrRestoreElement(CanvasElementType.NEWS_TICKER, null) }
            ) {
                Icon(
                    imageVector = if (project.showTicker) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle Ticker",
                    tint = if (project.showTicker) BroadcastEmerald else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MediaAndGalleryInspectorContent(
    shot: NewsShot,
    project: NewsProject,
    onPickGalleryVideoOrPhoto: () -> Unit,
    onSelectBackdropPreset: (StudioBackdropPreset) -> Unit,
    onPickVoiceoverAudio: () -> Unit,
    onToggleStudioSynth: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "গ্যালারি থেকে ভিডিও ক্লিপ, ফটো এবং ভয়েসওভার অডিও ইনপুট করুন:",
            style = MaterialTheme.typography.labelLarge,
            color = BroadcastAmber
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onPickGalleryVideoOrPhoto,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("import_gallery_photo_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BroadcastCrimson,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = "Import Video or Photo from Gallery",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "গ্যালারি ভিডিও / ফটো ইনপুট",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onPickVoiceoverAudio,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("import_voiceover_audio_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StudioSurfaceElevated,
                    contentColor = BroadcastAmber
                )
            ) {
                Icon(
                    imageVector = Icons.Default.AudioFile,
                    contentDescription = "Import Voiceover Audio",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ভয়েসওভার অডিও ইনপুট",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (shot.customMediaName != null) {
            Text(
                text = "✓ বর্তমান শটে যুক্ত মিডিয়া: ${shot.customMediaName}",
                style = MaterialTheme.typography.labelLarge,
                color = BroadcastEmerald
            )
        }

        Text(
            text = "অথবা ২ডি অ্যানিমেটেড দৃশ্য (canal / stage / house) ও টিভি স্টুডিও ব্যাকগ্রাউন্ড সিলেক্ট করুন:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StudioBackdropPreset.entries.forEach { preset ->
                val isSelected = shot.mediaType == ShotMediaType.PRESET_SCENE && shot.backdropPreset == preset
                Column(
                    modifier = Modifier
                        .width(126.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioObsidian)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) BroadcastCrimson else StudioCardBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectBackdropPreset(preset) }
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (preset.isToonVectorScene) {
                        val bgCol = when (preset) {
                            StudioBackdropPreset.TOON_CANAL -> Color(0xFF1F7A74)
                            StudioBackdropPreset.TOON_STAGE -> Color(0xFF9F1C24)
                            else -> Color(0xFF4A7856)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(bgCol),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "2D • ${preset.id.uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF2A900)
                            )
                        }
                    } else {
                        Image(
                            painter = painterResource(id = preset.drawableRes),
                            contentDescription = preset.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = preset.banglaTitle,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) BroadcastAmber else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(StudioObsidian)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "নিউজরুম ব্যাকগ্রাউন্ড মিউজিক ও সাউন্ড ইফেক্ট",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Text(
                    text = project.customAudioName?.let { "কাস্টম অডিও: $it" }
                        ?: "MP4 ভিডিও রেন্ডারের সময় ব্যাকগ্রাউন্ড অডিও ও SFX যুক্ত থাকবে",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Switch(
                checked = project.useStudioSynthAudio,
                onCheckedChange = onToggleStudioSynth,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = BroadcastEmerald
                )
            )
        }
    }
}

@Composable
private fun KeyframeAndSfxInspectorContent(
    shot: NewsShot,
    onUpdateDuration: (Float) -> Unit,
    onUpdateMotionPreset: (MotionPreset) -> Unit,
    onUpdateKeyframes: ((KeyframeConfig) -> KeyframeConfig) -> Unit,
    onUpdateSoundEffect: (SoundEffectType) -> Unit,
    onUpdateMotionGraphicOverlay: (MotionGraphicOverlay) -> Unit,
    onUpdateLowerThirdStyle: (LowerThirdStyle) -> Unit,
    onUpdateTextAnimation: (TextAnimationStyle) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // 1. Keyframe Controls (Start Frame -> End Frame Scale & Pan)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = "Keyframes",
                tint = BroadcastAmber,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "অ্যানিমেশন কী-ফ্রেম কন্ট্রোল (Keyframe Start → End) ও শট সময়:",
                style = MaterialTheme.typography.labelLarge,
                color = BroadcastAmber
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "শট স্থায়িত্ব (Duration): ${"%.1f".format(shot.durationSeconds)}s",
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Slider(
                    value = shot.durationSeconds,
                    onValueChange = onUpdateDuration,
                    valueRange = 2.0f..15.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = BroadcastAmber,
                        activeTrackColor = BroadcastCrimson
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "শুরু জুম (Start Keyframe): ${"%.2f".format(shot.keyframes.startScale)}x",
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Slider(
                    value = shot.keyframes.startScale,
                    onValueChange = { s -> onUpdateKeyframes { it.copy(startScale = s) } },
                    valueRange = 0.85f..1.50f,
                    colors = SliderDefaults.colors(
                        thumbColor = BroadcastEmerald,
                        activeTrackColor = BroadcastEmerald
                    )
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "শেষ জুম (End Keyframe): ${"%.2f".format(shot.keyframes.endScale)}x",
                    fontSize = 11.sp,
                    color = TextPrimary
                )
                Slider(
                    value = shot.keyframes.endScale,
                    onValueChange = { s -> onUpdateKeyframes { it.copy(endScale = s) } },
                    valueRange = 0.85f..1.60f,
                    colors = SliderDefaults.colors(
                        thumbColor = BroadcastCrimson,
                        activeTrackColor = BroadcastCrimson
                    )
                )
            }
        }

        // Motion Preset Quick Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MotionPreset.entries.forEach { preset ->
                val selected = shot.motionPreset == preset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) BroadcastCrimson else StudioObsidian)
                        .border(
                            1.dp,
                            if (selected) BroadcastCrimson else StudioCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onUpdateMotionPreset(preset) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = preset.banglaLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = preset.label,
                            fontSize = 10.sp,
                            color = if (selected) Color(0xFFFFE4E6) else TextSecondary
                        )
                    }
                }
            }
        }

        // 2. Sound Effects (SFX) Selector with Instant Preview
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Sound Effects",
                tint = BroadcastEmerald,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "সাউন্ড ইফেক্ট (SFX — ক্লিক করলে সাউন্ড বাজবে):",
                style = MaterialTheme.typography.labelLarge,
                color = BroadcastEmerald
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoundEffectType.entries.forEach { sfx ->
                val selected = shot.soundEffect == sfx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) BroadcastEmerald else StudioObsidian)
                        .border(
                            1.dp,
                            if (selected) BroadcastEmerald else StudioCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onUpdateSoundEffect(sfx) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = sfx.banglaLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color(0xFF052E16) else TextPrimary
                        )
                        Text(
                            text = sfx.label,
                            fontSize = 10.sp,
                            color = if (selected) Color(0xFF052E16) else TextSecondary
                        )
                    }
                }
            }
        }

        // 3. Motion Graphics Overlay & Lower-Third Theme
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Motion Graphics",
                tint = BroadcastAmber,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "মোশন গ্রাফিক্স ও লোয়ার-থার্ড কালার থিম:",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MotionGraphicOverlay.entries.forEach { overlay ->
                val selected = shot.motionGraphicOverlay == overlay
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selected) BroadcastAmber else StudioObsidian)
                        .border(1.dp, StudioCardBorder, RoundedCornerShape(9.dp))
                        .clickable { onUpdateMotionGraphicOverlay(overlay) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = overlay.banglaLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color(0xFF090D16) else TextPrimary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LowerThirdStyle.entries.forEach { style ->
                val selected = shot.lowerThirdStyle == style
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioObsidian)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) BroadcastAmber else StudioCardBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onUpdateLowerThirdStyle(style) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(style.primaryHex or 0xFF000000))
                    )
                    Text(
                        text = style.banglaLabel,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) BroadcastAmber else TextPrimary
                    )
                }
            }

            TextAnimationStyle.entries.forEach { anim ->
                val selected = shot.textAnimation == anim
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) StudioSurfaceElevated else StudioObsidian)
                        .border(
                            1.dp,
                            if (selected) BroadcastEmerald else StudioCardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onUpdateTextAnimation(anim) }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = anim.banglaLabel,
                        fontSize = 12.sp,
                        color = if (selected) BroadcastEmerald else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun SceneOrderInspectorContent(
    project: NewsProject,
    selectedShotIndex: Int,
    onAddNewShot: () -> Unit,
    onDuplicateShot: () -> Unit,
    onDeleteShot: () -> Unit,
    onMoveShotEarlier: () -> Unit,
    onMoveShotLater: () -> Unit,
    onSaveProjectJsonClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ViewCarousel,
                    contentDescription = "Scene & Motion Controls",
                    tint = BroadcastAmber,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "শট ও টাইমলাইন কন্ট্রোল — Shot ${selectedShotIndex + 1} / ${project.shots.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = BroadcastAmber
                )
            }

            OutlinedButton(
                onClick = onSaveProjectJsonClick,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = "Save JSON Project",
                    tint = BroadcastEmerald,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save .JSON", fontSize = 11.sp, color = BroadcastEmerald)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddNewShot,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scene_add_shot_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BroadcastCrimson,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Shot",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ নতুন শট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onDuplicateShot,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scene_duplicate_shot_button"),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplicate Shot",
                    modifier = Modifier.size(16.dp),
                    tint = BroadcastAmber
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("ডুপ্লিকেট", fontSize = 12.sp, color = BroadcastAmber)
            }

            OutlinedButton(
                onClick = onDeleteShot,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scene_delete_shot_button"),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Shot",
                    modifier = Modifier.size(18.dp),
                    tint = BroadcastCrimson
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("ডিলিট শট", fontSize = 12.sp, color = BroadcastCrimson)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onMoveShotEarlier,
                enabled = selectedShotIndex > 0,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scene_move_earlier_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Move Shot Earlier",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("আগে নিন (Move Earlier)", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = onMoveShotLater,
                enabled = selectedShotIndex < project.shots.lastIndex,
                modifier = Modifier
                    .weight(1f)
                    .testTag("scene_move_later_button"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("পরে নিন (Move Later)", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Move Shot Later",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

fun formatSeconds(seconds: Float): String {
    val totalSec = seconds.coerceAtLeast(0f)
    val mins = (totalSec / 60).toInt()
    val secs = totalSec % 60
    return "%02d:%04.1f".format(mins, secs)
}
