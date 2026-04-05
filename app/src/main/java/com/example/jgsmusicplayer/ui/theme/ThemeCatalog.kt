package com.example.jgsmusicplayer.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import com.example.jgsmusicplayer.R

enum class JGSThemeKey {
    DEEP_OCEAN,
    COASTAL_LIGHT,
    SUNRIVER,
    FROSTPEAK,
    LAGUNA,
    GATE,
    PATINA,
    MAUSOLEUM_ONE,
    MAUSOLEUM_TWO,
    MAUSOLEUM_THREE,
    MAUSOLEUM_FOUR,
    MAUSOLEUM_FIVE,
    MAUSOLEUM_SIX,
    MAUSOLEUM_SEVEN,
    MAUSOLEUM_EIGHT,
    MAUSOLEUM_NINE,
    MAUSOLEUM_TEN,
    MAUSOLEUM_ELEVEN,
    MAUSOLEUM_TWELVE,
    MAUSOLEUM_THIRTEEN,
    MAUSOLEUM_FOURTEEN,
    MAUSOLEUM_FIFTEEN,
    GRASSLAND,
    GRASSLAND_TWO,
    SUN_DESERT
}

enum class JGSThemeGroup(val displayName: String, val shortLabel: String) {
    MAUSOLEUM("Mausoleum", "Maus"),
    OCEAN("Ocean", "Ocean"),
    RIVER("River", "River"),
    FROST("Frost", "Frost"),
    GATES("Gates", "Gates"),
    LEAVES("Leaves", "Leaves"),
    DUNES("Dunes", "Dunes")
}

@Immutable
data class JGSBackgroundTokens(
    @param:DrawableRes val libraryBackgroundRes: Int,
    @param:DrawableRes val playerBackgroundRes: Int,
    val overlayAlpha: Float = 0.55f,
    val cropBiasX: Float = 0.5f,
    val cropBiasY: Float = 0.5f
)

@Immutable
data class JGSThemeSpec(
    val key: JGSThemeKey,
    val displayName: String,
    val buttonLabel: String,
    val group: JGSThemeGroup,
    val description: String,
    val designTokens: JGSDesignTokens,
    val typography: Typography,
    val backgrounds: JGSBackgroundTokens
)

private val DeepOceanTheme = JGSThemeSpec(
    key = JGSThemeKey.DEEP_OCEAN,
    displayName = "Deep Ocean",
    buttonLabel = "Deep",
    group = JGSThemeGroup.OCEAN,
    description = "Deep neon ocean glow.",
    designTokens = DefaultJGSDesignTokens,
    typography = DeepOceanTypography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_deep_ocean,
        playerBackgroundRes = R.drawable.bg_deep_ocean,
        overlayAlpha = 0.55f
    )
)

private val CoastalLightTheme = JGSThemeSpec(
    key = JGSThemeKey.COASTAL_LIGHT,
    displayName = "Coastal Light",
    buttonLabel = "Coast",
    group = JGSThemeGroup.OCEAN,
    description = "Bright coastal breeze.",
    designTokens = DefaultJGSDesignTokens.copy(
        colors = DefaultJGSDesignTokens.colors.copy(
            backgroundBase = Color(0xFFE7F7FB),
            surfaceBase = Color(0xFFD5EEF5),
            textPrimary = Color(0xFFF8FEFF),
            textSecondary = Color(0xD9F2FBFF),
            textOnAccent = Color(0xFFFFFFFF),
            listTextShadow = Color(0x4A0F2330),
            glassSurface = Color(0x88376572),
            centerOverlay = Color(0x3A2D5F72),
            whiteOverlay = Color(0x8CFFFFFF),
            seekKnob = Color(0xFF0B97B0),
            errorSurface = Color(0x33FFB28B),
            errorGlow = Color(0x334AAFC2),
            topBarBackIcon = Color(0xFFF8FEFF),
            topBarBackShadow = Color(0x180A2028),
            topBarTitleGlow = Color(0x40B8F1FF),
            seekWaveGlowColors = listOf(
                Color(0x3377D8E9),
                Color(0x2295F0C8),
                Color(0x1ABAD9FF),
                Color.Transparent
            ),
            seekWaveSweepColors = listOf(
                Color(0x1477D8E9),
                Color(0x1495F0C8),
                Color(0x14BAD9FF),
                Color(0x1477D8E9)
            ),
            seekWaveRingColors = listOf(
                Color(0x8C77D8E9),
                Color(0x8C95F0C8),
                Color(0x8CBAD9FF),
                Color(0x8C77D8E9)
            )
        ),
        brushes = DefaultJGSDesignTokens.brushes.copy(
            appBackground = Brush.verticalGradient(
                listOf(
                    Color(0xBF1F586B),
                    Color(0xA63A7281),
                    Color(0x99406F78)
                )
            ),
            primaryBorder = Brush.linearGradient(
                listOf(
                    Color(0xAA7ED8EC),
                    Color(0xAA8CE9CA),
                    Color(0xAA9CC6F2)
                )
            ),
            fabBackground = Brush.linearGradient(
                listOf(
                    Color(0xA8305A67),
                    Color(0x96304E63)
                )
            ),
            errorBorder = Brush.linearGradient(
                listOf(
                    Color(0x99FF9E7A),
                    Color(0x99FFD8A6)
                )
            ),
            timeText = Brush.linearGradient(
                listOf(
                    Color(0xFFE7FBFF),
                    Color(0xFFD8FFF0),
                    Color(0xFFE8F2FF)
                )
            ),
            topBarTitle = Brush.linearGradient(
                listOf(
                    Color(0xFFF5FDFF),
                    Color(0xFFE0FFF6),
                    Color(0xFFE5EEFF),
                    Color(0xFFF2FCFF)
                )
            ),
            seekTrack = Brush.linearGradient(
                listOf(
                    Color(0x667AB0BC),
                    Color(0x4087C4D2)
                )
            ),
            seekProgress = Brush.sweepGradient(
                listOf(
                    Color(0xFF2398B8),
                    Color(0xFF47BB90),
                    Color(0xFF6A94DE),
                    Color(0xFF2398B8)
                )
            ),
            controlActiveFill = Brush.linearGradient(
                listOf(
                    Color(0x66418AA0),
                    Color(0x4D519F85),
                    Color(0x4D5E82BE)
                )
            ),
            controlActiveGlow = Brush.linearGradient(
                listOf(
                    Color(0x16418AA0),
                    Color(0x14519F85),
                    Color(0x145E82BE)
                )
            ),
            controlActiveBorder = Brush.linearGradient(
                listOf(
                    Color(0xAA7ED8EC),
                    Color(0xAA8CE9CA),
                    Color(0xAA9CC6F2)
                )
            ),
            controlInactiveBorder = Brush.linearGradient(
                listOf(
                    Color(0x807ED8EC),
                    Color(0x808CE9CA),
                    Color(0x809CC6F2)
                )
            ),
            stopBorder = Brush.linearGradient(
                listOf(
                    Color(0x66F39E68),
                    Color(0x66E7C977)
                )
            )
        )
    ),
    typography = CoastalLightTypography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_coast,
        playerBackgroundRes = R.drawable.bg_coast,
        overlayAlpha = 0.5f
    )
)

