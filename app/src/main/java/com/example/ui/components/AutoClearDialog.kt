package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoDelete
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarViewWeek
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.ui.theme.TaskBlue

@Composable
fun AutoClearDialog(
    isOpen: Boolean,
    currentSettings: AppSettings,
    onDismiss: () -> Unit,
    onSaveSettings: (enabled: Boolean, frequencyHours: Int) -> Unit
) {
    if (!isOpen) return

    var enabled by remember(currentSettings) { mutableStateOf(currentSettings.autoClearEnabled) }
    var frequencyHours by remember(currentSettings) { mutableIntStateOf(currentSettings.autoClearFrequencyHours) }

    val frequencyOptions = listOf(
        0 to "Immediately upon completion",
        1 to "After 1 hour",
        24 to "After 1 day (24 hours)",
        72 to "After 3 days",
        168 to "After 1 week (7 days)",
        720 to "After 30 days"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.AutoDelete,
                contentDescription = null,
                tint = TaskBlue
            )
        },
        title = {
            Text("Auto-Clear Completed Tasks", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Toggle Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AutoDelete,
                                contentDescription = null,
                                tint = TaskBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable Auto-Clear",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = "Automatically remove completed tasks based on your schedule",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it },
                        modifier = Modifier.testTag("auto_clear_switch")
                    )
                }

                if (enabled) {
                    HorizontalDivider()

                    Text(
                        text = "Clear completed tasks:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        frequencyOptions.forEach { (hours, label) ->
                            val isSelected = frequencyHours == hours
                            val icon = when (hours) {
                                0 -> Icons.Outlined.FlashOn
                                1 -> Icons.Outlined.HourglassTop
                                24 -> Icons.Outlined.Today
                                72 -> Icons.Outlined.DateRange
                                168 -> Icons.Outlined.CalendarViewWeek
                                else -> Icons.Outlined.CalendarMonth
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) TaskBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { frequencyHours = hours }
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
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) TaskBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Selected",
                                            tint = TaskBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveSettings(enabled, frequencyHours)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = TaskBlue),
                modifier = Modifier.testTag("confirm_auto_clear_btn")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_auto_clear_btn")
            ) {
                Text("Cancel")
            }
        }
    )
}
