package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.RecurrenceFrequency
import com.example.ui.theme.TaskBlue
import java.util.Calendar

@Composable
fun RecurrenceDialog(
    isOpen: Boolean,
    currentFrequency: RecurrenceFrequency,
    currentInterval: Int,
    currentTimesPerPeriod: Int,
    currentDaysOfWeek: String = "",
    currentDaysOfMonth: String = "",
    onDismiss: () -> Unit,
    onSaveRecurrence: (
        frequency: RecurrenceFrequency,
        interval: Int,
        timesPerPeriod: Int,
        daysOfWeek: String,
        daysOfMonth: String
    ) -> Unit
) {
    if (!isOpen) return

    var selectedFrequency by remember(currentFrequency) { mutableStateOf(currentFrequency) }
    var interval by remember(currentInterval) { mutableIntStateOf(if (currentInterval > 0) currentInterval else 1) }
    var timesPerPeriod by remember(currentTimesPerPeriod) { mutableIntStateOf(if (currentTimesPerPeriod > 0) currentTimesPerPeriod else 1) }

    // Days of week state: e.g. set of Calendar.MONDAY, Calendar.WEDNESDAY
    val initialWeekDays = remember(currentDaysOfWeek) {
        currentDaysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }
    var selectedWeekDays by remember(currentDaysOfWeek) { mutableStateOf(initialWeekDays) }

    // Days of month state: set of days 1 to 31
    val initialMonthDays = remember(currentDaysOfMonth) {
        currentDaysOfMonth.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }
    var selectedMonthDays by remember(currentDaysOfMonth) { mutableStateOf(initialMonthDays) }

    val weekDaysList = listOf(
        Pair(Calendar.MONDAY, "Mon"),
        Pair(Calendar.TUESDAY, "Tue"),
        Pair(Calendar.WEDNESDAY, "Wed"),
        Pair(Calendar.THURSDAY, "Thu"),
        Pair(Calendar.FRIDAY, "Fri"),
        Pair(Calendar.SATURDAY, "Sat"),
        Pair(Calendar.SUNDAY, "Sun")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Repeat,
                contentDescription = null,
                tint = TaskBlue
            )
        },
        title = {
            Text(
                text = "Repeat task",
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Frequency Chips Row
                Text(
                    text = "Frequency",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RecurrenceFrequency.entries.forEach { freq ->
                        val isSelected = selectedFrequency == freq
                        val icon = when (freq) {
                            RecurrenceFrequency.NONE -> Icons.Outlined.Block
                            RecurrenceFrequency.DAILY -> Icons.Outlined.Today
                            RecurrenceFrequency.WEEKLY -> Icons.Outlined.DateRange
                            RecurrenceFrequency.MONTHLY -> Icons.Outlined.CalendarMonth
                            RecurrenceFrequency.YEARLY -> Icons.Outlined.EventRepeat
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFrequency = freq },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = { Text(freq.displayName, maxLines = 1, softWrap = false) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TaskBlue.copy(alpha = 0.2f),
                                selectedLabelColor = TaskBlue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("recurrence_freq_${freq.name}")
                        )
                    }
                }

                // If a recurring frequency is chosen: Interval & Custom day options
                if (selectedFrequency != RecurrenceFrequency.NONE) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))

                    val unitName = when (selectedFrequency) {
                        RecurrenceFrequency.DAILY -> if (interval == 1) "day" else "days"
                        RecurrenceFrequency.WEEKLY -> if (interval == 1) "week" else "weeks"
                        RecurrenceFrequency.MONTHLY -> if (interval == 1) "month" else "months"
                        RecurrenceFrequency.YEARLY -> if (interval == 1) "year" else "years"
                        RecurrenceFrequency.NONE -> ""
                    }

                    // Interval Counter (Repeat every N days/weeks/months/years)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Repeat every",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "$interval $unitName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (interval > 1) interval-- },
                                enabled = interval > 1
                            ) {
                                Icon(Icons.Filled.Remove, contentDescription = "Decrease interval")
                            }
                            Text(
                                text = "$interval",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(
                                onClick = { if (interval < 30) interval++ }
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Increase interval")
                            }
                        }
                    }

                    // WEEKLY: Day of week selection (Choose which days)
                    if (selectedFrequency == RecurrenceFrequency.WEEKLY) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Choose days of the week",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (selectedWeekDays.isNotEmpty()) {
                                    TextButton(
                                        onClick = { selectedWeekDays = emptySet() },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                weekDaysList.forEach { (calDay, label) ->
                                    val isSelected = selectedWeekDays.contains(calDay)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedWeekDays = if (isSelected) {
                                                selectedWeekDays - calDay
                                            } else {
                                                selectedWeekDays + calDay
                                            }
                                        },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = TaskBlue.copy(alpha = 0.2f),
                                            selectedLabelColor = TaskBlue
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // MONTHLY: Full 1 to 31 Day Selection Grid (Choose ANY days from the whole month)
                    if (selectedFrequency == RecurrenceFrequency.MONTHLY) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Choose days of the month (1–31)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (selectedMonthDays.isEmpty()) {
                                            "Select any days of the month:"
                                        } else {
                                            "Selected (${selectedMonthDays.size}): ${selectedMonthDays.sorted().joinToString(", ")}"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selectedMonthDays.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else TaskBlue
                                    )
                                }

                                if (selectedMonthDays.isNotEmpty()) {
                                    TextButton(
                                        onClick = { selectedMonthDays = emptySet() },
                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                    ) {
                                        Text("Clear", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }

                            // Quick helper action chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AssistChip(
                                    onClick = { selectedMonthDays = (1..31).toSet() },
                                    label = { Text("All (1–31)", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                                )
                                AssistChip(
                                    onClick = { selectedMonthDays = (1..31 step 2).toSet() },
                                    label = { Text("Odd days", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                                )
                                AssistChip(
                                    onClick = { selectedMonthDays = (2..31 step 2).toSet() },
                                    label = { Text("Even days", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                                )
                                AssistChip(
                                    onClick = { selectedMonthDays = setOf(1, 15) },
                                    label = { Text("1st & 15th", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                                )
                                AssistChip(
                                    onClick = { selectedMonthDays = setOf(1, 10, 20) },
                                    label = { Text("1st, 10th, 20th", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                                )
                                AssistChip(
                                    onClick = { selectedMonthDays = setOf(31) },
                                    label = { Text("Last day (31st)", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false) }
                                )
                            }

                            // Full Calendar 1–31 Grid (7 columns)
                            val allDays = (1..31).toList()
                            val chunkedDays = allDays.chunked(7)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                chunkedDays.forEach { rowDays ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        rowDays.forEach { dayNum ->
                                            val isSelected = selectedMonthDays.contains(dayNum)
                                            Box(
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isSelected) TaskBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                    )
                                                    .clickable {
                                                        selectedMonthDays = if (isSelected) {
                                                            selectedMonthDays - dayNum
                                                        } else {
                                                            selectedMonthDays + dayNum
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$dayNum",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        repeat(7 - rowDays.size) {
                                            Spacer(modifier = Modifier.size(38.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Summary preview
                    val summaryDays = when (selectedFrequency) {
                        RecurrenceFrequency.WEEKLY -> {
                            if (selectedWeekDays.isNotEmpty()) {
                                val names = weekDaysList.filter { selectedWeekDays.contains(it.first) }.joinToString { it.second }
                                " on $names"
                            } else ""
                        }
                        RecurrenceFrequency.MONTHLY -> {
                            if (selectedMonthDays.isNotEmpty()) {
                                val daysStr = selectedMonthDays.sorted().joinToString(", ") { "$it" }
                                " on day(s) $daysStr"
                            } else ""
                        }
                        else -> ""
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = null,
                                tint = TaskBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Repeats every $interval $unitName$summaryDays",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val daysOfWeekStr = selectedWeekDays.sorted().joinToString(",")
                    val daysOfMonthStr = selectedMonthDays.sorted().joinToString(",")
                    onSaveRecurrence(
                        selectedFrequency,
                        interval,
                        timesPerPeriod,
                        daysOfWeekStr,
                        daysOfMonthStr
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = TaskBlue),
                modifier = Modifier.testTag("save_recurrence_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_recurrence_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
