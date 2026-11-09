package com.example.engine

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import com.example.model.ExportMp4State
import com.example.model.NewsProject
import com.example.model.SoundEffectType
import com.example.model.VideoExportConfig
import com.example.model.VideoResolutionPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.coroutines.coroutineContext
import kotlin.math.PI
import kotlin.math.sin

class Mp4VideoExporter(
    private val context: Context,
    private val renderer: BroadcastFrameRenderer
) {

    /**
     * Plays a real-time synthesized Sound Effect preview when the user selects or tests an SFX in the editor.
     */
    fun playSoundEffectPreview(scope: CoroutineScope, sfx: SoundEffectType) {
        if (sfx == SoundEffectType.NONE || sfx.durationMs <= 0) return
        scope.launch(Dispatchers.Default) {
            runCatching {
                val sampleRate = 24000
                val totalSamples = (sampleRate * sfx.durationMs) / 1000
                val pcm = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val progress = i.toDouble() / totalSamples.toDouble()
                    val t = i.toDouble() / sampleRate.toDouble()
                    val freq = sfx.freqStartHz + (sfx.freqEndHz - sfx.freqStartHz) * progress
                    val env = sin(PI * progress)
                    val wave = sin(2.0 * PI * freq * t) * 0.7 + sin(4.0 * PI * freq * t) * 0.3
                    pcm[i] = (wave * env * 14000.0).toInt().coerceIn(-32767, 32767).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(pcm, 0, pcm.size)
                audioTrack.play()
                kotlinx.coroutines.delay(sfx.durationMs.toLong() + 80L)
                audioTrack.stop()
                audioTrack.release()
            }
        }
    }

    /**
     * High-speed Hardware H.264 + AAC `.mp4` Exporter supporting 144p, 360p, 480p, 720p, 1080p, and 1480p
     * with strictly monotonic timestamps and safe MediaMuxer track lifecycle management.
     */
    suspend fun exportProjectToMp4(
        project: NewsProject,
        exportConfig: VideoExportConfig,
        onProgress: (ExportMp4State.Rendering) -> Unit
    ): Result<ExportMp4State.Completed> = withContext(Dispatchers.Default) {
        runCatching {
            val startTimeMs = System.currentTimeMillis()
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val safeTitle = project.projectName
                .replace(Regex("[^a-zA-Z0-9\\u0980-\\u09FF_-]"), "_")
                .take(24)
                .ifBlank { "Bangla_News_Video" }

            val resPreset = exportConfig.resolution
            val outputFile = File(
                exportDir,
                "${safeTitle}_${resPreset.label.replace(" ", "")}_${System.currentTimeMillis()}.mp4"
            )

            val (width, height, videoEncoder) = createConfiguredVideoEncoder(exportConfig)
            val fps = exportConfig.fps.coerceIn(24, 60)
            val totalDurationSec = project.totalDurationSeconds.coerceIn(2f, 180f)
            val totalFrames = (totalDurationSec * fps).toInt().coerceAtLeast(fps)

            renderer.preloadBitmaps(project)

            val inputSurface = videoEncoder.createInputSurface()
            videoEncoder.start()

            // Precompute shot start times for Sound Effects mixing
            val shotTransitions = mutableListOf<Pair<Float, SoundEffectType>>()
            var accumSec = 0f
            project.shots.forEach { s ->
                if (s.soundEffect != SoundEffectType.NONE) {
                    shotTransitions.add(accumSec to s.soundEffect)
                }
                accumSec += s.durationSeconds
            }

            val sampleRate = 44100
            val audioSamplesPerFrame = sampleRate / fps
            val audioFormat = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                sampleRate,
                1
            ).apply {
                setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
                setInteger(MediaFormat.KEY_BIT_RATE, 96_000)
                setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
            }
            val audioEncoder = runCatching {
                MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC).apply {
                    configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                    start()
                }
            }.getOrNull()

            val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var audioTrackIndex = -1
            var muxerStarted = false

            var videoSamplesWritten = 0L
            var audioSamplesWritten = 0L
            var totalAudioSamplesQueued = 0L
            var lastVideoPtsUs = -1L
            var lastAudioPtsUs = -1L

            val videoBufferInfo = MediaCodec.BufferInfo()
            val audioBufferInfo = MediaCodec.BufferInfo()

            // Prime the audio encoder with the first frame of audio so its output format is ready immediately
            if (audioEncoder != null) {
                totalAudioSamplesQueued = feedPcmSamplesWithSfx(
                    audioEncoder = audioEncoder,
                    startSample = totalAudioSamplesQueued,
                    sampleCount = audioSamplesPerFrame,
                    sampleRate = sampleRate,
                    useSynthBed = project.useStudioSynthAudio,
                    shotTransitions = shotTransitions,
                    endOfStream = false,
                    timeoutUs = 5_000L
                )
            }

            var exportCompletedNormally = false

            try {
                for (frameIdx in 0 until totalFrames) {
                    coroutineContext.ensureActive()

                    val timeSec = frameIdx.toFloat() / fps.toFloat()

                    val surfaceCanvas = inputSurface.lockCanvas(null)
                    try {
                        renderer.renderFrame(
                            canvas = surfaceCanvas,
                            width = width.toFloat(),
                            height = height.toFloat(),
                            project = project,
                            globalTimeSec = timeSec,
                            drawSelectionHighlight = false
                        )
                    } finally {
                        inputSurface.unlockCanvasAndPost(surfaceCanvas)
                    }

                    // Feed audio for subsequent frames
                    if (audioEncoder != null && frameIdx > 0) {
                        totalAudioSamplesQueued = feedPcmSamplesWithSfx(
                            audioEncoder = audioEncoder,
                            startSample = totalAudioSamplesQueued,
                            sampleCount = audioSamplesPerFrame,
                            sampleRate = sampleRate,
                            useSynthBed = project.useStudioSynthAudio,
                            shotTransitions = shotTransitions,
                            endOfStream = false,
                            timeoutUs = if (muxerStarted) 0L else 2_000L
                        )
                    }

                    // Drain video encoder
                    val drainTimeoutUs = if (muxerStarted) 0L else 5_000L
                    while (true) {
                        val outIndex = videoEncoder.dequeueOutputBuffer(videoBufferInfo, drainTimeoutUs)
                        if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            break
                        } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            if (!muxerStarted && videoTrackIndex < 0) {
                                videoTrackIndex = muxer.addTrack(videoEncoder.outputFormat)
                            }
                            if (!muxerStarted) {
                                // Try to obtain audio output format before starting muxer
                                if (audioEncoder != null && audioTrackIndex < 0) {
                                    for (attempt in 0 until 10) {
                                        val aIdx = audioEncoder.dequeueOutputBuffer(audioBufferInfo, 3_000L)
                                        if (aIdx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                                            audioTrackIndex = muxer.addTrack(audioEncoder.outputFormat)
                                            break
                                        } else if (aIdx >= 0) {
                                            audioEncoder.releaseOutputBuffer(aIdx, false)
                                        } else {
                                            totalAudioSamplesQueued = feedPcmSamplesWithSfx(
                                                audioEncoder = audioEncoder,
                                                startSample = totalAudioSamplesQueued,
                                                sampleCount = audioSamplesPerFrame,
                                                sampleRate = sampleRate,
                                                useSynthBed = project.useStudioSynthAudio,
                                                shotTransitions = shotTransitions,
                                                endOfStream = false,
                                                timeoutUs = 2_000L
                                            )
                                        }
                                    }
                                }
                                muxer.start()
                                muxerStarted = true
                            }
                        } else if (outIndex >= 0) {
                            val encodedData = videoEncoder.getOutputBuffer(outIndex)
                            val isConfig = (videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0
                            if (encodedData != null && videoBufferInfo.size > 0 && muxerStarted && !isConfig) {
                                var ptsUs = (videoSamplesWritten * 1_000_000L) / fps
                                if (ptsUs <= lastVideoPtsUs) {
                                    ptsUs = lastVideoPtsUs + (1_000_000L / fps).coerceAtLeast(1000L)
                                }
                                lastVideoPtsUs = ptsUs
                                videoBufferInfo.presentationTimeUs = ptsUs

                                encodedData.position(videoBufferInfo.offset)
                                encodedData.limit(videoBufferInfo.offset + videoBufferInfo.size)
                                muxer.writeSampleData(videoTrackIndex, encodedData, videoBufferInfo)
                                videoSamplesWritten++
                            }
                            videoEncoder.releaseOutputBuffer(outIndex, false)
                        }
                    }

                    // Drain audio encoder when muxer is running
                    if (audioEncoder != null && muxerStarted && audioTrackIndex >= 0) {
                        val (writtenCount, updatedLastPts) = drainAudioEncoder(
                            audioEncoder = audioEncoder,
                            bufferInfo = audioBufferInfo,
                            muxer = muxer,
                            audioTrackIndex = audioTrackIndex,
                            lastAudioPtsUs = lastAudioPtsUs,
                            timeoutUs = 0L,
                            waitUntilEos = false
                        )
                        audioSamplesWritten += writtenCount
                        lastAudioPtsUs = updatedLastPts
                    }

                    if (frameIdx % 5 == 0 || frameIdx == totalFrames - 1) {
                        onProgress(
                            ExportMp4State.Rendering(
                                currentFrame = frameIdx + 1,
                                totalFrames = totalFrames,
                                resolutionLabel = "${width}×${height} (${resPreset.label})",
                                bitrateLabel = exportConfig.bitratePreset.label,
                                statusMessage = "Fast Encoding ${resPreset.label} @ ${exportConfig.bitratePreset.mbpsValue} Mbps • Frame ${frameIdx + 1}/$totalFrames"
                            )
                        )
                    }
                }

                // Final drain of remaining video frames with strictly monotonic PTS
                videoEncoder.signalEndOfInputStream()
                var eosRetries = 0
                while (eosRetries < 60) {
                    val outIndex = videoEncoder.dequeueOutputBuffer(videoBufferInfo, 5_000L)
                    if (outIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        eosRetries++
                        continue
                    } else if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!muxerStarted && videoTrackIndex < 0) {
                            videoTrackIndex = muxer.addTrack(videoEncoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                        continue
                    } else if (outIndex < 0) {
                        break
                    }

                    val encodedData = videoEncoder.getOutputBuffer(outIndex)
                    val isConfig = (videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0
                    if (encodedData != null && videoBufferInfo.size > 0 && muxerStarted && !isConfig) {
                        var ptsUs = (videoSamplesWritten * 1_000_000L) / fps
                        if (ptsUs <= lastVideoPtsUs) {
                            ptsUs = lastVideoPtsUs + (1_000_000L / fps).coerceAtLeast(1000L)
                        }
                        lastVideoPtsUs = ptsUs
                        videoBufferInfo.presentationTimeUs = ptsUs

                        encodedData.position(videoBufferInfo.offset)
                        encodedData.limit(videoBufferInfo.offset + videoBufferInfo.size)
                        muxer.writeSampleData(videoTrackIndex, encodedData, videoBufferInfo)
                        videoSamplesWritten++
                    }
                    videoEncoder.releaseOutputBuffer(outIndex, false)
                    if ((videoBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
                }

                // Final drain of audio encoder (guaranteeing at least 1 sample written if audioTrackIndex >= 0)
                if (audioEncoder != null && muxerStarted && audioTrackIndex >= 0) {
                    if (audioSamplesWritten == 0L) {
                        totalAudioSamplesQueued = feedPcmSamplesWithSfx(
                            audioEncoder = audioEncoder,
                            startSample = totalAudioSamplesQueued,
                            sampleCount = audioSamplesPerFrame,
                            sampleRate = sampleRate,
                            useSynthBed = project.useStudioSynthAudio,
                            shotTransitions = shotTransitions,
                            endOfStream = false,
                            timeoutUs = 5_000L
                        )
                    }
                    totalAudioSamplesQueued = feedPcmSamplesWithSfx(
                        audioEncoder = audioEncoder,
                        startSample = totalAudioSamplesQueued,
                        sampleCount = 0,
                        sampleRate = sampleRate,
                        useSynthBed = project.useStudioSynthAudio,
                        shotTransitions = shotTransitions,
                        endOfStream = true,
                        timeoutUs = 5_000L
                    )
                    val (writtenCount, updatedLastPts) = drainAudioEncoder(
                        audioEncoder = audioEncoder,
                        bufferInfo = audioBufferInfo,
                        muxer = muxer,
                        audioTrackIndex = audioTrackIndex,
                        lastAudioPtsUs = lastAudioPtsUs,
                        timeoutUs = 5_000L,
                        waitUntilEos = true
                    )
                    audioSamplesWritten += writtenCount
                    lastAudioPtsUs = updatedLastPts
                }

                exportCompletedNormally = true
            } finally {
                runCatching { videoEncoder.stop() }
                runCatching { videoEncoder.release() }
                runCatching { inputSurface.release() }
                runCatching { audioEncoder?.stop() }
                runCatching { audioEncoder?.release() }

                // Only call muxer.stop() if muxer was started AND all added tracks received at least 1 sample
                val allTracksStarted = videoSamplesWritten > 0L &&
                    (audioTrackIndex < 0 || audioSamplesWritten > 0L)
                if (muxerStarted && allTracksStarted && exportCompletedNormally) {
                    runCatching { muxer.stop() }
                }
                runCatching { muxer.release() }

                if (!exportCompletedNormally && outputFile.exists()) {
                    runCatching { outputFile.delete() }
                }
            }

            val elapsedMs = (System.currentTimeMillis() - startTimeMs).coerceAtLeast(100L)
            ExportMp4State.Completed(
                filePath = outputFile.absolutePath,
                fileName = outputFile.name,
                resolutionLabel = "${width}×${height} (${resPreset.label})",
                bitrateLabel = "${exportConfig.bitratePreset.mbpsValue} Mbps • $fps FPS",
                durationSeconds = totalDurationSec,
                fileSizeKb = (outputFile.length() / 1024L).coerceAtLeast(1L),
                elapsedMs = elapsedMs
            )
        }
    }

    private fun createConfiguredVideoEncoder(
        config: VideoExportConfig
    ): Triple<Int, Int, MediaCodec> {
        val requestedW = config.resolution.width
        val requestedH = config.resolution.height
        val fps = config.fps.coerceIn(24, 60)
        val bitrate = config.bitratePreset.bitrateBps

        val candidates = listOf(
            requestedW to requestedH,
            1920 to 1080,
            1280 to 720
        ).distinct()

        for ((w, h) in candidates) {
            val encoderResult = runCatching {
                val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, w, h).apply {
                    setInteger(
                        MediaFormat.KEY_COLOR_FORMAT,
                        MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
                    )
                    setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
                    setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                    if (config.ultraFastHardwareTurbo) {
                        setInteger(MediaFormat.KEY_PRIORITY, 0) // Real-time turbo priority
                    }
                }
                val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
                codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                Triple(w, h, codec)
            }
            if (encoderResult.isSuccess) {
                return encoderResult.getOrThrow()
            }
        }
        val fallbackW = VideoResolutionPreset.RES_720P.width
        val fallbackH = VideoResolutionPreset.RES_720P.height
        val fallbackFormat = MediaFormat.createVideoFormat(
            MediaFormat.MIMETYPE_VIDEO_AVC,
            fallbackW,
            fallbackH
        ).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, fps)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }
        val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
        codec.configure(fallbackFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        return Triple(fallbackW, fallbackH, codec)
    }

    /**
     * Feeds synthesized PCM audio samples + SFX into the AAC encoder and returns the updated `totalAudioSamplesQueued`.
     */
    private fun feedPcmSamplesWithSfx(
        audioEncoder: MediaCodec,
        startSample: Long,
        sampleCount: Int,
        sampleRate: Int,
        useSynthBed: Boolean,
        shotTransitions: List<Pair<Float, SoundEffectType>>,
        endOfStream: Boolean,
        timeoutUs: Long
    ): Long {
        val inIdx = audioEncoder.dequeueInputBuffer(timeoutUs)
        if (inIdx < 0) return startSample
        val inputBuffer: ByteBuffer = audioEncoder.getInputBuffer(inIdx) ?: return startSample
        inputBuffer.clear()

        val ptsUs = (startSample * 1_000_000L) / sampleRate
        if (endOfStream || sampleCount <= 0) {
            audioEncoder.queueInputBuffer(
                inIdx,
                0,
                0,
                ptsUs,
                MediaCodec.BUFFER_FLAG_END_OF_STREAM
            )
            return startSample
        }

        val maxShorts = inputBuffer.remaining() / 2
        val actualCount = sampleCount.coerceAtMost(maxShorts)

        for (i in 0 until actualCount) {
            val globalSample = startSample + i
            val t = globalSample.toDouble() / sampleRate.toDouble()
            var mixSample = 0.0

            // 1. Newsroom Synth Bed
            if (useSynthBed) {
                val beatPhase = (t * 2.0) % 1.0
                val envelope = if (beatPhase < 0.15) (1.0 - beatPhase / 0.15) else 0.14
                val carrier = sin(2.0 * PI * 220.0 * t) * 0.45 +
                    sin(2.0 * PI * 440.0 * t) * 0.30 +
                    sin(2.0 * PI * 659.25 * t) * 0.25
                mixSample += carrier * envelope * 3600.0
            }

            // 2. Shot Transition Sound Effects (SFX)
            for ((startSec, sfx) in shotTransitions) {
                val sfxDurSec = sfx.durationMs / 1000.0
                if (t >= startSec && t <= startSec + sfxDurSec && sfxDurSec > 0.0) {
                    val sfxProgress = (t - startSec) / sfxDurSec
                    val freq = sfx.freqStartHz + (sfx.freqEndHz - sfx.freqStartHz) * sfxProgress
                    val sfxEnv = sin(PI * sfxProgress)
                    val sfxWave = sin(2.0 * PI * freq * t)
                    mixSample += sfxWave * sfxEnv * 8500.0
                }
            }

            val clamped = mixSample.toInt().coerceIn(-32767, 32767).toShort()
            inputBuffer.putShort(clamped)
        }

        audioEncoder.queueInputBuffer(inIdx, 0, actualCount * 2, ptsUs, 0)
        return startSample + actualCount
    }

    /**
     * Drains encoded AAC packets into the MediaMuxer with strictly monotonic timestamps.
     * Returns `(samplesWrittenCount, updatedLastAudioPtsUs)`.
     */
    private fun drainAudioEncoder(
        audioEncoder: MediaCodec,
        bufferInfo: MediaCodec.BufferInfo,
        muxer: MediaMuxer,
        audioTrackIndex: Int,
        lastAudioPtsUs: Long,
        timeoutUs: Long,
        waitUntilEos: Boolean
    ): Pair<Long, Long> {
        var written = 0L
        var currentLastPts = lastAudioPtsUs
        var retries = 0
        val maxRetries = if (waitUntilEos) 30 else 1

        while (retries < maxRetries) {
            val outIdx = audioEncoder.dequeueOutputBuffer(bufferInfo, timeoutUs)
            if (outIdx == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!waitUntilEos) break
                retries++
                continue
            } else if (outIdx < 0) {
                break
            }

            val encodedData = audioEncoder.getOutputBuffer(outIdx)
            val isConfig = (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0
            if (encodedData != null && bufferInfo.size > 0 && !isConfig) {
                if (bufferInfo.presentationTimeUs <= currentLastPts) {
                    bufferInfo.presentationTimeUs = currentLastPts + 1_000L
                }
                currentLastPts = bufferInfo.presentationTimeUs

                encodedData.position(bufferInfo.offset)
                encodedData.limit(bufferInfo.offset + bufferInfo.size)
                muxer.writeSampleData(audioTrackIndex, encodedData, bufferInfo)
                written++
            }
            audioEncoder.releaseOutputBuffer(outIdx, false)
            if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
        }

        return written to currentLastPts
    }
}
