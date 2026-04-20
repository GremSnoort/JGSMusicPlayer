package com.example.jgsmusicplayer.ui.theme

import android.graphics.Matrix
import android.widget.ImageView

internal data class BiasedImageCropTransform(
    val scale: Float,
    val dx: Float,
    val dy: Float
)

internal fun calculateBiasedImageCropTransform(
    imageView: ImageView,
    biasX: Float,
    biasY: Float
): BiasedImageCropTransform? {
    val drawable = imageView.drawable ?: return null
    val viewW = imageView.width.toFloat()
    val viewH = imageView.height.toFloat()
    if (viewW <= 0f || viewH <= 0f) return null

    val dw = drawable.intrinsicWidth.toFloat()
    val dh = drawable.intrinsicHeight.toFloat()
    if (dw <= 0f || dh <= 0f) return null

    val viewRatio = viewW / viewH
    val imgRatio = dw / dh
    val bias = biasX.coerceIn(0f, 1f)
    val biasYClamped = biasY.coerceIn(0f, 1f)

    val scale: Float
    val dx: Float
    val dy: Float

    if (imgRatio > viewRatio) {
        scale = viewH / dh
        val scaledW = dw * scale
        val scaledH = dh * scale
        dx = (viewW - scaledW) * bias
        dy = (viewH - scaledH) * biasYClamped
    } else {
        scale = viewW / dw
        val scaledW = dw * scale
        val scaledH = dh * scale
        dx = (viewW - scaledW) * bias
        dy = (viewH - scaledH) * biasYClamped
    }

    return BiasedImageCropTransform(
        scale = scale,
        dx = dx,
        dy = dy
    )
}

internal fun applyBiasedImageCrop(
    imageView: ImageView,
    biasX: Float,
    biasY: Float
): Boolean {
    val transform = calculateBiasedImageCropTransform(imageView, biasX, biasY) ?: return false

    imageView.imageMatrix = Matrix().apply {
        setScale(transform.scale, transform.scale)
        postTranslate(transform.dx, transform.dy)
    }
    return true
}
