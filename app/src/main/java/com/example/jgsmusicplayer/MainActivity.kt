package com.example.jgsmusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

import com.example.jgsmusicplayer.model.PlayerActions
import com.example.jgsmusicplayer.model.PlayerUiState
import com.example.jgsmusicplayer.model.AudioFile
import com.example.jgsmusicplayer.data.queryAudio
import com.example.jgsmusicplayer.ui.components.NeonTopBar
import com.example.jgsmusicplayer.ui.screens.PlayerScreen

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
    val context = LocalContext.current
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
    val context = LocalContext.current

    var hasPermission by remember { mutableStateOf(false) }
    var files by remember { mutableStateOf<List<AudioFile>>(emptyList()) }
    var selectedFolder by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }

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

    // --- стиль как у плеера ---
    val SeaBorder = Brush.linearGradient(
        listOf(
            Color(0x662EE6FF),
            Color(0x6632FFA7),
            Color(0x669E6BFF)
        )
    )
    val GlassBg = Color(0x12FFFFFF)
    val TextPrimary = Color.White.copy(alpha = 0.92f)
    val TextSecondary = Color.White.copy(alpha = 0.65f)
    // --------------------------

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.jgsmusicplayer.ui.theme.PlayerBg)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NeonTopBar(
                    title = if (selectedFolder == null) "JGS Music Player" else selectedFolder.orEmpty(),
                    showBack = selectedFolder != null,
                    onBack = { selectedFolder = null; query = "" }
                )
            },
            floatingActionButton = {
                if (uiState.nowPlaying != null) {

                    val FabBg = Brush.linearGradient(
                        listOf(
                            Color(0xCC121826), // тёмный морской
                            Color(0xCC0D111A)
                        )
                    )

                    Surface(
                        onClick = actions.openNow,
                        shape = RoundedCornerShape(18.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, SeaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 10.dp // немного глубины!
                    ) {
                        Box(
                            modifier = Modifier
                                .background(FabBg)
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Now",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {

                if (!hasPermission) {
                    Text("No permission to access the audio", color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        onClick = { permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO) },
                        shape = RoundedCornerShape(16.dp),
                        color = GlassBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SeaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            "Allow",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = TextPrimary
                        )
                    }
                    return@Column
                }

                if (files.isEmpty()) {
                    Text("MP3 not found", color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        onClick = { files = queryAudio(context) },
                        shape = RoundedCornerShape(16.dp),
                        color = GlassBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SeaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            "Refresh",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = TextPrimary
                        )
                    }
                    return@Column
                }

                // "Playing"
                if (uiState.nowPlaying != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = GlassBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SeaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("Playing:", color = TextSecondary, style = MaterialTheme.typography.labelMedium)
                                Text(
                                    uiState.nowPlaying!!.name,
                                    color = TextPrimary,
                                    modifier = Modifier.clickable { actions.openNow() }
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SmallGlassIconButton(
                                    label = if (uiState.isPlaying) "Ⅱ" else "▶",
                                    onClick = actions.playPause,
                                    border = SeaBorder
                                )

                                SmallGlassIconButton(
                                    label = "■",
                                    onClick = actions.stop,
                                    border = Brush.linearGradient(listOf(Color(0x66FF5B7A), Color(0x66FFB36B)))
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                if (selectedFolder == null) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        val keys = folders.keys.toList()
                        items(keys.size) { index ->
                            val folder = keys[index]

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFolder = folder; query = "" }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(folder.ifBlank { "/" }, color = TextPrimary)
                                    Text(
                                        "${folders[folder]?.size ?: 0} file(s)",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            if (index != keys.lastIndex) {
                                SeaDivider(
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    brush = SeaBorder
                                )
                            }
                        }
                    }
                } else {
                    val list = folders[selectedFolder] ?: emptyList()
                    val focusManager = LocalFocusManager.current

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = GlassBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SeaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Search", color = TextSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color(0xFF2EE6FF),
                                focusedLabelColor = TextSecondary,
                                unfocusedLabelColor = TextSecondary
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    val filtered = remember(list, query) {
                        if (query.isBlank()) list
                        else list.filter { it.name.contains(query, ignoreCase = true) }
                    }

                    if (filtered.isEmpty()) {
                        Text("Nothing found", color = TextSecondary, modifier = Modifier.padding(horizontal = 4.dp))
                    }

                    LazyColumn(Modifier.fillMaxSize()) {
                        items(filtered.size) { index ->
                            val f = filtered[index]

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { actions.playTrack(f) }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    f.name,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (index != filtered.lastIndex) {
                                SeaDivider(
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    brush = SeaBorder
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallGlassIconButton(
    label: String,
    onClick: () -> Unit,
    border: Brush,
    size: Dp = 44.dp
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = Color(0x1AFFFFFF),
        border = BorderStroke(1.dp, border),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.size(size)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.95f),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun SeaDivider(
    modifier: Modifier = Modifier,
    height: Dp = 1.dp,
    brush: Brush
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush)
    )
}
