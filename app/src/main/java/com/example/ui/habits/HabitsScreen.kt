package com.example.ui.habits

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import com.example.data.model.HabitWithLogs
import com.example.ui.util.HapticFeedbackHelper

@Composable
fun HabitsScreen(
    habits: List<HabitWithLogs>,
    onToggleHabitToday: (habitId: Long, isCurrentlyCompleted: Boolean) -> Unit,
    onToggleHabitDate: (habitId: Long, dateString: String, isCurrentlyCompleted: Boolean) -> Unit = { id, _, c ->
        onToggleHabitToday(id, c)
    },
    onAddHabitClick: () -> Unit,
    onEditHabit: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val totalCount = habits.size
    val completedTodayCount = habits.count { it.isCompletedToday() }
    val progressFraction = if (totalCount > 0) completedTodayCount.toFloat() / totalCount else 0f

    var habitForCalendar by remember { mutableStateOf<HabitWithLogs?>(null) }
    var isOverallCalendarOpen by remember { mutableStateOf(false) }

    // Master All-Habits Streak:
    // Only counts up if ALL habits on the page are completed for that day.
    val (masterStreak, allCompletedToday) = remember(habits) {
        HabitWithLogs.calculateAllHabitsStreak(habits)
    }
    val masterRecordStreak = remember(habits) {
        HabitWithLogs.calculateAllHabitsRecordStreak(habits)
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = 16.dp,
                bottom = 96.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: Daily Routines Summary Card with All-Habits Master Streak
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Daily Routines",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { isOverallCalendarOpen = true },
                                        modifier = Modifier.size(28.dp).testTag("habits_overall_calendar_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CalendarMonth,
                                            contentDescription = "Streak Calendar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    if (allCompletedToday && totalCount > 0) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                                            contentColor = Color(0xFF10B981)
                                        ) {
                                            Text(
                                                text = "100% Done",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when {
                                        totalCount == 0 -> "No habits yet"
                                        allCompletedToday -> "🎉 All $totalCount habits completed today!"
                                        masterStreak > 0 -> "$completedTodayCount of $totalCount completed today • Complete all to reach ${masterStreak + 1}d!"
                                        else -> "$completedTodayCount of $totalCount completed today • Complete all to start master streak!"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Master All-Habits Streak Indicator: only counts up if all habits on page are completed
                            Surface(
                                onClick = { isOverallCalendarOpen = true },
                                shape = RoundedCornerShape(14.dp),
                                color = if (masterStreak > 0) Color(0xFFF97316).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (masterStreak > 0) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("master_streak_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.LocalFireDepartment,
                                            contentDescription = "All Habits Master Streak",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$masterStreak day streak",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    if (masterRecordStreak > 0) {
                                        Text(
                                            text = "Best: ${masterRecordStreak}d",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }

                        if (totalCount > 0) {
                            Spacer(modifier = Modifier.height(14.dp))
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (allCompletedToday) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }

            if (habits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Build lasting habits",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Track daily routines, maintain streaks, and level up with XP.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onAddHabitClick,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Create First Habit")
                            }
                        }
                    }
                }
            } else {
                items(habits, key = { it.habit.id }) { item ->
                    HabitCardItem(
                        habitWithLogs = item,
                        onToggleToday = { isCompleted ->
                            HapticFeedbackHelper.performSelectionToggleHaptic(context, hapticFeedback)
                            onToggleHabitToday(item.habit.id, isCompleted)
                        },
                        onOpenCalendar = { habitForCalendar = item },
                        onEdit = { onEditHabit(item.habit) },
                        onDelete = { onDeleteHabit(item.habit) }
                    )
                }
            }
        }

        // Streak Record Calendar Dialog (for individual habit or overall master streak)
        if (isOverallCalendarOpen) {
            HabitStreakCalendarDialog(
                habitWithLogs = null,
                allHabits = habits,
                onToggleHabitDate = onToggleHabitDate,
                onDismiss = { isOverallCalendarOpen = false }
            )
        }

        if (habitForCalendar != null) {
            val current = habits.find { it.habit.id == habitForCalendar?.habit?.id } ?: habitForCalendar
            HabitStreakCalendarDialog(
                habitWithLogs = current,
                allHabits = habits,
                onToggleHabitDate = onToggleHabitDate,
                onDismiss = { habitForCalendar = null }
            )
        }
    }
}

@Composable
private fun HabitCardItem(
    habitWithLogs: HabitWithLogs,
    onToggleToday: (isCompleted: Boolean) -> Unit,
    onOpenCalendar: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habit = habitWithLogs.habit
    val isCompletedToday = habitWithLogs.isDoneToday
    val streak = habitWithLogs.currentStreak
    val recordStreak = habitWithLogs.longestStreak
    val totalCheckIns = habitWithLogs.logs.size
    val weekCompletion = habitWithLogs.weekCompletionList
    val habitColor = Color(habit.colorHex)

    var showMenu by remember { mutableStateOf(false) }

    val iconVector = when (habit.iconName) {
        "wb_sunny" -> Icons.Filled.WbSunny
        "fitness_center" -> Icons.Filled.FitnessCenter
        "menu_book" -> Icons.Filled.MenuBook
        "water_drop" -> Icons.Filled.WaterDrop
        "self_improvement" -> Icons.Filled.SelfImprovement
        "hourglass_top" -> Icons.Filled.HourglassTop
        "fact_check" -> Icons.Filled.FactCheck
        else -> Icons.Filled.LocalFireDepartment
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Habit Icon Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(habitColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = habitColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Individual Streak Record
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Individual Habit Streak Record (Current Streak + Best Record)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            onClick = onOpenCalendar,
                            shape = RoundedCornerShape(6.dp),
                            color = if (streak > 0) Color(0xFFF97316).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (streak > 0) Color(0xFFEA580C) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("habit_streak_pill_${habit.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$streak d streak",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (recordStreak > 0) {
                            Surface(
                                onClick = onOpenCalendar,
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEAB308).copy(alpha = 0.15f),
                                contentColor = Color(0xFFB45309),
                                modifier = Modifier.testTag("habit_record_pill_${habit.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.EmojiEvents,
                                        contentDescription = null,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Best: ${recordStreak}d",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onOpenCalendar,
                            modifier = Modifier.size(24.dp).testTag("habit_calendar_btn_${habit.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "Streak Calendar",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        Text(
                            text = "• $totalCheckIns total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Check-in Button
                IconButton(
                    onClick = { onToggleToday(isCompletedToday) },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("habit_check_${habit.id}")
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isCompletedToday) habitColor else Color.Transparent,
                        border = if (!isCompletedToday) BorderStroke(2.dp, habitColor) else null,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCompletedToday) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Completed today",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Options Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit habit") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete habit") },
                            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // 7-Day Consistency Week Heatmap / Dots
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 0.8.dp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "This week:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    weekCompletion.forEach { (dayLabel, isDone) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isDone) habitColor else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
