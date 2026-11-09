package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AiPromptTemplates
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
            SampleNewsProjects.banglaToonCanalNewsReel(),
            SampleNewsProjects.primeTimeBulletin(),
            SampleNewsProjects.breakingNewsAlert()
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

                // 2. Primary Action: Import Video Project File (.HTML / .JS / .JSON / .SRT) or Paste Code
                item {
                    ImportCodeHeroSection(
                        onPickCodeFileClick = onPickCodeFileClick,
                        onOpenPasteCodeSheet = onOpenPasteCodeSheet
                    )
                }

                // 3. Guided AI Prompt Hub & 1-Click Copy with Script Box (for first-time users!)
                item {
                    AiPromptGeneratorGuideCard(
                        onOpenPasteCodeSheet = onOpenPasteCodeSheet
                    )
                }

                // 4. Pre-Built Bangla Toon & Broadcast Video Projects for Instant Timeline Editing
                item {
                    Column {
                        Text(
                            text = "রেডিমেড ভিডিও প্রজেক্ট টেমপ্লেট (Bangla Toon ও TV News)",
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

/**
 * Guided AI Prompt Hub for new users:
 * Explains how the app works in 3 steps, provides 4 ready-made prompts, embeds the user's news script
 * at the bottom of the prompt, and lets them copy the entire prompt in 1 click!
 */
@Composable
private fun AiPromptGeneratorGuideCard(
    onOpenPasteCodeSheet: () -> Unit
) {
    val context = LocalContext.current
    val prompts = remember { AiPromptTemplates.readyMadePrompts }
    var selectedPromptIndex by remember { mutableIntStateOf(0) }
    val selectedPrompt = prompts[selectedPromptIndex]

    var userScriptText by remember(selectedPromptIndex) {
        mutableStateOf(selectedPrompt.sampleScriptBangla)
    }
    var showFullPromptPreview by remember { mutableStateOf(false) }
    var copiedPromptTitle by remember { mutableStateOf<String?>(null) }

    val fullPromptText = remember(selectedPrompt, userScriptText) {
        AiPromptTemplates.buildFullCopyablePrompt(selectedPrompt, userScriptText)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(BroadcastAmber, BroadcastEmerald)
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = StudioSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BroadcastAmber.copy(alpha = 0.18f))
                        .border(1.dp, BroadcastAmber, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Prompt Guide",
                        tint = BroadcastAmber,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "নতুন ইউজার গাইড: AI প্রম্পট দিয়ে সহজেই ভিডিও তৈরি করুন",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BroadcastAmber
                    )
                    Text(
                        text = "১. নিচের রেডিমেড প্রম্পট ও আপনার স্ক্রিপ্ট ১-ক্লিকে কপি করুন → ২. যেকোনো AI (ChatGPT/Gemini)-এ পেস্ট করে ফাইল/কোড তৈরি করুন → ৩. সেই কোড এখানে পেস্ট করলেই ভিডিও তৈরি হয়ে যাবে!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                }
            }

            // 4 Ready-Made Prompt Selector Cards
            Text(
                text = "৪টি রেডিমেড প্রম্পট (যেকোনো একটি সিলেক্ট করুন):",
                style = MaterialTheme.typography.labelLarge,
                color = BroadcastEmerald
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                prompts.forEachIndexed { idx, item ->
                    val isSelected = idx == selectedPromptIndex
                    Column(
                        modifier = Modifier
                            .width(215.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF282012) else StudioObsidian)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) BroadcastAmber else StudioCardBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedPromptIndex = idx
                                copiedPromptTitle = null
                            }
                            .padding(10.dp)
                            .testTag("ready_prompt_card_${idx + 1}"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.badge,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) BroadcastAmber else BroadcastEmerald
                            )
                            Text(
                                text = "১-ক্লিক কপি",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BroadcastCrimson)
                                    .clickable {
                                        selectedPromptIndex = idx
                                        val textToCopy = AiPromptTemplates.buildFullCopyablePrompt(
                                            item,
                                            if (idx == selectedPromptIndex) userScriptText else item.sampleScriptBangla
                                        )
                                        copyToClipboard(context, textToCopy)
                                        copiedPromptTitle = item.titleBangla
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = item.titleBangla,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.descriptionBangla,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // User Script Box (Placed inside the bottom of the AI Prompt!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "প্রম্পটের নিচে আপনার সংবাদের স্ক্রিপ্ট রাখুন (AI এটি দেখেই ফাইল তৈরি করবে):",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { userScriptText = "" },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("স্ক্রিপ্ট মুছুন", fontSize = 11.sp, color = BroadcastCrimson)
                }
            }

            OutlinedTextField(
                value = userScriptText,
                onValueChange = { userScriptText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prompt_user_script_input"),
                minLines = 3,
                maxLines = 5,
                placeholder = {
                    Text(
                        text = "এখানে আপনার সংবাদের স্ক্রিপ্ট বা মূল ঘটনাটি বাংলায় লিখুন বা পেস্ট করুন...",
                        color = TextSecondary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = StudioObsidian,
                    unfocusedContainerColor = StudioObsidian,
                    focusedBorderColor = BroadcastAmber,
                    unfocusedBorderColor = StudioCardBorder
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "স্ক্রিপ্টটি স্বয়ংক্রিয়ভাবে প্রম্পটের নিচে যুক্ত হয়ে গেছে",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                TextButton(
                    onClick = { showFullPromptPreview = !showFullPromptPreview }
                ) {
                    Icon(
                        imageVector = if (showFullPromptPreview) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = BroadcastAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showFullPromptPreview) "প্রম্পট হাইড করুন" else "সম্পূর্ণ প্রম্পট দেখুন",
                        fontSize = 12.sp,
                        color = BroadcastAmber
                    )
                }
            }

            AnimatedVisibility(visible = showFullPromptPreview) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StudioObsidian)
                        .border(1.dp, StudioCardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = fullPromptText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0)
                    )
                }
            }

            AnimatedVisibility(visible = copiedPromptTitle != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF064E3B))
                        .border(1.dp, BroadcastEmerald, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Copied",
                        tint = BroadcastEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "প্রম্পট ও আপনার স্ক্রিপ্ট কপি হয়েছে! এখন যেকোনো AI-তে পেস্ট করে ফাইল তৈরি করুন এবং উপরে 'AI কোড পেস্ট করুন' বাটনে চাপুন।",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        copyToClipboard(context, fullPromptText)
                        copiedPromptTitle = selectedPrompt.titleBangla
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .height(50.dp)
                        .testTag("one_click_copy_prompt_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BroadcastAmber,
                        contentColor = Color(0xFF090D16)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Prompt in 1 Click",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "১-ক্লিকে প্রম্পট কপি করুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onOpenPasteCodeSheet,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("paste_ai_output_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BroadcastCrimson,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste AI Output",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI কোড পেস্ট করুন",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Bangla_News_AI_Prompt", text))
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
                        text = "বাংলা নিউজ স্টুডিও প্রো — ২ডি কার্টুন ও টিভি ভিডিও এডিটর",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Bangla Toon (HTML/JS) বা JSON প্রজেক্ট ইনপুট করুন, ক্যানভাসে ক্লিক করে যেকোনো লেখা/ক্যারেক্টার এডিট করুন এবং দ্রুত MP4 সেভ করুন।",
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
                        text = "প্রজেক্ট কোড পেস্ট করুন অথবা ফাইল ইনপুট দিন (HTML / JS / JSON)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "আপনার দেওয়া Bangla Toon — News Reel কোড বা JSON ফাইল দিলে এর সব শট, ক্যাপশন ও কার্টুন দৃশ্য সরাসরি ভিডিও এডিটরে ওপেন হবে",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Prominent Button 1: Paste Code / Script
            Button(
                onClick = onOpenPasteCodeSheet,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("paste_code_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BroadcastCrimson,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste Project Code",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "কোড বা প্রজেক্ট ফাইল এখানে পেস্ট করুন",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Secondary Button: Input Project File from Storage
            OutlinedButton(
                onClick = onPickCodeFileClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("import_code_file_button"),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextPrimary,
                    containerColor = StudioSurfaceElevated
                )
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "Input Video Project File",
                    tint = BroadcastAmber,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ফাইল ম্যানেজার থেকে প্রজেক্ট ফাইল ইনপুট করুন (.HTML / .JSON / .SRT)",
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
    val isToon = firstShot?.backdropPreset?.isToonVectorScene == true

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
                if (isToon) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF9F1C24), Color(0xFF1F7A74))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "2D TOON REEL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF2A900)
                        )
                    }
                } else {
                    Image(
                        painter = painterResource(id = previewRes),
                        contentDescription = project.projectName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
