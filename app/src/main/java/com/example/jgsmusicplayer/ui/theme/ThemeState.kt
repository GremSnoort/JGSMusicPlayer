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
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Immutable
data class JGSThemeController(
    val currentTheme: JGSThemeSpec,
    val currentButtonLabel: String,
    val cycleTheme: () -> Unit,
    val setTheme: (JGSThemeKey) -> Unit,
    val getThemeBias: (JGSThemeKey) -> ThemeBias?,
    val setThemeBias: (JGSThemeKey, ThemeBias) -> Unit
)

private val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")
private val ThemeKeyPreference = stringPreferencesKey("theme_key")
private const val SyncPrefsName = "theme_prefs_sync"
private const val SyncThemeKey = "theme_key"

@Immutable
data class ThemeBias(
    val x: Float,
    val y: Float
)

private fun biasXKey(key: JGSThemeKey) = floatPreferencesKey("theme_bias_x_${key.name}")
private fun biasYKey(key: JGSThemeKey) = floatPreferencesKey("theme_bias_y_${key.name}")

private fun loadBiasMap(prefs: androidx.datastore.preferences.core.Preferences): Map<JGSThemeKey, ThemeBias> {
    return JGSThemes.all.mapNotNull { theme ->
        val x = prefs[biasXKey(theme.key)]
        val y = prefs[biasYKey(theme.key)]
        if (x != null && y != null) {
            theme.key to ThemeBias(x, y)
        } else {
            null
        }
    }.toMap()
}

private fun applyBias(theme: JGSThemeSpec, biasMap: Map<JGSThemeKey, ThemeBias>): JGSThemeSpec {
    val bias = biasMap[theme.key] ?: return theme
    return theme.copy(
        backgrounds = theme.backgrounds.copy(
            cropBiasX = bias.x,
            cropBiasY = bias.y
        )
    )
}

@Composable
fun rememberJGSThemeController(): JGSThemeController {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncPrefs = remember(context) {
        context.getSharedPreferences(SyncPrefsName, Context.MODE_PRIVATE)
    }
    val syncThemeKeyName = remember(syncPrefs) {
        syncPrefs.getString(SyncThemeKey, null) ?: JGSThemes.default.key.name
    }
    val storedThemeKeyName by context.themeDataStore.data
        .map { prefs -> prefs[ThemeKeyPreference] ?: syncThemeKeyName }
        .collectAsState(initial = syncThemeKeyName)
    val biasMap by context.themeDataStore.data
        .map { prefs -> loadBiasMap(prefs) }
        .collectAsState(initial = emptyMap())

    var currentThemeKeyName by rememberSaveable { mutableStateOf(syncThemeKeyName) }
    var localBiasOverrides by remember { mutableStateOf<Map<JGSThemeKey, ThemeBias>>(emptyMap()) }

    LaunchedEffect(storedThemeKeyName) {
        currentThemeKeyName = storedThemeKeyName
    }
    val baseTheme = remember(currentThemeKeyName) { JGSThemes.fromKeyName(currentThemeKeyName) }
    val mergedBiasMap = remember(biasMap, localBiasOverrides) {
        biasMap + localBiasOverrides
    }
    val currentTheme = remember(baseTheme, mergedBiasMap) { applyBias(baseTheme, mergedBiasMap) }

    return remember(currentTheme, currentThemeKeyName, mergedBiasMap) {
        JGSThemeController(
            currentTheme = currentTheme,
            currentButtonLabel = currentTheme.buttonLabel,
            cycleTheme = {
                val nextThemeKeyName = JGSThemes.nextTheme(currentTheme).key.name
                currentThemeKeyName = nextThemeKeyName
                syncPrefs.edit().putString(SyncThemeKey, nextThemeKeyName).apply()
                scope.launch {
                    context.themeDataStore.edit { prefs ->
                        prefs[ThemeKeyPreference] = nextThemeKeyName
                    }
                }
            },
            setTheme = { key ->
                val keyName = key.name
                currentThemeKeyName = keyName
                syncPrefs.edit().putString(SyncThemeKey, keyName).apply()
                scope.launch {
                    context.themeDataStore.edit { prefs ->
                        prefs[ThemeKeyPreference] = keyName
                    }
                }
            },
            getThemeBias = { key -> biasMap[key] },
            setThemeBias = { key, bias ->
                localBiasOverrides = localBiasOverrides + (key to bias)
                scope.launch {
                    context.themeDataStore.edit { prefs ->
                        prefs[biasXKey(key)] = bias.x
                        prefs[biasYKey(key)] = bias.y
                    }
                }
            }
        )
    }
}