private val GrasslandTheme = JGSThemeSpec(
    key = JGSThemeKey.GRASSLAND,
    displayName = "Dewleaf 1",
    buttonLabel = "Dew 1",
    group = JGSThemeGroup.LEAVES,
    description = "Dewy leaves and soft greens.",
    designTokens = DefaultJGSDesignTokens.copy(
        colors = DefaultJGSDesignTokens.colors.copy(
            backgroundBase = Color(0xFF0E1A12),
            surfaceBase = Color(0xFF15261A),
            textPrimary = Color(0xFFE4FFD2),
            textSecondary = Color(0xFFC4E9AE),
            textOnAccent = Color(0xFFF0FFE2),
            glassSurface = Color(0x182C4A2A),
            centerOverlay = Color(0x260B1208),
            whiteOverlay = Color(0x38E8FFD6),
            seekKnob = Color(0xFFCDEB8B),
            errorSurface = Color(0x22E6815D),
            errorGlow = Color(0x554FBE6E),
            topBarBackIcon = Color(0xFFE4FFD2),
            topBarBackShadow = Color(0x30000000),
            topBarTitleGlow = Color(0x666BC97E),
            seekWaveGlowColors = listOf(
                Color(0x334DB56F),
                Color(0x2269D35D),
                Color(0x1ABAD66A),
                Color.Transparent
            ),
            seekWaveSweepColors = listOf(
                Color(0x144DB56F),
                Color(0x1469D35D),
                Color(0x14BAD66A),
                Color(0x144DB56F)
            ),
            seekWaveRingColors = listOf(
                Color(0x8C4DB56F),
                Color(0x8C69D35D),
                Color(0x8CBAD66A),
                Color(0x8C4DB56F)
            )
        ),
        brushes = DefaultJGSDesignTokens.brushes.copy(
            appBackground = Brush.verticalGradient(
                listOf(
                    Color(0xCC0E1A12),
                    Color(0xCC17301E),
                    Color(0xB31B2614)
                )
            ),
            primaryBorder = Brush.linearGradient(
                listOf(
                    Color(0x664DB56F),
                    Color(0x6669D35D),
                    Color(0x66BAD66A)
                )
            ),
            fabBackground = Brush.linearGradient(
                listOf(
                    Color(0xCC1A2A18),
                    Color(0xCC10170E)
                )
            ),
            errorBorder = Brush.linearGradient(
                listOf(
                    Color(0x99E6815D),
                    Color(0x99E7B06A)
                )
            ),
            timeText = Brush.linearGradient(
                listOf(
                    Color(0xFF7EDB7D),
                    Color(0xFFA0E987),
                    Color(0xFFD4F2A6)
                )
            ),
            topBarTitle = Brush.linearGradient(
                listOf(
                    Color(0xFF8CDD8D),
                    Color(0xFFB3E873),
                    Color(0xFFBFE6A2),
                    Color(0xFF76D18E)
                )
            ),
            seekTrack = Brush.linearGradient(
                listOf(
                    Color(0x33476C48),
                    Color(0x1A88A35A)
                )
            ),
            seekProgress = Brush.sweepGradient(
                listOf(
                    Color(0xFF4DB56F),
                    Color(0xFF69D35D),
                    Color(0xFFBAD66A),
                    Color(0xFF4DB56F)
                )
            ),
            controlActiveFill = Brush.linearGradient(
                listOf(
                    Color(0x334DB56F),
                    Color(0x2269D35D),
                    Color(0x22BAD66A)
                )
            ),
            controlActiveGlow = Brush.linearGradient(
                listOf(
                    Color(0x1A4DB56F),
                    Color(0x1269D35D),
                    Color(0x12BAD66A)
                )
            ),
            controlActiveBorder = Brush.linearGradient(
                listOf(
                    Color(0x664DB56F),
                    Color(0x6669D35D),
                    Color(0x66BAD66A)
                )
            ),
            controlInactiveBorder = Brush.linearGradient(
                listOf(
                    Color(0x334DB56F),
                    Color(0x3369D35D),
                    Color(0x33BAD66A)
                )
            ),
            stopBorder = Brush.linearGradient(
                listOf(
                    Color(0x66E6815D),
                    Color(0x66E7B06A)
                )
            )
        )
    ),
    typography = GrasslandTypography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_dewleaf_1,
        playerBackgroundRes = R.drawable.bg_dewleaf_1,
        overlayAlpha = 0.55f
    )
)

