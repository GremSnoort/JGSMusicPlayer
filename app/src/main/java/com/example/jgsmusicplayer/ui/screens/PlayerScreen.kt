package com.example.jgsmusicplayer.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player

import com.example.jgsmusicplayer.model.PlayerActions
import com.example.jgsmusicplayer.model.PlayerUiState
import com.example.jgsmusicplayer.ui.theme.PlayerBg
import com.example.jgsmusicplayer.ui.components.ArcSeekBar

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
    player: ExoPlayer,
    uiState: PlayerUiState,
    actions: PlayerActions
) {
    var isUserSeeking by remember { mutableStateOf(false) }
    var seekPreviewMs by remember { mutableStateOf(0L) }

    var isLooping by remember { mutableStateOf(player.repeatMode == Player.REPEAT_MODE_ONE) }

    LaunchedEffect(uiState.positionMs) {
        if (!isUserSeeking) {
            seekPreviewMs = uiState.positionMs
        }
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onRepeatModeChanged(repeatMode: Int) {
                isLooping = repeatMode == Player.REPEAT_MODE_ONE
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    val safeDuration = if (uiState.durationMs > 0) uiState.durationMs else 1L
    val sliderValue = (seekPreviewMs.coerceIn(0L, safeDuration)).toFloat() / safeDuration.toFloat()
    val screenInset = 24.dp // 12–28.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PlayerBg)
            .padding(top = screenInset, bottom = screenInset),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassBackButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 0.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        uiState.nowPlaying?.let {
                            TrackTitleWave(
                                title = it.name,
                                modifier = Modifier.padding(horizontal = 12.dp) // чтобы не налезал на стрелку
                            )
                        }
                    }
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).padding(16.dp).fillMaxSize()) {

                if (uiState.nowPlaying == null) {
                    LaunchedEffect(uiState.nowPlaying) { onBack() }
                    return@Column
                }

                val timeBrush = Brush.linearGradient(
                    listOf(
                        Color(0xFF2EE6FF), // cyan
                        Color(0xFF32FFA7), // green
                        Color(0xFF9E6BFF)  // purple
                    )
                )

                Row(Modifier.fillMaxWidth()) {

                    Text(
                        text = formatMs(seekPreviewMs),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            brush = timeBrush,
                            fontWeight = FontWeight.SemiBold,
                            shadow = Shadow(
                                color = Color(0x552EE6FF),
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
                                color = Color(0x552EE6FF),
                                offset = Offset(0f, 0f),
                                blurRadius = 12f
                            )
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                ArcSeekBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    progress = sliderValue,
                    onProgressChange = { p ->
                        isUserSeeking = true
                        seekPreviewMs = (p * safeDuration).toLong()
                    },
                    onProgressChangeFinished = {
                        player.seekTo(seekPreviewMs)
                        isUserSeeking = false
                    },
                    onCenterClick = actions.playPause,
                    gapDegrees = 70f,
                    startAngleDegrees = 125f,
                    strokeWidth = 10.dp,
                    centerContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0x14000000)) // лёгкая дымка
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.10f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Image(…)
                            Text(
                                text = "♪",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White.copy(alpha = 0.55f)
                            )
                        }
                    }
                )

                Spacer(Modifier.height(18.dp))

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
                            onClick = {
                                val target = (player.currentPosition - 10_000).coerceAtLeast(0L)
                                player.seekTo(target)
                            }
                        )

                        Spacer(Modifier.width(18.dp))

                        // Play/Pause
                        GlassIconButton(
                            label = if (uiState.isPlaying) "Ⅱ" else "▶",
                            onClick = actions.playPause,
                            size = 84.dp,
                            modifier = Modifier,
                            isActive = uiState.isPlaying
                        )

                        Spacer(Modifier.width(18.dp))

                        GhostIconButton(
                            label = "⟳",
                            onClick = {
                                val dur = if (player.duration > 0) player.duration else uiState.durationMs
                                val target = (player.currentPosition + 10_000)
                                    .coerceAtMost(if (dur > 0) dur else Long.MAX_VALUE)
                                player.seekTo(target)
                            }
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassIconButton(
                            label = "■",
                            onClick = actions.stop,
                            size = 72.dp,
                            isActive = false
                        )

                        GlassIconButton(
                            label = "∞",
                            onClick = {
                                val newMode = if (player.repeatMode == Player.REPEAT_MODE_ONE)
                                    Player.REPEAT_MODE_OFF
                                else
                                    Player.REPEAT_MODE_ONE

                                player.repeatMode = newMode
                                isLooping = (newMode == Player.REPEAT_MODE_ONE)
                            },
                            size = 72.dp,
                            isActive = isLooping
                        )
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
    val shape = RoundedCornerShape(14.dp)

    Surface(
        modifier = modifier.height(height),
        shape = shape,
        color = Color(0x14FFFFFF),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    Color(0x332EE6FF),
                    Color(0x3332FFA7),
                    Color(0x339E6BFF)
                )
            )
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
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
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
    val shape = RoundedCornerShape(18.dp)

    val activeFill = Brush.linearGradient(
        listOf(
            Color(0x332EE6FF),
            Color(0x2232FFA7),
            Color(0x229E6BFF)
        )
    )

    val borderBrush = Brush.linearGradient(
        listOf(
            Color(0x662EE6FF),
            Color(0x6632FFA7),
            Color(0x669E6BFF)
        )
    )

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
                        Brush.linearGradient(
                            listOf(
                                Color(0x1A2EE6FF),
                                Color(0x1232FFA7),
                                Color(0x129E6BFF)
                            )
                        )
                    )
            )
        }

        Surface(
            modifier = Modifier.size(size),
            shape = shape,
            color = Color(0x12FFFFFF),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isActive) borderBrush else Brush.linearGradient(
                    listOf(
                        Color(0x332EE6FF),
                        Color(0x3332FFA7),
                        Color(0x339E6BFF)
                    )
                )
            ),
            onClick = onClick
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isActive) activeFill else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = Color.White.copy(alpha = if (isActive) 0.98f else 0.92f),
                    fontSize = if (size >= 80.dp) 26.sp else 18.sp,
                    fontWeight = FontWeight.Bold
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
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        onClick = onClick
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun GlassBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .padding(start = 10.dp)
            .size(size) // удобная зона тапа
            .clickable(
                interactionSource = interaction,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "←", //"❮"
            modifier = Modifier.offset(y = (-5).dp),
            color = Color.White.copy(alpha = 0.96f),
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.35f),
                    offset = Offset(0f, 2f),
                    blurRadius = 8f
                )
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrackTitleWave(
    title: String,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "titleWaveSingle")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleWaveSingleT"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF80EBFF),
            Color(0xFF9CFFD4),
            Color(0xFFA7D8FF),
            Color(0xFF97EFFF)
        ),
        start = Offset(0f + 260f * t, 0f),
        end = Offset(900f - 260f * t, 0f)
    )

    Text(
        text = title,
        modifier = modifier
            .basicMarquee(
                iterations = Int.MAX_VALUE,
                repeatDelayMillis = 900,
                initialDelayMillis = 900,
                velocity = 35.dp
            ),
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip, // <-- важно: не Ellipsis
        style = MaterialTheme.typography.titleLarge.copy(
            brush = brush,
            fontWeight = FontWeight.Bold,
            shadow = Shadow(
                color = Color(0x662EE6FF),
                offset = Offset(0f, 0f),
                blurRadius = 12f
            )
        ),
        fontSize = 20.sp
    )
}
