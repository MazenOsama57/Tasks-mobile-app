package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.RecurrenceFrequency
import com.example.data.model.Subtask
import com.example.data.model.TaskWithSubtasks
import com.example.data.model.getTagList
import com.example.ui.theme.StarActiveColor
import com.example.ui.theme.TaskBlue
import com.example.ui.util.HapticFeedbackHelper
import com.example.ui.util.TaskDateUtils

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun TaskItem(
    taskWithSubtasks: TaskWithSubtasks,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleComplete: () -> Unit,
    onToggleStar: () -> Unit,
    onToggleSubtask: (Subtask) -> Unit,
    onDeleteSubtask: (Long) -> Unit,
    onAddSubtask: (String) -> Unit,
    onClick: () -> Unit,
    isLockedForCompletion: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongClick: () -> Unit = {},
    onToggleSelect: () -> Unit = {},
    onStartFocusSession: () -> Unit = {},
    onDeleteTask: (() -> Unit)? = null,
    onTagClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val task = taskWithSubtasks.task
    val subtasks = taskWithSubtasks.subtasks
    val isCompleted = task.isCompleted
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    val completedSubtasksCount = remember(subtasks) { subtasks.count { it.isCompleted } }
    val totalSubtasksCount = subtasks.size
    val isOverdue = remember(task.dueDateMillis, isCompleted) {
        !isCompleted && TaskDateUtils.isOverdue(task.dueDateMillis)
    }
    val isFutureRepeating = remember(task.dueDateMillis, task.recurrenceFrequency, isCompleted) {
        !isCompleted && task.recurrenceFrequency != RecurrenceFrequency.NONE && TaskDateUtils.isFutureDate(task.dueDateMillis)
    }
    val isLocked = isLockedForCompletion || isFutureRepeating

    var inlineSubtaskText by remember { mutableStateOf("") }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val itemBgColor = when {
        isSelected -> TaskBlue.copy(alpha = 0.14f)
        isCompleted -> surfaceColor
        else -> surfaceColor
    }
    val itemBorder = when {
        isSelected -> BorderStroke(2.dp, TaskBlue)
        else -> BorderStroke(1.dp, if (isDark) Color(0xFF2C2D30) else Color(0xFFE8EAED))
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        HapticFeedbackHelper.performSelectionToggleHaptic(context, hapticFeedback)
                        onToggleSelect()
                    } else {
                        onClick()
                    }
                },
                onLongClick = {
                    HapticFeedbackHelper.performSelectionToggleHaptic(context, hapticFeedback)
                    onLongClick()
                }
            )
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        color = itemBgColor,
        tonalElevation = if (isCompleted) 0.dp else 1.dp,
        border = itemBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // If in selection mode, show select checkbox
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(48.dp)
                            .clickable {
                                HapticFeedbackHelper.performSelectionToggleHaptic(context, hapticFeedback)
                                onToggleSelect()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                            contentDescription = if (isSelected) "Selected" else "Not selected",
                            tint = if (isSelected) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    // Checkbox Circle (Signature circular check)
                    TaskCheckCircle(
                        isCompleted = isCompleted,
                        isLocked = isLocked,
                        onClick = {
                            if (!isLocked) {
                                HapticFeedbackHelper.performTaskCompleteHaptic(context, hapticFeedback)
                            }
                            onToggleComplete()
                        },
                        testTag = "checkbox_${task.id}"
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Main task content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                ) {
                    // Task Title
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Task Details Preview
                    if (task.details.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (isCompleted) 0.5f else 0.8f
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Chips Row: Due Date, Priority, Recurrence, Tags, Subtasks
                    val hasChips = task.dueDateMillis != null ||
                                   task.priority != Priority.NONE ||
                                   task.recurrenceFrequency != RecurrenceFrequency.NONE ||
                                   task.tags.isNotBlank() ||
                                   totalSubtasksCount > 0

                    if (hasChips) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Due Date Chip
                            if (task.dueDateMillis != null) {
                                val dateText = remember(task.dueDateMillis, task.hasTime) {
                                    TaskDateUtils.formatDueDate(task.dueDateMillis, task.hasTime)
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        isCompleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                                        else -> TaskBlue.copy(alpha = 0.12f)
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (task.hasTime) Icons.Outlined.Schedule else Icons.Outlined.Event,
                                            contentDescription = "Due Date",
                                            tint = when {
                                                isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                                                isOverdue -> MaterialTheme.colorScheme.error
                                                else -> TaskBlue
                                            },
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = dateText,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isOverdue && !isCompleted) FontWeight.Bold else FontWeight.Medium
                                            ),
                                            color = when {
                                                isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                                                isOverdue -> MaterialTheme.colorScheme.error
                                                else -> TaskBlue
                                            },
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Priority Chip
                            if (task.priority != Priority.NONE) {
                                val priorityColor = task.priority.getColor(isDark)
                                val containerColor = task.priority.getContainerColor(isDark)

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isCompleted) containerColor.copy(alpha = 0.5f) else containerColor
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isCompleted) priorityColor.copy(alpha = 0.5f) else priorityColor)
                                        )
                                        Text(
                                            text = task.priority.displayName,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.SemiBold
                                            ),
                                            color = if (isCompleted) priorityColor.copy(alpha = 0.6f) else priorityColor,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            // Recurrence Chip
                            if (task.recurrenceFrequency != RecurrenceFrequency.NONE) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = TaskBlue.copy(alpha = 0.08f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Repeat,
                                            contentDescription = "Repeats",
                                            tint = TaskBlue,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = task.recurrenceFrequency.getSummaryText(
                                                task.recurrenceInterval,
                                                task.recurrenceTimesPerPeriod
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TaskBlue,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Tags Chips
                            val tagList = remember(task.tags) { task.getTagList() }
                            tagList.forEach { tag ->
                                Surface(
                                    onClick = { onTagClick?.invoke(tag) },
                                    enabled = onTagClick != null,
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isCompleted) 0.35f else 0.7f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "#$tag",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onPrimaryContainer,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            // Subtasks Expand/Collapse Toggle Chip
                            if (totalSubtasksCount > 0) {
                                Surface(
                                    onClick = onToggleExpand,
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isExpanded) TaskBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.testTag("expand_subtasks_${task.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Checklist,
                                            contentDescription = "Subtasks",
                                            tint = if (isExpanded) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "$completedSubtasksCount/$totalSubtasksCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isExpanded) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                                            tint = if (isExpanded) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Focus Timer & Star Buttons
                if (!isSelectionMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isCompleted) {
                            IconButton(
                                onClick = onStartFocusSession,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("focus_button_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.HourglassTop,
                                    contentDescription = "Start Focus Session",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleStar,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("star_button_${task.id}")
                        ) {
                            Icon(
                                imageVector = if (task.isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                contentDescription = if (task.isStarred) "Starred" else "Not starred",
                                tint = if (task.isStarred) StarActiveColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        if (onDeleteTask != null) {
                            IconButton(
                                onClick = {
                                    HapticFeedbackHelper.performTaskDeleteHaptic(context, hapticFeedback)
                                    onDeleteTask()
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("delete_task_button_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Delete task",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.75f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Subtasks Hierarchical Accordion View (Expand/Collapse)
            AnimatedVisibility(
                visible = isExpanded && totalSubtasksCount > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 28.dp, top = 8.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Subtask List
                    subtasks.forEach { subtask ->
                        SubtaskItem(
                            subtask = subtask,
                            isLocked = isLocked,
                            onToggleComplete = {
                                if (!isLocked) {
                                    onToggleSubtask(subtask)
                                }
                            },
                            onDelete = { onDeleteSubtask(subtask.id) }
                        )
                    }

                    // Inline Subtask Add Field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = inlineSubtaskText,
                            onValueChange = { inlineSubtaskText = it },
                            placeholder = {
                                Text(
                                    text = "Add a subtask",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 36.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(
                            onClick = {
                                if (inlineSubtaskText.isNotBlank()) {
                                    onAddSubtask(inlineSubtaskText)
                                    inlineSubtaskText = ""
                                }
                            },
                            enabled = inlineSubtaskText.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add subtask",
                                tint = if (inlineSubtaskText.isNotBlank()) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCheckCircle(
    isCompleted: Boolean,
    isLocked: Boolean = false,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val bgColor by animateColorAsState(
        targetValue = when {
            isCompleted -> TaskBlue
            isLocked -> if (isDark) Color(0xFF35383F) else Color(0xFFF1F3F4)
            else -> Color.Transparent
        },
        label = "checkBgColor"
    )
    val borderColor = when {
        isCompleted -> TaskBlue
        isLocked -> if (isDark) Color(0xFF5F6368) else Color(0xFFBDC1C6)
        else -> if (isDark) Color(0xFF74777F) else Color(0xFF747775)
    }

    Box(
        modifier = modifier
            .padding(top = 2.dp)
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(bgColor)
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isCompleted -> {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                isLocked -> {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Locked until scheduled date",
                        tint = if (isDark) Color(0xFF9AA0A6) else Color(0xFF5F6368),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
