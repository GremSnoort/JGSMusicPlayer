package com.example.jgsmusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.jgsmusicplayer.model.AudioFile
import com.example.jgsmusicplayer.model.PlayerActions
import com.example.jgsmusicplayer.model.PlayerUiState
import com.example.jgsmusicplayer.ui.components.NeonTopBar
import com.example.jgsmusicplayer.ui.screens.PlayerScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                App(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun App(viewModel: MainViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val uiState = viewModel.uiState
    val files = viewModel.files
    val audioPermission = remember { requiredAudioPermission() }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.refreshAudioFiles()
    }

    LaunchedEffect(audioPermission) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            audioPermission
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.refreshAudioFiles()
        } else {
            permissionLauncher.launch(audioPermission)
        }
    }

    val openPlayer: () -> Unit = remember(navController) {
        {
            navController.navigate("player") {
                launchSingleTop = true
            }
        }
    }

    val returnToLibrary: () -> Unit = remember(navController) {
        {
            val returned = navController.popBackStack("library", inclusive = false)
            if (!returned) {
                navController.navigate("library") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            }
        }
    }

    val actions = remember(navController, viewModel) {
        PlayerActions(
            openNow = openPlayer,
            playPause = viewModel::playPause,
            stop = viewModel::stopPlayback,
            playTrack = { file ->
                viewModel.playTrack(file)
                openPlayer()
            },
            seekTo = viewModel::seekTo,
            seekBy = viewModel::seekBy,
            toggleLooping = viewModel::toggleLooping
        )
    }

    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            Mp3BrowserAndPlayer(
                uiState = uiState,
                files = files,
                hasPermission = hasPermission,
                onRequestPermission = { permissionLauncher.launch(audioPermission) },
                onRefreshFiles = viewModel::refreshAudioFiles,
                onDismissError = viewModel::clearPlaybackError,
                actions = actions
            )
        }
        composable("player") {
            PlayerScreen(
                onBack = returnToLibrary,
                uiState = uiState,
                onDismissError = viewModel::clearPlaybackError,
                actions = actions
            )
        }
    }
}

