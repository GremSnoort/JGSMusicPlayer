package com.example.jgsmusicplayer.ui.components

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun ArcSeekBar(
    modifier: Modifier = Modifier,
    progress: Float,                 // 0..1
    onProgressChange: (Float) -> Unit,
    onProgressChangeFinished: (() -> Unit)? = null,
    onCenterClick: (() -> Unit)? = null,

    // вид
    strokeWidth: Dp = 10.dp,
    gapDegrees: Float = 70f,         // размер "пропуска" (незамкнутость)
    startAngleDegrees: Float = 125f, // где начинается дуга (примерно как в плеерах)
    trackBrush: Brush = Brush.linearGradient(
        listOf(Color(0x33FFFFFF), Color(0x1AFFFFFF))
    ),
    progressBrush: Brush = Brush.sweepGradient(
        listOf(
            Color(0xFF2EE6FF),
            Color(0xFF32FFA7),
            Color(0xFF9E6BFF),
            Color(0xFF2EE6FF)
        )
    ),
    knobColor: Color = Color(0xFFEAFBFF),
    knobRadius: Dp = 6.dp,

    // центр (обложка / контент)
    centerContent: @Composable BoxScope.() -> Unit = {}
) {
    var isDragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val flash = remember { Animatable(0f) } // 0..1
    val wave = remember { Animatable(0f) }  // 0..1 (фаза волны)

    val density = LocalDensity.current
    val centerRadiusPx = with(density) { 64.dp.toPx() }

    val swPx = with(LocalDensity.current) { strokeWidth.toPx() }
    val knobRpx = with(LocalDensity.current) { knobRadius.toPx() }

    // Длина дуги = 360 - gap
    val sweepTotal = 360f - gapDegrees

    fun clamp01(v: Float) = v.coerceIn(0f, 1f)

    // Перевод точки касания -> progress по дуге
    fun progressFromTouch(
        touch: Offset,
        center: Offset
    ): Float {
        val dx = touch.x - center.x
        val dy = touch.y - center.y

        // atan2 в радианах -> градусы; 0° справа, 90° вниз (из-за Y вниз)
        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        if (angle < 0f) angle += 360f

        // Приводим к системе координат дуги: startAngle -> 0
        var rel = angle - startAngleDegrees
        while (rel < 0f) rel += 360f
        while (rel >= 360f) rel -= 360f

        // Если попали в "gap" — не обновляем (или можно притягивать к краям)
        if (rel > sweepTotal) {
            // к ближайшему краю дуги
            val distToStart = rel
            val distToEnd = 360f - rel
            return if (distToStart < distToEnd) 0f else 1f
        }

        return clamp01(rel / sweepTotal)
    }

    Box(
        modifier = modifier.aspectRatio(1f)
    ) {
        Canvas(Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {

                    val down = awaitFirstDown(requireUnconsumed = false)

                    val center = Offset(size.width / 2f, size.height / 2f)
                    val distanceFromCenter = (down.position - center).getDistance()

                    val radius = min(size.width, size.height) / 2f - swPx / 2f
                    val ringTouchTolerance = with(density) { 24.dp.toPx() }

                    val isNearRing =
                        distanceFromCenter in (radius - ringTouchTolerance)..(radius + ringTouchTolerance)

                    // Старт должен быть на дуге (а не в центре и не где-то рядом)
                    if (!isNearRing) return@awaitEachGesture
                    if (distanceFromCenter <= centerRadiusPx) return@awaitEachGesture

                    // --- TAP: сразу прыгаем на позицию ---
                    // Но если пользователь реально потащит — перейдём в drag ниже.
                    val tapProgress = progressFromTouch(down.position, center)
                    onProgressChange(tapProgress)

                    // это будет drag или просто tap?
                    val changeAfterSlop = awaitTouchSlopOrCancellation(down.id) { change, _ ->
                        // как только превышен slop — считаем что это drag
                        change.consume()
                    }

                    // Если slop НЕ превышен — это TAP (палец отпустили)
                    if (changeAfterSlop == null) {
                        onProgressChangeFinished?.invoke()
                        return@awaitEachGesture
                    }

                    // --- DRAG: плавно двигаем пока палец зажат ---
                    isDragging = true

                    var pointerId = changeAfterSlop.id

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                        if (!change.pressed) break

                        val p = progressFromTouch(change.position, center)
                        onProgressChange(p)

                        change.consume()
                    }

                    isDragging = false
                    onProgressChangeFinished?.invoke()
                }
            }
        ) {
            val w = size.width
            val h = size.height
            val minSide = min(w, h)
            val radius = minSide / 2f - swPx / 2f
            val topLeft = Offset((w - 2 * radius) / 2f, (h - 2 * radius) / 2f)
            val arcSize = Size(2 * radius, 2 * radius)

            // трек (фон дуги)
            drawArc(
                brush = trackBrush,
                startAngle = startAngleDegrees,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = swPx, cap = StrokeCap.Round)
            )

            // прогресс (дуга)
            val sweepProgress = sweepTotal * clamp01(progress)
            drawArc(
                brush = progressBrush,
                startAngle = startAngleDegrees,
                sweepAngle = sweepProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = swPx, cap = StrokeCap.Round)
            )

            // knob (ползунок-точка)
            val angle = Math.toRadians((startAngleDegrees + sweepProgress).toDouble())
            val cx = (w / 2f) + cos(angle).toFloat() * radius
            val cy = (h / 2f) + sin(angle).toFloat() * radius

            drawCircle(
                color = knobColor.copy(alpha = if (isDragging) 1f else 0.9f),
                radius = knobRpx,
                center = Offset(cx, cy)
            )
        }

        // Центр (обложка) — кликабельный
        val interaction = remember { MutableInteractionSource() }
        // диаметр центра (чем меньше — тем меньше зона клика)
        val centerDiameter = 300.dp   // было примерно 2*(радиус 64dp)=128dp
        val centerPadding = 28.dp     // визуальный отступ

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(centerPadding),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(centerDiameter)
                    .clip(CircleShape)
                    .then(
                        if (onCenterClick != null)
                            Modifier.clickable(
                                interactionSource = interaction,
                                indication = null
                            ) {
                                onCenterClick()

                                scope.launch {
                                    flash.stop()
                                    wave.stop()
                                    flash.snapTo(1f)
                                    wave.snapTo(0f)

                                    launch {
                                        flash.animateTo(0f, tween(260))
                                    }
                                    launch {
                                        wave.animateTo(1f, tween(420))
                                    }
                                }
                            }
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                // обложка/контент
                centerContent()

                // Оверлей-вспышка (морская волна + кольцо)
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = flash.value } // альфа управляет всем оверлеем
                        .clip(CircleShape)
                ) {
                    val w = size.width
                    val h = size.height

                    // фаза волны: 0..1 -> 0..2π
                    val t = wave.value * (2f * Math.PI.toFloat())

                    val cx = w * 0.5f + kotlin.math.cos(t) * w * 0.10f
                    val cy = h * 0.5f + kotlin.math.sin(t * 1.3f) * h * 0.08f
                    val center = Offset(cx, cy)

                    val seaColors = listOf(
                        Color(0xFF2EE6FF), // cyan
                        Color(0xFF32FFA7), // green
                        Color(0xFF9E6BFF), // purple
                        Color(0xFF2EE6FF)  // back
                    )

                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x332EE6FF),
                                Color(0x2232FFA7),
                                Color(0x1A9E6BFF),
                                Color.Transparent
                            ),
                            center = center,
                            radius = (minOf(w, h) * 0.75f)
                        )
                    )

                    drawRect(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0x142EE6FF),
                                Color(0x1432FFA7),
                                Color(0x149E6BFF),
                                Color(0x142EE6FF)
                            ),
                            center = Offset(w * 0.5f, h * 0.5f)
                        )
                    )

                    val ringAlpha = (flash.value * 0.9f).coerceIn(0f, 1f)
                    val ringWidth = 1.5.dp.toPx() + (flash.value * 2.0.dp.toPx())

                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = seaColors.map { it.copy(alpha = 0.55f * ringAlpha) },
                            center = Offset(w * 0.5f, h * 0.5f)
                        ),
                        radius = (minOf(w, h) * 0.5f) - ringWidth * 0.7f,
                        center = Offset(w * 0.5f, h * 0.5f),
                        style = Stroke(width = ringWidth, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}
