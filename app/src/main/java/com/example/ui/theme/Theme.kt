package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.example.data.model.AppSettings

@Composable
fun MyApplicationTheme(
    settings: AppSettings = AppSettings(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (settings.themePresetId == "custom") {
        ThemePresets.createCustomColorScheme(
            primaryHex = settings.customPrimaryHex,
            bgHex = settings.customBackgroundHex,
            surfaceHex = settings.customSurfaceHex,
            isLight = settings.isCustomThemeLight
        )
    } else {
        val preset = ThemePresets.AllPresets.find { it.id == settings.themePresetId }
            ?: ThemePresets.ModernSlateDark
        preset.colorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
