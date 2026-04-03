package com.example.jgsmusicplayer.ui.theme

import android.graphics.Matrix
import android.widget.ImageView

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

enum class JGSBackgroundTarget {
    LIBRARY,
    PLAYER
}

@Composable
fun JGSThemedBackground(
    target: JGSBackgroundTarget,
    modifier: Modifier = Modifier
) {
    val backgrounds = JGSTheme.backgrounds
    val design = JGSTheme.design
    val backgroundRes = when (target) {
        JGSBackgroundTarget.LIBRARY -> backgrounds.libraryBackgroundRes
        JGSBackgroundTarget.PLAYER -> backgrounds.playerBackgroundRes
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                ImageView(context).apply {
                    scaleType = ImageView.ScaleType.MATRIX
                    setImageResource(backgroundRes)
                    alpha = 0f
                }
            },
            update = { imageView ->
                imageView.setImageResource(backgroundRes)
                if (applyCropBias(imageView, backgrounds.cropBiasX)) {
                    imageView.alpha = 1f
                } else {
                    imageView.post {
                        if (applyCropBias(imageView, backgrounds.cropBiasX)) {
                            imageView.alpha = 1f
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = design.brushes.appBackground,
                    alpha = backgrounds.overlayAlpha
                )
        )
    }
}

private fun applyCropBias(imageView: ImageView, biasX: Float): Boolean {
    val drawable = imageView.drawable ?: return false
    val viewW = imageView.width.toFloat()
    val viewH = imageView.height.toFloat()
    if (viewW <= 0f || viewH <= 0f) return false

    val dw = drawable.intrinsicWidth.toFloat()
    val dh = drawable.intrinsicHeight.toFloat()
    if (dw <= 0f || dh <= 0f) return false

    val viewRatio = viewW / viewH
    val imgRatio = dw / dh
    val bias = biasX.coerceIn(0f, 1f)

    val scale: Float
    val dx: Float
    val dy: Float

    if (imgRatio > viewRatio) {
        // Image is wider than the view. Match height, crop width, bias X if needed.
        scale = viewH / dh
        val scaledW = dw * scale
        val scaledH = dh * scale
        dx = (viewW - scaledW) * bias
        dy = (viewH - scaledH) * 0.5f
    } else {
        // Image is taller than the view. Match width, crop height, center vertically.
        scale = viewW / dw
        val scaledW = dw * scale
        val scaledH = dh * scale
        dx = (viewW - scaledW) * 0.5f
        dy = (viewH - scaledH) * 0.5f
    }

    imageView.imageMatrix = Matrix().apply {
        setScale(scale, scale)
        postTranslate(dx, dy)
    }
    return true
}
