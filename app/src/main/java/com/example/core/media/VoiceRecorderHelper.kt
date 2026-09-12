package com.example.core.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class VoiceRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordedDurationSeconds = MutableStateFlow(0)
    val recordedDurationSeconds: StateFlow<Int> = _recordedDurationSeconds.asStateFlow()

    fun startRecording(): Boolean {
        return try {
            val audioDir = File(context.cacheDir, "voice_notes").apply { mkdirs() }
            outputFile = File(audioDir, "rec_${System.currentTimeMillis()}.m4a")

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            _isRecording.value = true
            _recordedDurationSeconds.value = 0

            recordingJob?.cancel()
            recordingJob = scope.launch {
                while (_isRecording.value) {
                    delay(1000)
                    _recordedDurationSeconds.value += 1
                }
            }
            true
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Error starting recording: ${e.message}", e)
            _isRecording.value = false
            false
        }
    }

    fun stopRecording(): File? {
        recordingJob?.cancel()
        _isRecording.value = false
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            outputFile
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Error stopping recording", e)
            mediaRecorder = null
            null
        }
    }

    fun cancelRecording() {
        recordingJob?.cancel()
        _isRecording.value = false
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Ignore
        } finally {
            mediaRecorder = null
            outputFile?.delete()
            outputFile = null
        }
    }
}
