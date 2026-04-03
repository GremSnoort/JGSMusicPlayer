package com.example.jgsmusicplayer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity

import com.example.jgsmusicplayer.model.PlayerActions
import com.example.jgsmusicplayer.model.PlayerUiState
import com.example.jgsmusicplayer.ui.components.NeonTopBar
import com.example.jgsmusicplayer.ui.components.ArcSeekBar
import com.example.jgsmusicplayer.ui.theme.JGSBackgroundTarget
import com.example.jgsmusicplayer.ui.theme.JGSTheme
import com.example.jgsmusicplayer.ui.theme.JGSThemedBackground

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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    onBack: () -> Unit,
    uiState: PlayerUiState,
    onDismissError: () -> Unit,
    actions: PlayerActions
) {
    val design = JGSTheme.design
    BackHandler(onBack = onBack)

    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPreviewMs by remember { mutableStateOf(0L) }

    LaunchedEffect(uiState.positionMs) {
        if (!isUserSeeking) {
            seekPreviewMs = uiState.positionMs
        }
    }

    val safeDuration = if (uiState.durationMs > 0) uiState.durationMs else 1L
    val sliderValue = (seekPreviewMs.coerceIn(0L, safeDuration)).toFloat() / safeDuration.toFloat()
    val screenInset = design.sizes.playerScreenInset

    // --- swipe from RIGHT edge to go back ---
    val density = LocalDensity.current
    val cfg = LocalConfiguration.current
    val screenWidthPx = with(density) { cfg.screenWidthDp.dp.toPx() }

    val edgePx = with(density) { design.sizes.edgeSwipeZone.toPx() }
    val triggerPx = with(density) { design.sizes.edgeSwipeTrigger.toPx() }

    var dragSum by remember { mutableStateOf(0f) }
    var backTriggered by remember { mutableStateOf(false) }
    var isEdgeDrag by remember { mutableStateOf(false) }
    // ---------------------------------------

    Box(modifier = Modifier.fillMaxSize()) {
        JGSThemedBackground(
            target = JGSBackgroundTarget.PLAYER,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = screenInset, bottom = screenInset)
                .pointerInput(uiState.nowPlaying) {}
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        if (!isEdgeDrag || backTriggered) return@rememberDraggableState
                        dragSum += delta
                        if (dragSum <= -triggerPx) {
                            backTriggered = true
                            onBack()
                        }
                    },
                    onDragStarted = { startOffset ->
                        dragSum = 0f
                        backTriggered = false

                        val fromRight = startOffset.x >= (screenWidthPx - edgePx)
                        isEdgeDrag = fromRight
                    },
                    onDragStopped = {
                        isEdgeDrag = false
                        dragSum = 0f
                        backTriggered = false
                    }
                )
        ) {
            Scaffold(
                containerColor = design.colors.transparent,
                topBar = {
                    NeonTopBar(
                        title = uiState.nowPlaying?.name.orEmpty(),
                        showBack = true,
                        onBack = onBack
                    )
                }
            ) { padding ->
                Column(
                    Modifier
                        .padding(padding)
                        .padding(design.sizes.screenContentPadding)
                        .navigationBarsPadding()
                        .fillMaxSize()
                ) {

                    if (uiState.nowPlaying == null) {
                        LaunchedEffect(uiState.nowPlaying) { onBack() }
                        return@Column
                    }

                    uiState.errorMessage?.let { errorMessage ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(design.shapes.cardCorner),
                            color = design.colors.errorSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                design.brushes.errorBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = design.sizes.sectionSpacingMedium,
                                        vertical = design.sizes.sectionSpacingSmall
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = errorMessage,
                                    color = design.colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "Dismiss",
                                    color = design.colors.textPrimary.copy(alpha = 0.92f),
                                    modifier = Modifier
                                        .padding(start = design.sizes.sectionSpacingSmall)
                                        .clickable { onDismissError() }
                                )
                            }
                        }

                        Spacer(Modifier.height(design.sizes.sectionSpacingSmall))
                    }

                    val timeBrush = design.brushes.timeText

                    Row(Modifier.fillMaxWidth()) {

                        Text(
                            text = formatMs(seekPreviewMs),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                brush = timeBrush,
                                fontWeight = FontWeight.SemiBold,
                                shadow = Shadow(
                                    color = design.colors.errorGlow,
                                    offset = Offset(0f, 0f),
                                    blurRadius = 12f
                                )
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Text(
                            text = formatRemainingMs(seekPreviewMs, uiState.durationMs),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                brush = timeBrush,
                                fontWeight = FontWeight.SemiBold,
                                shadow = Shadow(
                                    color = design.colors.errorGlow,
                                    offset = Offset(0f, 0f),
                                    blurRadius = 12f
                                )
                            )
                        )
                    }

                    Spacer(Modifier.height(design.sizes.sectionSpacingSmall))

                    ArcSeekBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = design.sizes.sectionSpacingSmall),
                        progress = sliderValue,
                        onProgressChange = { p ->
                            isUserSeeking = true
                            seekPreviewMs = (p * safeDuration).toLong()
                        },
                        onProgressChangeFinished = {
                            actions.seekTo(seekPreviewMs)
                            isUserSeeking = false
                        },
                        onCenterClick = actions.playPause,
                        gapDegrees = 70f,
                        startAngleDegrees = 125f,
                        strokeWidth = design.sizes.seekBarStroke,
                        centerContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(design.colors.centerOverlay)
                                .border(
                                    width = 1.dp,
                                    color = design.colors.whiteOverlay,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Image(…)
                            Text(
                                text = "♪",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = design.text.playerCenterGlyph.fontSize
                                ),
                                color = design.colors.textPrimary.copy(alpha = 0.55f)
                            )
                        }
                        }
                    )

                    Spacer(Modifier.height(design.sizes.sectionSpacingLarge))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GhostIconButton(
                                label = "⟲",
                                onClick = { actions.seekBy(-10_000) }
                            )

                            Spacer(Modifier.width(design.sizes.sectionSpacingLarge))

                            GlassIconButton(
                                label = if (uiState.isPlaying) "Ⅱ" else "▶",
                                onClick = actions.playPause,
                                size = design.sizes.playerPrimaryButton,
                                modifier = Modifier,
                                isActive = uiState.isPlaying
                            )

                            Spacer(Modifier.width(design.sizes.sectionSpacingLarge))

                            GhostIconButton(
                                label = "⟳",
                                onClick = { actions.seekBy(10_000) }
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassIconButton(
                                label = "■",
                                onClick = actions.stop,
                                size = design.sizes.playerSecondaryButton,
                                isActive = false
                            )

                            GlassIconButton(
                                label = "∞",
                                onClick = actions.toggleLooping,
                                size = design.sizes.playerSecondaryButton,
                                isActive = uiState.isLooping
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 46.dp
) {
    val design = JGSTheme.design
    val shape = RoundedCornerShape(design.shapes.smallButtonCorner)

    Surface(
        modifier = modifier.height(height),
        shape = shape,
        color = design.colors.glassSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            design.brushes.controlInactiveBorder
        ),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = design.colors.textPrimary.copy(alpha = 0.88f),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = design.text.buttonLabel.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
private fun GlassIconButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    isActive: Boolean = false
) {
    val design = JGSTheme.design
    val shape = RoundedCornerShape(design.shapes.largeButtonCorner)

    // ----- PULSE -----
    val transition = rememberInfiniteTransition(label = "glowPulse")
    val glowAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val glowScale by transition.animateFloat(
        initialValue = 1.00f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    // -----------------

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {

        if (isActive) {
            Box(
                modifier = Modifier
                    .size(size + 16.dp)
                    .graphicsLayer {
                        alpha = glowAlpha
                        scaleX = glowScale
                        scaleY = glowScale
                    }
                    .clip(shape)
                    .background(
                        design.brushes.controlActiveGlow
                    )
            )
        }

        Surface(
            modifier = Modifier.size(size),
            shape = shape,
            color = design.colors.glassSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isActive) design.brushes.controlActiveBorder else design.brushes.controlInactiveBorder
            ),
            onClick = onClick
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isActive) design.brushes.controlActiveFill else design.brushes.transparentFill),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = design.colors.textPrimary.copy(alpha = if (isActive) 0.98f else 0.92f),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = if (size >= design.sizes.playerPrimaryButton) {
                            design.text.iconButtonLarge.fontSize
                        } else {
                            design.text.iconButtonSmall.fontSize
                        },
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun GhostIconButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val design = JGSTheme.design
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = design.colors.transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = design.colors.textPrimary.copy(alpha = 0.90f),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = design.text.iconButtonMedium.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
