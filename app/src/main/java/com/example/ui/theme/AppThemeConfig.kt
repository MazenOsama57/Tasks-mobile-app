package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

data class AppThemeOption(
    val id: String,
    val name: String,
    val primaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val isDark: Boolean = true,
    val colorScheme: ColorScheme
)

object ThemePresets {
    // 1. Modern Slate Dark (Default)
    val ModernSlateDark = AppThemeOption(
        id = "modern_dark",
        name = "Modern Slate Dark",
        primaryColor = Color(0xFF8AB4F8),
        backgroundColor = Color(0xFF1F1F1F),
        surfaceColor = Color(0xFF28292A),
        colorScheme = darkColorScheme(
            primary = Color(0xFF8AB4F8),
            onPrimary = Color(0xFF003062),
            primaryContainer = Color(0xFF042D5A),
            onPrimaryContainer = Color(0xFFD2E3FC),
            surface = Color(0xFF28292A),
            onSurface = Color(0xFFE8EAED),
            surfaceVariant = Color(0xFF35363A),
            onSurfaceVariant = Color(0xFFBDC1C6),
            background = Color(0xFF1F1F1F),
            onBackground = Color(0xFFE8EAED)
        )
    )

    // 2. Midnight OLED (Pitch Black True Black)
    val MidnightOLED = AppThemeOption(
        id = "midnight_oled",
        name = "Midnight OLED",
        primaryColor = Color(0xFF818CF8),
        backgroundColor = Color(0xFF000000),
        surfaceColor = Color(0xFF101010),
        colorScheme = darkColorScheme(
            primary = Color(0xFF818CF8),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF312E81),
            onPrimaryContainer = Color(0xFFE0E7FF),
            surface = Color(0xFF101010),
            onSurface = Color(0xFFF1F5F9),
            surfaceVariant = Color(0xFF1E1E1E),
            onSurfaceVariant = Color(0xFF94A3B8),
            background = Color(0xFF000000),
            onBackground = Color(0xFFF1F5F9)
        )
    )

    // 3. Cyberpunk Neon (Obsidian, Hot Pink & Cyan)
    val CyberpunkNeon = AppThemeOption(
        id = "cyberpunk_neon",
        name = "Cyberpunk Neon",
        primaryColor = Color(0xFFFF2A85),
        backgroundColor = Color(0xFF0D0C1D),
        surfaceColor = Color(0xFF181530),
        colorScheme = darkColorScheme(
            primary = Color(0xFFFF2A85),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF4A0E2B),
            onPrimaryContainer = Color(0xFFFFD1E6),
            secondary = Color(0xFF00F0FF),
            surface = Color(0xFF181530),
            onSurface = Color(0xFFFCE7F3),
            surfaceVariant = Color(0xFF242045),
            onSurfaceVariant = Color(0xFFC084FC),
            background = Color(0xFF0D0C1D),
            onBackground = Color(0xFFFCE7F3)
        )
    )

    // 4. Emerald Forest (Deep Pine & Emerald Green)
    val EmeraldForest = AppThemeOption(
        id = "emerald_forest",
        name = "Emerald Forest",
        primaryColor = Color(0xFF34D399),
        backgroundColor = Color(0xFF051711),
        surfaceColor = Color(0xFF0C291E),
        colorScheme = darkColorScheme(
            primary = Color(0xFF34D399),
            onPrimary = Color(0xFF022C1A),
            primaryContainer = Color(0xFF064E3B),
            onPrimaryContainer = Color(0xFFA7F3D0),
            surface = Color(0xFF0C291E),
            onSurface = Color(0xFFECFDF5),
            surfaceVariant = Color(0xFF133D2D),
            onSurfaceVariant = Color(0xFF6EE7B7),
            background = Color(0xFF051711),
            onBackground = Color(0xFFECFDF5)
        )
    )

    // 5. Amethyst Purple (Royal Violet & Lavender)
    val AmethystPurple = AppThemeOption(
        id = "amethyst_purple",
        name = "Amethyst Purple",
        primaryColor = Color(0xFFC084FC),
        backgroundColor = Color(0xFF120B1F),
        surfaceColor = Color(0xFF1E1333),
        colorScheme = darkColorScheme(
            primary = Color(0xFFC084FC),
            onPrimary = Color(0xFF2E1065),
            primaryContainer = Color(0xFF581C87),
            onPrimaryContainer = Color(0xFFF3E8FF),
            surface = Color(0xFF1E1333),
            onSurface = Color(0xFFFAF5FF),
            surfaceVariant = Color(0xFF2C1C4A),
            onSurfaceVariant = Color(0xFFD8B4FE),
            background = Color(0xFF120B1F),
            onBackground = Color(0xFFFAF5FF)
        )
    )

    // 6. Sunset Amber (Warm Dark Chocolate & Radiant Amber)
    val SunsetAmber = AppThemeOption(
        id = "sunset_amber",
        name = "Sunset Amber",
        primaryColor = Color(0xFFFBBF24),
        backgroundColor = Color(0xFF19120C),
        surfaceColor = Color(0xFF291D15),
        colorScheme = darkColorScheme(
            primary = Color(0xFFFBBF24),
            onPrimary = Color(0xFF451A03),
            primaryContainer = Color(0xFF78350F),
            onPrimaryContainer = Color(0xFFFEF3C7),
            surface = Color(0xFF291D15),
            onSurface = Color(0xFFFFFBEB),
            surfaceVariant = Color(0xFF3D2C20),
            onSurfaceVariant = Color(0xFFFCD34D),
            background = Color(0xFF19120C),
            onBackground = Color(0xFFFFFBEB)
        )
    )

    // 7. Ocean Abyss (Deep Oceanic Navy & Aquamarine)
    val OceanAbyss = AppThemeOption(
        id = "ocean_abyss",
        name = "Ocean Abyss",
        primaryColor = Color(0xFF38BDF8),
        backgroundColor = Color(0xFF081424),
        surfaceColor = Color(0xFF0F233D),
        colorScheme = darkColorScheme(
            primary = Color(0xFF38BDF8),
            onPrimary = Color(0xFF082F49),
            primaryContainer = Color(0xFF075985),
            onPrimaryContainer = Color(0xFFE0F2FE),
            surface = Color(0xFF0F233D),
            onSurface = Color(0xFFF0F9FF),
            surfaceVariant = Color(0xFF163255),
            onSurfaceVariant = Color(0xFF7DD3FC),
            background = Color(0xFF081424),
            onBackground = Color(0xFFF0F9FF)
        )
    )

    // 8. Crimson Ruby (Velvet Dark & Ruby Rose)
    val CrimsonRuby = AppThemeOption(
        id = "crimson_ruby",
        name = "Crimson Ruby",
        primaryColor = Color(0xFFFB7185),
        backgroundColor = Color(0xFF1C0A10),
        surfaceColor = Color(0xFF2E121B),
        colorScheme = darkColorScheme(
            primary = Color(0xFFFB7185),
            onPrimary = Color(0xFF4C0519),
            primaryContainer = Color(0xFF881337),
            onPrimaryContainer = Color(0xFFFFE4E6),
            surface = Color(0xFF2E121B),
            onSurface = Color(0xFFFFF1F2),
            surfaceVariant = Color(0xFF441B28),
            onSurfaceVariant = Color(0xFFFDA4AF),
            background = Color(0xFF1C0A10),
            onBackground = Color(0xFFFFF1F2)
        )
    )

    // 9. Slate Graphite (Industrial Slate & Cobalt)
    val SlateGraphite = AppThemeOption(
        id = "slate_graphite",
        name = "Slate Graphite",
        primaryColor = Color(0xFF60A5FA),
        backgroundColor = Color(0xFF0F172A),
        surfaceColor = Color(0xFF1E293B),
        colorScheme = darkColorScheme(
            primary = Color(0xFF60A5FA),
            onPrimary = Color(0xFF172554),
            primaryContainer = Color(0xFF1E3A8A),
            onPrimaryContainer = Color(0xFFDBEAFE),
            surface = Color(0xFF1E293B),
            onSurface = Color(0xFFF8FAFC),
            surfaceVariant = Color(0xFF334155),
            onSurfaceVariant = Color(0xFF94A3B8),
            background = Color(0xFF0F172A),
            onBackground = Color(0xFFF8FAFC)
        )
    )

    // 10. Espresso Warm (Rich Espresso & Caramel)
    val EspressoWarm = AppThemeOption(
        id = "espresso_warm",
        name = "Espresso Warm",
        primaryColor = Color(0xFFF59E0B),
        backgroundColor = Color(0xFF1C1815),
        surfaceColor = Color(0xFF2E2722),
        colorScheme = darkColorScheme(
            primary = Color(0xFFF59E0B),
            onPrimary = Color(0xFF361E04),
            primaryContainer = Color(0xFF713F12),
            onPrimaryContainer = Color(0xFFFEF3C7),
            surface = Color(0xFF2E2722),
            onSurface = Color(0xFFFDFCFB),
            surfaceVariant = Color(0xFF403630),
            onSurfaceVariant = Color(0xFFD6C7BC),
            background = Color(0xFF1C1815),
            onBackground = Color(0xFFFDFCFB)
        )
    )

    // 11. Pure Minimal White
    val PureMinimalWhite = AppThemeOption(
        id = "pure_white",
        name = "Pure Minimal White",
        primaryColor = Color(0xFF1A73E8),
        backgroundColor = Color(0xFFFFFFFF),
        surfaceColor = Color(0xFFF8F9FA),
        isDark = false,
        colorScheme = lightColorScheme(
            primary = Color(0xFF1A73E8),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE8F0FE),
            onPrimaryContainer = Color(0xFF174EA6),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF202124),
            surfaceVariant = Color(0xFFF1F3F4),
            onSurfaceVariant = Color(0xFF5F6368),
            background = Color(0xFFFFFFFF),
            onBackground = Color(0xFF202124)
        )
    )

    // 12. Warm Cream & Paper
    val WarmCreamLight = AppThemeOption(
        id = "warm_cream",
        name = "Warm Cream & Paper",
        primaryColor = Color(0xFFB45309),
        backgroundColor = Color(0xFFFAF8F5),
        surfaceColor = Color(0xFFFFFFFF),
        isDark = false,
        colorScheme = lightColorScheme(
            primary = Color(0xFFB45309),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFEF3C7),
            onPrimaryContainer = Color(0xFF78350F),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF292524),
            surfaceVariant = Color(0xFFF5F3EF),
            onSurfaceVariant = Color(0xFF78716C),
            background = Color(0xFFFAF8F5),
            onBackground = Color(0xFF292524)
        )
    )

    // 13. Nordic Ice White
    val NordicIceLight = AppThemeOption(
        id = "nordic_ice",
        name = "Nordic Ice White",
        primaryColor = Color(0xFF0284C7),
        backgroundColor = Color(0xFFF0F9FF),
        surfaceColor = Color(0xFFFFFFFF),
        isDark = false,
        colorScheme = lightColorScheme(
            primary = Color(0xFF0284C7),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFE0F2FE),
            onPrimaryContainer = Color(0xFF0369A1),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF0F172A),
            surfaceVariant = Color(0xFFE2E8F0),
            onSurfaceVariant = Color(0xFF475569),
            background = Color(0xFFF0F9FF),
            onBackground = Color(0xFF0F172A)
        )
    )

    // 14. Lavender Pearl White
    val LavenderPearlLight = AppThemeOption(
        id = "lavender_pearl",
        name = "Lavender Pearl",
        primaryColor = Color(0xFF9333EA),
        backgroundColor = Color(0xFFFAF5FF),
        surfaceColor = Color(0xFFFFFFFF),
        isDark = false,
        colorScheme = lightColorScheme(
            primary = Color(0xFF9333EA),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFF3E8FF),
            onPrimaryContainer = Color(0xFF6B21A8),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1E1B4B),
            surfaceVariant = Color(0xFFF3E8FF),
            onSurfaceVariant = Color(0xFF6B7280),
            background = Color(0xFFFAF5FF),
            onBackground = Color(0xFF1E1B4B)
        )
    )

    // 15. Mint Whisper White
    val MintWhisperLight = AppThemeOption(
        id = "mint_whisper",
        name = "Mint Whisper White",
        primaryColor = Color(0xFF059669),
        backgroundColor = Color(0xFFF0FDF4),
        surfaceColor = Color(0xFFFFFFFF),
        isDark = false,
        colorScheme = lightColorScheme(
            primary = Color(0xFF059669),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFD1FAE5),
            onPrimaryContainer = Color(0xFF065F46),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF064E3B),
            surfaceVariant = Color(0xFFE6F4EA),
            onSurfaceVariant = Color(0xFF4B5563),
            background = Color(0xFFF0FDF4),
            onBackground = Color(0xFF064E3B)
        )
    )

    val AllPresets: List<AppThemeOption> = listOf(
        ModernSlateDark,
        MidnightOLED,
        CyberpunkNeon,
        EmeraldForest,
        AmethystPurple,
        SunsetAmber,
        OceanAbyss,
        CrimsonRuby,
        SlateGraphite,
        EspressoWarm,
        PureMinimalWhite,
        WarmCreamLight,
        NordicIceLight,
        LavenderPearlLight,
        MintWhisperLight
    )

    fun createCustomColorScheme(
        primaryHex: String,
        bgHex: String,
        surfaceHex: String,
        isLight: Boolean = false
    ): ColorScheme {
        val primary = try { Color(primaryHex.toColorInt()) } catch (e: Exception) { Color(0xFF1A73E8) }
        val bg = try { Color(bgHex.toColorInt()) } catch (e: Exception) { if (isLight) Color(0xFFFFFFFF) else Color(0xFF121212) }
        val surface = try { Color(surfaceHex.toColorInt()) } catch (e: Exception) { if (isLight) Color(0xFFF8F9FA) else Color(0xFF1E1E1E) }

        return if (isLight) {
            lightColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.15f),
                onPrimaryContainer = primary,
                surface = surface,
                onSurface = Color(0xFF202124),
                surfaceVariant = Color(0xFFF1F3F4),
                onSurfaceVariant = Color(0xFF5F6368),
                background = bg,
                onBackground = Color(0xFF202124)
            )
        } else {
            darkColorScheme(
                primary = primary,
                onPrimary = Color.White,
                primaryContainer = primary.copy(alpha = 0.25f),
                onPrimaryContainer = Color.White,
                surface = surface,
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = surface.copy(alpha = 0.8f),
                onSurfaceVariant = Color(0xFFCBD5E1),
                background = bg,
                onBackground = Color(0xFFF1F5F9)
            )
        }
    }
}
