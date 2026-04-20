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

import com.example.jgsmusicplayer.model.PlayerActions
import com.example.jgsmusicplayer.model.PlayerUiState
import com.example.jgsmusicplayer.ui.components.NeonTopBar
import com.example.jgsmusicplayer.ui.components.ArcSeekBar
import com.example.jgsmusicplayer.ui.components.ErrorBanner
import com.example.jgsmusicplayer.ui.components.GlassTextButton
import com.example.jgsmusicplayer.ui.components.edgeSwipeBackModifier
import com.example.jgsmusicplayer.ui.theme.JGSBackgroundTarget
import com.example.jgsmusicplayer.ui.theme.JGSTheme
import com.example.jgsmusicplayer.ui.theme.JGSThemedBackground

private enum class PlaybackTimeFormat {
    ELAPSED,
    REMAINING
}

private fun formatPlaybackTime(
    ms: Long,
    format: PlaybackTimeFormat = PlaybackTimeFormat.ELAPSED
): String {
    val safeMs = ms.coerceAtLeast(0L)
    if (safeMs <= 0L) {
        return if (format == PlaybackTimeFormat.REMAINING) "-0:00" else "0:00"
    }

    val totalSec = safeMs / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    val prefix = if (format == PlaybackTimeFormat.REMAINING) "-" else ""
    return "${prefix}${min}:${sec.toString().padStart(2, '0')}"
}

private fun formatMs(ms: Long): String {
    return formatPlaybackTime(ms)
}

private fun formatRemainingMs(positionMs: Long, durationMs: Long): String {
    val remain = (durationMs - positionMs).coerceAtLeast(0L)
    return formatPlaybackTime(remain, PlaybackTimeFormat.REMAINING)
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

    Box(modifier = Modifier.fillMaxSize()) {
        JGSThemedBackground(
            target = JGSBackgroundTarget.PLAYER,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = screenInset, bottom = screenInset)
                .then(edgeSwipeBackModifier(onBack))
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
                        ErrorBanner(
                            message = errorMessage,
                            onDismiss = onDismissError,
                            dismissTextColor = design.colors.textPrimary.copy(alpha = 0.92f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = design.sizes.sectionSpacingMedium,
                                vertical = design.sizes.sectionSpacingSmall
                            ),
                            dismissStartPadding = design.sizes.sectionSpacingSmall,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            dismissTextStyle = MaterialTheme.typography.bodyMedium
                        )

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

    GlassTextButton(
        text = text,
        onClick = onClick,
        modifier = modifier.height(height),
        shape = shape,
        surfaceColor = design.colors.glassSurface,
        textColor = design.colors.textPrimary.copy(alpha = 0.88f),
        textStyle = MaterialTheme.typography.titleMedium.copy(
            fontSize = design.text.buttonLabel.fontSize,
            fontWeight = FontWeight.SemiBold
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            design.brushes.controlInactiveBorder
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp)
    )
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
