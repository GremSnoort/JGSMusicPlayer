package com.example.jgsmusicplayer.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val PlayerBg = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF050B12), // почти чёрный (верх)
        Color(0xFF071823), // тёмный морской
        Color(0xFF120E24)  // тёмный фиолетовый (низ)
    )
)

val TopBarBg = Color(0xFF050B12) // можно чуть прозрачнее, если хочешь
val Accent = Color(0xFF7EE8FA)   // морской акцент (стрелка/кнопки и т.п.)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
