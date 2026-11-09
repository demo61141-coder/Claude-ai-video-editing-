package com.example.data

import com.example.model.CustomTextLayer
import com.example.model.KeyframeConfig
import com.example.model.LowerThirdStyle
import com.example.model.MotionGraphicOverlay
import com.example.model.MotionPreset
import com.example.model.NewsProject
import com.example.model.NewsShot
import com.example.model.SampleNewsProjects
import com.example.model.SoundEffectType
import com.example.model.StudioBackdropPreset
import com.example.model.TextAnimationStyle
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object ProjectCodeParserAndGenerator {

    /**
     * Universal Video Project Parser:
     * Supports JSON (News Studio JSON, CapCut/Timeline JSON, Scene Arrays),
     * SRT Subtitles, XML/HTML Project Files, or Structured Plain Scripts.
     */
    fun parseCodeToProject(rawCode: String): Result<NewsProject> {
        val trimmed = rawCode.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("Project file or code is empty."))
        }

        return runCatching {
            // 1. Direct JSON Object or Array
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                return@runCatching parseJsonString(trimmed)
            }

            // 2. SRT Subtitle format (1 \n 00:00:00,000 --> 00:00:05,000)
            if (trimmed.contains("-->")) {
                return@runCatching parseSrtToProject(trimmed)
            }

            // 3. Embedded JSON inside script or code block
            val scriptRegex = Regex(
                """<script[^>]*id=["']news-project-data["'][^>]*>([\s\S]*?)</script>""",
                RegexOption.IGNORE_CASE
            )
            val scriptMatch = scriptRegex.find(trimmed)
            if (scriptMatch != null) {
                val jsonContent = scriptMatch.groupValues[1].trim()
                if (jsonContent.startsWith("{") || jsonContent.startsWith("[")) {
                    return@runCatching parseJsonString(jsonContent)
                }
            }

            // Extract any JSON object inside a code/script file if present
            val jsVarRegex = Regex(
                """(?:const|let|var)\s+\w+\s*=\s*(\{[\s\S]*?"(?:shots|scenes|tracks|clips)"[\s\S]*?\});""",
                RegexOption.IGNORE_CASE
            )
            val jsVarMatch = jsVarRegex.find(trimmed)
            if (jsVarMatch != null) {
                return@runCatching parseJsonString(jsVarMatch.groupValues[1].trim())
            }

            // 4. XML / HTML structured tags
            if (trimmed.contains("<") && trimmed.contains(">")) {
                return@runCatching parseXmlOrHtmlDomProject(trimmed)
            }

            // 5. Plain structured text script
            parsePlainScriptToProject(trimmed)
        }
    }

    private fun parseJsonString(jsonStr: String): NewsProject {
        if (jsonStr.startsWith("[")) {
            val arr = JSONArray(jsonStr)
            val shots = parseShotsArray(arr)
            return SampleNewsProjects.primeTimeBulletin().copy(
                id = UUID.randomUUID().toString(),
                projectName = "ইমপোর্ট করা ভিডিও প্রজেক্ট (JSON)",
                shots = shots.ifEmpty { SampleNewsProjects.primeTimeBulletin().shots }
            )
        }

        val root = JSONObject(jsonStr)
        val projectName = root.optString("projectName")
            .ifEmpty { root.optString("title", root.optString("name", "ইমপোর্ট করা ভিডিও প্রজেক্ট")) }
        val channelName = root.optString("channelName")
            .ifEmpty { root.optString("channel", "বাংলা নিউজ ২৪") }
        val tickerText = root.optString("tickerText")
            .ifEmpty {
                root.optString(
                    "ticker",
                    "ব্রেকিং নিউজ: সরাসরি স্টুডিও থেকে সম্প্রচারিত বিশেষ সংবাদ বুলেটিন"
                )
            }

        val shotsArray = root.optJSONArray("shots")
            ?: root.optJSONArray("scenes")
            ?: root.optJSONArray("clips")
            ?: root.optJSONArray("slides")
            ?: root.optJSONArray("timeline")
            ?: JSONArray()

        val parsedShots = parseShotsArray(shotsArray)
        return NewsProject(
            id = UUID.randomUUID().toString(),
            projectName = projectName,
            channelName = channelName,
            tickerText = tickerText,
            shots = parsedShots.ifEmpty { SampleNewsProjects.primeTimeBulletin().shots }
        )
    }

    private fun parseShotsArray(arr: JSONArray): List<NewsShot> {
        val list = mutableListOf<NewsShot>()
        val presets = StudioBackdropPreset.entries
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val category = obj.optString("categoryTag")
                .ifEmpty { obj.optString("category", obj.optString("tag", "ব্রেকিং নিউজ")) }
            val headline = obj.optString("headline")
                .ifEmpty { obj.optString("title", obj.optString("text", "সংবাদ শিরোনাম ${i + 1}")) }
            val subheadline = obj.optString("subheadline")
                .ifEmpty { obj.optString("subtitle", obj.optString("description", "বিস্তারিত প্রতিবেদন")) }
            val location = obj.optString("reporterLocation")
                .ifEmpty { obj.optString("location", "ঢাকা স্টুডিও • LIVE") }
            val duration = obj.optDouble("durationSeconds", obj.optDouble("duration", 5.0))
                .toFloat()
                .coerceIn(1.5f, 20.0f)

            val motionStr = obj.optString("motionPreset", obj.optString("motion", ""))
            val motion = MotionPreset.entries.find {
                it.name.equals(motionStr, ignoreCase = true) ||
                    it.label.contains(motionStr, ignoreCase = true)
            } ?: MotionPreset.entries[i % 5]

            val styleStr = obj.optString("lowerThirdStyle", obj.optString("style", ""))
            val style = LowerThirdStyle.entries.find {
                it.name.equals(styleStr, ignoreCase = true) ||
                    it.label.contains(styleStr, ignoreCase = true)
            } ?: LowerThirdStyle.entries[i % LowerThirdStyle.entries.size]

            val textAnimStr = obj.optString("textAnimation", "")
            val textAnim = TextAnimationStyle.entries.find {
                it.name.equals(textAnimStr, ignoreCase = true)
            } ?: TextAnimationStyle.SLIDE_UP_REVEAL

            val sfxStr = obj.optString("soundEffect", obj.optString("sfx", ""))
            val sfx = SoundEffectType.entries.find {
                it.name.equals(sfxStr, ignoreCase = true)
            } ?: SoundEffectType.BREAKING_SWOOSH

            val overlayStr = obj.optString("motionGraphicOverlay", "")
            val overlay = MotionGraphicOverlay.entries.find {
                it.name.equals(overlayStr, ignoreCase = true)
            } ?: MotionGraphicOverlay.FULL_BROADCAST_PACK

            val backdropStr = obj.optString("backdropPreset", obj.optString("backdrop", ""))
            val backdrop = presets.find {
                it.id.equals(backdropStr, ignoreCase = true) ||
                    it.name.equals(backdropStr, ignoreCase = true)
            } ?: presets[i % presets.size]

            val kfObj = obj.optJSONObject("keyframes")
            val keyframes = if (kfObj != null) {
                KeyframeConfig(
                    startScale = kfObj.optDouble("startScale", motion.defaultStartScale.toDouble()).toFloat(),
                    endScale = kfObj.optDouble("endScale", motion.defaultEndScale.toDouble()).toFloat(),
                    startOffsetX = kfObj.optDouble("startOffsetX", motion.defaultStartX.toDouble()).toFloat(),
                    endOffsetX = kfObj.optDouble("endOffsetX", motion.defaultEndX.toDouble()).toFloat(),
                    startOffsetY = kfObj.optDouble("startOffsetY", 0.0).toFloat(),
                    endOffsetY = kfObj.optDouble("endOffsetY", -10.0).toFloat()
                )
            } else {
                KeyframeConfig(
                    startScale = motion.defaultStartScale,
                    endScale = motion.defaultEndScale,
                    startOffsetX = motion.defaultStartX,
                    endOffsetX = motion.defaultEndX
                )
            }

            val customTexts = mutableListOf<CustomTextLayer>()
            val textsArr = obj.optJSONArray("customTextLayers")
            if (textsArr != null) {
                for (tIdx in 0 until textsArr.length()) {
                    val tObj = textsArr.optJSONObject(tIdx) ?: continue
                    customTexts.add(
                        CustomTextLayer(
                            text = tObj.optString("text", "টেক্সট"),
                            xPercent = tObj.optDouble("xPercent", 0.5).toFloat(),
                            yPercent = tObj.optDouble("yPercent", 0.3).toFloat(),
                            fontSizePx = tObj.optDouble("fontSizePx", 34.0).toFloat()
                        )
                    )
                }
            }

            list.add(
                NewsShot(
                    categoryTag = category,
                    headline = headline,
                    subheadline = subheadline,
                    reporterLocation = location,
                    durationSeconds = duration,
                    motionPreset = motion,
                    keyframes = keyframes,
                    lowerThirdStyle = style,
                    textAnimation = textAnim,
                    motionGraphicOverlay = overlay,
                    soundEffect = sfx,
                    backdropPreset = backdrop,
                    showCategoryBadge = obj.optBoolean("showCategoryBadge", true),
                    showHeadline = obj.optBoolean("showHeadline", true),
                    showSubheadline = obj.optBoolean("showSubheadline", true),
                    showLiveBug = obj.optBoolean("showLiveBug", true),
                    customTextLayers = customTexts
                )
            )
        }
        return list
    }

    private fun parseSrtToProject(srt: String): NewsProject {
        val blocks = srt.trim().split(Regex("\\r?\\n\\r?\\n"))
        val presets = StudioBackdropPreset.entries
        val shots = mutableListOf<NewsShot>()

        blocks.forEachIndexed { idx, block ->
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            val textLines = lines.filter { !it.matches(Regex("^\\d+$")) && !it.contains("-->") }
            if (textLines.isNotEmpty()) {
                val headline = textLines.first()
                val sub = textLines.drop(1).joinToString(" ").ifEmpty { "সাবটাইটেল শট #${idx + 1}" }
                shots.add(
                    NewsShot(
                        categoryTag = "সংবাদ আপডেট",
                        headline = headline,
                        subheadline = sub,
                        durationSeconds = 4.5f,
                        motionPreset = MotionPreset.entries[idx % 5],
                        lowerThirdStyle = LowerThirdStyle.entries[idx % LowerThirdStyle.entries.size],
                        backdropPreset = presets[idx % presets.size]
                    )
                )
            }
        }
        return NewsProject(
            projectName = "ইমপোর্ট করা সাবটাইটেল প্রজেক্ট (SRT)",
            channelName = "বাংলা নিউজ ২৪",
            shots = shots.ifEmpty { SampleNewsProjects.primeTimeBulletin().shots }
        )
    }

    private fun parseXmlOrHtmlDomProject(xmlOrHtml: String): NewsProject {
        val titleMatch = Regex("""<title[^>]*>(.*?)</title>""", RegexOption.IGNORE_CASE).find(xmlOrHtml)
        val projectTitle = titleMatch?.groupValues?.get(1)?.trim()
            ?.takeIf { it.isNotEmpty() } ?: "ইমপোর্ট করা ভিডিও প্রজেক্ট"

        val channelMatch = Regex("""data-channel=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(xmlOrHtml)
        val channelName = channelMatch?.groupValues?.get(1)?.trim() ?: "বাংলা নিউজ ২৪"

        val tickerMatch = Regex("""data-ticker=["']([^"']+)["']""", RegexOption.IGNORE_CASE).find(xmlOrHtml)
            ?: Regex("""<marquee[^>]*>(.*?)</marquee>""", RegexOption.IGNORE_CASE).find(xmlOrHtml)
        val tickerText = tickerMatch?.groupValues?.get(1)?.replace(Regex("<[^>]+>"), "")?.trim()
            ?: "ব্রেকিং নিউজ: সরাসরি স্টুডিও থেকে সম্প্রচারিত সংবাদ বুলেটিন"

        val blockRegex = Regex(
            """<(?:section|article|shot|scene|clip|div)[^>]*(?:data-shot|class=["'][^"']*shot[^"']*["'])[^>]*>([\s\S]*?)</(?:section|article|shot|scene|clip|div)>""",
            RegexOption.IGNORE_CASE
        )
        val blocks = blockRegex.findAll(xmlOrHtml).toList()
        val presets = StudioBackdropPreset.entries
        val shots = mutableListOf<NewsShot>()

        if (blocks.isNotEmpty()) {
            blocks.forEachIndexed { idx, matchResult ->
                val fullTag = matchResult.value
                val inner = matchResult.groupValues[1]
                val durationAttr = Regex("""data-duration=["']([0-9.]+)["']""").find(fullTag)
                    ?.groupValues?.get(1)?.toFloatOrNull() ?: 5.0f
                val categoryAttr = Regex("""data-category=["']([^"']+)["']""").find(fullTag)
                    ?.groupValues?.get(1)
                    ?: Regex("""<span[^>]*>(.*?)</span>""").find(inner)?.groupValues?.get(1)
                    ?: "প্রধান সংবাদ"
                val h1 = Regex("""<h[1-3][^>]*>(.*?)</h[1-3]>""", RegexOption.IGNORE_CASE)
                    .find(inner)?.groupValues?.get(1)?.replace(Regex("<[^>]+>"), "")?.trim()
                    ?: "সংবাদ শিরোনাম ${idx + 1}"
                val p = Regex("""<p[^>]*>(.*?)</p>""", RegexOption.IGNORE_CASE)
                    .find(inner)?.groupValues?.get(1)?.replace(Regex("<[^>]+>"), "")?.trim()
                    ?: "স্টুডিও থেকে সরাসরি সম্প্রচারিত বিশেষ প্রতিবেদন"

                val motionAttr = Regex("""data-motion=["']([^"']+)["']""").find(fullTag)?.groupValues?.get(1) ?: ""
                val motion = MotionPreset.entries.find {
                    it.name.equals(motionAttr, ignoreCase = true)
                } ?: MotionPreset.entries[idx % 5]

                shots.add(
                    NewsShot(
                        categoryTag = categoryAttr.replace(Regex("<[^>]+>"), "").trim(),
                        headline = h1,
                        subheadline = p,
                        durationSeconds = durationAttr.coerceIn(2f, 15f),
                        motionPreset = motion,
                        lowerThirdStyle = LowerThirdStyle.entries[idx % LowerThirdStyle.entries.size],
                        backdropPreset = presets[idx % presets.size]
                    )
                )
            }
        } else {
            val headings = Regex("""<h[1-3][^>]*>(.*?)</h[1-3]>""", RegexOption.IGNORE_CASE)
                .findAll(xmlOrHtml)
                .map { it.groupValues[1].replace(Regex("<[^>]+>"), "").trim() }
                .filter { it.isNotEmpty() }
                .toList()
            val paragraphs = Regex("""<p[^>]*>(.*?)</p>""", RegexOption.IGNORE_CASE)
                .findAll(xmlOrHtml)
                .map { it.groupValues[1].replace(Regex("<[^>]+>"), "").trim() }
                .filter { it.isNotEmpty() }
                .toList()

            headings.forEachIndexed { idx, heading ->
                shots.add(
                    NewsShot(
                        categoryTag = if (idx == 0) "ব্রেকিং নিউজ" else "প্রধান সংবাদ",
                        headline = heading,
                        subheadline = paragraphs.getOrNull(idx) ?: "বিস্তারিত সংবাদ প্রতিবেদন",
                        durationSeconds = 5.0f,
                        motionPreset = MotionPreset.entries[idx % 5],
                        lowerThirdStyle = LowerThirdStyle.entries[idx % LowerThirdStyle.entries.size],
                        backdropPreset = presets[idx % presets.size]
                    )
                )
            }
        }

        return NewsProject(
            projectName = projectTitle,
            channelName = channelName,
            tickerText = tickerText,
            shots = shots.ifEmpty { SampleNewsProjects.primeTimeBulletin().shots }
        )
    }

    private fun parsePlainScriptToProject(text: String): NewsProject {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val presets = StudioBackdropPreset.entries
        val shots = lines.chunked(2).mapIndexed { index, pair ->
            NewsShot(
                categoryTag = if (index == 0) "ব্রেকিং নিউজ" else "সংবাদ আপডেট",
                headline = pair.first(),
                subheadline = pair.getOrNull(1) ?: "ঢাকা স্টুডিও থেকে সরাসরি সম্প্রচারিত",
                durationSeconds = 5.0f,
                motionPreset = MotionPreset.entries[index % 5],
                lowerThirdStyle = LowerThirdStyle.entries[index % LowerThirdStyle.entries.size],
                backdropPreset = presets[index % presets.size]
            )
        }
        return NewsProject(
            projectName = "কাস্টম ভিডিও প্রজেক্ট",
            channelName = "বাংলা নিউজ ২৪",
            shots = shots.ifEmpty { SampleNewsProjects.primeTimeBulletin().shots }
        )
    }

    fun projectToJsonString(project: NewsProject): String {
        val root = JSONObject()
        root.put("projectName", project.projectName)
        root.put("channelName", project.channelName)
        root.put("tickerText", project.tickerText)
        root.put("showTicker", project.showTicker)
        root.put("tickerSpeedMultiplier", project.tickerSpeedMultiplier.toDouble())

        val shotsArray = JSONArray()
        project.shots.forEach { shot ->
            val obj = JSONObject()
            obj.put("id", shot.id)
            obj.put("categoryTag", shot.categoryTag)
            obj.put("headline", shot.headline)
            obj.put("subheadline", shot.subheadline)
            obj.put("reporterLocation", shot.reporterLocation)
            obj.put("durationSeconds", shot.durationSeconds.toDouble())
            obj.put("motionPreset", shot.motionPreset.name)
            obj.put("lowerThirdStyle", shot.lowerThirdStyle.name)
            obj.put("textAnimation", shot.textAnimation.name)
            obj.put("motionGraphicOverlay", shot.motionGraphicOverlay.name)
            obj.put("soundEffect", shot.soundEffect.name)
            obj.put("backdropPreset", shot.backdropPreset.id)
            obj.put("showCategoryBadge", shot.showCategoryBadge)
            obj.put("showHeadline", shot.showHeadline)
            obj.put("showSubheadline", shot.showSubheadline)
            obj.put("showLiveBug", shot.showLiveBug)

            val kf = JSONObject()
            kf.put("startScale", shot.keyframes.startScale.toDouble())
            kf.put("endScale", shot.keyframes.endScale.toDouble())
            kf.put("startOffsetX", shot.keyframes.startOffsetX.toDouble())
            kf.put("endOffsetX", shot.keyframes.endOffsetX.toDouble())
            kf.put("startOffsetY", shot.keyframes.startOffsetY.toDouble())
            kf.put("endOffsetY", shot.keyframes.endOffsetY.toDouble())
            obj.put("keyframes", kf)

            if (shot.customTextLayers.isNotEmpty()) {
                val textsArr = JSONArray()
                shot.customTextLayers.forEach { layer ->
                    val tObj = JSONObject()
                    tObj.put("text", layer.text)
                    tObj.put("xPercent", layer.xPercent.toDouble())
                    tObj.put("yPercent", layer.yPercent.toDouble())
                    tObj.put("fontSizePx", layer.fontSizePx.toDouble())
                    textsArr.put(tObj)
                }
                obj.put("customTextLayers", textsArr)
            }
            shotsArray.put(obj)
        }
        root.put("shots", shotsArray)
        return root.toString(2)
    }

    fun projectToHtmlProjectCode(project: NewsProject): String {
        val sb = StringBuilder()
        sb.appendLine("<!DOCTYPE html>")
        sb.appendLine("<html lang=\"bn\">")
        sb.appendLine("<head>")
        sb.appendLine("  <meta charset=\"UTF-8\" />")
        sb.appendLine("  <title>${escapeHtml(project.projectName)}</title>")
        sb.appendLine("</head>")
        sb.appendLine("<body data-channel=\"${escapeHtml(project.channelName)}\" data-ticker=\"${escapeHtml(project.tickerText)}\">")
        project.shots.forEachIndexed { idx, shot ->
            sb.appendLine(
                "  <section class=\"shot\" data-shot=\"${idx + 1}\" data-category=\"${escapeHtml(shot.categoryTag)}\" " +
                    "data-duration=\"${shot.durationSeconds}\" data-motion=\"${shot.motionPreset.name}\">"
            )
            sb.appendLine("    <h1>${escapeHtml(shot.headline)}</h1>")
            sb.appendLine("    <p>${escapeHtml(shot.subheadline)}</p>")
            sb.appendLine("  </section>")
        }
        sb.appendLine("  <script id=\"news-project-data\" type=\"application/json\">")
        sb.appendLine(projectToJsonString(project))
        sb.appendLine("  </script>")
        sb.appendLine("</body>")
        sb.appendLine("</html>")
        return sb.toString()
    }

    fun generateStandaloneWebStudioHtml(project: NewsProject): String {
        return projectToHtmlProjectCode(project)
    }

    private fun escapeHtml(input: String): String =
        input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
}
