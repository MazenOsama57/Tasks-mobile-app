package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Colorize
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.data.model.AppSettings
import com.example.ui.theme.TaskBlue
import com.example.ui.theme.ThemePresets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePickerDialog(
    isOpen: Boolean,
    currentSettings: AppSettings,
    onDismiss: () -> Unit,
    onSelectPreset: (String) -> Unit,
    onSaveCustomTheme: (primaryHex: String, bgHex: String, surfaceHex: String, isLight: Boolean) -> Unit
) {
    if (!isOpen) return

    var selectedTab by remember { mutableIntStateOf(if (currentSettings.themePresetId == "custom") 1 else 0) }
    var presetFilter by remember { mutableStateOf("all") } // "all", "light", "dark"

    // Custom theme builder state
    val accentSwatches = listOf(
        "#1A73E8" to "Task Blue",
        "#818CF8" to "Indigo",
        "#FF2A85" to "Neon Pink",
        "#059669" to "Emerald",
        "#9333EA" to "Purple",
        "#D97706" to "Amber",
        "#0284C7" to "Cyan",
        "#E11D48" to "Rose",
        "#EA580C" to "Orange",
        "#0D9488" to "Teal"
    )

    val backgroundSwatches = listOf(
        // Light & White backgrounds
        Triple("#FFFFFF", "Pure White", true),
        Triple("#FAF8F5", "Warm Cream", true),
        Triple("#F0F9FF", "Ice White", true),
        Triple("#FAF5FF", "Lavender White", true),
        Triple("#F0FDF4", "Mint White", true),
        // Dark backgrounds
        Triple("#000000", "Pitch Black", false),
        Triple("#0F172A", "Dark Slate", false),
        Triple("#121212", "Charcoal Dark", false),
        Triple("#0B1424", "Abyss Navy", false),
        Triple("#18110D", "Warm Mocha", false)
    )

    var customPrimary by remember { mutableStateOf(currentSettings.customPrimaryHex) }
    var customBg by remember { mutableStateOf(currentSettings.customBackgroundHex) }
    var customSurface by remember { mutableStateOf(currentSettings.customSurfaceHex) }
    var isCustomLight by remember { mutableStateOf(currentSettings.isCustomThemeLight) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = TaskBlue
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Theme & Colors", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
            ) {
                // Tab switcher: Presets vs Custom Builder
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.GridView,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        text = { Text("Presets (${ThemePresets.AllPresets.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Tune,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        text = { Text("Customize") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    // Filter row: All, White & Light, Dark
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = presetFilter == "all",
                            onClick = { presetFilter = "all" },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Palette,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "All (${ThemePresets.AllPresets.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        )
                        FilterChip(
                            selected = presetFilter == "light",
                            onClick = { presetFilter = "light" },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.WbSunny,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "White & Light (5)",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        )
                        FilterChip(
                            selected = presetFilter == "dark",
                            onClick = { presetFilter = "dark" },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = "Dark Themes (10)",
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        )
                    }

                    val filteredPresets = ThemePresets.AllPresets.filter {
                        when (presetFilter) {
                            "light" -> !it.isDark
                            "dark" -> it.isDark
                            else -> true
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredPresets) { preset ->
                            val isSelected = currentSettings.themePresetId == preset.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) TaskBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, TaskBlue) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectPreset(preset.id)
                                    }
                                    .testTag("theme_preset_${preset.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Color Swatch preview circle
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                                .background(preset.backgroundColor)
                                                .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(13.dp)
                                                    .clip(CircleShape)
                                                    .background(preset.primaryColor)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                                            Text(
                                                text = preset.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (preset.isDark) Icons.Outlined.DarkMode else Icons.Outlined.WbSunny,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (preset.isDark) "Dark Theme" else "White & Light Theme",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = TaskBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Custom Theme Builder (White and Dark theme customization!)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. Accent Color
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Colorize,
                                contentDescription = null,
                                tint = TaskBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "1. Accent Color",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            accentSwatches.forEach { (hex, name) ->
                                val color = try { Color(hex.toColorInt()) } catch (e: Exception) { Color.Blue }
                                val isSelected = customPrimary.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { customPrimary = hex }
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = name,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Background Shade (White & Dark options)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Wallpaper,
                                contentDescription = null,
                                tint = TaskBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "2. Background Canvas (White & Dark Options)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            backgroundSwatches.forEach { (hex, name, isLight) ->
                                val color = try { Color(hex.toColorInt()) } catch (e: Exception) { Color.Black }
                                val isSelected = customBg.equals(hex, ignoreCase = true)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            customBg = hex
                                            isCustomLight = isLight
                                            customSurface = when (hex) {
                                                "#FFFFFF" -> "#F8F9FA"
                                                "#FAF8F5" -> "#FFFFFF"
                                                "#F0F9FF" -> "#FFFFFF"
                                                "#FAF5FF" -> "#FFFFFF"
                                                "#F0FDF4" -> "#FFFFFF"
                                                "#000000" -> "#121212"
                                                "#0F172A" -> "#1E293B"
                                                "#0B1424" -> "#14223A"
                                                "#18110D" -> "#271E19"
                                                else -> "#1E1E1E"
                                            }
                                        }
                                        .padding(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 2.5.dp else 1.dp,
                                                color = if (isSelected) TaskBlue else Color.Gray.copy(alpha = 0.5f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = name,
                                                tint = if (isLight) TaskBlue else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        // Preview of custom theme
                        val prevPrimary = try { Color(customPrimary.toColorInt()) } catch (e: Exception) { Color.Blue }
                        val prevBg = try { Color(customBg.toColorInt()) } catch (e: Exception) { Color.White }
                        val prevSurface = try { Color(customSurface.toColorInt()) } catch (e: Exception) { Color.LightGray }
                        val textColor = if (isCustomLight) Color(0xFF202124) else Color(0xFFF1F5F9)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = prevBg,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isCustomLight) "White Theme Preview" else "Dark Theme Preview",
                                    color = textColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = prevSurface,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(prevPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Sample task item preview",
                                            color = textColor,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                onSaveCustomTheme(customPrimary, customBg, customSurface, isCustomLight)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TaskBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apply_custom_theme_btn")
                        ) {
                            Text(if (isCustomLight) "Apply Custom White Theme" else "Apply Custom Dark Theme")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