private val GrasslandThemeTwo = JGSThemeSpec(
    key = JGSThemeKey.GRASSLAND_TWO,
    displayName = "Dewleaf 2",
    buttonLabel = "Dew 2",
    group = JGSThemeGroup.LEAVES,
    description = "Deeper greens with rainlight.",
    designTokens = GrasslandTheme.designTokens,
    typography = GrasslandTheme.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_dewleaf_2,
        playerBackgroundRes = R.drawable.bg_dewleaf_2,
        overlayAlpha = GrasslandTheme.backgrounds.overlayAlpha
    )
)

private val SunriverTheme = JGSThemeSpec(
    key = JGSThemeKey.SUNRIVER,
    displayName = "Sunriver",
    buttonLabel = "River",
    group = JGSThemeGroup.RIVER,
    description = "Warm river at sunset.",
    designTokens = DefaultJGSDesignTokens.copy(
        colors = DefaultJGSDesignTokens.colors.copy(
            backgroundBase = Color(0xFF181A1B),
            surfaceBase = Color(0xFF233237),
            textPrimary = Color(0xFFF8EDCF),
            textSecondary = Color(0xE0D9DCCB),
            textOnAccent = Color(0xFFFFF3DC),
            glassSurface = Color(0x72455F64),
            centerOverlay = Color(0x2E213239),
            whiteOverlay = Color(0x42FFF4DA),
            seekKnob = Color(0xFF7CD3C9),
            errorSurface = Color(0x33F59664),
            errorGlow = Color(0x5578C6B5),
            topBarBackIcon = Color(0xFFF8EDCF),
            topBarBackShadow = Color(0x33000000),
            topBarTitleGlow = Color(0x55E4B56F),
            seekWaveGlowColors = listOf(
                Color(0x3386D6CB),
                Color(0x22EAB96B),
                Color(0x1AD59A69),
                Color.Transparent
            ),
            seekWaveSweepColors = listOf(
                Color(0x1486D6CB),
                Color(0x14EAB96B),
                Color(0x14D59A69),
                Color(0x1486D6CB)
            ),
            seekWaveRingColors = listOf(
                Color(0x8C86D6CB),
                Color(0x8CEAB96B),
                Color(0x8CD59A69),
                Color(0x8C86D6CB)
            )
        ),
        brushes = DefaultJGSDesignTokens.brushes.copy(
            appBackground = Brush.verticalGradient(
                listOf(
                    Color(0xB82A5962),
                    Color(0x964C625D),
                    Color(0x8A28484F)
                )
            ),
            primaryBorder = Brush.linearGradient(
                listOf(
                    Color(0xAA87D7CC),
                    Color(0xAAE4C37A),
                    Color(0xAAD79A69)
                )
            ),
            fabBackground = Brush.linearGradient(
                listOf(
                    Color(0xA0344B51),
                    Color(0x8C543F33)
                )
            ),
            errorBorder = Brush.linearGradient(
                listOf(
                    Color(0x99F59664),
                    Color(0x99F0C67F)
                )
            ),
            timeText = Brush.linearGradient(
                listOf(
                    Color(0xFFF7E6BE),
                    Color(0xFF8EE1D5),
                    Color(0xFFFFC57D)
                )
            ),
            topBarTitle = Brush.linearGradient(
                listOf(
                    Color(0xFFFFEBC5),
                    Color(0xFF9AE6DE),
                    Color(0xFFF2CC8B),
                    Color(0xFFFFE3BC)
                )
            ),
            seekTrack = Brush.linearGradient(
                listOf(
                    Color(0x4D8AA5A7),
                    Color(0x266D7B74)
                )
            ),
            seekProgress = Brush.sweepGradient(
                listOf(
                    Color(0xFF86D6CB),
                    Color(0xFFE4C37A),
                    Color(0xFFD79A69),
                    Color(0xFF86D6CB)
                )
            ),
            controlActiveFill = Brush.linearGradient(
                listOf(
                    Color(0x5586D6CB),
                    Color(0x44E4C37A),
                    Color(0x44D79A69)
                )
            ),
            controlActiveGlow = Brush.linearGradient(
                listOf(
                    Color(0x1686D6CB),
                    Color(0x14E4C37A),
                    Color(0x14D79A69)
                )
            ),
            controlActiveBorder = Brush.linearGradient(
                listOf(
                    Color(0xAA86D6CB),
                    Color(0xAAE4C37A),
                    Color(0xAAD79A69)
                )
            ),
            controlInactiveBorder = Brush.linearGradient(
                listOf(
                    Color(0x8086D6CB),
                    Color(0x80E4C37A),
                    Color(0x80D79A69)
                )
            ),
            stopBorder = Brush.linearGradient(
                listOf(
                    Color(0x66F59664),
                    Color(0x66F0C67F)
                )
            )
        )
    ),
    typography = SunDesertTypography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_sunriver,
        playerBackgroundRes = R.drawable.bg_sunriver,
        overlayAlpha = 0.56f
    )
)

