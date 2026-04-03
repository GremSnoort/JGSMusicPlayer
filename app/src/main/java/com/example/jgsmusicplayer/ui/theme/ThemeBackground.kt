package com.example.jgsmusicplayer.ui.theme

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
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageResource(backgroundRes)
                }
            },
            update = { imageView ->
                imageView.setImageResource(backgroundRes)
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
