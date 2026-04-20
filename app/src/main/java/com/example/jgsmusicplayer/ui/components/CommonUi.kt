package com.example.jgsmusicplayer.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

import com.example.jgsmusicplayer.ui.theme.JGSTheme

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    textColor: Color,
    labelColor: Color,
    cursorColor: Color,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = label,
                color = labelColor,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Text(
                text = "⌕",
                color = labelColor,
                style = MaterialTheme.typography.titleMedium
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                Text(
                    text = "✕",
                    color = labelColor,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .clickable { onValueChange("") }
                        .padding(horizontal = 6.dp)
                )
            }
        },
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            imeAction = ImeAction.Done
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onDone = { onDone() }
        ),
        modifier = modifier.height(56.dp),
        minLines = 1,
        maxLines = 1,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = cursorColor,
            focusedPlaceholderColor = labelColor,
            unfocusedPlaceholderColor = labelColor
        )
    )
}

@Composable
fun SmallGlassIconButton(
    label: String,
    onClick: () -> Unit,
    border: Brush,
    size: Dp = 44.dp
) {
    val design = JGSTheme.design
    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.smallButtonCorner),
        color = design.colors.glassSurface,
        border = BorderStroke(1.dp, border),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.size(size)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = design.colors.textPrimary.copy(alpha = 0.95f),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun BottomGlassActionButton(
    text: String,
    onClick: () -> Unit
) {
    val design = JGSTheme.design

    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.fabCorner),
        color = design.colors.transparent,
        border = BorderStroke(1.dp, design.brushes.primaryBorder),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .background(design.brushes.fabBackground)
                .padding(
                    horizontal = design.sizes.floatingNowHorizontalPadding,
                    vertical = design.sizes.floatingNowVerticalPadding
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = design.colors.textOnAccent,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun SeaDivider(
    modifier: Modifier = Modifier,
    height: Dp = 1.dp,
    brush: Brush
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(brush)
    )
}

@Composable
fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = JGSTheme.design.colors.textPrimary,
    dismissTextColor: Color = JGSTheme.design.colors.textPrimary,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    dismissStartPadding: Dp = 0.dp,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    dismissTextStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelMedium
) {
    val design = JGSTheme.design

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(design.shapes.cardCorner),
        color = design.colors.errorSurface,
        border = BorderStroke(
            1.dp,
            design.brushes.errorBorder
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = textColor,
                modifier = Modifier.weight(1f),
                style = textStyle
            )
            Text(
                text = "Dismiss",
                color = dismissTextColor,
                modifier = Modifier
                    .padding(start = dismissStartPadding)
                    .clickable { onDismiss() },
                style = dismissTextStyle
            )
        }
    }
}

@Composable
fun edgeSwipeBackModifier(
    onBack: () -> Unit
): Modifier {
    val design = JGSTheme.design
    val density = LocalDensity.current
    val cfg = LocalConfiguration.current
    val screenWidthPx = with(density) { cfg.screenWidthDp.dp.toPx() }
    val edgePx = with(density) { design.sizes.edgeSwipeZone.toPx() }
    val triggerPx = with(density) { design.sizes.edgeSwipeTrigger.toPx() }

    var dragSum by remember { mutableStateOf(0f) }
    var backTriggered by remember { mutableStateOf(false) }
    var isEdgeDrag by remember { mutableStateOf(false) }

    return Modifier
        .pointerInput(Unit) {}
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
                isEdgeDrag = startOffset.x >= (screenWidthPx - edgePx)
            },
            onDragStopped = {
                isEdgeDrag = false
                dragSum = 0f
                backTriggered = false
            }
        )
}
