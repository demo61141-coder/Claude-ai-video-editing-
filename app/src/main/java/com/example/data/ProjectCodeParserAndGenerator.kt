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
import com.example.model.ToonCharacterPose
import com.example.model.ToonCharacterWho
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object ProjectCodeParserAndGenerator {

    /**
     * Universal Video Project & HTML/JS/JSON/SRT Parser:
     * 1. Parses HTML/JS files with `const SHOTS = [ {key:'...', text:'...', hi:'...', bg:'canal', who:'host', pose:'point'}, ... ]`
     * 2. Parses JSON objects/arrays (`{"shots": [...]}` or `[...]`)
     * 3. Parses SRT subtitles (`00:00:00,000 --> 00:00:05,000`)
     * 4. Parses HTML/XML `<section class="shot">` or heading tags
     * 5. Parses plain Bangla/English scripts line by line
     */
    fun parseCodeToProject(rawCode: String): Result<NewsProject> {
        val trimmed = rawCode.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("ফাইল বা কোড খালি।"))
        }

        return runCatching {
            // 1. Check for JavaScript `SHOTS = [...]` or `scenes = [...]` (e.g. Bangla Toon — News Reel HTML/JS!)
            val jsShotsProject = tryParseJavaScriptShotsFile(trimmed)
            if (jsShotsProject != null && jsShotsProject.shots.isNotEmpty()) {
                return@runCatching jsShotsProject
            }

            // 2. Direct JSON Object or Array
            if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
                return@runCatching parseJsonString(trimmed)
            }

            // 3. Embedded JSON inside <script id="news-project-data">
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

            // 4. SRT Subtitle format
            if (trimmed.contains("-->")) {
                return@runCatching parseSrtToProject(trimmed)
            }

            // 5. HTML / XML DOM tags
            if (trimmed.contains("<") && trimmed.contains(">")) {
                val domProject = parseXmlOrHtmlDomProject(trimmed)
                if (domProject.shots.isNotEmpty()) {
                    return@runCatching domProject
                }
            }

            // 6. Plain script fallback
            parsePlainScriptToProject(trimmed)
        }
    }

    /**
     * Parses JavaScript/HTML files containing `const SHOTS = [ { ... }, ... ]`
     * such as `Bangla Toon — News Reel (Full)` or any custom JS canvas animation script.
     */
    private fun tryParseJavaScriptShotsFile(code: String): NewsProject? {
        // Match `const SHOTS = [ ... ]` or `let SHOTS = [ ... ]` or `SHOTS = [ ... ]`
        val shotsArrayRegex = Regex(
            """(?:const|let|var)?\s*(?:SHOTS|shots|SCENES|scenes|CLIPS|clips)\s*=\s*\[([\s\S]*?)\]\s*;""",
            RegexOption.IGNORE_CASE
        )
        val match = shotsArrayRegex.find(code) ?: return null
        val arrayBody = match.groupValues[1]

        // Extract title from <title>...</title> if present
        val titleMatch = Regex("""<title[^>]*>(.*?)</title>""", RegexOption.IGNORE_CASE).find(code)
        val projectTitle = titleMatch?.groupValues?.get(1)?.trim()
            ?.takeIf { it.isNotEmpty() } ?: "Bangla Toon — News Reel"

        // Extract `const TOTAL = 57;` if present
        val totalSecMatch = Regex("""(?:const|let|var)\s+TOTAL\s*=\s*([0-9.]+)""").find(code)
        val totalDurationTarget = totalSecMatch?.groupValues?.get(1)?.toFloatOrNull() ?: 57f

        // Extract stage banner text from `fillText('...', 960, 332)` if present
        val stageTextMatch = Regex("""fillText\(\s*['"]([^'"]+)['"]\s*,\s*960""").find(code)
        val extractedStageBanner = stageTextMatch?.groupValues?.get(1)?.trim()
            ?: "৫৫তম জাতীয় সমবায় দিবস"

        // Find each `{ ... }` object inside SHOTS array
        val objectRegex = Regex("""\{([^{}]+)\}""")
        val objMatches = objectRegex.findAll(arrayBody).toList()
        if (objMatches.isEmpty()) return null

        data class RawJsShot(
            val key: String,
            val text: String,
            val hi: String,
            val bg: String,
            val who: String,
            val pose: String,
            val duration: Float?,
            val captionAt: Float?
        )

        val rawList = mutableListOf<RawJsShot>()
        for (objMatch in objMatches) {
            val propsStr = objMatch.groupValues[1]
            val props = parseJsObjectProperties(propsStr)
            val text = props["text"] ?: props["headline"] ?: props["title"] ?: continue
            val key = props["key"] ?: props["id"] ?: "shot_${rawList.size + 1}"
            val hi = props["hi"] ?: props["highlightWord"] ?: props["highlight"] ?: ""
            val bg = props["bg"] ?: props["backdrop"] ?: "canal"
            val who = props["who"] ?: props["character"] ?: "host"
            val pose = props["pose"] ?: "talk"
            val dur = props["duration"]?.toFloatOrNull() ?: props["durationSeconds"]?.toFloatOrNull()
            val capAt = props["captionAt"]?.toFloatOrNull()
            rawList.add(RawJsShot(key, text, hi, bg, who, pose, dur, capAt))
        }

        if (rawList.isEmpty()) return null

        val totalChars = rawList.sumOf { it.text.length }.coerceAtLeast(1)

        val parsedShots = rawList.mapIndexed { idx, raw ->
            val calculatedDur = raw.duration ?: run {
                // Just like buildDefaultTimings() in Bangla Toon HTML!
                ((raw.text.length.toFloat() / totalChars.toFloat()) * totalDurationTarget)
                    .coerceIn(3.0f, 15.0f)
            }

            val backdrop = when (raw.bg.lowercase()) {
                "canal" -> StudioBackdropPreset.TOON_CANAL
                "stage" -> StudioBackdropPreset.TOON_STAGE
                "house", "village" -> StudioBackdropPreset.TOON_HOUSE
                "studio", "studio_desk" -> StudioBackdropPreset.NEWS_STUDIO_DESK
                "dhaka_metro", "metro" -> StudioBackdropPreset.DHAKA_METRO_RAIL
                "padma_bridge", "padma" -> StudioBackdropPreset.PADMA_BRIDGE_AERIAL
                else -> StudioBackdropPreset.TOON_CANAL
            }

            val toonWho = when (raw.who.lowercase()) {
                "host" -> ToonCharacterWho.HOST
                "minister", "both" -> ToonCharacterWho.MINISTER
                "minister_solo" -> ToonCharacterWho.MINISTER_SOLO
                "none" -> ToonCharacterWho.NONE
                else -> ToonCharacterWho.HOST
            }

            val toonPose = when (raw.pose.lowercase()) {
                "point" -> ToonCharacterPose.POINT
                else -> ToonCharacterPose.TALK
            }

            NewsShot(
                shotKey = raw.key,
                categoryTag = "সংবাদ রিল #${idx + 1}",
                headline = raw.text,
                highlightWord = raw.hi,
                subheadline = if (raw.hi.isNotEmpty()) {
                    "হাইলাইট: ${raw.hi} • দৃশ্য: ${backdrop.banglaTitle}"
                } else {
                    "দৃশ্য: ${backdrop.banglaTitle}"
                },
                stageBannerText = extractedStageBanner,
                reporterLocation = "${backdrop.banglaTitle} • ${toonWho.banglaLabel}",
                durationSeconds = calculatedDur,
                toonWho = toonWho,
                toonPose = toonPose,
                motionPreset = MotionPreset.STATIC_STUDIO,
                lowerThirdStyle = LowerThirdStyle.TOON_CAPTION_PILL,
                motionGraphicOverlay = MotionGraphicOverlay.TOON_CLEAN_REEL,
                backdropPreset = backdrop,
                showCategoryBadge = false,
                showHeadline = true,
                showSubheadline = false,
                showStageBanner = true,
                showLiveBug = false
            )
        }

        val tickerJoined = parsedShots.joinToString(" • ") { it.headline }
        return NewsProject(
            id = UUID.randomUUID().toString(),
            projectName = projectTitle,
            channelName = "বাংলা টুন নিউজ",
            tickerText = tickerJoined,
            showTicker = false,
            shots = parsedShots
        )
    }

    /**
     * Parses key:value pairs inside a JS object literal like:
     * `key:'khál_20k', text:'দেশে কুড়ি হাজার কিলোমিটার খাল খনন হবে', hi:'কুড়ি হাজার কিলোমিটার', bg:'canal', who:'host', pose:'point', captionAt:0`
     */
    private fun parseJsObjectProperties(propsBody: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        // Match key : 'value' or key : "value" or key : number
        val propRegex = Regex(
            """['"]?([a-zA-Z0-9_]+)['"]?\s*:\s*(?:'([^']*)'|"([^"]*)"|([0-9.]+|true|false))"""
        )
        propRegex.findAll(propsBody).forEach { m ->
            val key = m.groupValues[1]
            val value = m.groups[2]?.value
                ?: m.groups[3]?.value
                ?: m.groups[4]?.value
                ?: ""
            map[key] = value.trim()
        }
        return map
    }

    private fun parseJsonString(jsonStr: String): NewsProject {
        if (jsonStr.startsWith("[")) {
            val arr = JSONArray(jsonStr)
            val shots = parseShotsArray(arr)
            if (shots.isEmpty()) {
                throw IllegalArgumentException("JSON অ্যারেতে কোনো শট পাওয়া যায়নি।")
            }
            return NewsProject(
                id = UUID.randomUUID().toString(),
                projectName = "ইমপোর্ট করা ভিডিও প্রজেক্ট (JSON)",
                channelName = "বাংলা নিউজ ২৪",
                shots = shots
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
        val showTicker = root.optBoolean("showTicker", false)

        val shotsArray = root.optJSONArray("shots")
            ?: root.optJSONArray("SHOTS")
            ?: root.optJSONArray("scenes")
            ?: root.optJSONArray("clips")
            ?: root.optJSONArray("slides")
            ?: root.optJSONArray("timeline")
            ?: JSONArray()

        val parsedShots = parseShotsArray(shotsArray)
        if (parsedShots.isEmpty()) {
            throw IllegalArgumentException("JSON ফাইলে কোনো 'shots' বা 'scenes' পাওয়া যায়নি।")
        }
        return NewsProject(
            id = UUID.randomUUID().toString(),
            projectName = projectName,
            channelName = channelName,
            tickerText = tickerText,
            showTicker = showTicker,
            shots = parsedShots
        )
    }

    private fun parseShotsArray(arr: JSONArray): List<NewsShot> {
        val list = mutableListOf<NewsShot>()
        val presets = StudioBackdropPreset.entries
        for (i in 0 until arr.length()) {
            val obj = arr.optJSONObject(i) ?: continue
            val shotKey = obj.optString("shotKey", obj.optString("key", "shot_${i + 1}"))
            val category = obj.optString("categoryTag")
                .ifEmpty { obj.optString("category", obj.optString("tag", "ব্রেকিং নিউজ")) }
            val headline = obj.optString("headline")
                .ifEmpty { obj.optString("text", obj.optString("title", "সংবাদ শিরোনাম ${i + 1}")) }
            val highlightWord = obj.optString("highlightWord")
                .ifEmpty { obj.optString("hi", obj.optString("highlight", "")) }
            val subheadline = obj.optString("subheadline")
                .ifEmpty { obj.optString("subtitle", obj.optString("description", "")) }
            val stageBannerText = obj.optString("stageBannerText", "৫৫তম জাতীয় সমবায় দিবস")
            val location = obj.optString("reporterLocation")
                .ifEmpty { obj.optString("location", "ঢাকা স্টুডিও • LIVE") }
            val duration = obj.optDouble("durationSeconds", obj.optDouble("duration", 5.0))
                .toFloat()
                .coerceIn(1.5f, 25.0f)

            val bgStr = obj.optString("backdropPreset", obj.optString("bg", obj.optString("backdrop", "")))
            val isToonShot = bgStr.lowercase() in listOf("canal", "stage", "house", "village") ||
                obj.has("who") || obj.has("hi")

            val backdrop = presets.find {
                it.id.equals(bgStr, ignoreCase = true) ||
                    it.name.equals(bgStr, ignoreCase = true)
            } ?: if (isToonShot) StudioBackdropPreset.TOON_CANAL else presets[(i + 3) % presets.size]

            val whoStr = obj.optString("toonWho", obj.optString("who", if (isToonShot) "host" else "none"))
            val toonWho = ToonCharacterWho.entries.find {
                it.id.equals(whoStr, ignoreCase = true) || it.name.equals(whoStr, ignoreCase = true)
            } ?: if (isToonShot) ToonCharacterWho.HOST else ToonCharacterWho.NONE

            val poseStr = obj.optString("toonPose", obj.optString("pose", "talk"))
            val toonPose = ToonCharacterPose.entries.find {
                it.id.equals(poseStr, ignoreCase = true) || it.name.equals(poseStr, ignoreCase = true)
            } ?: ToonCharacterPose.TALK

            val motionStr = obj.optString("motionPreset", obj.optString("motion", ""))
            val motion = MotionPreset.entries.find {
                it.name.equals(motionStr, ignoreCase = true) ||
                    it.label.contains(motionStr, ignoreCase = true)
            } ?: if (isToonShot) MotionPreset.STATIC_STUDIO else MotionPreset.KEN_BURNS_IN

            val styleStr = obj.optString("lowerThirdStyle", obj.optString("style", ""))
            val style = LowerThirdStyle.entries.find {
                it.name.equals(styleStr, ignoreCase = true) ||
                    it.label.contains(styleStr, ignoreCase = true)
            } ?: if (isToonShot) LowerThirdStyle.TOON_CAPTION_PILL else LowerThirdStyle.CRIMSON_BREAKING

            val textAnimStr = obj.optString("textAnimation", "")
            val textAnim = TextAnimationStyle.entries.find {
                it.name.equals(textAnimStr, ignoreCase = true)
            } ?: TextAnimationStyle.SLIDE_UP_REVEAL

            val sfxStr = obj.optString("soundEffect", obj.optString("sfx", ""))
            val sfx = SoundEffectType.entries.find {
                it.name.equals(sfxStr, ignoreCase = true)
            } ?: if (isToonShot) SoundEffectType.NONE else SoundEffectType.BREAKING_SWOOSH

            val overlayStr = obj.optString("motionGraphicOverlay", "")
            val overlay = MotionGraphicOverlay.entries.find {
                it.name.equals(overlayStr, ignoreCase = true)
            } ?: if (isToonShot) MotionGraphicOverlay.TOON_CLEAN_REEL else MotionGraphicOverlay.FULL_BROADCAST_PACK

            val kfObj = obj.optJSONObject("keyframes")
            val keyframes = if (kfObj != null) {
                KeyframeConfig(
                    startScale = kfObj.optDouble("startScale", motion.defaultStartScale.toDouble()).toFloat(),
                    endScale = kfObj.optDouble("endScale", motion.defaultEndScale.toDouble()).toFloat(),
                    startOffsetX = kfObj.optDouble("startOffsetX", motion.defaultStartX.toDouble()).toFloat(),
                    endOffsetX = kfObj.optDouble("endOffsetX", motion.defaultEndX.toDouble()).toFloat(),
                    startOffsetY = kfObj.optDouble("startOffsetY", 0.0).toFloat(),
                    endOffsetY = kfObj.optDouble("endOffsetY", 0.0).toFloat()
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
                    shotKey = shotKey,
                    categoryTag = category,
                    headline = headline,
                    highlightWord = highlightWord,
                    subheadline = subheadline,
                    stageBannerText = stageBannerText,
                    reporterLocation = location,
                    durationSeconds = duration,
                    toonWho = toonWho,
                    toonPose = toonPose,
                    motionPreset = motion,
                    keyframes = keyframes,
                    lowerThirdStyle = style,
                    textAnimation = textAnim,
                    motionGraphicOverlay = overlay,
                    soundEffect = sfx,
                    backdropPreset = backdrop,
                    showCategoryBadge = obj.optBoolean("showCategoryBadge", !isToonShot),
                    showHeadline = obj.optBoolean("showHeadline", true),
                    showSubheadline = obj.optBoolean("showSubheadline", !isToonShot && subheadline.isNotBlank()),
                    showStageBanner = obj.optBoolean("showStageBanner", true),
                    showLiveBug = obj.optBoolean("showLiveBug", !isToonShot),
                    customTextLayers = customTexts
                )
            )
        }
        return list
    }

    private fun parseSrtToProject(srt: String): NewsProject {
        val blocks = srt.trim().split(Regex("\\r?\\n\\r?\\n"))
        val presets = listOf(
            StudioBackdropPreset.TOON_CANAL,
            StudioBackdropPreset.TOON_STAGE,
            StudioBackdropPreset.TOON_HOUSE
        )
        val shots = mutableListOf<NewsShot>()

        blocks.forEachIndexed { idx, block ->
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            val textLines = lines.filter { !it.matches(Regex("^\\d+$")) && !it.contains("-->") }
            if (textLines.isNotEmpty()) {
                val headline = textLines.first()
                val sub = textLines.drop(1).joinToString(" ")
                val words = headline.split(" ")
                val hi = if (words.size >= 2) "${words[0]} ${words[1]}" else words.firstOrNull().orEmpty()
                shots.add(
                    NewsShot(
                        categoryTag = "সাবটাইটেল #${idx + 1}",
                        headline = headline,
                        highlightWord = hi,
                        subheadline = sub,
                        durationSeconds = 5.0f,
                        toonWho = if (idx % 3 == 1) ToonCharacterWho.MINISTER else ToonCharacterWho.HOST,
                        toonPose = if (idx % 2 == 0) ToonCharacterPose.POINT else ToonCharacterPose.TALK,
                        backdropPreset = presets[idx % presets.size]
                    )
                )
            }
        }
        return NewsProject(
            projectName = "ইমপোর্ট করা সাবটাইটেল প্রজেক্ট (SRT)",
            channelName = "বাংলা টুন নিউজ",
            shots = shots
        )
    }

    private fun parseXmlOrHtmlDomProject(xmlOrHtml: String): NewsProject {
        val titleMatch = Regex("""<title[^>]*>(.*?)</title>""", RegexOption.IGNORE_CASE).find(xmlOrHtml)
        val projectTitle = titleMatch?.groupValues?.get(1)?.trim()
            ?.takeIf { it.isNotEmpty() } ?: "ইমপোর্ট করা ভিডিও প্রজেক্ট"

        val blockRegex = Regex(
            """<(?:section|article|shot|scene|clip|div)[^>]*(?:data-shot|class=["'][^"']*shot[^"']*["'])[^>]*>([\s\S]*?)</(?:section|article|shot|scene|clip|div)>""",
            RegexOption.IGNORE_CASE
        )
        val blocks = blockRegex.findAll(xmlOrHtml).toList()
        val presets = listOf(
            StudioBackdropPreset.TOON_CANAL,
            StudioBackdropPreset.TOON_STAGE,
            StudioBackdropPreset.TOON_HOUSE
        )
        val shots = mutableListOf<NewsShot>()

        if (blocks.isNotEmpty()) {
            blocks.forEachIndexed { idx, matchResult ->
                val fullTag = matchResult.value
                val inner = matchResult.groupValues[1]
                val durationAttr = Regex("""data-duration=["']([0-9.]+)["']""").find(fullTag)
                    ?.groupValues?.get(1)?.toFloatOrNull() ?: 5.0f
                val h1 = Regex("""<h[1-3][^>]*>(.*?)</h[1-3]>""", RegexOption.IGNORE_CASE)
                    .find(inner)?.groupValues?.get(1)?.replace(Regex("<[^>]+>"), "")?.trim()
                    ?: inner.replace(Regex("<[^>]+>"), "").trim().take(80)
                val p = Regex("""<p[^>]*>(.*?)</p>""", RegexOption.IGNORE_CASE)
                    .find(inner)?.groupValues?.get(1)?.replace(Regex("<[^>]+>"), "")?.trim()
                    ?: ""

                if (h1.isNotEmpty()) {
                    shots.add(
                        NewsShot(
                            categoryTag = "দৃশ্য #${idx + 1}",
                            headline = h1,
                            subheadline = p,
                            durationSeconds = durationAttr.coerceIn(2f, 15f),
                            backdropPreset = presets[idx % presets.size]
                        )
                    )
                }
            }
        }

        // Also check any Bangla strings inside JS/HTML if DOM blocks weren't used
        if (shots.isEmpty()) {
            val banglaQuotes = Regex("""['"]([^'"]*[\u0980-\u09FF]{3,}[^'"]*)['"]""")
                .findAll(xmlOrHtml)
                .map { it.groupValues[1].trim() }
                .filter {
                    it.length in 8..140 &&
                        !it.contains("Noto Sans") &&
                        !it.contains("▶") &&
                        !it.contains("●") &&
                        !it.contains("অডিও বাছুন")
                }
                .distinct()
                .toList()

            banglaQuotes.forEachIndexed { idx, text ->
                val words = text.split(" ")
                val hi = if (words.size >= 2) words.takeLast(2).joinToString(" ") else words.firstOrNull().orEmpty()
                shots.add(
                    NewsShot(
                        categoryTag = "দৃশ্য #${idx + 1}",
                        headline = text,
                        highlightWord = hi,
                        durationSeconds = 5.0f,
                        toonWho = if (idx % 3 == 1) ToonCharacterWho.MINISTER else ToonCharacterWho.HOST,
                        toonPose = if (idx % 2 == 0) ToonCharacterPose.POINT else ToonCharacterPose.TALK,
                        backdropPreset = presets[idx % presets.size]
                    )
                )
            }
        }

        return NewsProject(
            projectName = projectTitle,
            channelName = "বাংলা টুন নিউজ",
            shots = shots
        )
    }

    private fun parsePlainScriptToProject(text: String): NewsProject {
        val lines = text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val presets = listOf(
            StudioBackdropPreset.TOON_CANAL,
            StudioBackdropPreset.TOON_STAGE,
            StudioBackdropPreset.TOON_HOUSE
        )
        val shots = lines.mapIndexed { index, line ->
            val cleanLine = line.replace(Regex("^[০-৯0-9]+[.।)\\-]\\s*"), "").trim()
            val words = cleanLine.split(" ")
            val hi = if (words.size >= 2) words.takeLast(2).joinToString(" ") else words.firstOrNull().orEmpty()
            NewsShot(
                shotKey = "shot_${index + 1}",
                categoryTag = "দৃশ্য #${index + 1}",
                headline = cleanLine,
                highlightWord = hi,
                durationSeconds = 5.5f,
                toonWho = if (index % 3 == 1) ToonCharacterWho.MINISTER else ToonCharacterWho.HOST,
                toonPose = if (index % 2 == 0) ToonCharacterPose.POINT else ToonCharacterPose.TALK,
                backdropPreset = presets[index % presets.size]
            )
        }
        return NewsProject(
            projectName = "কাস্টম সংবাদ রিল প্রজেক্ট",
            channelName = "বাংলা টুন নিউজ",
            shots = shots.ifEmpty { SampleNewsProjects.banglaToonCanalNewsReel().shots }
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
            obj.put("key", shot.shotKey)
            obj.put("categoryTag", shot.categoryTag)
            obj.put("text", shot.headline)
            obj.put("headline", shot.headline)
            obj.put("hi", shot.highlightWord)
            obj.put("highlightWord", shot.highlightWord)
            obj.put("subheadline", shot.subheadline)
            obj.put("stageBannerText", shot.stageBannerText)
            obj.put("bg", shot.backdropPreset.id)
            obj.put("who", shot.toonWho.id)
            obj.put("pose", shot.toonPose.id)
            obj.put("durationSeconds", shot.durationSeconds.toDouble())
            obj.put("motionPreset", shot.motionPreset.name)
            obj.put("lowerThirdStyle", shot.lowerThirdStyle.name)
            obj.put("textAnimation", shot.textAnimation.name)
            obj.put("motionGraphicOverlay", shot.motionGraphicOverlay.name)
            obj.put("soundEffect", shot.soundEffect.name)
            obj.put("showCategoryBadge", shot.showCategoryBadge)
            obj.put("showHeadline", shot.showHeadline)
            obj.put("showSubheadline", shot.showSubheadline)
            obj.put("showStageBanner", shot.showStageBanner)
            obj.put("showLiveBug", shot.showLiveBug)

            val kf = JSONObject()
            kf.put("startScale", shot.keyframes.startScale.toDouble())
            kf.put("endScale", shot.keyframes.endScale.toDouble())
            kf.put("startOffsetX", shot.keyframes.startOffsetX.toDouble())
            kf.put("endOffsetX", shot.keyframes.endOffsetX.toDouble())
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
        sb.appendLine("<meta charset=\"UTF-8\">")
        sb.appendLine("<title>${escapeHtml(project.projectName)}</title>")
        sb.appendLine("</head>")
        sb.appendLine("<body>")
        sb.appendLine("<script>")
        sb.appendLine("const SHOTS = [")
        project.shots.forEachIndexed { idx, s ->
            val comma = if (idx < project.shots.lastIndex) "," else ""
            sb.appendLine(
                "  {key:'${s.shotKey.ifEmpty { "shot_${idx + 1}" }}', text:'${escapeJs(s.headline)}', hi:'${escapeJs(s.highlightWord)}', bg:'${s.backdropPreset.id}', who:'${s.toonWho.id}', pose:'${s.toonPose.id}', duration:${s.durationSeconds}}$comma"
            )
        }
        sb.appendLine("];")
        sb.appendLine("const TOTAL = ${project.totalDurationSeconds.toInt()};")
        sb.appendLine("</script>")
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

    private fun escapeJs(input: String): String =
        input.replace("\\", "\\\\").replace("'", "\\'")
}
