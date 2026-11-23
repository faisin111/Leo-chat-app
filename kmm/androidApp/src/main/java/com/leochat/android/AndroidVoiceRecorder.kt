package com.leochat.android

import android.content.Context
import android.media.MediaRecorder
import java.io.File

class AndroidVoiceRecorder(context: Context) {
    private val outputFile = File(context.cacheDir, "leochat-voice-${System.currentTimeMillis()}.m4a")
    private var recorder: MediaRecorder? = null

    fun start() {
        check(recorder == null) { "Voice recorder is already running" }
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
    }

    fun stop(): ByteArray {
        val active = recorder ?: error("Voice recorder is not running")
        runCatching { active.stop() }
        active.release()
        recorder = null
        return outputFile.readBytes()
    }

    fun cancel() {
        recorder?.runCatching { stop() }?.getOrNull()
        recorder?.release()
        recorder = null
        outputFile.delete()
    }
}
