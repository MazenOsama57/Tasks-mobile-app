package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.model.Priority
import com.example.data.model.RecurrenceFrequency
import com.example.data.model.Subtask
import com.example.data.model.Task
import com.example.data.model.TaskList
import com.example.data.model.TaskWithSubtasks
import com.example.ui.theme.StarActiveColor
import com.example.ui.theme.TaskBlue
import com.example.ui.util.HapticFeedbackHelper
import com.example.ui.util.TaskDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskWithSubtasks: TaskWithSubtasks,
    lists: List<TaskList>,
    onBack: () -> Unit,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onAddSubtask: (taskId: Long, title: String) -> Unit,
    onToggleSubtask: (Subtask) -> Unit,
    onDeleteSubtask: (subtaskId: Long) -> Unit,
    onStartFocusSession: (TaskWithSubtasks) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    fun handleBack() {
        keyboardController?.hide()
        focusManager.clearFocus()
        onBack()
    }

    BackHandler { handleBack() }

    val task = taskWithSubtasks.task
    val subtasks = taskWithSubtasks.subtasks
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    var isCompleted by remember(task.id) { mutableStateOf(task.isCompleted) }
    var title by remember(task.id) { mutableStateOf(task.title) }
    var details by remember(task.id) { mutableStateOf(task.details) }
    var selectedDateMillis by remember(task.id) { mutableStateOf(task.dueDateMillis) }
    var selectedPriority by remember(task.id) { mutableStateOf(task.priority) }
    var recurrenceFrequency by remember(task.id) { mutableStateOf(task.recurrenceFrequency) }
    var recurrenceInterval by remember(task.id) { mutableIntStateOf(task.recurrenceInterval) }
    var recurrenceTimesPerPeriod by remember(task.id) { mutableIntStateOf(task.recurrenceTimesPerPeriod) }
    var recurrenceDaysOfWeek by remember(task.id) { mutableStateOf(task.recurrenceDaysOfWeek) }
    var recurrenceDaysOfMonth by remember(task.id) { mutableStateOf(task.recurrenceDaysOfMonth) }
    var isStarred by remember(task.id) { mutableStateOf(task.isStarred) }
    var currentListId by remember(task.id) { mutableStateOf(task.listId) }
    var tags by remember(task.id) { mutableStateOf(task.tags) }
    var customTagInput by remember { mutableStateOf("") }
    var showCustomTagInput by remember { mutableStateOf(false) }
    val presetTags = remember { listOf("Work", "Personal", "Urgent", "Study", "Health", "Finance") }

    var newSubtaskTitle by remember { mutableStateOf("") }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showRecurrenceDialog by remember { mutableStateOf(false) }
    var showListMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isFutureRepeating = !isCompleted && recurrenceFrequency != RecurrenceFrequency.NONE && TaskDateUtils.isFutureDate(selectedDateMillis)
    val isLocked = isFutureRepeating

    fun syncTask(
        newTitle: String = title,
        newDetails: String = details,
        newDate: Long? = selectedDateMillis,
        newPriority: Priority = selectedPriority,
        newRecurrence: RecurrenceFrequency = recurrenceFrequency,
        newInterval: Int = recurrenceInterval,
        newTimes: Int = recurrenceTimesPerPeriod,
        newDaysOfWeek: String = recurrenceDaysOfWeek,
        newDaysOfMonth: String = recurrenceDaysOfMonth,
        newStarred: Boolean = isStarred,
        newListId: Long = currentListId,
        newTags: String = tags
    ) {
        onUpdateTask(
            task.copy(
                title = newTitle,
                details = newDetails,
                dueDateMillis = newDate,
                priority = newPriority,
                recurrenceFrequency = newRecurrence,
                recurrenceInterval = newInterval,
                recurrenceTimesPerPeriod = newTimes,
                recurrenceDaysOfWeek = newDaysOfWeek,
                recurrenceDaysOfMonth = newDaysOfMonth,
                isStarred = newStarred,
                listId = newListId,
                tags = newTags
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box {
                        val listName = lists.find { it.id == currentListId }?.name ?: "My Tasks"
                        Surface(
                            onClick = { showListMenu = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = listName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Outlined.ArrowDropDown,
                                    contentDescription = "Switch list",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showListMenu,
                            onDismissRequest = { showListMenu = false }
                        ) {
                            lists.forEach { list ->
                                DropdownMenuItem(
                                    text = { Text(list.name) },
                                    onClick = {
                                        currentListId = list.id
                                        syncTask(newListId = list.id)
                                        showListMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = ::handleBack,
                        modifier = Modifier.testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val newStar = !isStarred
                            isStarred = newStar
                            syncTask(newStarred = newStar)
                        },
                        modifier = Modifier.testTag("detail_star_button")
                    ) {
                        Icon(
                            imageVector = if (isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Star",
                            tint = if (isStarred) StarActiveColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("detail_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete task",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = when {
                                isCompleted -> "Completed"
                                isLocked -> "Scheduled for future"
                                else -> "Active task"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isLocked) {
                            Text(
                                text = "Repeating task scheduled for ${TaskDateUtils.formatDueDate(selectedDateMillis, task.hasTime)}. Can be completed when that day arrives.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (isLocked) return@Button
                            HapticFeedbackHelper.performTaskCompleteHaptic(context, hapticFeedback)
                            val newCompleted = !isCompleted
                            isCompleted = newCompleted
                            onUpdateTask(
                                task.copy(
                                    isCompleted = newCompleted,
                                    completedAt = if (newCompleted) System.currentTimeMillis() else null
                                )
                            )
                        },
                        enabled = !isLocked,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isLocked -> MaterialTheme.colorScheme.surfaceVariant
                                isCompleted -> MaterialTheme.colorScheme.surfaceVariant
                                else -> TaskBlue
                            },
                            contentColor = when {
                                isLocked -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> Color.White
                            }
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("detail_mark_completed_button")
                    ) {
                        Icon(
                            imageVector = when {
                                isLocked -> Icons.Outlined.Lock
                                isCompleted -> Icons.Filled.CheckCircle
                                else -> Icons.Outlined.CheckCircle
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            when {
                                isLocked -> "Scheduled"
                                isCompleted -> "Mark uncompleted"
                                else -> "Mark completed"
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Title Input
                TextField(
                    value = title,
                    onValueChange = {
                        title = it
                        syncTask(newTitle = it)
                    },
                    placeholder = { Text("Task title", style = MaterialTheme.typography.headlineSmall) },
                    singleLine = false,
                    maxLines = 5,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detail_title_input")
                )
            }

            item {
                // Details / Notes Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = details,
                        onValueChange = {
                            details = it
                            syncTask(newDetails = it)
                        },
                        placeholder = { Text("Add details") },
                        singleLine = false,
                        maxLines = 10,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_details_input")
                    )
                }
            }

            if (!isCompleted) {
                item {
                    Surface(
                        onClick = { onStartFocusSession(taskWithSubtasks) },
                        shape = RoundedCornerShape(14.dp),
                        color = TaskBlue.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, TaskBlue.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_start_focus_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(TaskBlue.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.HourglassTop,
                                        contentDescription = null,
                                        tint = TaskBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Start Pomodoro Focus",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TaskBlue
                                    )
                                    Text(
                                        text = "Focus work session with progress timer",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Start",
                                tint = TaskBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            // Due Date Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Event,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    if (selectedDateMillis == null) {
                        OutlinedButton(
                            onClick = { showDatePickerDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("detail_add_date_btn")
                        ) {
                            Text("Set due date")
                        }
                    } else {
                        val isOverdue = !task.isCompleted && TaskDateUtils.isOverdue(selectedDateMillis)
                        InputChip(
                            selected = true,
                            onClick = { showDatePickerDialog = true },
                            label = {
                                Text(
                                    text = TaskDateUtils.formatDueDate(selectedDateMillis),
                                    color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        selectedDateMillis = null
                                        syncTask(newDate = null)
                                    },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear date",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Repeat / Recurrence Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    if (recurrenceFrequency == RecurrenceFrequency.NONE) {
                        OutlinedButton(
                            onClick = { showRecurrenceDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("detail_set_repeat_btn")
                        ) {
                            Text("Repeat")
                        }
                    } else {
                        InputChip(
                            selected = true,
                            onClick = { showRecurrenceDialog = true },
                            label = {
                                Text(
                                    text = recurrenceFrequency.getSummaryText(
                                        recurrenceInterval,
                                        recurrenceTimesPerPeriod
                                    ),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        recurrenceFrequency = RecurrenceFrequency.NONE
                                        syncTask(newRecurrence = RecurrenceFrequency.NONE)
                                    },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear repeat",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Priority Selection Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Priority Level",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Priority.entries.forEach { priority ->
                                val isSelected = selectedPriority == priority
                                val chipColor = priority.getColor(isDark)

                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedPriority = priority
                                        syncTask(newPriority = priority)
                                    },
                                    label = {
                                        Text(
                                            text = priority.displayName,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected && priority != Priority.NONE) chipColor else Color.Unspecified,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (priority != Priority.NONE) priority.getContainerColor(isDark) else MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    modifier = Modifier.testTag("detail_priority_${priority.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            // Tags & Categorization Section
            item {
                val activeTagList = tags.split(",")
                    .map { it.trim().removePrefix("#") }
                    .filter { it.isNotEmpty() }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Tags & Categories",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Existing tags chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        activeTagList.forEach { tagItem ->
                            FilterChip(
                                selected = true,
                                onClick = {
                                    val updated = activeTagList.filter { it != tagItem }.joinToString(",")
                                    tags = updated
                                    syncTask(newTags = updated)
                                },
                                label = { Text("#$tagItem") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Remove tag",
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }

                        // Presets not yet added
                        presetTags.filter { it !in activeTagList }.forEach { preset ->
                            AssistChip(
                                onClick = {
                                    val updated = (activeTagList + preset).joinToString(",")
                                    tags = updated
                                    syncTask(newTags = updated)
                                },
                                label = { Text("+$preset", style = MaterialTheme.typography.labelSmall) }
                            )
                        }

                        // Add custom tag chip
                        if (!showCustomTagInput) {
                            AssistChip(
                                onClick = { showCustomTagInput = true },
                                label = { Text("+ Custom Tag", style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.testTag("detail_add_tag_chip")
                            )
                        }
                    }

                    if (showCustomTagInput) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customTagInput,
                                onValueChange = { customTagInput = it },
                                placeholder = { Text("Tag name (e.g. ProjectX)", style = MaterialTheme.typography.bodySmall) },
                                prefix = { Text("#", style = MaterialTheme.typography.bodySmall) },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("detail_custom_tag_input")
                            )
                            Button(
                                onClick = {
                                    val clean = customTagInput.trim().removePrefix("#")
                                    if (clean.isNotEmpty()) {
                                        val updated = if (activeTagList.contains(clean)) tags else (activeTagList + clean).joinToString(",")
                                        tags = updated
                                        syncTask(newTags = updated)
                                        customTagInput = ""
                                        showCustomTagInput = false
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Add")
                            }
                            IconButton(onClick = { showCustomTagInput = false; customTagInput = "" }) {
                                Icon(imageVector = Icons.Filled.Close, contentDescription = "Cancel tag")
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            // Subtasks Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Checklist,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Subtasks (${subtasks.count { it.isCompleted }}/${subtasks.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Subtasks list with hierarchical indentation
            items(subtasks, key = { it.id }) { subtask ->
                val isChild = subtask.parentSubtaskId != null
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = if (isChild) 20.dp else 0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
            }

            // Add Subtask input row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = newSubtaskTitle,
                        onValueChange = { newSubtaskTitle = it },
                        placeholder = { Text("Add subtask") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_subtask_input"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (newSubtaskTitle.isNotBlank()) {
                                onAddSubtask(task.id, newSubtaskTitle)
                                newSubtaskTitle = ""
                            }
                        })
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newSubtaskTitle.isNotBlank()) {
                                onAddSubtask(task.id, newSubtaskTitle)
                                newSubtaskTitle = ""
                            }
                        },
                        enabled = newSubtaskTitle.isNotBlank(),
                        modifier = Modifier.testTag("add_subtask_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add subtask",
                            tint = if (newSubtaskTitle.isNotBlank()) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val date = datePickerState.selectedDateMillis
                        selectedDateMillis = date
                        syncTask(newDate = date)
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Recurrence Dialog
    RecurrenceDialog(
        isOpen = showRecurrenceDialog,
        currentFrequency = recurrenceFrequency,
        currentInterval = recurrenceInterval,
        currentTimesPerPeriod = recurrenceTimesPerPeriod,
        currentDaysOfWeek = recurrenceDaysOfWeek,
        currentDaysOfMonth = recurrenceDaysOfMonth,
        onDismiss = { showRecurrenceDialog = false },
        onSaveRecurrence = { freq, interval, times, daysOfWeek, daysOfMonth ->
            recurrenceFrequency = freq
            recurrenceInterval = interval
            recurrenceTimesPerPeriod = times
            recurrenceDaysOfWeek = daysOfWeek
            recurrenceDaysOfMonth = daysOfMonth
            syncTask(
                newRecurrence = freq,
                newInterval = interval,
                newTimes = times,
                newDaysOfWeek = daysOfWeek,
                newDaysOfMonth = daysOfMonth
            )
            showRecurrenceDialog = false
        }
    )

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete task?") },
            text = { Text("This will permanently remove '${task.title}' and its subtasks.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteTask(task)
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