private val FrostpeakTheme = JGSThemeSpec(
    key = JGSThemeKey.FROSTPEAK,
    displayName = "Frostpeak",
    buttonLabel = "Frost",
    group = JGSThemeGroup.FROST,
    description = "Frozen mountain stream.",
    designTokens = DefaultJGSDesignTokens.copy(
        colors = DefaultJGSDesignTokens.colors.copy(
            backgroundBase = Color(0xFF0D131C),
            surfaceBase = Color(0xFF16212B),
            textPrimary = Color(0xFFF2F7FF),
            textSecondary = Color(0xD1DCEBFF),
            textOnAccent = Color(0xFFF7FBFF),
            listTextShadow = Color(0x660D1620),
            glassSurface = Color(0x70324152),
            centerOverlay = Color(0x50324D68),
            whiteOverlay = Color(0x52DDEEFF),
            seekKnob = Color(0xFFCBE8FF),
            errorSurface = Color(0x33FF8A72),
            errorGlow = Color(0x556EAEDD),
            topBarBackIcon = Color(0xFFF2F7FF),
            topBarBackShadow = Color(0x30000000),
            topBarTitleGlow = Color(0x5586BCFF),
            seekWaveGlowColors = listOf(
                Color(0x338DCAFF),
                Color(0x22CBE8FF),
                Color(0x1ABCCBFF),
                Color.Transparent
            ),
            seekWaveSweepColors = listOf(
                Color(0x148DCAFF),
                Color(0x14CBE8FF),
                Color(0x14BCCBFF),
                Color(0x148DCAFF)
            ),
            seekWaveRingColors = listOf(
                Color(0x8C8DCAFF),
                Color(0x8CCBE8FF),
                Color(0x8CBCCBFF),
                Color(0x8C8DCAFF)
            )
        ),
        brushes = DefaultJGSDesignTokens.brushes.copy(
            appBackground = Brush.verticalGradient(
                listOf(
                    Color(0xC0203145),
                    Color(0x9E2B3E57),
                    Color(0x8A1C2733)
                )
            ),
            primaryBorder = Brush.linearGradient(
                listOf(
                    Color(0xAA94CFFF),
                    Color(0xAACAE8FF),
                    Color(0xAAEAF4FF)
                )
            ),
            fabBackground = Brush.linearGradient(
                listOf(
                    Color(0xA02D3B4D),
                    Color(0x8C1B2633)
                )
            ),
            errorBorder = Brush.linearGradient(
                listOf(
                    Color(0x99FF8A72),
                    Color(0x99FFD1A3)
                )
            ),
            timeText = Brush.linearGradient(
                listOf(
                    Color(0xFFF1F7FF),
                    Color(0xFFCBE8FF),
                    Color(0xFFE7F4FF)
                )
            ),
            topBarTitle = Brush.linearGradient(
                listOf(
                    Color(0xFFF6FAFF),
                    Color(0xFFDCEBFF),
                    Color(0xFFC5E2FF),
                    Color(0xFFF2F8FF)
                )
            ),
            seekTrack = Brush.linearGradient(
                listOf(
                    Color(0x4D7E98B8),
                    Color(0x267D9CB2)
                )
            ),
            seekProgress = Brush.sweepGradient(
                listOf(
                    Color(0xFF8DCAFF),
                    Color(0xFFCBE8FF),
                    Color(0xFFB6D3F1),
                    Color(0xFF8DCAFF)
                )
            ),
            controlActiveFill = Brush.linearGradient(
                listOf(
                    Color(0x558DCAFF),
                    Color(0x44CBE8FF),
                    Color(0x44B6D3F1)
                )
            ),
            controlActiveGlow = Brush.linearGradient(
                listOf(
                    Color(0x168DCAFF),
                    Color(0x14CBE8FF),
                    Color(0x14B6D3F1)
                )
            ),
            controlActiveBorder = Brush.linearGradient(
                listOf(
                    Color(0xAA8DCAFF),
                    Color(0xAACBE8FF),
                    Color(0xAAB6D3F1)
                )
            ),
            controlInactiveBorder = Brush.linearGradient(
                listOf(
                    Color(0x808DCAFF),
                    Color(0x80CBE8FF),
                    Color(0x80B6D3F1)
                )
            ),
            stopBorder = Brush.linearGradient(
                listOf(
                    Color(0x66FF8A72),
                    Color(0x66FFD1A3)
                )
            )
        )
    ),
    typography = DeepOceanTypography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_frostpeak,
        playerBackgroundRes = R.drawable.bg_frostpeak,
        overlayAlpha = 0.52f
    )
)

