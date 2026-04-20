package com.example.jgsmusicplayer.ui.screens.themes

import android.widget.ImageView

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex

import com.example.jgsmusicplayer.ui.components.NeonTopBar
import com.example.jgsmusicplayer.ui.components.GlassTextButton
import com.example.jgsmusicplayer.ui.components.edgeSwipeBackModifier
import com.example.jgsmusicplayer.ui.theme.JGSBackgroundTarget
import com.example.jgsmusicplayer.ui.theme.JGSMusicPlayerTheme
import com.example.jgsmusicplayer.ui.theme.JGSTheme
import com.example.jgsmusicplayer.ui.theme.JGSThemeGroup
import com.example.jgsmusicplayer.ui.theme.JGSThemeKey
import com.example.jgsmusicplayer.ui.theme.JGSThemeSpec
import com.example.jgsmusicplayer.ui.theme.JGSThemedBackground
import com.example.jgsmusicplayer.ui.theme.JGSThemes
import com.example.jgsmusicplayer.ui.theme.ThemeBias

@Composable
fun ThemesCollectionScreen(
    currentThemeName: String,
    onBack: () -> Unit,
    onOpenGroup: (JGSThemeGroup) -> Unit
) {
    val design = JGSTheme.design
    val groups = JGSThemes.groups

    BackHandler(onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(edgeSwipeBackModifier(onBack))
    ) {
        JGSThemedBackground(
            target = JGSBackgroundTarget.LIBRARY,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            containerColor = design.colors.transparent,
            topBar = {
                NeonTopBar(
                    title = "ThemesCollection",
                    showBack = true,
                    onBack = onBack
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(design.sizes.screenContentPadding)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Current: $currentThemeName",
                    color = design.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(design.sizes.sectionSpacingMedium))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(design.sizes.sectionSpacingSmall),
                    horizontalArrangement = Arrangement.spacedBy(design.sizes.sectionSpacingSmall)
                ) {
                    items(groups) { group ->
                        ThemeGroupTile(
                            group = group,
                            onOpenGroup = onOpenGroup
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeGroupTile(
    group: JGSThemeGroup,
    onOpenGroup: (JGSThemeGroup) -> Unit
) {
    val design = JGSTheme.design
    val previewTheme = remember(group) { JGSThemes.byGroup(group).firstOrNull() }

    ThemeGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenGroup(group) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (previewTheme != null) {
                ThemePreviewImage(
                    backgroundRes = previewTheme.backgrounds.libraryBackgroundRes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
            Text(
                text = group.displayName,
                color = design.colors.textPrimary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
fun ThemeGroupScreen(
    group: JGSThemeGroup,
    currentThemeKey: JGSThemeKey,
    onBack: () -> Unit,
    onSelectTheme: (JGSThemeSpec) -> Unit,
    onOpenTheme: (JGSThemeSpec) -> Unit
) {
    val design = JGSTheme.design
    val themes = remember(group) { JGSThemes.byGroup(group) }

    BackHandler(onBack = onBack)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(edgeSwipeBackModifier(onBack))
    ) {
        JGSThemedBackground(
            target = JGSBackgroundTarget.LIBRARY,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            containerColor = design.colors.transparent,
            topBar = {
                NeonTopBar(
                    title = group.displayName,
                    showBack = true,
                    onBack = onBack
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(design.sizes.screenContentPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(design.sizes.sectionSpacingSmall)
            ) {
                items(themes.size) { index ->
                    val theme = themes[index]
                    ThemeCard(
                        theme = theme,
                        isActive = theme.key == currentThemeKey,
                        onOpen = { onOpenTheme(theme) },
                        onApply = { onSelectTheme(theme) }
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeDetailScreen(
    theme: JGSThemeSpec,
    isActive: Boolean,
    onBack: () -> Unit,
    getThemeBias: (JGSThemeKey) -> ThemeBias?,
    onSetThemeBias: (JGSThemeKey, ThemeBias) -> Unit,
    onPrev: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onApply: () -> Unit
) {
    JGSMusicPlayerTheme(themeSpec = theme) {
        val design = JGSTheme.design
        val initialBias = getThemeBias(theme.key)
            ?: ThemeBias(theme.backgrounds.cropBiasX, theme.backgrounds.cropBiasY)
        var biasX by remember(theme.key) { mutableStateOf(initialBias.x) }
        var biasY by remember(theme.key) { mutableStateOf(initialBias.y) }
        LaunchedEffect(theme.key, initialBias.x, initialBias.y) {
            biasX = initialBias.x
            biasY = initialBias.y
        }
        val density = LocalDensity.current
        val cfg = LocalConfiguration.current
        val screenWidthPx = with(density) { cfg.screenWidthDp.dp.toPx() }
        val edgePx = with(density) { design.sizes.edgeSwipeZone.toPx() }
        var allowDrag by remember(theme.key) { mutableStateOf(false) }

        BackHandler(onBack = onBack)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(edgeSwipeBackModifier(onBack))
                .pointerInput(theme.key) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            allowDrag = startOffset.x < (screenWidthPx - edgePx)
                        },
                        onDragEnd = { allowDrag = false },
                        onDragCancel = { allowDrag = false },
                        onDrag = { change, dragAmount ->
                            if (!allowDrag) return@detectDragGestures
                            change.consume()
                            val w = size.width
                            val h = size.height
                            if (w <= 0f || h <= 0f) return@detectDragGestures
                            biasX = (biasX - dragAmount.x / w).coerceIn(0f, 1f)
                            biasY = (biasY - dragAmount.y / h).coerceIn(0f, 1f)
                        }
                    )
                }
        ) {
            ThemePreviewBackground(
                theme = theme,
                cropBiasX = biasX,
                cropBiasY = biasY,
                modifier = Modifier.fillMaxSize()
            )

            Scaffold(
                containerColor = design.colors.transparent,
                topBar = {
                    NeonTopBar(
                        title = theme.displayName,
                        showBack = true,
                        onBack = onBack
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = design.sizes.screenContentPadding,
                                vertical = design.sizes.sectionSpacingLarge
                            )
                            .background(
                                color = design.colors.glassSurface,
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner)
                            )
                            .padding(design.sizes.sectionSpacingMedium),
                        verticalArrangement = Arrangement.spacedBy(design.sizes.sectionSpacingSmall)
                    ) {
                        Text(
                            text = "Drag to adjust framing",
                            color = design.colors.textSecondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = theme.description,
                            color = design.colors.textSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (isActive) {
                            Text(
                                text = "Active",
                                color = design.colors.textPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        GlassTextButton(
                            text = if (isActive) "Applied" else "Apply",
                            onClick = {
                                onSetThemeBias(theme.key, ThemeBias(biasX, biasY))
                                onApply()
                            },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner),
                            border = BorderStroke(1.dp, design.brushes.primaryBorder),
                            surfaceColor = design.colors.transparent,
                            textColor = design.colors.textPrimary,
                            textStyle = MaterialTheme.typography.titleMedium,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp),
                            fillContentWidth = true
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .zIndex(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (onPrev != null) {
                    Surface(
                        onClick = onPrev,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner),
                        color = design.colors.glassSurface,
                        border = BorderStroke(1.dp, design.brushes.primaryBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "‹",
                                color = design.colors.textPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.size(44.dp))
                }
                if (onNext != null) {
                    Surface(
                        onClick = onNext,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner),
                        color = design.colors.glassSurface,
                        border = BorderStroke(1.dp, design.brushes.primaryBorder),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "›",
                                color = design.colors.textPrimary,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.size(44.dp))
                }
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: JGSThemeSpec,
    isActive: Boolean,
    onOpen: () -> Unit,
    onApply: () -> Unit
) {
    ThemeGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
    ) {
        val design = JGSTheme.design

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ThemePreviewImage(
                backgroundRes = theme.backgrounds.libraryBackgroundRes,
                modifier = Modifier
                    .size(64.dp)
            )
            Spacer(Modifier.width(22.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = theme.displayName,
                    color = design.colors.textPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = theme.description,
                    color = design.colors.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (isActive) {
                    Text(
                        text = "Active",
                        color = design.colors.textPrimary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            GlassTextButton(
                text = if (isActive) "Applied" else "Use",
                onClick = onApply,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.smallButtonCorner),
                border = BorderStroke(1.dp, design.brushes.primaryBorder),
                surfaceColor = design.colors.transparent,
                textColor = design.colors.textOnAccent,
                textStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ThemeGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val design = JGSTheme.design

    Surface(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner),
        color = design.colors.glassSurface,
        border = BorderStroke(1.dp, design.brushes.primaryBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        content()
    }
}

@Composable
private fun ThemePreviewImage(
    backgroundRes: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageResource(backgroundRes)
            }
        },
        update = { imageView ->
            imageView.setImageResource(backgroundRes)
        },
        modifier = modifier
    )
}

@Composable
private fun ThemePreviewBackground(
    theme: JGSThemeSpec,
    cropBiasX: Float = theme.backgrounds.cropBiasX,
    cropBiasY: Float = theme.backgrounds.cropBiasY,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.MATRIX
                    setImageResource(theme.backgrounds.libraryBackgroundRes)
                }
            },
            update = { imageView ->
                imageView.setImageResource(theme.backgrounds.libraryBackgroundRes)
                if (!applyPreviewCropBias(imageView, cropBiasX, cropBiasY)) {
                    imageView.post {
                        applyPreviewCropBias(imageView, cropBiasX, cropBiasY)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = theme.designTokens.brushes.appBackground,
                    alpha = theme.backgrounds.overlayAlpha
                )
        )
    }
}

private fun applyPreviewCropBias(imageView: ImageView, biasX: Float, biasY: Float): Boolean {
    return com.example.jgsmusicplayer.ui.theme.applyBiasedImageCrop(imageView, biasX, biasY)
}
