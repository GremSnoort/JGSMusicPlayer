package com.example.jgsmusicplayer.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class JGSColorTokens(
    val backgroundBase: Color,
    val surfaceBase: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textOnAccent: Color,
    val listTextShadow: Color,
    val glassSurface: Color,
    val centerOverlay: Color,
    val whiteOverlay: Color,
    val transparent: Color,
    val seekKnob: Color,
    val errorSurface: Color,
    val errorGlow: Color,
    val topBarBackIcon: Color,
    val topBarBackShadow: Color,
    val topBarTitleGlow: Color,
    val seekWaveGlowColors: List<Color>,
    val seekWaveSweepColors: List<Color>,
    val seekWaveRingColors: List<Color>
)

@Immutable
data class JGSBrushTokens(
    val appBackground: Brush,
    val primaryBorder: Brush,
    val fabBackground: Brush,
    val errorBorder: Brush,
    val timeText: Brush,
    val topBarTitle: Brush,
    val seekTrack: Brush,
    val seekProgress: Brush,
    val controlActiveFill: Brush,
    val controlActiveGlow: Brush,
    val controlActiveBorder: Brush,
    val controlInactiveBorder: Brush,
    val stopBorder: Brush,
    val transparentFill: Brush
)

@Immutable
data class JGSShapeTokens(
    val smallButtonCorner: Dp,
    val cardCorner: Dp,
    val largeButtonCorner: Dp,
    val fabCorner: Dp
)

@Immutable
data class JGSSizeTokens(
    val topBarHeight: Dp,
    val topBarBackSize: Dp,
    val topBarSidePadding: Dp,
    val topBarBackOffsetY: Dp,
    val topBarEndPadding: Dp,
    val topBarTitleHorizontalPadding: Dp,
    val floatingNowHorizontalPadding: Dp,
    val floatingNowVerticalPadding: Dp,
    val playerScreenInset: Dp,
    val screenContentPadding: Dp,
    val sectionSpacingSmall: Dp,
    val sectionSpacingMedium: Dp,
    val sectionSpacingLarge: Dp,
    val listItemHorizontalPadding: Dp,
    val listItemVerticalPadding: Dp,
    val searchFieldPadding: Dp,
    val edgeSwipeZone: Dp,
    val edgeSwipeTrigger: Dp,
    val seekBarStroke: Dp,
    val seekBarKnob: Dp,
    val seekCenterDiameter: Dp,
    val seekCenterPadding: Dp,
    val seekCenterTouchRadius: Dp,
    val playerPrimaryButton: Dp,
    val playerSecondaryButton: Dp,
    val librarySmallButton: Dp
)

@Immutable
data class JGSTextTokens(
    val topBarBack: TextStyle,
    val topBarTitle: TextStyle,
    val time: TextStyle,
    val playerCenterGlyph: TextStyle,
    val buttonLabel: TextStyle,
    val iconButtonSmall: TextStyle,
    val iconButtonMedium: TextStyle,
    val iconButtonLarge: TextStyle
)

@Immutable
data class JGSDesignTokens(
    val colors: JGSColorTokens,
    val brushes: JGSBrushTokens,
    val shapes: JGSShapeTokens,
    val sizes: JGSSizeTokens,
    val text: JGSTextTokens
)