@Composable
private fun Mp3BrowserAndPlayer(
    uiState: PlayerUiState,
    files: List<AudioFile>,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onRefreshFiles: () -> Unit,
    onDismissError: () -> Unit,
    actions: PlayerActions
) {
    var selectedFolder by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val libraryListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val folderScrollSnapshotsState = rememberSaveable(stateSaver = FolderScrollSnapshotsSaver) {
        mutableStateOf(emptyMap<String, Pair<Int, Int>>())
    }
    val folderScrollSnapshots = folderScrollSnapshotsState.value

    fun backFromFolder() {
        selectedFolder = null
        query = ""
        focusManager.clearFocus()
    }

    BackHandler(enabled = selectedFolder != null) {
        backFromFolder()
    }

    val folders = remember(files) { files.groupBy { it.folder }.toSortedMap() }

    val density = LocalDensity.current
    val cfg = LocalConfiguration.current
    val screenWidthPx = with(density) { cfg.screenWidthDp.dp.toPx() }
    val edgePx = with(density) { 28.dp.toPx() }
    val triggerPx = with(density) { 72.dp.toPx() }

    var dragSum by remember { mutableStateOf(0f) }
    var backTriggered by remember { mutableStateOf(false) }
    var isEdgeDrag by remember { mutableStateOf(false) }

    val seaBorder = Brush.linearGradient(
        listOf(
            Color(0x662EE6FF),
            Color(0x6632FFA7),
            Color(0x669E6BFF)
        )
    )
    val glassBg = Color(0x12FFFFFF)
    val textPrimary = Color.White.copy(alpha = 0.92f)
    val textSecondary = Color.White.copy(alpha = 0.65f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(com.example.jgsmusicplayer.ui.theme.PlayerBg)
            .pointerInput(selectedFolder) {}
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    if (selectedFolder == null || !isEdgeDrag || backTriggered) return@rememberDraggableState

                    dragSum += delta
                    if (dragSum <= -triggerPx) {
                        backTriggered = true
                        backFromFolder()
                    }
                },
                onDragStarted = { startOffset ->
                    if (selectedFolder == null) return@draggable

                    dragSum = 0f
                    backTriggered = false
                    isEdgeDrag = startOffset.x >= (screenWidthPx - edgePx)
                },
                onDragStopped = {
                    isEdgeDrag = false
                    dragSum = 0f
                    backTriggered = false
                }
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NeonTopBar(
                    title = selectedFolder ?: "JGS Music Player",
                    showBack = selectedFolder != null,
                    onBack = { backFromFolder() }
                )
            },
            floatingActionButton = {
                if (uiState.nowPlaying != null) {
                    val fabBg = Brush.linearGradient(
                        listOf(
                            Color(0xCC121826),
                            Color(0xCC0D111A)
                        )
                    )

                    Surface(
                        onClick = actions.openNow,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.dp, seaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 10.dp
                    ) {
                        Box(
                            modifier = Modifier
                                .background(fabBg)
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
                    Text("No permission to access the audio", color = textSecondary)
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        onClick = onRequestPermission,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        color = glassBg,
                        border = BorderStroke(1.dp, seaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            "Allow",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = textPrimary
                        )
                    }
                    return@Column
                }

                uiState.errorMessage?.let { errorMessage ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        color = Color(0x22FF5B7A),
                        border = BorderStroke(
                            1.dp,
                            Brush.linearGradient(listOf(Color(0x99FF5B7A), Color(0x99FFB36B)))
                        ),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Dismiss",
                                color = textPrimary,
                                modifier = Modifier.clickable { onDismissError() }
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                }

                if (files.isEmpty()) {
                    Text("MP3 not found", color = textSecondary)
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        onClick = onRefreshFiles,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        color = glassBg,
                        border = BorderStroke(1.dp, seaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            "Refresh",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            color = textPrimary
                        )
                    }
                    return@Column
                }

                if (uiState.nowPlaying != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        color = glassBg,
                        border = BorderStroke(1.dp, seaBorder),
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
                                Text("Playing:", color = textSecondary, style = MaterialTheme.typography.labelMedium)
                                Text(
                                    uiState.nowPlaying.name,
                                    color = textPrimary,
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
                                    border = seaBorder
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
                    LazyColumn(
                        state = libraryListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val keys = folders.keys.toList()
                        items(keys.size) { index ->
                            val folder = keys[index]

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedFolder = folder
                                        query = ""
                                    }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(folder.ifBlank { "/" }, color = textPrimary)
                                    Text(
                                        "${folders[folder]?.size ?: 0} file(s)",
                                        color = textSecondary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            if (index != keys.lastIndex) {
                                SeaDivider(
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    brush = seaBorder
                                )
                            }
                        }
                    }
                } else {
                    val currentFolder = selectedFolder.orEmpty()
                    val savedFolderScroll = folderScrollSnapshots[currentFolder]
                    val folderListState = rememberSaveable(
                        currentFolder,
                        saver = LazyListState.Saver
                    ) {
                        LazyListState(
                            firstVisibleItemIndex = savedFolderScroll?.first ?: 0,
                            firstVisibleItemScrollOffset = savedFolderScroll?.second ?: 0
                        )
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
                        color = glassBg,
                        border = BorderStroke(1.dp, seaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text("Search", color = textSecondary) },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color(0xFF2EE6FF),
                                focusedLabelColor = textSecondary,
                                unfocusedLabelColor = textSecondary
                            )
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    val list = folders[selectedFolder] ?: emptyList()
                    val filtered = remember(list, query) {
                        if (query.isBlank()) list else list.filter { it.name.contains(query, ignoreCase = true) }
                    }

                    if (filtered.isEmpty()) {
                        Text("Nothing found", color = textSecondary, modifier = Modifier.padding(horizontal = 4.dp))
                    }

                    DisposableEffect(currentFolder, folderListState) {
                        onDispose {
                            folderScrollSnapshotsState.value = folderScrollSnapshots.toMutableMap().apply {
                                put(
                                    currentFolder,
                                    folderListState.firstVisibleItemIndex to folderListState.firstVisibleItemScrollOffset
                                )
                            }
                        }
                    }

                    LazyColumn(
                        state = folderListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered.size) { index ->
                            val file = filtered[index]

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { actions.playTrack(file) }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    file.name,
                                    color = textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (index != filtered.lastIndex) {
                                SeaDivider(
                                    modifier = Modifier.padding(horizontal = 6.dp),
                                    brush = seaBorder
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
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

private fun requiredAudioPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

private val FolderScrollSnapshotsSaver = listSaver<Map<String, Pair<Int, Int>>, String>(
    save = { snapshots ->
        snapshots.entries.flatMap { (folder, position) ->
            listOf(folder, position.first.toString(), position.second.toString())
        }
    },
    restore = { restored ->
        restored.chunked(3).associate { chunk ->
            val folder = chunk[0]
            val index = chunk[1].toIntOrNull() ?: 0
            val offset = chunk[2].toIntOrNull() ?: 0
            folder to (index to offset)
        }
    }
)