private val LagunaTheme = JGSThemeSpec(
    key = JGSThemeKey.LAGUNA,
    displayName = "Laguna",
    buttonLabel = "Laguna",
    group = JGSThemeGroup.OCEAN,
    description = "Clear lagoons and sea glass.",
    designTokens = DefaultJGSDesignTokens.copy(
        colors = DefaultJGSDesignTokens.colors.copy(
            backgroundBase = Color(0xFF0E1820),
            surfaceBase = Color(0xFF152832),
            textPrimary = Color(0xFFEAF8FF),
            textSecondary = Color(0xD1D2EBF2),
            textOnAccent = Color(0xFFF0FBFF),
            listTextShadow = Color(0x40101E2A),
            glassSurface = Color(0x72416068),
            centerOverlay = Color(0x38325A66),
            whiteOverlay = Color(0x4AF2FFFF),
            seekKnob = Color(0xFFBBF5F0),
            errorSurface = Color(0x33FF9F79),
            errorGlow = Color(0x5569C3D2),
            topBarBackIcon = Color(0xFFEAF8FF),
            topBarBackShadow = Color(0x30000000),
            topBarTitleGlow = Color(0x4C7CE3EA),
            seekWaveGlowColors = listOf(
                Color(0x3396E7E3),
                Color(0x22B7F3D3),
                Color(0x1AA8D4FF),
                Color.Transparent
            ),
            seekWaveSweepColors = listOf(
                Color(0x1496E7E3),
                Color(0x14B7F3D3),
                Color(0x14A8D4FF),
                Color(0x1496E7E3)
            ),
            seekWaveRingColors = listOf(
                Color(0x8C96E7E3),
                Color(0x8CB7F3D3),
                Color(0x8CA8D4FF),
                Color(0x8C96E7E3)
            )
        ),
        brushes = DefaultJGSDesignTokens.brushes.copy(
            appBackground = Brush.verticalGradient(
                listOf(
                    Color(0xB0205560),
                    Color(0x964F6A65),
                    Color(0x80293A46)
                )
            ),
            primaryBorder = Brush.linearGradient(
                listOf(
                    Color(0xAA9EE5E6),
                    Color(0xAAC7F1D7),
                    Color(0xAAAED3FF)
                )
            ),
            fabBackground = Brush.linearGradient(
                listOf(
                    Color(0xA0335960),
                    Color(0x8C31474D)
                )
            ),
            errorBorder = Brush.linearGradient(
                listOf(
                    Color(0x99FF9F79),
                    Color(0x99FFD8A9)
                )
            ),
            timeText = Brush.linearGradient(
                listOf(
                    Color(0xFFF1FFFF),
                    Color(0xFFC9FFF1),
                    Color(0xFFDFF0FF)
                )
            ),
            topBarTitle = Brush.linearGradient(
                listOf(
                    Color(0xFFF6FFFF),
                    Color(0xFFD6FFF7),
                    Color(0xFFDDEAFF),
                    Color(0xFFF2FFFF)
                )
            ),
            seekTrack = Brush.linearGradient(
                listOf(
                    Color(0x4D7AAAB0),
                    Color(0x2688C9CC)
                )
            ),
            seekProgress = Brush.sweepGradient(
                listOf(
                    Color(0xFF96E7E3),
                    Color(0xFFB7F3D3),
                    Color(0xFFA8D4FF),
                    Color(0xFF96E7E3)
                )
            ),
            controlActiveFill = Brush.linearGradient(
                listOf(
                    Color(0x5596E7E3),
                    Color(0x44B7F3D3),
                    Color(0x44A8D4FF)
                )
            ),
            controlActiveGlow = Brush.linearGradient(
                listOf(
                    Color(0x1696E7E3),
                    Color(0x14B7F3D3),
                    Color(0x14A8D4FF)
                )
            ),
            controlActiveBorder = Brush.linearGradient(
                listOf(
                    Color(0xAA96E7E3),
                    Color(0xAAB7F3D3),
                    Color(0xAAA8D4FF)
                )
            ),
            controlInactiveBorder = Brush.linearGradient(
                listOf(
                    Color(0x8096E7E3),
                    Color(0x80B7F3D3),
                    Color(0x80A8D4FF)
                )
            ),
            stopBorder = Brush.linearGradient(
                listOf(
                    Color(0x66FF9F79),
                    Color(0x66FFD8A9)
                )
            )
        )
    ),
    typography = CoastalLightTypography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_laguna,
        playerBackgroundRes = R.drawable.bg_laguna,
        overlayAlpha = 0.44f
    )
)

