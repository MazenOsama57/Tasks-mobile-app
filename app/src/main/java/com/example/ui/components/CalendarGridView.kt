package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskWithSubtasks
import com.example.ui.theme.TaskBlue
import com.example.ui.util.TaskDateUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarGridView(
    allTasks: List<TaskWithSubtasks>,
    selectedDateMillis: Long,
    onSelectDate: (Long) -> Unit,
    onAddTaskForDate: (Long) -> Unit,
    onTaskClick: (TaskWithSubtasks) -> Unit,
    onToggleComplete: (TaskWithSubtasks) -> Unit,
    onToggleStar: (TaskWithSubtasks) -> Unit,
    onStartFocusSession: (TaskWithSubtasks) -> Unit,
    onToggleSubtask: (Subtask) -> Unit,
    onDeleteSubtask: (Long) -> Unit,
    onAddSubtask: (taskId: Long, title: String) -> Unit,
    expandedTaskIds: Set<Long>,
    onToggleExpand: (Long) -> Unit,
    onDeleteTask: ((TaskWithSubtasks) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    // Calendar state anchored to year and month
    var currentYear by remember {
        mutableIntStateOf(Calendar.getInstance().apply { timeInMillis = selectedDateMillis }.get(Calendar.YEAR))
    }
    var currentMonth by remember {
        mutableIntStateOf(Calendar.getInstance().apply { timeInMillis = selectedDateMillis }.get(Calendar.MONTH))
    }

    val monthCalendar = remember(currentYear, currentMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    val todayCalendar = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val todayMillis = todayCalendar.timeInMillis

    val monthTitle = remember(monthCalendar) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCalendar.time)
    }

    // Map tasks to their scheduled day timestamps (normalized to start of day)
    val tasksByDay = remember(allTasks) {
        val map = mutableMapOf<Long, MutableList<TaskWithSubtasks>>()
        allTasks.forEach { taskWithSubs ->
            val due = taskWithSubs.task.dueDateMillis
            if (due != null) {
                val dayStart = TaskDateUtils.toStartOfDayMillis(due)
                map.getOrPut(dayStart) { mutableListOf() }.add(taskWithSubs)
            }
        }
        map
    }

    // Normalized selected date at start of day
    val selectedDayStart = remember(selectedDateMillis) {
        TaskDateUtils.toStartOfDayMillis(selectedDateMillis)
    }

    val selectedDayTasks = tasksByDay[selectedDayStart] ?: emptyList()

    val selectedDateFormatted = remember(selectedDayStart) {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDayStart))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_grid_view"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            // Month Navigation Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Month title and arrows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentMonth == 0) {
                                    currentMonth = 11
                                    currentYear -= 1
                                } else {
                                    currentMonth -= 1
                                }
                            },
                            modifier = Modifier.testTag("calendar_prev_month_btn")
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
                        }

                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("calendar_month_title")
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Today Jump Button
                            TextButton(
                                onClick = {
                                    val now = Calendar.getInstance()
                                    currentYear = now.get(Calendar.YEAR)
                                    currentMonth = now.get(Calendar.MONTH)
                                    onSelectDate(todayMillis)
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("calendar_today_btn")
                            ) {
                                Text("Today", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = {
                                    if (currentMonth == 11) {
                                        currentMonth = 0
                                        currentYear += 1
                                    } else {
                                        currentMonth += 1
                                    }
                                },
                                modifier = Modifier.testTag("calendar_next_month_btn")
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Days of Week Header (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLetter ->
                            Text(
                                text = dayLetter,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Days Grid (Calculated for current month)
                    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                    val firstDayOfWeek = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
                    val totalSlots = firstDayOfWeek + daysInMonth
                    val rows = (totalSlots + 6) / 7

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (rowIndex in 0 until rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (colIndex in 0..6) {
                                    val slotIndex = rowIndex * 7 + colIndex
                                    val dayNumber = slotIndex - firstDayOfWeek + 1

                                    if (dayNumber in 1..daysInMonth) {
                                        val dayCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, currentYear)
                                            set(Calendar.MONTH, currentMonth)
                                            set(Calendar.DAY_OF_MONTH, dayNumber)
                                            set(Calendar.HOUR_OF_DAY, 0)
                                            set(Calendar.MINUTE, 0)
                                            set(Calendar.SECOND, 0)
                                            set(Calendar.MILLISECOND, 0)
                                        }
                                        val cellDateMillis = dayCal.timeInMillis
                                        val isSelected = cellDateMillis == selectedDayStart
                                        val isToday = cellDateMillis == todayMillis
                                        val dayTasks = tasksByDay[cellDateMillis] ?: emptyList()
                                        val hasTasks = dayTasks.isNotEmpty()
                                        val allCompleted = hasTasks && dayTasks.all { it.task.isCompleted }

                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isSelected -> TaskBlue
                                                        isToday -> TaskBlue.copy(alpha = 0.2f)
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .border(
                                                    width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                                    color = if (isToday && !isSelected) TaskBlue else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable { onSelectDate(cellDateMillis) }
                                                .testTag("calendar_day_$dayNumber"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "$dayNumber",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        isToday -> TaskBlue
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )

                                                // Task indicator dots
                                                if (hasTasks) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 1.dp)
                                                    ) {
                                                        if (allCompleted) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (isSelected) Color.White else Color(0xFF10B981))
                                                            )
                                                        } else {
                                                            val topPriority = dayTasks.filter { !it.task.isCompleted }
                                                                .maxByOrNull { it.task.priority.weight }?.task?.priority ?: Priority.NONE

                                                            val dotColor = when {
                                                                isSelected -> Color.White
                                                                topPriority == Priority.HIGH -> Color(0xFFEF4444)
                                                                topPriority == Priority.MEDIUM -> Color(0xFFF59E0B)
                                                                else -> TaskBlue
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(4.dp)
                                                                    .clip(CircleShape)
                                                                    .background(dotColor)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Empty cell for alignment
                                        Spacer(modifier = Modifier.size(42.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Day Inspector Header
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedDateFormatted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedDayTasks.isEmpty()) "No tasks scheduled" else "${selectedDayTasks.size} task(s) scheduled",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { onAddTaskForDate(selectedDayStart) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("calendar_add_task_day_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add task", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // List of tasks due on this day
        if (selectedDayTasks.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(TaskBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            tint = TaskBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tasks due on this day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap '+ Add task' above to schedule something for this date.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(selectedDayTasks, key = { it.task.id }) { taskWithSubtasks ->
                val isExpanded = expandedTaskIds.contains(taskWithSubtasks.task.id)
                TaskItem(
                    taskWithSubtasks = taskWithSubtasks,
                    isExpanded = isExpanded,
                    onToggleExpand = { onToggleExpand(taskWithSubtasks.task.id) },
                    onToggleComplete = { onToggleComplete(taskWithSubtasks) },
                    onToggleStar = { onToggleStar(taskWithSubtasks) },
                    onToggleSubtask = onToggleSubtask,
                    onDeleteSubtask = onDeleteSubtask,
                    onAddSubtask = { title -> onAddSubtask(taskWithSubtasks.task.id, title) },
                    onStartFocusSession = { onStartFocusSession(taskWithSubtasks) },
                    onDeleteTask = onDeleteTask?.let { { it(taskWithSubtasks) } },
                    onClick = { onTaskClick(taskWithSubtasks) }
                )
            }
        }
    }
}
