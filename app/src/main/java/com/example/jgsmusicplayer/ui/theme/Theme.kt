package com.example.jgsmusicplayer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun JGSMusicPlayerTheme(
    themeSpec: JGSThemeSpec = JGSThemes.default,
    content: @Composable () -> Unit
) {
    val designTokens = themeSpec.designTokens
    CompositionLocalProvider(
        LocalJGSThemeSpec provides themeSpec,
        LocalJGSDesignTokens provides designTokens
    ) {
        MaterialTheme(
            colorScheme = jgsDarkColorScheme(designTokens),
            typography = themeSpec.typography,
            content = content
        )
    }
}

object JGSTheme {
    val design: JGSDesignTokens
        @Composable
        get() = LocalJGSDesignTokens.current

    val current: JGSThemeSpec
        @Composable
        get() = LocalJGSThemeSpec.current

    val backgrounds: JGSBackgroundTokens
        @Composable
        get() = LocalJGSThemeSpec.current.backgrounds
}

private fun jgsDarkColorScheme(tokens: JGSDesignTokens) = darkColorScheme(
    primary = tokens.colors.textOnAccent,
    secondary = tokens.colors.textSecondary,
    tertiary = tokens.colors.textPrimary,
    background = tokens.colors.backgroundBase,
    surface = tokens.colors.surfaceBase,
    onSurface = tokens.colors.textPrimary,
    onBackground = tokens.colors.textPrimary
)
