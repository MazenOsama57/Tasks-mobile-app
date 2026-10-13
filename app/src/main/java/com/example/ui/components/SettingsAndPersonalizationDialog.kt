package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.AutoDelete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.ui.SortOrder
import com.example.ui.theme.TaskBlue
import com.example.ui.theme.ThemePresets
import com.example.ui.util.HapticFeedbackHelper

@Composable
fun SettingsAndPersonalizationDialog(
    isOpen: Boolean,
    settings: AppSettings,
    currentSort: SortOrder,
    onDismiss: () -> Unit,
    onOpenReminders: () -> Unit,
    onOpenTheme: () -> Unit,
    onOpenAutoClear: () -> Unit,
    onOpenSort: () -> Unit,
    onClearCompleted: () -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    val currentThemeName = if (settings.themePresetId == "custom") {
        "Custom Theme (${if (settings.isCustomThemeLight) "Light" else "Dark"})"
    } else {
        ThemePresets.AllPresets.find { it.id == settings.themePresetId }?.name ?: "Modern Dark"
    }

    val autoClearSummary = if (settings.autoClearEnabled) {
        when (settings.autoClearFrequencyHours) {
            0 -> "Immediate upon completion"
            1 -> "After 1 hour"
            24 -> "After 24 hours"
            72 -> "After 3 days"
            168 -> "After 1 week"
            else -> "After 30 days"
        }
    } else {
        "Disabled"
    }

    val reminderSummary = buildString {
        if (settings.morningReminderEnabled && settings.eveningReminderEnabled) {
            append("Morning & Evening active")
        } else if (settings.morningReminderEnabled) {
            append("Morning only")
        } else if (settings.eveningReminderEnabled) {
            append("Evening only")
        } else {
            append("Reminders off")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = TaskBlue,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = "Settings & Personalization",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Preferences & customization for your to-do experience.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                // 1. Reminder Notifications (Bell icon)
                SettingsOptionRow(
                    icon = Icons.Outlined.Notifications,
                    iconTint = TaskBlue,
                    title = "Reminder Notifications",
                    subtitle = reminderSummary,
                    onClick = {
                        onDismiss()
                        onOpenReminders()
                    },
                    testTag = "settings_reminder_option"
                )

                // 2. Personalization & Theme (Palette icon)
                SettingsOptionRow(
                    icon = Icons.Outlined.Palette,
                    iconTint = Color(0xFF818CF8),
                    title = "Theme & Personalization",
                    subtitle = currentThemeName,
                    onClick = {
                        onDismiss()
                        onOpenTheme()
                    },
                    testTag = "settings_theme_option"
                )

                // 3. Auto-Clear Tasks (Auto-delete icon)
                SettingsOptionRow(
                    icon = Icons.Outlined.AutoDelete,
                    iconTint = Color(0xFF0D9488),
                    title = "Auto-Clear Completed Tasks",
                    subtitle = autoClearSummary,
                    onClick = {
                        onDismiss()
                        onOpenAutoClear()
                    },
                    testTag = "settings_autoclear_option"
                )

                // 4. Sort Tasks (Sort icon)
                SettingsOptionRow(
                    icon = Icons.AutoMirrored.Outlined.Sort,
                    iconTint = Color(0xFFD97706),
                    title = "Sort Order",
                    subtitle = currentSort.label,
                    onClick = {
                        onDismiss()
                        onOpenSort()
                    },
                    testTag = "settings_sort_option"
                )

                // 5. Tactile Haptic Feedback (Vibration icon)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEA580C).copy(alpha = 0.14f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Vibration,
                                            contentDescription = null,
                                            tint = Color(0xFFEA580C),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                    Text(
                                        text = "Tactile Haptic Feedback",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Vibrate on complete & swipe delete",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Switch(
                                checked = settings.hapticFeedbackEnabled,
                                onCheckedChange = { isEnabled ->
                                    if (isEnabled) {
                                        HapticFeedbackHelper.performTaskCompleteHaptic(context, hapticFeedback, true)
                                    }
                                    onToggleHapticFeedback(isEnabled)
                                },
                                modifier = Modifier.testTag("switch_haptic_feedback")
                            )
                        }

                        if (settings.hapticFeedbackEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        HapticFeedbackHelper.performTaskCompleteHaptic(context, hapticFeedback, true)
                                    },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.TouchApp,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test Tactile Feel", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // 6. Clear Completed Tasks (DeleteSweep icon)
                SettingsOptionRow(
                    icon = Icons.Outlined.DeleteSweep,
                    iconTint = MaterialTheme.colorScheme.error,
                    title = "Clear Completed Tasks",
                    subtitle = "Instantly purge all completed items",
                    onClick = {
                        onDismiss()
                        onClearCompleted()
                    },
                    testTag = "settings_clear_completed_option"
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("settings_dialog_close")
            ) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun SettingsOptionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = iconTint.copy(alpha = 0.14f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
