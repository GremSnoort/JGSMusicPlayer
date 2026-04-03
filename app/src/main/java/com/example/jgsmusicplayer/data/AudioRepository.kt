package com.example.jgsmusicplayer.data

import android.content.Context

import com.example.jgsmusicplayer.model.AudioFile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioRepository(
    private val context: Context
) {
    suspend fun loadAudioFiles(): List<AudioFile> = withContext(Dispatchers.IO) {
        queryAudio(context)
    }
}
