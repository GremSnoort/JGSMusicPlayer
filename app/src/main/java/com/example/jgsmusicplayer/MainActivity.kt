package com.example.jgsmusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
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
import com.example.jgsmusicplayer.ui.theme.JGSBackgroundTarget
import com.example.jgsmusicplayer.ui.theme.JGSMusicPlayerTheme
import com.example.jgsmusicplayer.ui.theme.JGSTheme
import com.example.jgsmusicplayer.ui.theme.JGSThemedBackground
import com.example.jgsmusicplayer.ui.theme.rememberJGSThemeController

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            AppRoot(viewModel = viewModel)
        }
    }
}

@Composable
private fun AppRoot(viewModel: MainViewModel) {
    val themeController = rememberJGSThemeController()

    JGSMusicPlayerTheme(themeSpec = themeController.currentTheme) {
        App(
            viewModel = viewModel,
            currentThemeButtonLabel = themeController.currentButtonLabel,
            onCycleTheme = themeController.cycleTheme
        )
    }
}

@Composable
private fun App(
    viewModel: MainViewModel,
    currentThemeButtonLabel: String,
    onCycleTheme: () -> Unit
) {
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

    Box(modifier = Modifier.fillMaxSize()) {
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(
                    start = JGSTheme.design.sizes.screenContentPadding,
                    bottom = JGSTheme.design.sizes.sectionSpacingSmall
                )
        ) {
            BottomGlassActionButton(
                text = currentThemeButtonLabel,
                onClick = onCycleTheme
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
    var globalQuery by rememberSaveable { mutableStateOf("") }
    var query by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val libraryListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val folderScrollSnapshotsState = rememberSaveable(saver = FolderScrollSnapshotsStateSaver) {
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
    val design = JGSTheme.design

    val density = LocalDensity.current
    val cfg = LocalConfiguration.current
    val screenWidthPx = with(density) { cfg.screenWidthDp.dp.toPx() }
    val edgePx = with(density) { design.sizes.edgeSwipeZone.toPx() }
    val triggerPx = with(density) { design.sizes.edgeSwipeTrigger.toPx() }

    var dragSum by remember { mutableStateOf(0f) }
    var backTriggered by remember { mutableStateOf(false) }
    var isEdgeDrag by remember { mutableStateOf(false) }
    val seaBorder = design.brushes.primaryBorder
    val glassBg = design.colors.glassSurface
    val textPrimary = design.colors.textPrimary
    val textSecondary = design.colors.textSecondary
    val listTextShadowColor = design.colors.listTextShadow
    val listTextShadow = if (listTextShadowColor.alpha > 0f) {
        Shadow(
            color = listTextShadowColor,
            offset = Offset(0f, 0f),
            blurRadius = with(density) { 3.dp.toPx() }
        )
    } else {
        null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
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
        JGSThemedBackground(
            target = JGSBackgroundTarget.LIBRARY,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            containerColor = design.colors.transparent,
            topBar = {
                NeonTopBar(
                    title = selectedFolder ?: "JGS Music Player",
                    showBack = selectedFolder != null,
                    onBack = { backFromFolder() }
                )
            },
            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(
                            horizontal = design.sizes.screenContentPadding,
                            vertical = design.sizes.sectionSpacingSmall
                        ),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.nowPlaying != null) {
                        BottomGlassActionButton(
                            text = "Now",
                            onClick = actions.openNow
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .padding(design.sizes.screenContentPadding)
                    .fillMaxSize()
            ) {
                if (!hasPermission) {
                    Text(
                        "No permission to access the audio",
                        color = textSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(design.sizes.sectionSpacingSmall))
                    Surface(
                        onClick = onRequestPermission,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner - 2.dp),
                        color = glassBg,
                        border = BorderStroke(1.dp, seaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            "Allow",
                            modifier = Modifier.padding(
                                horizontal = design.sizes.screenContentPadding,
                                vertical = design.sizes.sectionSpacingSmall
                            ),
                            color = textPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    return@Column
                }

                uiState.errorMessage?.let { errorMessage ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner),
                        color = design.colors.errorSurface,
                        border = BorderStroke(
                            1.dp,
                            design.brushes.errorBorder
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
                                color = textPrimary,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Dismiss",
                                color = textPrimary,
                                modifier = Modifier.clickable { onDismissError() },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    Spacer(Modifier.height(design.sizes.sectionSpacingMedium))
                }

                if (files.isEmpty()) {
                    Text(
                        "MP3 not found",
                        color = textSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(design.sizes.sectionSpacingSmall))
                    Surface(
                        onClick = onRefreshFiles,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner - 2.dp),
                        color = glassBg,
                        border = BorderStroke(1.dp, seaBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Text(
                            "Refresh",
                            modifier = Modifier.padding(
                                horizontal = design.sizes.screenContentPadding,
                                vertical = design.sizes.sectionSpacingSmall
                            ),
                            color = textPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    return@Column
                }

                if (uiState.nowPlaying != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner),
                        color = glassBg,
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
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    uiState.nowPlaying.name,
                                    color = textPrimary,
                                    modifier = Modifier.clickable { actions.openNow() },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }

                            Spacer(Modifier.width(design.sizes.sectionSpacingSmall))

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
                                    border = design.brushes.stopBorder
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(design.sizes.sectionSpacingMedium))
                }

                if (selectedFolder == null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner),
                        color = glassBg,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        SearchField(
                            value = globalQuery,
                            onValueChange = { globalQuery = it },
                            label = "Search all tracks",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = design.sizes.screenContentPadding,
                                    vertical = design.sizes.sectionSpacingSmall
                                ),
                            textColor = textPrimary,
                            labelColor = textSecondary,
                            cursorColor = design.colors.seekKnob,
                            onDone = { focusManager.clearFocus() }
                        )
                    }

                    Spacer(Modifier.height(design.sizes.sectionSpacingSmall))

                    val globalResults = remember(files, globalQuery) {
                        if (globalQuery.isBlank()) {
                            emptyList()
                        } else {
                            files.filter { file ->
                                file.name.contains(globalQuery, ignoreCase = true) ||
                                    file.folder.contains(globalQuery, ignoreCase = true)
                            }
                        }
                    }

                    if (globalQuery.isNotBlank() && globalResults.isEmpty()) {
                        Text(
                            "Nothing found",
                            color = textSecondary,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    LazyColumn(
                        state = libraryListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (globalQuery.isBlank()) {
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
                                        .padding(
                                            vertical = design.sizes.listItemVerticalPadding,
                                            horizontal = design.sizes.listItemHorizontalPadding
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            folder.ifBlank { "/" },
                                            color = textPrimary,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                shadow = listTextShadow
                                            )
                                        )
                                        Text(
                                            "${folders[folder]?.size ?: 0} file(s)",
                                            color = textSecondary,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                shadow = listTextShadow
                                            )
                                        )
                                    }
                                }

                                if (index != keys.lastIndex) {
                                    SeaDivider(
                                        modifier = Modifier.padding(horizontal = design.sizes.listItemHorizontalPadding),
                                        brush = seaBorder
                                    )
                                }
                            }
                        } else {
                            items(globalResults.size) { index ->
                                val file = globalResults[index]

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { actions.playTrack(file) }
                                        .padding(
                                            vertical = design.sizes.listItemVerticalPadding,
                                            horizontal = design.sizes.listItemHorizontalPadding
                                        ),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            file.name,
                                            color = textPrimary,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                shadow = listTextShadow
                                            )
                                        )
                                        Text(
                                            file.folder.ifBlank { "/" },
                                            color = textSecondary,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                shadow = listTextShadow
                                            )
                                        )
                                    }
                                }

                                if (index != globalResults.lastIndex) {
                                    SeaDivider(
                                        modifier = Modifier.padding(horizontal = design.sizes.listItemHorizontalPadding),
                                        brush = seaBorder
                                    )
                                }
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
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner),
                        color = glassBg,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        SearchField(
                            value = query,
                            onValueChange = { query = it },
                            label = "Search",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = design.sizes.screenContentPadding,
                                    vertical = design.sizes.sectionSpacingSmall
                                ),
                            textColor = textPrimary,
                            labelColor = textSecondary,
                            cursorColor = design.colors.seekKnob,
                            onDone = { focusManager.clearFocus() }
                        )
                    }

                    Spacer(Modifier.height(design.sizes.sectionSpacingSmall))

                    val list = folders[selectedFolder] ?: emptyList()
                    val filtered = remember(list, query) {
                        if (query.isBlank()) list else list.filter { it.name.contains(query, ignoreCase = true) }
                    }

                    if (filtered.isEmpty()) {
                        Text(
                            "Nothing found",
                            color = textSecondary,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
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
                                    .padding(
                                        vertical = design.sizes.listItemVerticalPadding,
                                        horizontal = design.sizes.listItemHorizontalPadding
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    file.name,
                                    color = textPrimary,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        shadow = listTextShadow
                                    )
                                )
                            }

                            if (index != filtered.lastIndex) {
                                SeaDivider(
                                    modifier = Modifier.padding(horizontal = design.sizes.listItemHorizontalPadding),
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
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    textColor: Color,
    labelColor: Color,
    cursorColor: Color,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = label,
                color = labelColor,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Text(
                text = "⌕",
                color = labelColor,
                style = MaterialTheme.typography.titleMedium
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onDone = { onDone() }
        ),
        modifier = modifier.height(56.dp),
        minLines = 1,
        maxLines = 1,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = cursorColor,
            focusedPlaceholderColor = labelColor,
            unfocusedPlaceholderColor = labelColor
        )
    )
}

