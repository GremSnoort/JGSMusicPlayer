package com.example.jgsmusicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NeonTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
    sidePadding: Dp = 10.dp,
    backSize: Dp = 52.dp,
    backOffsetY: Dp = (-5).dp,
    endPadding: Dp = 10.dp,
    titleHorizontalPadding: Dp = 12.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            Box(
                modifier = Modifier
                    .padding(start = sidePadding)
                    .size(backSize)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    modifier = Modifier.offset(y = backOffsetY),
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
        } else {
            Spacer(Modifier.width(backSize + sidePadding))
        }

        val transition = rememberInfiniteTransition(label = "neonTopBarTitle")
        val t by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "neonTopBarTitleT"
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
        // ----------------------------------------

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = endPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = titleHorizontalPadding)
                    .clipToBounds()
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        repeatDelayMillis = 900,
                        initialDelayMillis = 900,
                        velocity = 35.dp
                    ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
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
    }
}
