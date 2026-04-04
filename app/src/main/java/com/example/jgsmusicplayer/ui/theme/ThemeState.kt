package com.example.jgsmusicplayer.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Immutable
data class JGSThemeController(
    val currentTheme: JGSThemeSpec,
    val currentButtonLabel: String,
    val cycleTheme: () -> Unit,
    val setTheme: (JGSThemeKey) -> Unit
)

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")
private val ThemeKeyPreference = stringPreferencesKey("theme_key")

@Composable
fun rememberJGSThemeController(): JGSThemeController {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val storedThemeKeyName by context.themeDataStore.data
        .map { prefs -> prefs[ThemeKeyPreference] ?: JGSThemes.default.key.name }
        .collectAsState(initial = JGSThemes.default.key.name)

    var currentThemeKeyName by rememberSaveable { mutableStateOf(storedThemeKeyName) }

    LaunchedEffect(storedThemeKeyName) {
        currentThemeKeyName = storedThemeKeyName
    }
    val currentTheme = remember(currentThemeKeyName) { JGSThemes.fromKeyName(currentThemeKeyName) }

    return remember(currentTheme, currentThemeKeyName) {
        JGSThemeController(
            currentTheme = currentTheme,
            currentButtonLabel = currentTheme.buttonLabel,
            cycleTheme = {
                val nextThemeKeyName = JGSThemes.nextTheme(currentTheme).key.name
                currentThemeKeyName = nextThemeKeyName
                scope.launch {
                    context.themeDataStore.edit { prefs ->
                        prefs[ThemeKeyPreference] = nextThemeKeyName
                    }
                }
            },
            setTheme = { key ->
                val keyName = key.name
                currentThemeKeyName = keyName
                scope.launch {
                    context.themeDataStore.edit { prefs ->
                        prefs[ThemeKeyPreference] = keyName
                    }
                }
            }
        )
    }
}