@Composable
private fun SmallGlassIconButton(
    label: String,
    onClick: () -> Unit,
    border: Brush,
    size: Dp = 44.dp
) {
    val design = JGSTheme.design
    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.smallButtonCorner),
        color = design.colors.glassSurface,
        border = BorderStroke(1.dp, border),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.size(size)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = design.colors.textPrimary.copy(alpha = 0.95f),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun BottomGlassActionButton(
    text: String,
    onClick: () -> Unit
) {
    val design = JGSTheme.design

    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.fabCorner),
        color = design.colors.transparent,
        border = BorderStroke(1.dp, design.brushes.primaryBorder),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .background(design.brushes.fabBackground)
                .padding(
                    horizontal = design.sizes.floatingNowHorizontalPadding,
                    vertical = design.sizes.floatingNowVerticalPadding
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = design.colors.textOnAccent,
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

private val FolderScrollSnapshotsStateSaver =
    listSaver<androidx.compose.runtime.MutableState<Map<String, Pair<Int, Int>>>, String>(
        save = { snapshotsState ->
            snapshotsState.value.entries.flatMap { (folder, position) ->
                listOf(folder, position.first.toString(), position.second.toString())
            }
        },
        restore = { restored ->
            mutableStateOf(
                restored.chunked(3).associate { chunk ->
                    val folder = chunk[0]
                    val index = chunk[1].toIntOrNull() ?: 0
                    val offset = chunk[2].toIntOrNull() ?: 0
                    folder to (index to offset)
                }
            )
        }
    )