private val GateTheme = JGSThemeSpec(
    key = JGSThemeKey.GATE,
    displayName = "Gate",
    buttonLabel = "Gate",
    group = JGSThemeGroup.GATES,
    description = "Dark brass and emerald gate.",
    designTokens = DefaultJGSDesignTokens.copy(
        colors = DefaultJGSDesignTokens.colors.copy(
            backgroundBase = Color(0xFF100E0B),
            surfaceBase = Color(0xFF1B1812),
            textPrimary = Color(0xFFEAD8AA),
            textSecondary = Color(0xD0C6A67A),
            textOnAccent = Color(0xFFF0E0B8),
            glassSurface = Color(0x70322B20),
            centerOverlay = Color(0x4A241E17),
            whiteOverlay = Color(0x30FFE0A8),
            seekKnob = Color(0xFFD2B06C),
            errorSurface = Color(0x33D66A4D),
            errorGlow = Color(0x555A9A7D),
            topBarBackIcon = Color(0xFFEAD8AA),
            topBarBackShadow = Color(0x33000000),
            topBarTitleGlow = Color(0x4480A57C),
            seekWaveGlowColors = listOf(
                Color(0x3376A17B),
                Color(0x22C0A060),
                Color(0x1A7A8F78),
                Color.Transparent
            ),
            seekWaveSweepColors = listOf(
                Color(0x1476A17B),
                Color(0x14C0A060),
                Color(0x147A8F78),
                Color(0x1476A17B)
            ),
            seekWaveRingColors = listOf(
                Color(0x8C76A17B),
                Color(0x8CC0A060),
                Color(0x8C7A8F78),
                Color(0x8C76A17B)
            )
        ),
        brushes = DefaultJGSDesignTokens.brushes.copy(
            appBackground = Brush.verticalGradient(
                listOf(
                    Color(0xC0252A23),
                    Color(0xA640392A),
                    Color(0x8C161813)
                )
            ),
            primaryBorder = Brush.linearGradient(
                listOf(
                    Color(0xAA7AA07A),
                    Color(0xAAC0A060),
                    Color(0xAACBB07A)
                )
            ),
            fabBackground = Brush.linearGradient(
                listOf(
                    Color(0xA0332B20),
                    Color(0x8C241E16)
                )
            ),
            errorBorder = Brush.linearGradient(
                listOf(
                    Color(0x99D66A4D),
                    Color(0x99D5A06E)
                )
            ),
            timeText = Brush.linearGradient(
                listOf(
                    Color(0xFFEAD8AA),
                    Color(0xFFBFAE7A),
                    Color(0xFF86A186)
                )
            ),
            topBarTitle = Brush.linearGradient(
                listOf(
                    Color(0xFFF0E0B8),
                    Color(0xFFC5B481),
                    Color(0xFF8CAA8A),
                    Color(0xFFE3D2AA)
                )
            ),
            seekTrack = Brush.linearGradient(
                listOf(
                    Color(0x4D705F45),
                    Color(0x26758872)
                )
            ),
            seekProgress = Brush.sweepGradient(
                listOf(
                    Color(0xFF7AA07A),
                    Color(0xFFC0A060),
                    Color(0xFFCBAD72),
                    Color(0xFF7AA07A)
                )
            ),
            controlActiveFill = Brush.linearGradient(
                listOf(
                    Color(0x557AA07A),
                    Color(0x44C0A060),
                    Color(0x44CBAD72)
                )
            ),
            controlActiveGlow = Brush.linearGradient(
                listOf(
                    Color(0x167AA07A),
                    Color(0x14C0A060),
                    Color(0x14CBAD72)
                )
            ),
            controlActiveBorder = Brush.linearGradient(
                listOf(
                    Color(0xAA7AA07A),
                    Color(0xAAC0A060),
                    Color(0xAACBAD72)
                )
            ),
            controlInactiveBorder = Brush.linearGradient(
                listOf(
                    Color(0x807AA07A),
                    Color(0x80C0A060),
                    Color(0x80CBAD72)
                )
            ),
            stopBorder = Brush.linearGradient(
                listOf(
                    Color(0x66D66A4D),
                    Color(0x66D5A06E)
                )
            )
        )
    ),
    typography = DeepOceanTypography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_gate,
        playerBackgroundRes = R.drawable.bg_gate,
        overlayAlpha = 0.6f
    )
)

private val PatinaTheme = JGSThemeSpec(
    key = JGSThemeKey.PATINA,
    displayName = "Patina",
    buttonLabel = "Patina",
    group = JGSThemeGroup.GATES,
    description = "Verdigris brass patina.",
    designTokens = GateTheme.designTokens.copy(
        colors = GateTheme.designTokens.colors.copy(
            textPrimary = Color(0xFFE2D7B7),
            textSecondary = Color(0xC7C1AC84),
            textOnAccent = Color(0xFFE8DDBF),
            topBarTitleGlow = Color(0x4C7CA48E)
        ),
        brushes = GateTheme.designTokens.brushes.copy(
            topBarTitle = Brush.linearGradient(
                listOf(
                    Color(0xFFE9DFC2),
                    Color(0xFFCDBF95),
                    Color(0xFF8FAE97),
                    Color(0xFFE1D6B8)
                )
            )
        )
    ),
    typography = GateTheme.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_patina,
        playerBackgroundRes = R.drawable.bg_patina,
        overlayAlpha = 0.6f
    )
)

