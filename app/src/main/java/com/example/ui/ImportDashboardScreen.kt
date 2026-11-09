package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.NewsProject
import com.example.model.SampleNewsProjects
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
fun ImportDashboardScreen(
    currentProject: NewsProject,
    onPickCodeFileClick: () -> Unit,
    onOpenPasteCodeSheet: () -> Unit,
    onOpenTemplateProject: (NewsProject) -> Unit,
    onResumeCurrentEditor: () -> Unit
) {
    val templates = remember {
        listOf(
            SampleNewsProjects.primeTimeBulletin(),
            SampleNewsProjects.breakingNewsAlert(),
            SampleNewsProjects.economyAndInfrastructureReport()
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = StudioObsidian
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. Hero Broadcast Video Editor Header
                item {
                    HeroBroadcastBanner(
                        onResumeEditor = onResumeCurrentEditor
                    )
                }

                // 2. Primary Action: Import Video Project File (.JSON / .SRT / .XML / Script)
                item {
                    ImportCodeHeroSection(
                        onPickCodeFileClick = onPickCodeFileClick,
                        onOpenPasteCodeSheet = onOpenPasteCodeSheet
                    )
                }

                // 3. Pre-Built Bangla Broadcast Video Projects for Instant Timeline Editing
                item {
                    Column {
                        Text(
                            text = "রেডিমেড ভিডিও প্রজেক্ট টেমপ্লেট (Instant Video Projects)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "যেকোনো প্রজেক্টে ক্লিক করলেই সরাসরি ভিডিও এডিটিং পেজ ও লাইভ প্রিভিউ ওপেন হবে",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                items(templates, key = { it.projectName }) { template ->
                    TemplateProjectCard(
                        project = template,
                        onClick = { onOpenTemplateProject(template) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBroadcastBanner(
    onResumeEditor: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StudioCardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_news_studio_bg_1794205289445),
                contentDescription = "Bangla News Studio Pro Video Editor",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x88080C14),
                                Color(0xCC080C14),
                                Color(0xF5080C14)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(BroadcastCrimson)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BANGLA NEWS STUDIO PRO",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xCC111827))
                            .border(1.dp, BroadcastEmerald.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "144p • 360p • 480p • 720p • 1480p MP4",
                            style = MaterialTheme.typography.labelMedium,
                            color = BroadcastEmerald
                        )
                    }
                }

                Column {
                    Text(
                        text = "বাংলা নিউজ স্টুডিও প্রো — প্রফেশনাল ভিডিও এডিটর",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "প্রজেক্ট ফাইল ইনপুট দিন, প্রিভিউর ওপর ক্লিক করে টেক্সট এডিট/ডিলিট করুন, গ্যালারি থেকে ভিডিও বা ফটো যুক্ত করুন এবং দ্রুত MP4 রেন্ডার করুন।",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE2E8F0),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onResumeEditor,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BroadcastAmber,
                            contentColor = Color(0xFF090D16)
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("open_direct_editor_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Open Direct Video Editor",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "সরাসরি ভিডিও এডিটিং পেজে যান (Open Editor)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportCodeHeroSection(
    onPickCodeFileClick: () -> Unit,
    onOpenPasteCodeSheet: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(BroadcastCrimson, BroadcastAmber)
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BroadcastCrimson.copy(alpha = 0.18f))
                        .border(1.dp, BroadcastCrimson, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.UploadFile,
                        contentDescription = "Import Video Project File",
                        tint = BroadcastCrimson,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "১. ভিডিও প্রজেক্ট বা কোড ফাইল ইনপুট করুন",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "JSON, SRT, XML, স্ক্রিপ্ট বা প্রজেক্ট ফাইল সিলেক্ট করলে সরাসরি ভিডিও এডিটিং ও প্রিভিউ পেজে নিয়ে যাবে",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Prominent Primary Button: Input Project File
            Button(
                onClick = onPickCodeFileClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("import_code_file_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BroadcastCrimson,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Input Video Project File",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "প্রজেক্ট ফাইল ইনপুট করুন (JSON / SRT / Script)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Secondary Button: Paste Project Code / JSON
            OutlinedButton(
                onClick = onOpenPasteCodeSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("paste_code_button"),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary,
                    containerColor = StudioSurfaceElevated
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste Project Code",
                    tint = BroadcastAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "কোড বা স্ক্রিপ্ট পেস্ট করে এডিটর ওপেন করুন",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun TemplateProjectCard(
    project: NewsProject,
    onClick: () -> Unit
) {
    val firstShot = project.shots.firstOrNull()
    val previewRes = firstShot?.backdropPreset?.drawableRes ?: R.drawable.img_news_studio_bg_1794205289445

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(1.dp, StudioCardBorder, RoundedCornerShape(16.dp))
            .testTag("template_card_${project.channelName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(116.dp)
                    .height(74.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, StudioCardBorder, RoundedCornerShape(10.dp))
            ) {
                Image(
                    painter = painterResource(id = previewRes),
                    contentDescription = project.projectName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .background(BroadcastCrimson)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${project.shots.size} SHOTS",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = project.channelName,
                        style = MaterialTheme.typography.labelMedium,
                        color = BroadcastAmber
                    )
                    Text(
                        text = "• ${project.totalDurationSeconds.toInt()}s Video",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                }
                Text(
                    text = project.projectName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = firstShot?.headline ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(BroadcastCrimson.copy(alpha = 0.16f))
                    .border(1.dp, BroadcastCrimson, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MovieCreation,
                    contentDescription = "Edit in Timeline",
                    tint = BroadcastCrimson,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
