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
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                App()
            }
        }
    }
}

@Composable
private fun App() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val navController = rememberNavController()
    var uiState by remember { mutableStateOf(PlayerUiState()) }
    val latestUiState by rememberUpdatedState(uiState)

    val player = remember { ExoPlayer.Builder(context).build() }
    val actions = PlayerActions(
        openNow = { navController.navigate("player") { launchSingleTop = true } },

        playPause = {
            if (latestUiState.isPlaying) player.pause() else player.play()
        },

        stop = {
            player.stop()
            player.clearMediaItems()
            uiState = latestUiState.copy(
                nowPlaying = null,
                isPlaying = false,
                durationMs = 0L,
                positionMs = 0L
            )
        },

        playTrack = { f ->
            player.stop()
            player.clearMediaItems()
            player.setMediaItem(MediaItem.fromUri(f.uri))
            player.prepare()
            player.play()
            uiState = latestUiState.copy(nowPlaying = f, isPlaying = true)
            navController.navigate("player") { launchSingleTop = true }
        }
    )

    LaunchedEffect(player) {
        while (true) {
            val d = player.duration
            val p = player.currentPosition

            val newDuration = if (d > 0) d else 0L
            val newPosition = if (p > 0) p else 0L

            val cur = latestUiState
            if (cur.durationMs != newDuration || cur.positionMs != newPosition) {
                uiState = cur.copy(durationMs = newDuration, positionMs = newPosition)
            }

            delay(250)
        }
    }

    DisposableEffect(player) {
        onDispose {
            player.release()
        }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                uiState = latestUiState.copy(isPlaying = playing)
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    uiState = latestUiState.copy(
                        nowPlaying = null,
                        isPlaying = false,
                        durationMs = 0L,
                        positionMs = 0L
                    )
                    player.clearMediaItems()
                }
            }
        }

        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            Mp3BrowserAndPlayer(
                navController = navController,
                player = player,
                uiState = latestUiState,
                actions = actions
            )
        }
        composable("player") {
            PlayerScreen(
                onBack = { navController.popBackStack() },
                player = player,
                uiState = latestUiState,
                actions = actions
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun Mp3BrowserAndPlayer(
    navController: androidx.navigation.NavHostController,
    player: ExoPlayer,
    uiState: PlayerUiState,
    actions: PlayerActions
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    var hasPermission by remember { mutableStateOf(false) }
    var files by remember { mutableStateOf<List<AudioFile>>(emptyList()) }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) files = queryAudio(context)
    }

    LaunchedEffect(player) {
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
        },
        floatingActionButton = {
            if (uiState.nowPlaying != null) {
                FloatingActionButton(onClick = actions.openNow) {
                    Text("Now")
                }
            }
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
            if (uiState.nowPlaying != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        "Playing: ${uiState.nowPlaying!!.name}",
                        modifier = Modifier
                            .weight(1f)
                            .clickable { actions.openNow() }
                    )

                    Button(onClick = actions.playPause) {
                        Text(if (uiState.isPlaying) "Pause" else "Play")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(onClick = { actions.stop() }) { Text("Stop") }

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
                            modifier = Modifier.clickable { actions.playTrack(f) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun PlayerScreen(
    onBack: () -> Unit,
    player: ExoPlayer,
    uiState: PlayerUiState,
    actions: PlayerActions
) {
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPreviewMs by remember { mutableStateOf(0L) }

    LaunchedEffect(uiState.positionMs) {
        if (!isUserSeeking) {
            seekPreviewMs = uiState.positionMs
        }
    }

    val safeDuration = if (uiState.durationMs > 0) uiState.durationMs else 1L
    val sliderValue = (seekPreviewMs.coerceIn(0L, safeDuration)).toFloat() / safeDuration.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Player") },
                navigationIcon = {
                    Text(
                        "←",
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable { onBack() }
                    )
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

            if (uiState.nowPlaying == null) {
                LaunchedEffect(uiState.nowPlaying) { onBack() }
                return@Column
            }

            Text(uiState.nowPlaying.name, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                Text(formatMs(seekPreviewMs))
                Spacer(Modifier.weight(1f))
                Text(formatRemainingMs(seekPreviewMs, uiState.durationMs))
            }
            Spacer(Modifier.height(12.dp))

            Slider(
                value = sliderValue,
                onValueChange = { v ->
                    isUserSeeking = true
                    seekPreviewMs = (v * safeDuration).toLong()
                },
                onValueChangeFinished = {
                    player.seekTo(seekPreviewMs)
                    isUserSeeking = false
                }
            )

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val target = (player.currentPosition - 10_000).coerceAtLeast(0L)
                        player.seekTo(target)
                    }
                ) { Text("⟲ 10s") }
                Button(
                    onClick = {
                        val dur = if (player.duration > 0) player.duration else uiState.durationMs
                        val target = (player.currentPosition + 10_000).coerceAtMost(if (dur > 0) dur else Long.MAX_VALUE)
                        player.seekTo(target)
                    }
                ) { Text("10s ⟳") }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = actions.playPause) {
                    Text(if (uiState.isPlaying) "Pause" else "Play")
                }
                Button(onClick = actions.stop) { Text("Stop") }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "${min}:${sec.toString().padStart(2, '0')}"
}

private fun formatRemainingMs(positionMs: Long, durationMs: Long): String {
    val remain = (durationMs - positionMs).coerceAtLeast(0L)
    val totalSec = remain / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "-${min}:${sec.toString().padStart(2, '0')}"
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

