package com.example.ui.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.HabitWithLogs
import com.example.ui.theme.TaskBlue
import com.example.ui.util.HapticFeedbackHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HabitStreakCalendarDialog(
    habitWithLogs: HabitWithLogs?, // null = view overall habits streak calendar
    allHabits: List<HabitWithLogs>,
    onToggleHabitDate: (habitId: Long, dateString: String, isCurrentlyCompleted: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val isOverall = habitWithLogs == null

    val habitColor = if (habitWithLogs != null) Color(habitWithLogs.habit.colorHex) else Color(0xFFF97316)
    val todayCal = remember { Calendar.getInstance() }
    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(todayCal.time)
    }

    // Selected month state
    var displayedCalendar by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        )
    }

    // Selected date for day expansion / detailed breakdown
    var selectedDateString by remember { mutableStateOf(todayDateStr) }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val fullDateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }

    // Month calculations
    val daysInMonth = displayedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (displayedCalendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7 // 0 = Mon ... 6 = Sun

    val displayedMonthLabel = monthYearFormat.format(displayedCalendar.time)

    // Current month check
    val isCurrentMonth = todayCal.get(Calendar.YEAR) == displayedCalendar.get(Calendar.YEAR) &&
            todayCal.get(Calendar.MONTH) == displayedCalendar.get(Calendar.MONTH)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("habit_streak_calendar_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
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
                            shape = CircleShape,
                            color = habitColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isOverall) Icons.Filled.LocalFireDepartment else when (habitWithLogs?.habit?.iconName) {
                                        "wb_sunny" -> Icons.Filled.WbSunny
                                        "fitness_center" -> Icons.Filled.FitnessCenter
                                        "menu_book" -> Icons.Filled.MenuBook
                                        "water_drop" -> Icons.Filled.WaterDrop
                                        "self_improvement" -> Icons.Filled.SelfImprovement
                                        "hourglass_top" -> Icons.Filled.HourglassTop
                                        "fact_check" -> Icons.Filled.FactCheck
                                        else -> Icons.Filled.LocalFireDepartment
                                    },
                                    contentDescription = null,
                                    tint = habitColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isOverall) "Habits Streak Calendar" else "${habitWithLogs?.habit?.title} Streak",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isOverall) {
                                    val (streak, _) = HabitWithLogs.calculateAllHabitsStreak(allHabits)
                                    val best = HabitWithLogs.calculateAllHabitsRecordStreak(allHabits)
                                    "$streak d master streak • Best: ${best}d"
                                } else {
                                    "${habitWithLogs?.currentStreak ?: 0}d streak • Best: ${habitWithLogs?.longestStreak ?: 0}d • ${habitWithLogs?.logs?.size ?: 0} check-ins"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("streak_calendar_close_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Legend indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Text("Active 🔥", style = MaterialTheme.typography.labelSmall)
                    }

                    if (isOverall) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF59E0B))
                            )
                            Text("Partial ⚡", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF94A3B8))
                        )
                        Text("Lazy / Inactive 💤", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Month navigation bar
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                val prev = (displayedCalendar.clone() as Calendar).apply {
                                    add(Calendar.MONTH, -1)
                                }
                                displayedCalendar = prev
                            },
                            modifier = Modifier.testTag("streak_calendar_prev_month")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                        }

                        Text(
                            text = displayedMonthLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isCurrentMonth) {
                                TextButton(
                                    onClick = {
                                        displayedCalendar = Calendar.getInstance().apply {
                                            set(Calendar.DAY_OF_MONTH, 1)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        selectedDateString = todayDateStr
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Today", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            IconButton(
                                onClick = {
                                    val next = (displayedCalendar.clone() as Calendar).apply {
                                        add(Calendar.MONTH, 1)
                                    }
                                    displayedCalendar = next
                                },
                                modifier = Modifier.testTag("streak_calendar_next_month")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable container for Calendar Grid + Detailed Day Expansion
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        // Days of week header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            daysOfWeek.forEach { dayName ->
                                Text(
                                    text = dayName,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Calendar Month Grid
                        val totalCells = firstDayOfWeek + daysInMonth
                        val numRows = (totalCells + 6) / 7

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0 until numRows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    for (col in 0 until 7) {
                                        val cellIndex = row * 7 + col
                                        val dayOfMonth = cellIndex - firstDayOfWeek + 1

                                        if (dayOfMonth in 1..daysInMonth) {
                                            val dayCal = (displayedCalendar.clone() as Calendar).apply {
                                                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            }
                                            val dateStr = dayFormat.format(dayCal.time)
                                            val isToday = dateStr == todayDateStr
                                            val isFuture = dateStr > todayDateStr
                                            val isSelected = dateStr == selectedDateString

                                            // Determine Active vs Lazy status for this day
                                            val isActive: Boolean
                                            val isPartial: Boolean
                                            val isLazy: Boolean

                                            if (isFuture) {
                                                isActive = false
                                                isPartial = false
                                                isLazy = false
                                            } else if (isOverall) {
                                                val completedCount = allHabits.count { it.completedDateStrings.contains(dateStr) }
                                                val allCount = allHabits.size
                                                isActive = allCount > 0 && completedCount == allCount
                                                isPartial = completedCount in 1 until allCount
                                                isLazy = completedCount == 0
                                            } else {
                                                isActive = habitWithLogs?.completedDateStrings?.contains(dateStr) == true
                                                isPartial = false
                                                isLazy = !isActive
                                            }

                                            val cellBg = when {
                                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                isActive -> Color(0xFF10B981).copy(alpha = 0.20f)
                                                isPartial -> Color(0xFFF59E0B).copy(alpha = 0.20f)
                                                isLazy -> Color(0xFF94A3B8).copy(alpha = 0.12f)
                                                else -> Color.Transparent
                                            }

                                            val borderStroke = when {
                                                isSelected -> BorderStroke(2.dp, TaskBlue)
                                                isToday -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                                isActive -> BorderStroke(1.dp, Color(0xFF10B981))
                                                isLazy -> BorderStroke(1.dp, Color(0xFFCBD5E1))
                                                else -> null
                                            }

                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .padding(2.dp)
                                                    .clickable {
                                                        selectedDateString = dateStr
                                                        HapticFeedbackHelper.performSelectionToggleHaptic(context, hapticFeedback)
                                                    }
                                                    .testTag("streak_calendar_day_$dateStr"),
                                                shape = RoundedCornerShape(10.dp),
                                                color = cellBg,
                                                border = borderStroke
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = dayOfMonth.toString(),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = if (isSelected || isToday || isActive) FontWeight.Bold else FontWeight.Normal,
                                                            color = when {
                                                                isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                                isActive -> Color(0xFF047857)
                                                                isPartial -> Color(0xFFB45309)
                                                                isLazy -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                                else -> MaterialTheme.colorScheme.onSurface
                                                            }
                                                        )

                                                        if (!isFuture) {
                                                            if (isActive) {
                                                                Icon(
                                                                    imageVector = Icons.Filled.LocalFireDepartment,
                                                                    contentDescription = "Active Streak Day",
                                                                    tint = Color(0xFF10B981),
                                                                    modifier = Modifier.size(11.dp)
                                                                )
                                                            } else if (isPartial) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(5.dp)
                                                                        .clip(CircleShape)
                                                                        .background(Color(0xFFF59E0B))
                                                                )
                                                            } else if (isLazy) {
                                                                Text(
                                                                    text = "💤",
                                                                    fontSize = 8.sp,
                                                                    lineHeight = 10.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Detailed Day Expansion Section
                    item {
                        val parsedCal = Calendar.getInstance().apply {
                            try {
                                time = dayFormat.parse(selectedDateString) ?: Date()
                            } catch (_: Exception) {}
                        }
                        val formattedSelectedDay = fullDateFormat.format(parsedCal.time)
                        val isSelectedDayFuture = selectedDateString > todayDateStr

                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("streak_calendar_day_detail")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = formattedSelectedDay,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (selectedDateString == todayDateStr) "Today" else "Historical day record",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Day Status Badge
                                    if (isSelectedDayFuture) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = "Upcoming",
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    } else if (isOverall) {
                                        val completedCount = allHabits.count { it.completedDateStrings.contains(selectedDateString) }
                                        val total = allHabits.size
                                        val isMasterDone = total > 0 && completedCount == total
                                        val isLazy = completedCount == 0

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when {
                                                isMasterDone -> Color(0xFF10B981).copy(alpha = 0.2f)
                                                isLazy -> Color(0xFF94A3B8).copy(alpha = 0.2f)
                                                else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                            },
                                            contentColor = when {
                                                isMasterDone -> Color(0xFF047857)
                                                isLazy -> Color(0xFF475569)
                                                else -> Color(0xFFB45309)
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = when {
                                                        isMasterDone -> "🔥 Active ($completedCount/$total Done)"
                                                        isLazy -> "💤 Lazy / Rest Day (0/$total)"
                                                        else -> "⚡ Partial ($completedCount/$total Done)"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        val isDone = habitWithLogs?.completedDateStrings?.contains(selectedDateString) == true
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isDone) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFF94A3B8).copy(alpha = 0.2f),
                                            contentColor = if (isDone) Color(0xFF047857) else Color(0xFF475569)
                                        ) {
                                            Text(
                                                text = if (isDone) "🔥 Active / Completed" else "💤 Lazy / Missed Day",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Detailed Habit Breakdown for this day
                                if (isSelectedDayFuture) {
                                    Text(
                                        text = "Future day. Habits scheduled for this date will appear here.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else if (isOverall) {
                                    Text(
                                        text = "Habit check-ins for this date:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    allHabits.forEach { item ->
                                        val isCompletedOnDate = item.completedDateStrings.contains(selectedDateString)
                                        val itemColor = Color(item.habit.colorHex)

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isCompletedOnDate) itemColor else Color.Gray)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = item.habit.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isCompletedOnDate) FontWeight.SemiBold else FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            // Quick toggle button for this date
                                            IconButton(
                                                onClick = {
                                                    onToggleHabitDate(item.habit.id, selectedDateString, isCompletedOnDate)
                                                    HapticFeedbackHelper.performTaskCompleteHaptic(context, hapticFeedback)
                                                },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isCompletedOnDate) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                                    contentDescription = if (isCompletedOnDate) "Completed" else "Not completed",
                                                    tint = if (isCompletedOnDate) itemColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    val isDone = habitWithLogs?.completedDateStrings?.contains(selectedDateString) == true
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = if (isDone) {
                                                "🎉 You completed \"${habitWithLogs?.habit?.title}\" on this day and maintained your streak!"
                                            } else {
                                                "💤 This habit was not marked as completed on this day."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Button(
                                            onClick = {
                                                habitWithLogs?.let {
                                                    onToggleHabitDate(it.habit.id, selectedDateString, isDone)
                                                    HapticFeedbackHelper.performTaskCompleteHaptic(context, hapticFeedback)
                                                }
                                            },
                                            colors = if (isDone) {
                                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                            } else {
                                                ButtonDefaults.buttonColors(containerColor = habitColor, contentColor = Color.White)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth().testTag("toggle_habit_date_button")
                                        ) {
                                            Icon(
                                                imageVector = if (isDone) Icons.Filled.RemoveDone else Icons.Filled.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isDone) "Mark as Incomplete" else "Mark Completed for this Date")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