internal val DefaultJGSDesignTokens = JGSDesignTokens(
    colors = JGSColorTokens(
        backgroundBase = Color(0xFF050B12),
        surfaceBase = Color(0xFF071823),
        textPrimary = Color.White.copy(alpha = 0.92f),
        textSecondary = Color.White.copy(alpha = 0.65f),
        textOnAccent = Color.White,
        listTextShadow = Color.Transparent,
        glassSurface = Color(0x12FFFFFF),
        centerOverlay = Color(0x26000000),
        whiteOverlay = Color.White.copy(alpha = 0.10f),
        transparent = Color.Transparent,
        seekKnob = Color(0xFFEAFBFF),
        errorSurface = Color(0x22FF5B7A),
        errorGlow = Color(0x552EE6FF),
        topBarBackIcon = Color.White.copy(alpha = 0.96f),
        topBarBackShadow = Color.Black.copy(alpha = 0.35f),
        topBarTitleGlow = Color(0x662EE6FF),
        seekWaveGlowColors = listOf(
            Color(0x332EE6FF),
            Color(0x2232FFA7),
            Color(0x1A9E6BFF),
            Color.Transparent
        ),
        seekWaveSweepColors = listOf(
            Color(0x142EE6FF),
            Color(0x1432FFA7),
            Color(0x149E6BFF),
            Color(0x142EE6FF)
        ),
        seekWaveRingColors = listOf(
            Color(0x8C2EE6FF),
            Color(0x8C32FFA7),
            Color(0x8C9E6BFF),
            Color(0x8C2EE6FF)
        )
    ),
    brushes = JGSBrushTokens(
        appBackground = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF050B12),
                Color(0xFF071823),
                Color(0xFF120E24)
            )
        ),
        primaryBorder = Brush.linearGradient(
            listOf(
                Color(0x662EE6FF),
                Color(0x6632FFA7),
                Color(0x669E6BFF)
            )
        ),
        fabBackground = Brush.linearGradient(
            listOf(
                Color(0xCC121826),
                Color(0xCC0D111A)
            )
        ),
        errorBorder = Brush.linearGradient(
            listOf(
                Color(0x99FF5B7A),
                Color(0x99FFB36B)
            )
        ),
        timeText = Brush.linearGradient(
            listOf(
                Color(0xFF85EEFF),
                Color(0xFF9EFFD3),
                Color(0xFFDECBFF)
            )
        ),
        topBarTitle = Brush.linearGradient(
            colors = listOf(
                Color(0xFF80EBFF),
                Color(0xFF9CFFD4),
                Color(0xFFA7D8FF),
                Color(0xFF97EFFF)
            )
        ),
        seekTrack = Brush.linearGradient(
            listOf(
                Color(0x33FFFFFF),
                Color(0x1AFFFFFF)
            )
        ),
        seekProgress = Brush.sweepGradient(
            listOf(
                Color(0xFF2EE6FF),
                Color(0xFF32FFA7),
                Color(0xFF9E6BFF),
                Color(0xFF2EE6FF)
            )
        ),
        controlActiveFill = Brush.linearGradient(
            listOf(
                Color(0x332EE6FF),
                Color(0x2232FFA7),
                Color(0x229E6BFF)
            )
        ),
        controlActiveGlow = Brush.linearGradient(
            listOf(
                Color(0x1A2EE6FF),
                Color(0x1232FFA7),
                Color(0x129E6BFF)
            )
        ),
        controlActiveBorder = Brush.linearGradient(
            listOf(
                Color(0x662EE6FF),
                Color(0x6632FFA7),
                Color(0x669E6BFF)
            )
        ),
        controlInactiveBorder = Brush.linearGradient(
            listOf(
                Color(0x332EE6FF),
                Color(0x3332FFA7),
                Color(0x339E6BFF)
            )
        ),
        stopBorder = Brush.linearGradient(
            listOf(
                Color(0x66FF5B7A),
                Color(0x66FFB36B)
            )
        ),
        transparentFill = Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    ),
    shapes = JGSShapeTokens(
        smallButtonCorner = 14.dp,
        cardCorner = 18.dp,
        largeButtonCorner = 18.dp,
        fabCorner = 18.dp
    ),
    sizes = JGSSizeTokens(
        topBarHeight = 64.dp,
        topBarBackSize = 52.dp,
        topBarSidePadding = 10.dp,
        topBarBackOffsetY = (-5).dp,
        topBarEndPadding = 10.dp,
        topBarTitleHorizontalPadding = 12.dp,
        floatingNowHorizontalPadding = 20.dp,
        floatingNowVerticalPadding = 14.dp,
        playerScreenInset = 24.dp,
        screenContentPadding = 16.dp,
        sectionSpacingSmall = 12.dp,
        sectionSpacingMedium = 14.dp,
        sectionSpacingLarge = 18.dp,
        listItemHorizontalPadding = 6.dp,
        listItemVerticalPadding = 12.dp,
        searchFieldPadding = 10.dp,
        edgeSwipeZone = 28.dp,
        edgeSwipeTrigger = 72.dp,
        seekBarStroke = 10.dp,
        seekBarKnob = 6.dp,
        seekCenterDiameter = 300.dp,
        seekCenterPadding = 28.dp,
        seekCenterTouchRadius = 64.dp,
        playerPrimaryButton = 84.dp,
        playerSecondaryButton = 72.dp,
        librarySmallButton = 44.dp
    ),
    text = JGSTextTokens(
        topBarBack = TextStyle(
            fontSize = 30.sp
        ),
        topBarTitle = TextStyle(
            fontSize = 20.sp
        ),
        time = TextStyle(
            fontSize = 14.sp
        ),
        playerCenterGlyph = TextStyle(
            fontSize = 32.sp
        ),
        buttonLabel = TextStyle(
            fontSize = 14.sp
        ),
        iconButtonSmall = TextStyle(
            fontSize = 18.sp
        ),
        iconButtonMedium = TextStyle(
            fontSize = 22.sp
        ),
        iconButtonLarge = TextStyle(
            fontSize = 26.sp
        )
    )
)

internal val LocalJGSDesignTokens = staticCompositionLocalOf { DefaultJGSDesignTokens }