private val SunDesertTheme = JGSThemeSpec(
    key = JGSThemeKey.SUN_DESERT,
    displayName = "Sun Desert",
    buttonLabel = "Dune",
    group = JGSThemeGroup.DUNES,
    description = "Amber dunes and warm sand.",
    designTokens = DefaultJGSDesignTokens.copy(
        colors = DefaultJGSDesignTokens.colors.copy(
            backgroundBase = Color(0xFF26170D),
            surfaceBase = Color(0xFF382114),
            textPrimary = Color(0xFFF6E4BF),
            textSecondary = Color(0xE0D2B27E),
            textOnAccent = Color(0xFFF8E8C9),
            glassSurface = Color(0x1AF6D2A4),
            centerOverlay = Color(0x260C0704),
            whiteOverlay = Color(0x33FFF7EA),
            seekKnob = Color(0xFFFFD07A),
            errorSurface = Color(0x33F27F56),
            errorGlow = Color(0x55E1A34A),
            topBarBackIcon = Color(0xFFF6E4BF),
            topBarBackShadow = Color(0x33000000),
            topBarTitleGlow = Color(0x66E7B05A),
            seekWaveGlowColors = listOf(
                Color(0x33E7B05A),
                Color(0x22F3C96A),
                Color(0x1AF09052),
                Color.Transparent
            ),
            seekWaveSweepColors = listOf(
                Color(0x14E7B05A),
                Color(0x14F3C96A),
                Color(0x14F09052),
                Color(0x14E7B05A)
            ),
            seekWaveRingColors = listOf(
                Color(0x8CE7B05A),
                Color(0x8CF3C96A),
                Color(0x8CF09052),
                Color(0x8CE7B05A)
            )
        ),
        brushes = DefaultJGSDesignTokens.brushes.copy(
            appBackground = Brush.verticalGradient(
                listOf(
                    Color(0xCC28160C),
                    Color(0xCC5A381D),
                    Color(0xB37A4D22)
                )
            ),
            primaryBorder = Brush.linearGradient(
                listOf(
                    Color(0x66E7B05A),
                    Color(0x66F3C96A),
                    Color(0x66F09052)
                )
            ),
            fabBackground = Brush.linearGradient(
                listOf(
                    Color(0xCC4E2F19),
                    Color(0xCC2D180C)
                )
            ),
            errorBorder = Brush.linearGradient(
                listOf(
                    Color(0x99F27F56),
                    Color(0x99F2B467)
                )
            ),
            timeText = Brush.linearGradient(
                listOf(
                    Color(0xFFFFC26F),
                    Color(0xFFF4D983),
                    Color(0xFFFFE4AF)
                )
            ),
            topBarTitle = Brush.linearGradient(
                listOf(
                    Color(0xFFFFCA77),
                    Color(0xFFF6D07B),
                    Color(0xFFFFE3B2),
                    Color(0xFFF0A15D)
                )
            ),
            seekTrack = Brush.linearGradient(
                listOf(
                    Color(0x336D4B32),
                    Color(0x1AA57A3D)
                )
            ),
            seekProgress = Brush.sweepGradient(
                listOf(
                    Color(0xFFE7B05A),
                    Color(0xFFF3C96A),
                    Color(0xFFF09052),
                    Color(0xFFE7B05A)
                )
            ),
            controlActiveFill = Brush.linearGradient(
                listOf(
                    Color(0x33E7B05A),
                    Color(0x22F3C96A),
                    Color(0x22F09052)
                )
            ),
            controlActiveGlow = Brush.linearGradient(
                listOf(
                    Color(0x1AE7B05A),
                    Color(0x12F3C96A),
                    Color(0x12F09052)
                )
            ),
            controlActiveBorder = Brush.linearGradient(
                listOf(
                    Color(0x66E7B05A),
                    Color(0x66F3C96A),
                    Color(0x66F09052)
                )
            ),
            controlInactiveBorder = Brush.linearGradient(
                listOf(
                    Color(0x33E7B05A),
                    Color(0x33F3C96A),
                    Color(0x33F09052)
                )
            ),
            stopBorder = Brush.linearGradient(
                listOf(
                    Color(0x66F27F56),
                    Color(0x66F2B467)
                )
            )
        )
    ),
    typography = SunDesertTypography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_dune,
        playerBackgroundRes = R.drawable.bg_dune,
        overlayAlpha = 0.55f
    )
)

private val MausoleumThemeOne = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_ONE,
    displayName = "Mausoleum I",
    buttonLabel = "Maus I",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series I.",
    designTokens = SunDesertTheme.designTokens.copy(
        colors = SunDesertTheme.designTokens.colors.copy(
            textPrimary = Color(0xFFF4E7C6),
            textSecondary = Color(0xD9DBC59B),
            textOnAccent = Color(0xFFF7EBD1),
            topBarTitleGlow = Color(0x66D6A75D),
            seekKnob = Color(0xFFE6BF73)
        ),
        brushes = SunDesertTheme.designTokens.brushes.copy(
            topBarTitle = Brush.linearGradient(
                listOf(
                    Color(0xFFF5E7C9),
                    Color(0xFFE2C48D),
                    Color(0xFFB58F58),
                    Color(0xFFF0E1C0)
                )
            ),
            appBackground = Brush.verticalGradient(
                listOf(
                    Color(0xCC1E1E1C),
                    Color(0xCC2A2520),
                    Color(0xB31A1917)
                )
            )
        )
    ),
    typography = SunDesertTheme.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_1,
        playerBackgroundRes = R.drawable.bg_mausoleum_1,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeTwo = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_TWO,
    displayName = "Mausoleum II",
    buttonLabel = "Maus II",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series II.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_2,
        playerBackgroundRes = R.drawable.bg_mausoleum_2,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeThree = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_THREE,
    displayName = "Mausoleum III",
    buttonLabel = "Maus III",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series III.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_3,
        playerBackgroundRes = R.drawable.bg_mausoleum_3,
        overlayAlpha = 0.58f,
        cropBiasX = 0.9f
    )
)

