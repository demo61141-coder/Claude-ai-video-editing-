package com.example.ui

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CanvasElementType
import com.example.model.ExportMp4State
import com.example.model.MotionGraphicOverlay
import com.example.model.NewsProject
import com.example.model.NewsShot
import com.example.model.SoundEffectType
import com.example.model.VideoBitratePreset
import com.example.model.VideoExportConfig
import com.example.model.VideoResolutionPreset
import com.example.ui.theme.BroadcastAmber
import com.example.ui.theme.BroadcastCrimson
import com.example.ui.theme.BroadcastEmerald
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioObsidian
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PasteOrEditCodeDialog(
    codeDraft: String,
    onCodeChange: (String) -> Unit,
    onLoadSampleJson: () -> Unit,
    onLoadSampleHtml: () -> Unit,
    onPickFileFromDevice: () -> Unit,
    onImportAndLaunch: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = StudioSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Project Code Input",
                            tint = BroadcastCrimson
                        )
                        Column {
                            Text(
                                text = "প্রজেক্ট ফাইল বা কোড ইনপুট (JSON / SRT / Script)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "কোড পেস্ট করুন অথবা ফোনের স্টোরেজ থেকে ফাইল সিলেক্ট করুন",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onLoadSampleJson,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sample JSON", fontSize = 12.sp, color = BroadcastEmerald)
                    }
                    OutlinedButton(
                        onClick = onLoadSampleHtml,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sample Script", fontSize = 12.sp, color = BroadcastAmber)
                    }
                    OutlinedButton(
                        onClick = onPickFileFromDevice,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Upload File", fontSize = 12.sp, color = TextPrimary)
                    }
                }

                OutlinedTextField(
                    value = codeDraft,
                    onValueChange = onCodeChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .testTag("code_editor_text_field"),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = Color(0xFFE2E8F0)
                    ),
                    placeholder = {
                        Text(
                            text = "Paste JSON, SRT, XML, or script here...",
                            color = TextSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = StudioObsidian,
                        unfocusedContainerColor = StudioObsidian,
                        focusedBorderColor = BroadcastCrimson,
                        unfocusedBorderColor = StudioCardBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("বাতিল (Cancel)", color = TextSecondary)
                    }
                    Button(
                        onClick = { onImportAndLaunch(codeDraft) },
                        modifier = Modifier
                            .weight(2f)
                            .testTag("confirm_import_code_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BroadcastCrimson,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Launch Editor",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ইমপোর্ট ও এডিটরে যান",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive On-Canvas Element Inspector Dialog:
 * Opens immediately when the user clicks on any part of the Live Video Preview Canvas
 * so they can fix text, delete text, add new text layers, fix keyframes, sound effects,
 * motion graphics, or replace video/photo from gallery!
 */
@Composable
fun QuickCanvasInlineEditDialog(
    shotIndex: Int,
    shot: NewsShot,
    project: NewsProject,
    selectedElement: CanvasElementType,
    selectedCustomTextIndex: Int?,
    onSelectElement: (CanvasElementType, Int?) -> Unit,
    onUpdateCategory: (String) -> Unit,
    onUpdateHeadline: (String) -> Unit,
    onUpdateSubheadline: (String) -> Unit,
    onUpdateTicker: (String) -> Unit,
    onUpdateChannelName: (String) -> Unit,
    onDeleteOrRestoreElement: (CanvasElementType, Int?) -> Unit,
    onAddNewCustomText: () -> Unit,
    onUpdateCustomTextLayer: (Int, String?, Float?, Float?, Float?) -> Unit,
    onUpdateSoundEffect: (SoundEffectType) -> Unit,
    onUpdateMotionGraphicOverlay: (MotionGraphicOverlay) -> Unit,
    onPickGalleryVideoOrPhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .border(1.5.dp, BroadcastAmber, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = StudioSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ক্যানভাস এলিমেন্ট এডিটর (Shot #${shotIndex + 1})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BroadcastAmber
                        )
                        Text(
                            text = "সিলেক্টেড: ${selectedElement.banglaName} (${selectedElement.displayName})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    TextButton(onClick = onDismiss) {
                        Text("সম্পন্ন (Done)", color = BroadcastEmerald, fontWeight = FontWeight.Bold)
                    }
                }

                // Element Switcher Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CanvasElementType.entries.forEach { el ->
                        val active = el == selectedElement
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (active) BroadcastCrimson else StudioObsidian)
                                .border(
                                    1.dp,
                                    if (active) BroadcastCrimson else StudioCardBorder,
                                    RoundedCornerShape(50)
                                )
                                .clickable { onSelectElement(el, null) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = el.banglaName,
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = Color.White
                            )
                        }
                    }
                }

                // Context-Aware Editor for the Clicked Canvas Element
                when (selectedElement) {
                    CanvasElementType.HEADLINE -> {
                        OutlinedTextField(
                            value = shot.headline,
                            onValueChange = onUpdateHeadline,
                            label = { Text("প্রধান শিরোনাম (Main Headline)") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    CanvasElementType.SUBHEADLINE -> {
                        OutlinedTextField(
                            value = shot.subheadline,
                            onValueChange = onUpdateSubheadline,
                            label = { Text("উপ-শিরোনাম (Sub-headline)") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    CanvasElementType.CATEGORY_BADGE -> {
                        OutlinedTextField(
                            value = shot.categoryTag,
                            onValueChange = onUpdateCategory,
                            label = { Text("ক্যাটাগরি ট্যাগ (যেমন: ব্রেকিং নিউজ)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    CanvasElementType.LIVE_CHANNEL_BUG -> {
                        OutlinedTextField(
                            value = project.channelName,
                            onValueChange = onUpdateChannelName,
                            label = { Text("চ্যানেল লোগো টেক্সট (Channel Name)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    CanvasElementType.NEWS_TICKER -> {
                        OutlinedTextField(
                            value = project.tickerText,
                            onValueChange = onUpdateTicker,
                            label = { Text("নিচের স্ক্রলিং সংবাদ (Scrolling Ticker)") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    CanvasElementType.CUSTOM_TEXT_LAYER -> {
                        val layerIdx = selectedCustomTextIndex ?: 0
                        val layer = shot.customTextLayers.getOrNull(layerIdx)
                        if (layer != null) {
                            OutlinedTextField(
                                value = layer.text,
                                onValueChange = { onUpdateCustomTextLayer(layerIdx, it, null, null, null) },
                                label = { Text("কাস্টম টেক্সট লেয়ার #${layerIdx + 1}") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "অনুভূমিক পজিশন (X: ${(layer.xPercent * 100).toInt()}%) ও উল্লম্ব পজিশন (Y: ${(layer.yPercent * 100).toInt()}%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Slider(
                                    value = layer.xPercent,
                                    onValueChange = { onUpdateCustomTextLayer(layerIdx, null, it, null, null) },
                                    valueRange = 0.1f..0.9f,
                                    modifier = Modifier.weight(1f)
                                )
                                Slider(
                                    value = layer.yPercent,
                                    onValueChange = { onUpdateCustomTextLayer(layerIdx, null, null, it, null) },
                                    valueRange = 0.1f..0.85f,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Text(
                                text = "কোনো অতিরিক্ত টেক্সট লেয়ার নেই। নিচে '+ নতুন টেক্সট এড করুন' বাটনে চাপুন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    CanvasElementType.BACKDROP_MEDIA -> {
                        Text(
                            text = "ব্যাকগ্রাউন্ড ভিডিও/ফটো, মোশন গ্রাফিক্স ও সাউন্ড ইফেক্ট পরিবর্তন করুন:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Button(
                            onClick = onPickGalleryVideoOrPhoto,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = BroadcastCrimson)
                        ) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("গ্যালারি থেকে ভিডিও বা ফটো ইনপুট করুন")
                        }
                    }
                }

                // Quick Action Row: Delete Selected Text / Add New Text / Replace Media
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDeleteOrRestoreElement(selectedElement, selectedCustomTextIndex) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BroadcastCrimson)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete or Toggle Element",
                            tint = BroadcastCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ডিলিট / হাইড", fontSize = 12.sp, color = BroadcastCrimson)
                    }

                    Button(
                        onClick = onAddNewCustomText,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BroadcastAmber,
                            contentColor = Color(0xFF090D16)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add New Text",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ নতুন টেক্সট এড",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Quick Fix for Motion Graphics & Sound Effects (SFX)
                Text(
                    text = "সাউন্ড ইফেক্ট (SFX) ও মোশন গ্রাফিক্স ঠিক করুন:",
                    style = MaterialTheme.typography.labelLarge,
                    color = BroadcastEmerald
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SoundEffectType.entries.forEach { sfx ->
                        val active = shot.soundEffect == sfx
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) BroadcastEmerald else StudioObsidian)
                                .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                                .clickable { onUpdateSoundEffect(sfx) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sfx.banglaLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color(0xFF052E16) else TextPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MotionGraphicOverlay.entries.forEach { overlay ->
                        val active = shot.motionGraphicOverlay == overlay
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) BroadcastAmber else StudioObsidian)
                                .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                                .clickable { onUpdateMotionGraphicOverlay(overlay) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = overlay.banglaLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color(0xFF090D16) else TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Professional Video Export Modal with Resolution (144p, 360p, 480p, 720p, 1080p, 1480p),
 * Bitrate (1 Mbps - 16 Mbps), FPS (24, 30, 60), and Turbo Hardware MP4 Encoding!
 */
@Composable
fun ExportMp4Modal(
    exportState: ExportMp4State,
    exportConfig: VideoExportConfig,
    onSelectResolution: (VideoResolutionPreset) -> Unit,
    onSelectBitrate: (VideoBitratePreset) -> Unit,
    onSelectFps: (Int) -> Unit,
    onStartRenderClick: () -> Unit,
    onSaveToDeviceClick: (String) -> Unit,
    onShareVideoClick: () -> Unit,
    onDismiss: () -> Unit
) {
    if (exportState is ExportMp4State.Idle) return

    Dialog(
        onDismissRequest = {
            if (exportState !is ExportMp4State.Rendering) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .border(1.dp, StudioCardBorder, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = StudioSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                when (exportState) {
                    is ExportMp4State.ConfiguringSettings -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MovieCreation,
                                    contentDescription = "Export Settings",
                                    tint = BroadcastCrimson
                                )
                                Column {
                                    Text(
                                        text = "ভিডিও রেন্ডার ও এক্সপোর্ট সেটিংস (MP4)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "রেজোলিউশন, বিটরেট (Bitrate) ও ফ্রেমরেট সিলেক্ট করুন",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = TextSecondary
                                )
                            }
                        }

                        // 1. Resolution Selection (144p, 360p, 480p, 720p, 1080p, 1480p)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "১. ভিডিও রেজোলিউশন (Resolution: 144p – 1480p):",
                                style = MaterialTheme.typography.labelLarge,
                                color = BroadcastAmber
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VideoResolutionPreset.entries.forEach { res ->
                                    val selected = exportConfig.resolution == res
                                    Column(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (selected) BroadcastCrimson else StudioObsidian)
                                            .border(
                                                width = if (selected) 2.dp else 1.dp,
                                                color = if (selected) BroadcastAmber else StudioCardBorder,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { onSelectResolution(res) }
                                            .padding(horizontal = 14.dp, vertical = 10.dp)
                                            .testTag("export_res_${res.label}"),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = res.label,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = res.subLabel,
                                            fontSize = 10.sp,
                                            color = if (selected) Color(0xFFFFE4E6) else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Bitrate Selection
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "২. ভিডিও বিটরেট (Bitrate Quality):",
                                style = MaterialTheme.typography.labelLarge,
                                color = BroadcastEmerald
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VideoBitratePreset.entries.forEach { br ->
                                    val selected = exportConfig.bitratePreset == br
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (selected) BroadcastEmerald else StudioObsidian)
                                            .border(
                                                1.dp,
                                                if (selected) BroadcastEmerald else StudioCardBorder,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { onSelectBitrate(br) }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = br.label,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color(0xFF052E16) else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Frame Rate (FPS) Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "৩. ফ্রেম রেট (Frame Rate):",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(24, 30, 60).forEach { fpsVal ->
                                    val selected = exportConfig.fps == fpsVal
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (selected) BroadcastAmber else StudioObsidian)
                                            .border(1.dp, StudioCardBorder, RoundedCornerShape(8.dp))
                                            .clickable { onSelectFps(fpsVal) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "$fpsVal FPS",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color(0xFF090D16) else TextPrimary
                                        )
                                    }
                                }
                            }
                        }

                        // Start Fast Hardware Render Button
                        Button(
                            onClick = onStartRenderClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_mp4_render_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BroadcastCrimson,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Turbo Render",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "দ্রুত ভিডিও সেভ / রেন্ডার করুন (${exportConfig.resolution.label} MP4)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    is ExportMp4State.Rendering -> {
                        CircularProgressIndicator(
                            color = BroadcastCrimson,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "দ্রুত গতিতে ভিডিও রেন্ডার হচ্ছে...",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "${exportState.resolutionLabel} • ${exportState.bitrateLabel}",
                            style = MaterialTheme.typography.labelLarge,
                            color = BroadcastEmerald
                        )
                        Text(
                            text = exportState.statusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = BroadcastAmber
                        )
                        LinearProgressIndicator(
                            progress = { exportState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = BroadcastCrimson,
                            trackColor = StudioSurfaceElevated
                        )
                        TextButton(onClick = onDismiss) {
                            Text("Cancel Render", color = TextSecondary)
                        }
                    }

                    is ExportMp4State.Completed -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Export Complete",
                            tint = BroadcastEmerald,
                            modifier = Modifier.size(54.dp)
                        )
                        Text(
                            text = "MP4 ভিডিও তৈরি সম্পন্ন!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "${exportState.fileName}\n${exportState.resolutionLabel} • ${exportState.bitrateLabel}\nসময়: ${"%.1f".format(exportState.durationSeconds)}s • ফাইল সাইজ: ${exportState.fileSizeKb} KB • রেন্ডার সময়: ${"%.1f".format(exportState.elapsedMs / 1000f)}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Button(
                            onClick = { onSaveToDeviceClick(exportState.fileName) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_exported_mp4_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BroadcastCrimson,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Save MP4",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ফোনে ভিডিও সেভ করুন (Save MP4)",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onShareVideoClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share MP4",
                                    modifier = Modifier.size(18.dp),
                                    tint = BroadcastAmber
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Video", color = BroadcastAmber)
                            }
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Close", color = TextSecondary)
                            }
                        }
                    }

                    is ExportMp4State.Error -> {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Export Error",
                            tint = BroadcastCrimson,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "রেন্ডার ব্যর্থ হয়েছে",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = exportState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = BroadcastCrimson)
                        ) {
                            Text("Close")
                        }
                    }

                    ExportMp4State.Idle -> Unit
                }
            }
        }
    }
}
