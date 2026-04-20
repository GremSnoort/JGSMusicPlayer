package com.example.jgsmusicplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.jgsmusicplayer.model.PlayerActions
import com.example.jgsmusicplayer.ui.components.BottomGlassActionButton
import com.example.jgsmusicplayer.ui.screens.Mp3BrowserAndPlayer
import com.example.jgsmusicplayer.ui.screens.PlayerScreen
import com.example.jgsmusicplayer.ui.screens.themes.ThemeDetailScreen
import com.example.jgsmusicplayer.ui.screens.themes.ThemeGroupScreen
import com.example.jgsmusicplayer.ui.screens.themes.ThemesCollectionScreen
import com.example.jgsmusicplayer.ui.theme.JGSMusicPlayerTheme
import com.example.jgsmusicplayer.ui.theme.JGSTheme
import com.example.jgsmusicplayer.ui.theme.JGSThemeKey
import com.example.jgsmusicplayer.ui.theme.JGSThemes
import com.example.jgsmusicplayer.ui.theme.ThemeBias
import com.example.jgsmusicplayer.ui.theme.rememberJGSThemeController

@Composable
internal fun AppRoot(viewModel: MainViewModel) {
    val themeController = rememberJGSThemeController()

    JGSMusicPlayerTheme(themeSpec = themeController.currentTheme) {
        App(
            viewModel = viewModel,
            currentThemeName = themeController.currentTheme.displayName,
            currentThemeKey = themeController.currentTheme.key,
            onSetTheme = themeController.setTheme,
            getThemeBias = themeController.getThemeBias,
            onSetThemeBias = themeController.setThemeBias
        )
    }
}

@Composable
private fun App(
    viewModel: MainViewModel,
    currentThemeName: String,
    currentThemeKey: JGSThemeKey,
    onSetTheme: (JGSThemeKey) -> Unit,
    getThemeBias: (JGSThemeKey) -> ThemeBias?,
    onSetThemeBias: (JGSThemeKey, ThemeBias) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
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

    val openThemesCollection: () -> Unit = remember(navController) {
        {
            navController.navigate("themes_collection") {
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

    val safePop: () -> Unit = remember(navController) {
        {
            val returned = navController.popBackStack()
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

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route.orEmpty()

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
            composable("themes_collection") {
                ThemesCollectionScreen(
                    currentThemeName = currentThemeName,
                    onBack = safePop,
                    onOpenGroup = { group ->
                        navController.navigate("themes_group/${group.name}") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("themes_group/{group}") { backStackEntry ->
                val groupName = backStackEntry.arguments?.getString("group").orEmpty()
                val group = JGSThemes.groupFromName(groupName)
                ThemeGroupScreen(
                    group = group,
                    currentThemeKey = currentThemeKey,
                    onBack = safePop,
                    onSelectTheme = { theme ->
                        onSetTheme(theme.key)
                    },
                    onOpenTheme = { theme ->
                        navController.navigate("theme_detail/${theme.key.name}") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("theme_detail/{themeKey}") { backStackEntry ->
                val keyName = backStackEntry.arguments?.getString("themeKey").orEmpty()
                val theme = JGSThemes.fromKeyName(keyName)
                val groupThemes = JGSThemes.byGroup(theme.group)
                val currentIndex = groupThemes.indexOfFirst { it.key == theme.key }
                val prevTheme = groupThemes.getOrNull(currentIndex - 1)
                val nextTheme = groupThemes.getOrNull(currentIndex + 1)
                ThemeDetailScreen(
                    theme = theme,
                    isActive = theme.key == currentThemeKey,
                    onBack = safePop,
                    getThemeBias = getThemeBias,
                    onSetThemeBias = onSetThemeBias,
                    onPrev = prevTheme?.let {
                        {
                            navController.popBackStack()
                            navController.navigate("theme_detail/${it.key.name}") {
                                launchSingleTop = true
                            }
                        }
                    },
                    onNext = nextTheme?.let {
                        {
                            navController.popBackStack()
                            navController.navigate("theme_detail/${it.key.name}") {
                                launchSingleTop = true
                            }
                        }
                    },
                    onApply = {
                        onSetTheme(theme.key)
                        safePop()
                    }
                )
            }
        }

        val isThemesRoute = currentRoute.startsWith("themes_collection") ||
            currentRoute.startsWith("themes_group") ||
            currentRoute.startsWith("theme_detail")

        if (!isThemesRoute) {
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
                    text = "Themes",
                    onClick = openThemesCollection
                )
            }
        }
    }
}

private fun requiredAudioPermission(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
