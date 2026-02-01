package com.example.jgsmusicplayer

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Mp3BrowserAndPlayer()
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun Mp3BrowserAndPlayer() {
    val context = androidx.compose.ui.platform.LocalContext.current

    val player = remember { ExoPlayer.Builder(context).build() }

    var hasPermission by remember { mutableStateOf(false) }
    var files by remember { mutableStateOf<List<AudioFile>>(emptyList()) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var nowPlaying by remember { mutableStateOf<AudioFile?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    DisposableEffect(player) {
        val listener = object : androidx.media3.common.Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == androidx.media3.common.Player.STATE_ENDED) {
                    nowPlaying = null
                    player.clearMediaItems()
                }
            }
        }

        player.addListener(listener)

        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) files = queryAudio(context)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_MEDIA_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        hasPermission = granted
        if (granted) files = queryAudio(context)
        else permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
    }

    val folders = remember(files) { files.groupBy { it.folder }.toSortedMap() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MP3 Player") },
                navigationIcon = {
                    if (selectedFolder != null) {
                        Text(
                            "←",
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .clickable { selectedFolder = null; query = "" }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            if (!hasPermission) {
                Text("No permission to access the audio", modifier = Modifier.padding(16.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { Text("Allow") }
                return@Column
            }

            if (files.isEmpty()) {
                Text("MP3 not found", modifier = Modifier.padding(16.dp))
                Button(
                    onClick = { files = queryAudio(context) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { Text("Refresh") }
                return@Column
            }

            // Панель управления
            if (nowPlaying != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        "Playing: ${nowPlaying!!.name}",
                        modifier = Modifier.weight(1f)
                    )

                    Button(onClick = {
                        if (isPlaying) player.pause() else player.play()
                    }) {
                        Text(if (isPlaying) "Pause" else "Play")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        player.stop()
                        player.clearMediaItems()
                        nowPlaying = null
                    }) { Text("Stop") }

                }
                HorizontalDivider()
            }

            if (selectedFolder == null) {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(folders.keys.toList()) { folder ->
                        ListItem(
                            headlineContent = { Text(folder.ifBlank { "/" }) },
                            supportingContent = { Text("${folders[folder]?.size ?: 0} file(s)") },
                            modifier = Modifier.clickable { selectedFolder = folder; query = "" }
                        )
                        HorizontalDivider()
                    }
                }
            } else {
                val list = folders[selectedFolder] ?: emptyList()
                val focusManager = LocalFocusManager.current

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )

                val filtered = remember(list, query) {
                    if (query.isBlank()) list
                    else list.filter { it.name.contains(query, ignoreCase = true) }
                }

                if (filtered.isEmpty()) {
                    Text(
                        "Nothing found",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                LazyColumn(Modifier.fillMaxSize()) {
                    items(filtered) { f ->
                        ListItem(
                            headlineContent = { Text(f.name) },
                            modifier = Modifier.clickable {
                                nowPlaying = f
                                player.stop()
                                player.clearMediaItems()
                                player.setMediaItem(MediaItem.fromUri(f.uri))
                                player.prepare()
                                player.play()
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

private fun queryVideo(context: Context): List<VideoFile> {
    val cr = context.contentResolver
    val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.RELATIVE_PATH
    )

    val selection = "${MediaStore.Video.Media.MIME_TYPE}=?"
    val selectionArgs = arrayOf("video/mp4")

    val sortOrder =
        "${MediaStore.Video.Media.RELATIVE_PATH} ASC, ${MediaStore.Video.Media.DISPLAY_NAME} ASC"

    val out = mutableListOf<VideoFile>()

    cr.query(collection, projection, selection, selectionArgs, sortOrder)?.use { c ->
        val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val pathCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)

        while (c.moveToNext()) {
            val id = c.getLong(idCol)
            val name = c.getString(nameCol) ?: "unknown.mp4"
            val folder = c.getString(pathCol) ?: ""
            val uri = ContentUris.withAppendedId(collection, id)
            out += VideoFile(name, folder, uri)
        }
    }
    return out
}

private fun queryAudio(context: Context): List<AudioFile> {
    val cr = context.contentResolver
    val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    val projection = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.DISPLAY_NAME,
        MediaStore.Audio.Media.RELATIVE_PATH
    )

    val selection = "(${MediaStore.Audio.Media.MIME_TYPE}=? OR ${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?)"
    val selectionArgs = arrayOf("audio/mpeg", "%.mp3")

    val sortOrder =
        "${MediaStore.Audio.Media.RELATIVE_PATH} ASC, ${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

    val out = mutableListOf<AudioFile>()

    cr.query(collection, projection, selection, selectionArgs, sortOrder)?.use { c ->
        val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
        val pathCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)

        while (c.moveToNext()) {
            val id = c.getLong(idCol)
            val name = c.getString(nameCol) ?: "unknown.mp3"
            val folder = c.getString(pathCol) ?: ""
            val uri = ContentUris.withAppendedId(collection, id)
            out += AudioFile(name, folder, uri)
        }
    }
    return out
}

