package com.example.jgsmusicplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.unit.Dp

import com.example.jgsmusicplayer.ui.theme.JGSTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NeonTopBar(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp? = null,
    sidePadding: Dp? = null,
    backSize: Dp? = null,
    backOffsetY: Dp? = null,
    endPadding: Dp? = null,
    titleHorizontalPadding: Dp? = null
) {
    val design = JGSTheme.design
    val resolvedHeight = height ?: design.sizes.topBarHeight
    val resolvedSidePadding = sidePadding ?: design.sizes.topBarSidePadding
    val resolvedBackSize = backSize ?: design.sizes.topBarBackSize
    val resolvedBackOffsetY = backOffsetY ?: design.sizes.topBarBackOffsetY
    val resolvedEndPadding = endPadding ?: design.sizes.topBarEndPadding
    val resolvedTitleHorizontalPadding =
        titleHorizontalPadding ?: design.sizes.topBarTitleHorizontalPadding

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = design.sizes.sectionSpacingSmall)
            .height(resolvedHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            Box(
                modifier = Modifier
                    .padding(start = resolvedSidePadding)
                    .size(resolvedBackSize)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "←",
                    modifier = Modifier.offset(y = resolvedBackOffsetY),
                    color = design.colors.topBarBackIcon,
                    fontSize = design.text.topBarBack.fontSize,
                    fontWeight = FontWeight.Black,
                    style = TextStyle(
                        shadow = Shadow(
                            color = design.colors.topBarBackShadow,
                            offset = Offset(0f, 2f),
                            blurRadius = 8f
                        )
                    )
                )
            }
        } else {
            Spacer(Modifier.width(resolvedBackSize + resolvedSidePadding))
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = resolvedEndPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = resolvedTitleHorizontalPadding)
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
                    brush = design.brushes.topBarTitle,
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = design.colors.topBarTitleGlow,
                        offset = Offset(0f, 0f),
                        blurRadius = 12f
                    )
                ),
                fontSize = design.text.topBarTitle.fontSize
            )
        }
    }
}
