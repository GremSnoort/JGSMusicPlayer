package com.example.jgsmusicplayer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

import com.example.jgsmusicplayer.model.AudioFile
import com.example.jgsmusicplayer.model.PlayerActions
import com.example.jgsmusicplayer.model.PlayerUiState
import com.example.jgsmusicplayer.ui.components.BottomGlassActionButton
import com.example.jgsmusicplayer.ui.components.ErrorBanner
import com.example.jgsmusicplayer.ui.components.NeonTopBar
import com.example.jgsmusicplayer.ui.components.SearchField
import com.example.jgsmusicplayer.ui.components.SeaDivider
import com.example.jgsmusicplayer.ui.components.SmallGlassIconButton
import com.example.jgsmusicplayer.ui.theme.JGSBackgroundTarget
import com.example.jgsmusicplayer.ui.theme.JGSTheme
import com.example.jgsmusicplayer.ui.theme.JGSThemedBackground

@Composable
fun Mp3BrowserAndPlayer(
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
                    LibraryEmptyActionBlock(
                        message = "No permission to access the audio",
                        actionLabel = "Allow",
                        messageColor = textSecondary,
                        actionTextColor = textPrimary,
                        actionBorder = seaBorder,
                        actionBackground = glassBg,
                        onActionClick = onRequestPermission
                    )
                    return@Column
                }

                uiState.errorMessage?.let { errorMessage ->
                    ErrorBanner(
                        message = errorMessage,
                        onDismiss = onDismissError,
                        textColor = textPrimary
                    )
                    Spacer(Modifier.height(design.sizes.sectionSpacingMedium))
                }

                if (files.isEmpty()) {
                    LibraryEmptyActionBlock(
                        message = "MP3 not found",
                        actionLabel = "Refresh",
                        messageColor = textSecondary,
                        actionTextColor = textPrimary,
                        actionBorder = seaBorder,
                        actionBackground = glassBg,
                        onActionClick = onRefreshFiles
                    )
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

                    if (globalQuery.isBlank()) {
                        val foldersList = folders.keys.toList()

                        LibraryListWithDividers(
                            items = foldersList,
                            state = libraryListState,
                            dividerBrush = seaBorder,
                            dividerPadding = Modifier.padding(horizontal = design.sizes.listItemHorizontalPadding),
                            modifier = Modifier.fillMaxSize()
                        ) { folder ->
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
                        }
                    } else {
                        LibraryListWithDividers(
                            items = globalResults,
                            state = libraryListState,
                            dividerBrush = seaBorder,
                            dividerPadding = Modifier.padding(horizontal = design.sizes.listItemHorizontalPadding),
                            modifier = Modifier.fillMaxSize()
                        ) { file ->
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

                    LibraryListWithDividers(
                        items = filtered,
                        state = folderListState,
                        dividerBrush = seaBorder,
                        dividerPadding = Modifier.padding(horizontal = design.sizes.listItemHorizontalPadding),
                        modifier = Modifier.fillMaxSize()
                    ) { file ->
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
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> LibraryListWithDividers(
    items: List<T>,
    state: LazyListState,
    dividerBrush: Brush,
    modifier: Modifier = Modifier,
    dividerPadding: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    LazyColumn(
        state = state,
        modifier = modifier
    ) {
        items(items.size) { index ->
            val item = items[index]

            itemContent(item)

            if (index != items.lastIndex) {
                SeaDivider(
                    modifier = dividerPadding,
                    brush = dividerBrush
                )
            }
        }
    }
}

@Composable
private fun LibraryEmptyActionBlock(
    message: String,
    actionLabel: String,
    messageColor: androidx.compose.ui.graphics.Color,
    actionTextColor: androidx.compose.ui.graphics.Color,
    actionBorder: Brush,
    actionBackground: androidx.compose.ui.graphics.Color,
    onActionClick: () -> Unit
) {
    val design = JGSTheme.design

    Text(
        text = message,
        color = messageColor,
        style = MaterialTheme.typography.bodyLarge
    )
    Spacer(Modifier.height(design.sizes.sectionSpacingSmall))
    Surface(
        onClick = onActionClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner - 2.dp),
        color = actionBackground,
        border = BorderStroke(1.dp, actionBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = actionLabel,
            modifier = Modifier.padding(
                horizontal = design.sizes.screenContentPadding,
                vertical = design.sizes.sectionSpacingSmall
            ),
            color = actionTextColor,
            style = MaterialTheme.typography.titleMedium
        )
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
