package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.example.data.model.AppSettings
import com.example.ui.theme.TaskBlue

@Composable
fun ReminderSettingsDialog(
    isOpen: Boolean,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSaveReminderSettings: (
        morningEnabled: Boolean,
        morningHour: Int,
        morningMinute: Int,
        eveningEnabled: Boolean,
        eveningHour: Int,
        eveningMinute: Int
    ) -> Unit,
    onSendTestReminder: () -> Unit,
    onRequestNotificationPermission: () -> Unit = {}
) {
    if (!isOpen) return

    val context = LocalContext.current
    val areNotificationsEnabled = remember(isOpen) {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    var morningEnabled by remember(settings) { mutableStateOf(settings.morningReminderEnabled) }
    var morningHour by remember(settings) { mutableIntStateOf(settings.morningReminderHour) }
    var morningMinute by remember(settings) { mutableIntStateOf(settings.morningReminderMinute) }

    var eveningEnabled by remember(settings) { mutableStateOf(settings.eveningReminderEnabled) }
    var eveningHour by remember(settings) { mutableIntStateOf(settings.eveningReminderHour) }
    var eveningMinute by remember(settings) { mutableIntStateOf(settings.eveningReminderMinute) }

    val morningPresets = listOf(
        Pair(6, 0) to "6:00 AM",
        Pair(7, 0) to "7:00 AM",
        Pair(8, 0) to "8:00 AM",
        Pair(9, 0) to "9:00 AM",
        Pair(10, 0) to "10:00 AM"
    )

    val eveningPresets = listOf(
        Pair(18, 0) to "6:00 PM",
        Pair(19, 0) to "7:00 PM",
        Pair(20, 0) to "8:00 PM",
        Pair(21, 0) to "9:00 PM",
        Pair(22, 0) to "10:00 PM"
    )

    fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour >= 12) "PM" else "AM"
        val h = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val m = if (minute < 10) "0$minute" else "$minute"
        return "$h:$m $amPm"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.NotificationsActive,
                contentDescription = null,
                tint = TaskBlue
            )
        },
        title = {
            Text(
                text = "Daily Reminder Notifications",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Permission Status Card
                if (!areNotificationsEnabled) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Notification access required",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = "Tap to grant permission so you receive daily reminders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = onRequestNotificationPermission,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Allow", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = TaskBlue.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = TaskBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Notification access active",
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = "Configure reminders for the starting of each day (agenda preview) and near ending (uncompleted tasks wrap-up) whenever you want.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                // Morning Reminder Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.WbSunny,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Morning Day-Starter",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "Reminder at start of day: ${formatTime(morningHour, morningMinute)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = morningEnabled,
                            onCheckedChange = { morningEnabled = it },
                            modifier = Modifier.testTag("switch_morning_reminder")
                        )
                    }

                    if (morningEnabled) {
                        // Quick Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            morningPresets.forEach { (time, label) ->
                                val (h, m) = time
                                val isSelected = morningHour == h && morningMinute == m
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        morningHour = h
                                        morningMinute = m
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Alarm,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                                )
                            }
                        }

                        // Custom Time Adjuster (When the user wants it)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Custom: ${formatTime(morningHour, morningMinute)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (morningMinute >= 15) {
                                                morningMinute -= 15
                                            } else if (morningHour > 0) {
                                                morningHour--
                                                morningMinute = 45
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Earlier", modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            if (morningMinute <= 40) {
                                                morningMinute += 15
                                            } else if (morningHour < 23) {
                                                morningHour++
                                                morningMinute = 0
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Later", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                // Evening Reminder Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.Bedtime,
                                    contentDescription = null,
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Evening Wrap-Up",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = "Reminder near day end: ${formatTime(eveningHour, eveningMinute)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = eveningEnabled,
                            onCheckedChange = { eveningEnabled = it },
                            modifier = Modifier.testTag("switch_evening_reminder")
                        )
                    }

                    if (eveningEnabled) {
                        // Quick Presets
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            eveningPresets.forEach { (time, label) ->
                                val (h, m) = time
                                val isSelected = eveningHour == h && eveningMinute == m
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        eveningHour = h
                                        eveningMinute = m
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Alarm,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                                )
                            }
                        }

                        // Custom Time Adjuster (When the user wants it)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Schedule,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Custom: ${formatTime(eveningHour, eveningMinute)}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = {
                                            if (eveningMinute >= 15) {
                                                eveningMinute -= 15
                                            } else if (eveningHour > 0) {
                                                eveningHour--
                                                eveningMinute = 45
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = "Earlier", modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = {
                                            if (eveningMinute <= 40) {
                                                eveningMinute += 15
                                            } else if (eveningHour < 23) {
                                                eveningHour++
                                                eveningMinute = 0
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Later", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                // Test Notification Button
                OutlinedButton(
                    onClick = onSendTestReminder,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Test Reminder Notification")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveReminderSettings(
                        morningEnabled,
                        morningHour,
                        morningMinute,
                        eveningEnabled,
                        eveningHour,
                        eveningMinute
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TaskBlue)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
