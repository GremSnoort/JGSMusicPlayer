package com.example.jgsmusicplayer.model

import android.net.Uri

data class AudioFile(
    val name: String,
    val folder: String,
    val uri: Uri
)

data class VideoFile(
    val name: String,
    val folder: String,
    val uri: Uri
)

data class PlayerUiState(
    val nowPlaying: AudioFile? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L
)

data class PlayerActions(
    val openNow: () -> Unit,
    val playPause: () -> Unit,
    val stop: () -> Unit,
    val playTrack: (AudioFile) -> Unit
)