private val MausoleumThemeFour = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_FOUR,
    displayName = "Mausoleum IV",
    buttonLabel = "Maus IV",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series IV.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_4,
        playerBackgroundRes = R.drawable.bg_mausoleum_4,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeFive = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_FIVE,
    displayName = "Mausoleum V",
    buttonLabel = "Maus V",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series V.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_5,
        playerBackgroundRes = R.drawable.bg_mausoleum_5,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeSix = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_SIX,
    displayName = "Mausoleum VI",
    buttonLabel = "Maus VI",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series VI.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_6,
        playerBackgroundRes = R.drawable.bg_mausoleum_6,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeSeven = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_SEVEN,
    displayName = "Mausoleum VII",
    buttonLabel = "Maus VII",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series VII.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_7,
        playerBackgroundRes = R.drawable.bg_mausoleum_7,
        overlayAlpha = 0.58f,
        cropBiasX = 0.9f
    )
)

private val MausoleumThemeEight = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_EIGHT,
    displayName = "Mausoleum VIII",
    buttonLabel = "Maus VIII",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series VIII.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_8,
        playerBackgroundRes = R.drawable.bg_mausoleum_8,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeNine = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_NINE,
    displayName = "Mausoleum IX",
    buttonLabel = "Maus IX",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series IX.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_9,
        playerBackgroundRes = R.drawable.bg_mausoleum_9,
        overlayAlpha = 0.58f,
        cropBiasX = 0.4f
    )
)

private val MausoleumThemeTen = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_TEN,
    displayName = "Mausoleum X",
    buttonLabel = "Maus X",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series X.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_10,
        playerBackgroundRes = R.drawable.bg_mausoleum_10,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeEleven = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_ELEVEN,
    displayName = "Mausoleum XI",
    buttonLabel = "Maus XI",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series XI.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_11,
        playerBackgroundRes = R.drawable.bg_mausoleum_11,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeTwelve = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_TWELVE,
    displayName = "Mausoleum XII",
    buttonLabel = "Maus XII",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series XII.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_12,
        playerBackgroundRes = R.drawable.bg_mausoleum_12,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeThirteen = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_THIRTEEN,
    displayName = "Mausoleum XIII",
    buttonLabel = "Maus XIII",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series XIII.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_13,
        playerBackgroundRes = R.drawable.bg_mausoleum_13,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeFourteen = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_FOURTEEN,
    displayName = "Mausoleum XIV",
    buttonLabel = "Maus XIV",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series XIV.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_14,
        playerBackgroundRes = R.drawable.bg_mausoleum_14,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

private val MausoleumThemeFifteen = JGSThemeSpec(
    key = JGSThemeKey.MAUSOLEUM_FIFTEEN,
    displayName = "Mausoleum XV",
    buttonLabel = "Maus XV",
    group = JGSThemeGroup.MAUSOLEUM,
    description = "Mausoleum series XV.",
    designTokens = MausoleumThemeOne.designTokens,
    typography = MausoleumThemeOne.typography,
    backgrounds = JGSBackgroundTokens(
        libraryBackgroundRes = R.drawable.bg_mausoleum_15,
        playerBackgroundRes = R.drawable.bg_mausoleum_15,
        overlayAlpha = 0.58f,
        cropBiasX = 1f
    )
)

object JGSThemes {
    val all: List<JGSThemeSpec> = listOf(
        DeepOceanTheme,
        CoastalLightTheme,
        LagunaTheme,
        GateTheme,
        PatinaTheme,
        GrasslandTheme,
        GrasslandThemeTwo,
        SunDesertTheme,
        MausoleumThemeOne,
        MausoleumThemeTwo,
        MausoleumThemeThree,
        MausoleumThemeFour,
        MausoleumThemeFive,
        MausoleumThemeSix,
        MausoleumThemeSeven,
        MausoleumThemeEight,
        MausoleumThemeNine,
        MausoleumThemeTen,
        MausoleumThemeEleven,
        MausoleumThemeTwelve,
        MausoleumThemeThirteen,
        MausoleumThemeFourteen,
        MausoleumThemeFifteen,
        SunriverTheme,
        FrostpeakTheme
    )

    val groups: List<JGSThemeGroup> = listOf(
        JGSThemeGroup.MAUSOLEUM,
        JGSThemeGroup.OCEAN,
        JGSThemeGroup.RIVER,
        JGSThemeGroup.FROST,
        JGSThemeGroup.GATES,
        JGSThemeGroup.LEAVES,
        JGSThemeGroup.DUNES
    )

    val default: JGSThemeSpec = DeepOceanTheme

    fun byKey(key: JGSThemeKey): JGSThemeSpec {
        return all.firstOrNull { it.key == key } ?: default
    }

    fun byGroup(group: JGSThemeGroup): List<JGSThemeSpec> {
        return all.filter { it.group == group }
    }

    fun fromKeyName(name: String): JGSThemeSpec {
        val key = runCatching { JGSThemeKey.valueOf(name) }.getOrNull() ?: default.key
        return byKey(key)
    }

    fun groupFromName(name: String): JGSThemeGroup {
        return runCatching { JGSThemeGroup.valueOf(name) }.getOrNull() ?: JGSThemeGroup.OCEAN
    }

    fun nextTheme(after: JGSThemeSpec): JGSThemeSpec {
        val currentIndex = all.indexOfFirst { it.key == after.key }
        if (currentIndex == -1) return default
        return all[(currentIndex + 1) % all.size]
    }
}

internal val LocalJGSThemeSpec = staticCompositionLocalOf { JGSThemes.default }
