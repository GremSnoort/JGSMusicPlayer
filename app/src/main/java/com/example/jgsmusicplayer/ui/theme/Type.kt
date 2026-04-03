package com.example.jgsmusicplayer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import com.example.jgsmusicplayer.R

private val FacultyGlyphicFontFamily = FontFamily(
    Font(R.font.faculty_glyphic_regular, FontWeight.Normal)
)

private fun jgsTypography(
    displayFamily: FontFamily,
    bodyFamily: FontFamily,
    labelFamily: FontFamily,
    titleLetterSpacing: Float = 0f,
    bodyLetterSpacing: Float = 0.5f
): Typography {
    return Typography(
        headlineLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            letterSpacing = titleLetterSpacing.sp
        ),
        titleMedium = TextStyle(
            fontFamily = displayFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = (titleLetterSpacing * 0.5f).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = bodyLetterSpacing.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = (bodyLetterSpacing * 0.5f).sp
        ),
        labelMedium = TextStyle(
            fontFamily = labelFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        )
    )
}

val DeepOceanTypography = jgsTypography(
    displayFamily = FacultyGlyphicFontFamily,
    bodyFamily = FacultyGlyphicFontFamily,
    labelFamily = FacultyGlyphicFontFamily,
    titleLetterSpacing = 0.2f,
    bodyLetterSpacing = 0.08f
)

val CoastalLightTypography = jgsTypography(
    displayFamily = FacultyGlyphicFontFamily,
    bodyFamily = FacultyGlyphicFontFamily,
    labelFamily = FacultyGlyphicFontFamily,
    titleLetterSpacing = 0.2f,
    bodyLetterSpacing = 0.08f
)

val GrasslandTypography = jgsTypography(
    displayFamily = FacultyGlyphicFontFamily,
    bodyFamily = FacultyGlyphicFontFamily,
    labelFamily = FacultyGlyphicFontFamily,
    titleLetterSpacing = 0.2f,
    bodyLetterSpacing = 0.08f
)

val SunDesertTypography = jgsTypography(
    displayFamily = FacultyGlyphicFontFamily,
    bodyFamily = FacultyGlyphicFontFamily,
    labelFamily = FacultyGlyphicFontFamily,
    titleLetterSpacing = 0.2f,
    bodyLetterSpacing = 0.08f
)
