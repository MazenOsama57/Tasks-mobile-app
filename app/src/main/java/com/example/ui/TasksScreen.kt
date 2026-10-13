package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.achievements.AchievementsScreen
import com.example.ui.components.*
import com.example.ui.habits.AddEditHabitDialog
import com.example.ui.habits.HabitsScreen
import com.example.ui.theme.TaskBlue
import com.example.ui.util.HapticFeedbackHelper
import com.example.ui.workflows.AppBottomTab
import com.example.ui.workflows.WorkflowsScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Habit creation/editing state
    var isAddEditHabitDialogOpen by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<com.example.data.model.Habit?>(null) }

    // Android 13+ Notification Permission Launcher
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.sendTestReminder(context)
        }
    }

    // Intercept back button in batch selection mode
    BackHandler(enabled = uiState.isBatchSelectionMode) {
        viewModel.clearBatchSelection()
    }

    // Display user messages (such as locked repeating task notification)
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUserMessage()
        }
    }

    // If detail view is open, show TaskDetailScreen
    if (uiState.selectedTaskForDetail != null) {
        TaskDetailScreen(
            taskWithSubtasks = uiState.selectedTaskForDetail!!,
            lists = uiState.lists,
            onBack = { viewModel.closeTaskDetail() },
            onUpdateTask = { task -> viewModel.updateTask(task) },
            onDeleteTask = { task -> viewModel.deleteTask(task) },
            onAddSubtask = { taskId, title -> viewModel.addSubtask(taskId, title) },
            onToggleSubtask = { subtask -> viewModel.toggleSubtaskComplete(subtask) },
            onDeleteSubtask = { subtaskId -> viewModel.deleteSubtask(subtaskId) },
            onStartFocusSession = { task -> viewModel.openFocusTimer(task) }
        )
        return
    }

    Scaffold(
        topBar = {
            when (uiState.currentBottomTab) {
                AppBottomTab.TASKS -> {
                    val visibleTasks = when {
                        uiState.isCompletedView -> uiState.completedTasks
                        uiState.isUpcomingRecurringView -> uiState.upcomingRecurringTasks
                        else -> uiState.activeTasks
                    }

                    TasksTopBar(
                        currentListName = uiState.currentListName,
                        lists = uiState.lists,
                        selectedListId = uiState.selectedListId,
                        isStarredView = uiState.isStarredView,
                        isCompletedView = uiState.isCompletedView,
                        isUpcomingRecurringView = uiState.isUpcomingRecurringView,
                        isCalendarView = uiState.isCalendarView,
                        sortOrder = uiState.sortOrder,
                        isSearching = uiState.isSearching,
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onToggleSearch = { viewModel.setSearching(it) },
                        onSelectList = { viewModel.selectList(it) },
                        onSelectStarred = { viewModel.selectStarredView() },
                        onSelectCompleted = { viewModel.selectCompletedView() },
                        onSelectUpcomingRecurring = { viewModel.selectUpcomingRecurringView() },
                        onSelectCalendarView = { viewModel.selectCalendarView() },
                        onOpenCreateList = { viewModel.openCreateListDialog() },
                        onOpenRenameList = { viewModel.openRenameListDialog(it) },
                        onOpenDeleteList = { viewModel.openDeleteListDialog(it) },
                        onOpenSortDialog = { viewModel.toggleSortDialog(true) },
                        onOpenThemeDialog = { viewModel.toggleThemeDialog(true) },
                        onOpenAutoClearDialog = { viewModel.toggleAutoClearDialog(true) },
                        onOpenReminderDialog = { viewModel.openReminderDialog() },
                        onOpenSettingsDialog = { viewModel.openSettingsDialog() },
                        onOpenFocusTimer = { viewModel.openFocusTimer() },
                        onOpenAnalytics = { viewModel.openAnalyticsDialog() },
                        onClearCompleted = { viewModel.clearCompletedTasks() },
                        // Multi-select / Batch selection mode
                        isBatchSelectionMode = uiState.isBatchSelectionMode,
                        selectedTaskCount = uiState.selectedTaskIdsForBatch.size,
                        onClearBatchSelection = { viewModel.clearBatchSelection() },
                        onSelectAllBatch = {
                            val allVisibleIds = visibleTasks.map { it.task.id }
                            viewModel.selectAllTasks(allVisibleIds)
                        },
                        onMoveBatch = { viewModel.openMoveTasksDialog() },
                        onDeleteBatch = { viewModel.deleteSelectedTasks() }
                    )
                }
                AppBottomTab.WORKFLOWS -> {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "Workflows",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                AppBottomTab.HABITS -> {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "Habits",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
                AppBottomTab.ACHIEVEMENTS -> {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = "Badges & Stats",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (!uiState.isBatchSelectionMode) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier.testTag("app_bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = uiState.currentBottomTab == AppBottomTab.TASKS,
                        onClick = { viewModel.selectBottomTab(AppBottomTab.TASKS) },
                        alwaysShowLabel = true,
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentBottomTab == AppBottomTab.TASKS) Icons.Filled.TaskAlt else Icons.Outlined.TaskAlt,
                                contentDescription = "Tasks",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Tasks",
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("tab_tasks")
                    )

                    NavigationBarItem(
                        selected = uiState.currentBottomTab == AppBottomTab.WORKFLOWS,
                        onClick = { viewModel.selectBottomTab(AppBottomTab.WORKFLOWS) },
                        alwaysShowLabel = true,
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentBottomTab == AppBottomTab.WORKFLOWS) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Workflows",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Workflows",
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("tab_workflows")
                    )

                    NavigationBarItem(
                        selected = uiState.currentBottomTab == AppBottomTab.HABITS,
                        onClick = { viewModel.selectBottomTab(AppBottomTab.HABITS) },
                        alwaysShowLabel = true,
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentBottomTab == AppBottomTab.HABITS) Icons.Filled.SelfImprovement else Icons.Outlined.SelfImprovement,
                                contentDescription = "Habits",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Habits",
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("tab_habits")
                    )

                    NavigationBarItem(
                        selected = uiState.currentBottomTab == AppBottomTab.ACHIEVEMENTS,
                        onClick = { viewModel.selectBottomTab(AppBottomTab.ACHIEVEMENTS) },
                        alwaysShowLabel = true,
                        icon = {
                            Icon(
                                imageVector = if (uiState.currentBottomTab == AppBottomTab.ACHIEVEMENTS) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                                contentDescription = "Badges",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Badges",
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("tab_achievements")
                    )
                }
            }
        },
        floatingActionButton = {
            if (!uiState.isBatchSelectionMode) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.pomodoroTimerState.isRunning) {
                        val mins = uiState.pomodoroTimerState.secondsRemaining / 60
                        val secs = uiState.pomodoroTimerState.secondsRemaining % 60
                        Surface(
                            onClick = { viewModel.openFocusTimer() },
                            shape = RoundedCornerShape(20.dp),
                            color = TaskBlue,
                            shadowElevation = 6.dp,
                            modifier = Modifier.testTag("floating_focus_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.HourglassTop,
                                    contentDescription = "Active focus timer",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "%02d:%02d".format(mins, secs),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (uiState.currentBottomTab == AppBottomTab.TASKS) {
                        ExtendedFloatingActionButton(
                            onClick = { viewModel.openAddTaskSheet() },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add task",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = "New task",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier.testTag("add_task_fab")
                        )
                    } else if (uiState.currentBottomTab == AppBottomTab.HABITS && uiState.habits.isNotEmpty()) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                habitToEdit = null
                                isAddEditHabitDialogOpen = true
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "New Habit",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = "New habit",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = CircleShape,
                            modifier = Modifier.testTag("add_habit_fab_main")
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState.currentBottomTab) {
                AppBottomTab.WORKFLOWS -> {
                    WorkflowsScreen(
                        tasks = uiState.allTasks,
                        lists = uiState.lists,
                        selectedListId = uiState.selectedListId,
                        onSelectList = { viewModel.selectList(it) },
                        activeFocusTaskId = uiState.pomodoroTimerState.boundTask?.task?.id,
                        onTaskClick = { viewModel.openTaskDetail(it) },
                        onToggleTaskComplete = { task ->
                            val taskWithSub = uiState.allTasks.find { it.task.id == task.id }
                            if (taskWithSub != null) {
                                viewModel.toggleTaskComplete(taskWithSub)
                            }
                        },
                        onMoveTaskToQuadrant = { task, quadrant ->
                            viewModel.moveTaskToQuadrant(task, quadrant)
                        },
                        onMoveTaskKanban = { task, col ->
                            viewModel.moveTaskKanban(task, col)
                        },
                        onStartFocusSession = { viewModel.openFocusTimer(it) },
                        onQuickAddTaskToQuadrant = { quadrant ->
                            viewModel.quickAddTaskToQuadrant(quadrant)
                        },
                        onQuickAddTaskToKanban = { col ->
                            viewModel.openAddTaskSheet()
                        }
                    )
                }

                AppBottomTab.HABITS -> {
                    HabitsScreen(
                        habits = uiState.habits,
                        onToggleHabitToday = { id, completed ->
                            viewModel.toggleHabitToday(id, completed)
                        },
                        onAddHabitClick = {
                            habitToEdit = null
                            isAddEditHabitDialogOpen = true
                        },
                        onEditHabit = { habit ->
                            habitToEdit = habit
                            isAddEditHabitDialogOpen = true
                        },
                        onDeleteHabit = { habit ->
                            viewModel.deleteHabit(habit)
                        }
                    )
                }

                AppBottomTab.ACHIEVEMENTS -> {
                    AchievementsScreen(
                        userStats = uiState.userProductivityStats
                    )
                }

                AppBottomTab.TASKS -> {
                    when {
                        // View 0: Interactive Calendar Grid View
                        uiState.isCalendarView -> {
                            CalendarGridView(
                                allTasks = uiState.allTasks,
                                selectedDateMillis = uiState.selectedCalendarDateMillis,
                                onSelectDate = { viewModel.setSelectedCalendarDate(it) },
                                onAddTaskForDate = { dateMillis -> viewModel.openAddTaskSheet(dateMillis) },
                                onTaskClick = { viewModel.openTaskDetail(it) },
                                onToggleComplete = { viewModel.toggleTaskComplete(it) },
                                onToggleStar = { viewModel.toggleTaskStarred(it) },
                                onStartFocusSession = { viewModel.openFocusTimer(it) },
                                onToggleSubtask = { viewModel.toggleSubtaskComplete(it) },
                                onDeleteSubtask = { viewModel.deleteSubtask(it) },
                                onAddSubtask = { taskId, title -> viewModel.addSubtask(taskId, title) },
                                expandedTaskIds = uiState.expandedTaskIds,
                                onToggleExpand = { viewModel.toggleTaskExpanded(it) },
                                onDeleteTask = { taskWithSubtasks ->
                                    val deletedTask = taskWithSubtasks.task
                                    viewModel.deleteTask(deletedTask)
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Task \"${deletedTask.title.take(20)}\" deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.createTask(
                                                title = deletedTask.title,
                                                details = deletedTask.details,
                                                dueDateMillis = deletedTask.dueDateMillis,
                                                hasTime = deletedTask.hasTime,
                                                priority = deletedTask.priority,
                                                isStarred = deletedTask.isStarred,
                                                targetListId = deletedTask.listId,
                                                recurrenceFrequency = deletedTask.recurrenceFrequency,
                                                recurrenceInterval = deletedTask.recurrenceInterval,
                                                recurrenceTimesPerPeriod = deletedTask.recurrenceTimesPerPeriod,
                                                recurrenceDaysOfWeek = deletedTask.recurrenceDaysOfWeek,
                                                recurrenceDaysOfMonth = deletedTask.recurrenceDaysOfMonth
                                            )
                                        }
                                    }
                                }
                            )
                        }

                        // View 1: Dedicated Completed Tasks View (Clean and isolated)
                        uiState.isCompletedView -> {
                            val completedList = uiState.completedTasks
                            if (completedList.isEmpty()) {
                                TasksEmptyState(
                                    isSearching = uiState.isSearching,
                                    isStarredView = false,
                                    isCompletedView = true,
                                    onAddTask = { viewModel.openAddTaskSheet() },
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(top = 4.dp),
                                    contentPadding = PaddingValues(bottom = 96.dp)
                                ) {
                                    item {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${completedList.size} Completed task(s)",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                TextButton(
                                                    onClick = { viewModel.clearCompletedTasks() },
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.DeleteSweep,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Clear all", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }

                                    items(completedList, key = { it.task.id }) { taskWithSubtasks ->
                                        val isExpanded = uiState.expandedTaskIds.contains(taskWithSubtasks.task.id)
                                        val isSelected = uiState.selectedTaskIdsForBatch.contains(taskWithSubtasks.task.id)
                                        TaskItem(
                                            taskWithSubtasks = taskWithSubtasks,
                                            isExpanded = isExpanded,
                                            isSelectionMode = uiState.isBatchSelectionMode,
                                            isSelected = isSelected,
                                            onToggleSelect = { viewModel.toggleTaskSelection(taskWithSubtasks.task.id) },
                                            onLongClick = { viewModel.enterBatchSelection(taskWithSubtasks.task.id) },
                                            onToggleExpand = { viewModel.toggleTaskExpanded(taskWithSubtasks.task.id) },
                                            onToggleComplete = { viewModel.toggleTaskComplete(taskWithSubtasks) },
                                            onToggleStar = { viewModel.toggleTaskStarred(taskWithSubtasks) },
                                            onToggleSubtask = { subtask -> viewModel.toggleSubtaskComplete(subtask) },
                                            onDeleteSubtask = { subtaskId -> viewModel.deleteSubtask(subtaskId) },
                                            onAddSubtask = { title -> viewModel.addSubtask(taskWithSubtasks.task.id, title) },
                                            onTagClick = { tag -> viewModel.selectTag(tag) },
                                            onDeleteTask = {
                                                val deletedTask = taskWithSubtasks.task
                                                viewModel.deleteTask(deletedTask)
                                                coroutineScope.launch {
                                                    val result = snackbarHostState.showSnackbar(
                                                        message = "Task \"${deletedTask.title.take(20)}\" deleted",
                                                        actionLabel = "Undo",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                    if (result == SnackbarResult.ActionPerformed) {
                                                        viewModel.createTask(
                                                            title = deletedTask.title,
                                                            details = deletedTask.details,
                                                            dueDateMillis = deletedTask.dueDateMillis,
                                                            hasTime = deletedTask.hasTime,
                                                            priority = deletedTask.priority,
                                                            isStarred = deletedTask.isStarred,
                                                            targetListId = deletedTask.listId,
                                                            recurrenceFrequency = deletedTask.recurrenceFrequency,
                                                            recurrenceInterval = deletedTask.recurrenceInterval,
                                                            recurrenceTimesPerPeriod = deletedTask.recurrenceTimesPerPeriod,
                                                            recurrenceDaysOfWeek = deletedTask.recurrenceDaysOfWeek,
                                                            recurrenceDaysOfMonth = deletedTask.recurrenceDaysOfMonth
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                if (uiState.isBatchSelectionMode) {
                                                    viewModel.toggleTaskSelection(taskWithSubtasks.task.id)
                                                } else {
                                                    viewModel.openTaskDetail(taskWithSubtasks)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                // View 2: Dedicated Upcoming Repeating Tasks View (Clean and protected)
                uiState.isUpcomingRecurringView -> {
                    val activeList = uiState.activeTasks
                    val upcomingList = uiState.upcomingRecurringTasks
                    val hasAnyRecurring = activeList.isNotEmpty() || upcomingList.isNotEmpty()
                    if (!hasAnyRecurring) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(24.dp),
                                    color = TaskBlue.copy(alpha = 0.12f),
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Outlined.Repeat,
                                            contentDescription = null,
                                            tint = TaskBlue,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "No repeating tasks",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Repeating tasks scheduled for today and future dates appear here in their own organized view so you never forget or mistouch them.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 4.dp),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            // Repeating tasks due today (or overdue) - fully active and checkable!
                            if (activeList.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Due Today (${activeList.size})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "Ready to complete",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                items(activeList, key = { it.task.id }) { taskWithSubtasks ->
                                    val isExpanded = uiState.expandedTaskIds.contains(taskWithSubtasks.task.id)
                                    val isSelected = uiState.selectedTaskIdsForBatch.contains(taskWithSubtasks.task.id)
                                    TaskItem(
                                        taskWithSubtasks = taskWithSubtasks,
                                        isExpanded = isExpanded,
                                        isLockedForCompletion = false,
                                        isSelectionMode = uiState.isBatchSelectionMode,
                                        isSelected = isSelected,
                                        onToggleSelect = { viewModel.toggleTaskSelection(taskWithSubtasks.task.id) },
                                        onLongClick = { viewModel.enterBatchSelection(taskWithSubtasks.task.id) },
                                        onToggleExpand = { viewModel.toggleTaskExpanded(taskWithSubtasks.task.id) },
                                        onToggleComplete = { viewModel.toggleTaskComplete(taskWithSubtasks) },
                                        onToggleStar = { viewModel.toggleTaskStarred(taskWithSubtasks) },
                                        onToggleSubtask = { subtask -> viewModel.toggleSubtaskComplete(subtask) },
                                        onDeleteSubtask = { subtaskId -> viewModel.deleteSubtask(subtaskId) },
                                        onAddSubtask = { title -> viewModel.addSubtask(taskWithSubtasks.task.id, title) },
                                        onStartFocusSession = { viewModel.openFocusTimer(taskWithSubtasks) },
                                        onTagClick = { tag -> viewModel.selectTag(tag) },
                                        onDeleteTask = {
                                            val deletedTask = taskWithSubtasks.task
                                            viewModel.deleteTask(deletedTask)
                                            coroutineScope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Task \"${deletedTask.title.take(20)}\" deleted",
                                                    actionLabel = "Undo",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.createTask(
                                                        title = deletedTask.title,
                                                        details = deletedTask.details,
                                                        dueDateMillis = deletedTask.dueDateMillis,
                                                        hasTime = deletedTask.hasTime,
                                                        priority = deletedTask.priority,
                                                        isStarred = deletedTask.isStarred,
                                                        targetListId = deletedTask.listId,
                                                        recurrenceFrequency = deletedTask.recurrenceFrequency,
                                                        recurrenceInterval = deletedTask.recurrenceInterval,
                                                        recurrenceTimesPerPeriod = deletedTask.recurrenceTimesPerPeriod,
                                                        recurrenceDaysOfWeek = deletedTask.recurrenceDaysOfWeek,
                                                        recurrenceDaysOfMonth = deletedTask.recurrenceDaysOfMonth
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            if (uiState.isBatchSelectionMode) {
                                                viewModel.toggleTaskSelection(taskWithSubtasks.task.id)
                                            } else {
                                                viewModel.openTaskDetail(taskWithSubtasks)
                                            }
                                        }
                                    )
                                }
                            }

                            // Upcoming repeating tasks scheduled for future dates - locked
                            if (upcomingList.isNotEmpty()) {
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = TaskBlue.copy(alpha = 0.1f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Lock,
                                                contentDescription = null,
                                                tint = TaskBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "${upcomingList.size} repeating task(s) scheduled for future days. Protected from early completion until their day arrives.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TaskBlue,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }

                                items(upcomingList, key = { it.task.id }) { taskWithSubtasks ->
                                    val isExpanded = uiState.expandedTaskIds.contains(taskWithSubtasks.task.id)
                                    val isSelected = uiState.selectedTaskIdsForBatch.contains(taskWithSubtasks.task.id)
                                    TaskItem(
                                        taskWithSubtasks = taskWithSubtasks,
                                        isExpanded = isExpanded,
                                        isLockedForCompletion = true,
                                        isSelectionMode = uiState.isBatchSelectionMode,
                                        isSelected = isSelected,
                                        onToggleSelect = { viewModel.toggleTaskSelection(taskWithSubtasks.task.id) },
                                        onLongClick = { viewModel.enterBatchSelection(taskWithSubtasks.task.id) },
                                        onToggleExpand = { viewModel.toggleTaskExpanded(taskWithSubtasks.task.id) },
                                        onToggleComplete = { viewModel.toggleTaskComplete(taskWithSubtasks) },
                                        onToggleStar = { viewModel.toggleTaskStarred(taskWithSubtasks) },
                                        onToggleSubtask = { subtask -> viewModel.toggleSubtaskComplete(subtask) },
                                        onDeleteSubtask = { subtaskId -> viewModel.deleteSubtask(subtaskId) },
                                        onAddSubtask = { title -> viewModel.addSubtask(taskWithSubtasks.task.id, title) },
                                        onTagClick = { tag -> viewModel.selectTag(tag) },
                                        onDeleteTask = {
                                            val deletedTask = taskWithSubtasks.task
                                            viewModel.deleteTask(deletedTask)
                                            coroutineScope.launch {
                                                val result = snackbarHostState.showSnackbar(
                                                    message = "Task \"${deletedTask.title.take(20)}\" deleted",
                                                    actionLabel = "Undo",
                                                    duration = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.createTask(
                                                        title = deletedTask.title,
                                                        details = deletedTask.details,
                                                        dueDateMillis = deletedTask.dueDateMillis,
                                                        hasTime = deletedTask.hasTime,
                                                        priority = deletedTask.priority,
                                                        isStarred = deletedTask.isStarred,
                                                        targetListId = deletedTask.listId,
                                                        recurrenceFrequency = deletedTask.recurrenceFrequency,
                                                        recurrenceInterval = deletedTask.recurrenceInterval,
                                                        recurrenceTimesPerPeriod = deletedTask.recurrenceTimesPerPeriod,
                                                        recurrenceDaysOfWeek = deletedTask.recurrenceDaysOfWeek,
                                                        recurrenceDaysOfMonth = deletedTask.recurrenceDaysOfMonth
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            if (uiState.isBatchSelectionMode) {
                                                viewModel.toggleTaskSelection(taskWithSubtasks.task.id)
                                            } else {
                                                viewModel.openTaskDetail(taskWithSubtasks)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // View 3: Standard List / Starred View (Active tasks ONLY - clean UI)
                else -> {
                    val activeList = uiState.activeTasks
                    val hasAnyTasks = activeList.isNotEmpty()

                    if (!hasAnyTasks && !uiState.isSearching) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            DailyProgressCard(feedback = uiState.dailyCoachFeedback)

                            if (uiState.availableTags.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilterChip(
                                        selected = uiState.selectedTag == null,
                                        onClick = { viewModel.selectTag(null) },
                                        label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("tag_filter_empty_all")
                                    )

                                    uiState.availableTags.forEach { tag ->
                                        val isTagSelected = uiState.selectedTag == tag
                                        FilterChip(
                                            selected = isTagSelected,
                                            onClick = { viewModel.selectTag(tag) },
                                            label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("tag_filter_empty_$tag")
                                        )
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                TasksEmptyState(
                                    isSearching = false,
                                    isStarredView = uiState.isStarredView,
                                    isCompletedView = false,
                                    onAddTask = { viewModel.openAddTaskSheet() }
                                )
                            }
                        }
                    } else if (!hasAnyTasks && uiState.isSearching) {
                        TasksEmptyState(
                            isSearching = true,
                            isStarredView = uiState.isStarredView,
                            isCompletedView = false,
                            onAddTask = { viewModel.openAddTaskSheet() },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 4.dp),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            // Daily Progress Card with Motivational Words
                            if (!uiState.isSearching) {
                                item {
                                    DailyProgressCard(feedback = uiState.dailyCoachFeedback)
                                }
                            }

                            // Smart Task Tags and Categorization Filter Bar
                            if (uiState.availableTags.isNotEmpty() && !uiState.isSearching) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilterChip(
                                            selected = uiState.selectedTag == null,
                                            onClick = { viewModel.selectTag(null) },
                                            label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("tag_filter_all")
                                        )

                                        uiState.availableTags.forEach { tag ->
                                            val isTagSelected = uiState.selectedTag == tag
                                            FilterChip(
                                                selected = isTagSelected,
                                                onClick = { viewModel.selectTag(tag) },
                                                label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.testTag("tag_filter_$tag")
                                            )
                                        }
                                    }
                                }
                            }

                            // Sorting & Due date filter badges header
                            if (uiState.sortOrder != SortOrder.MY_ORDER || uiState.dueDateFilter != com.example.data.repository.TaskRepository.DueDateFilter.ALL) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (uiState.sortOrder != SortOrder.MY_ORDER) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            ) {
                                                Text(
                                                    text = "Sorted by: ${uiState.sortOrder.label}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }

                                        if (uiState.dueDateFilter != com.example.data.repository.TaskRepository.DueDateFilter.ALL) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = TaskBlue.copy(alpha = 0.12f)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 2.dp, bottom = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "Filter: ${uiState.dueDateFilter.label}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Medium,
                                                        color = TaskBlue
                                                    )
                                                    IconButton(
                                                        onClick = { viewModel.setDueDateFilter(com.example.data.repository.TaskRepository.DueDateFilter.ALL) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Close,
                                                            contentDescription = "Clear filter",
                                                            tint = TaskBlue,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Active Tasks (Today, Overdue, or Non-recurring)
                            items(activeList, key = { it.task.id }) { taskWithSubtasks ->
                                val isExpanded = uiState.expandedTaskIds.contains(taskWithSubtasks.task.id)
                                val isSelected = uiState.selectedTaskIdsForBatch.contains(taskWithSubtasks.task.id)
                                TaskItem(
                                    taskWithSubtasks = taskWithSubtasks,
                                    isExpanded = isExpanded,
                                    isSelectionMode = uiState.isBatchSelectionMode,
                                    isSelected = isSelected,
                                    onToggleSelect = { viewModel.toggleTaskSelection(taskWithSubtasks.task.id) },
                                    onLongClick = { viewModel.enterBatchSelection(taskWithSubtasks.task.id) },
                                    onToggleExpand = { viewModel.toggleTaskExpanded(taskWithSubtasks.task.id) },
                                    onToggleComplete = { viewModel.toggleTaskComplete(taskWithSubtasks) },
                                    onToggleStar = { viewModel.toggleTaskStarred(taskWithSubtasks) },
                                    onToggleSubtask = { subtask -> viewModel.toggleSubtaskComplete(subtask) },
                                    onDeleteSubtask = { subtaskId -> viewModel.deleteSubtask(subtaskId) },
                                    onAddSubtask = { title -> viewModel.addSubtask(taskWithSubtasks.task.id, title) },
                                    onStartFocusSession = { viewModel.openFocusTimer(taskWithSubtasks) },
                                    onTagClick = { tag -> viewModel.selectTag(tag) },
                                    onDeleteTask = {
                                        val deletedTask = taskWithSubtasks.task
                                        viewModel.deleteTask(deletedTask)
                                        coroutineScope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Task \"${deletedTask.title.take(20)}\" deleted",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.createTask(
                                                    title = deletedTask.title,
                                                    details = deletedTask.details,
                                                    dueDateMillis = deletedTask.dueDateMillis,
                                                    hasTime = deletedTask.hasTime,
                                                    priority = deletedTask.priority,
                                                    isStarred = deletedTask.isStarred,
                                                    targetListId = deletedTask.listId,
                                                    recurrenceFrequency = deletedTask.recurrenceFrequency,
                                                    recurrenceInterval = deletedTask.recurrenceInterval,
                                                    recurrenceTimesPerPeriod = deletedTask.recurrenceTimesPerPeriod,
                                                    recurrenceDaysOfWeek = deletedTask.recurrenceDaysOfWeek,
                                                    recurrenceDaysOfMonth = deletedTask.recurrenceDaysOfMonth
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (uiState.isBatchSelectionMode) {
                                            viewModel.toggleTaskSelection(taskWithSubtasks.task.id)
                                        } else {
                                            viewModel.openTaskDetail(taskWithSubtasks)
                                        }
                                    }
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

    // Add Task Bottom Sheet
    if (uiState.isAddTaskSheetOpen) {
        AddTaskBottomSheet(
            lists = uiState.lists,
            currentListId = uiState.selectedListId,
            initialDueDateMillis = uiState.initialDueDateForNewTask,
            onDismiss = { viewModel.closeAddTaskSheet() },
            onSaveTask = { title, details, dueDate, hasTime, priority, isStarred, targetListId, freq, interval, times, daysOfWeek, daysOfMonth, tags ->
                viewModel.createTask(
                    title = title,
                    details = details,
                    dueDateMillis = dueDate,
                    hasTime = hasTime,
                    priority = priority,
                    isStarred = isStarred,
                    targetListId = targetListId,
                    recurrenceFrequency = freq,
                    recurrenceInterval = interval,
                    recurrenceTimesPerPeriod = times,
                    recurrenceDaysOfWeek = daysOfWeek,
                    recurrenceDaysOfMonth = daysOfMonth,
                    tags = tags
                )
            }
        )
    }

    // Settings & Personalization Dialog
    SettingsAndPersonalizationDialog(
        isOpen = uiState.isSettingsDialogOpen,
        settings = uiState.appSettings,
        currentSort = uiState.sortOrder,
        onDismiss = { viewModel.closeSettingsDialog() },
        onOpenReminders = { viewModel.openReminderDialog() },
        onOpenTheme = { viewModel.toggleThemeDialog(true) },
        onOpenAutoClear = { viewModel.toggleAutoClearDialog(true) },
        onOpenSort = { viewModel.toggleSortDialog(true) },
        onClearCompleted = { viewModel.clearCompletedTasks() },
        onToggleHapticFeedback = { viewModel.toggleHapticFeedback(it) }
    )

    // Theme Picker Dialog
    ThemePickerDialog(
        isOpen = uiState.isThemeDialogOpen,
        currentSettings = uiState.appSettings,
        onDismiss = { viewModel.toggleThemeDialog(false) },
        onSelectPreset = { presetId -> viewModel.updateThemePreset(presetId) },
        onSaveCustomTheme = { primary, bg, surface, isLight ->
            viewModel.updateCustomTheme(primary, bg, surface, isLight)
        }
    )

    // Auto-Clear Settings Dialog
    AutoClearDialog(
        isOpen = uiState.isAutoClearDialogOpen,
        currentSettings = uiState.appSettings,
        onDismiss = { viewModel.toggleAutoClearDialog(false) },
        onSaveSettings = { enabled, hours ->
            viewModel.updateAutoClear(enabled, hours)
        }
    )

    // Reminder Settings Dialog (Starting of day & near ending)
    ReminderSettingsDialog(
        isOpen = uiState.isReminderDialogOpen,
        settings = uiState.appSettings,
        onDismiss = { viewModel.closeReminderDialog() },
        onSaveReminderSettings = { mEn, mH, mM, eEn, eH, eM ->
            viewModel.updateReminderSettings(mEn, mH, mM, eEn, eH, eM)
        },
        onSendTestReminder = { viewModel.sendTestReminder(context) },
        onRequestNotificationPermission = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    )

    // Move Tasks Dialog (Multi-select move)
    MoveTasksDialog(
        isOpen = uiState.isMoveTasksDialogOpen,
        lists = uiState.lists,
        selectedCount = uiState.selectedTaskIdsForBatch.size,
        onDismiss = { viewModel.closeMoveTasksDialog() },
        onMoveToList = { targetListId -> viewModel.moveSelectedTasks(targetListId) }
    )

    // Create List Dialog
    CreateListDialog(
        isOpen = uiState.isCreateListDialogOpen,
        onDismiss = { viewModel.closeCreateListDialog() },
        onCreateList = { name -> viewModel.createList(name) }
    )

    // Rename List Dialog
    RenameListDialog(
        list = uiState.listToRename,
        onDismiss = { viewModel.closeRenameListDialog() },
        onRenameList = { list, newName -> viewModel.renameList(list, newName) }
    )

    // Delete List Dialog
    DeleteListDialog(
        list = uiState.listToDelete,
        onDismiss = { viewModel.closeDeleteListDialog() },
        onDeleteList = { list -> viewModel.deleteList(list) }
    )

    // Sort Options Bottom Sheet
    if (uiState.isSortDialogOpen) {
        SortOptionsBottomSheet(
            currentSort = uiState.sortOrder,
            onSelectSort = { viewModel.setSortOrder(it) },
            currentDueDateFilter = uiState.dueDateFilter,
            onSelectDueDateFilter = { viewModel.setDueDateFilter(it) },
            onDismiss = { viewModel.toggleSortDialog(false) }
        )
    }

    // Pomodoro Focus Timer Dialog (Background-persistent)
    if (uiState.isFocusTimerOpen) {
        PomodoroTimerDialog(
            timerState = uiState.pomodoroTimerState,
            activeTasks = uiState.activeTasks,
            onStart = { viewModel.startPomodoroTimer() },
            onPause = { viewModel.pausePomodoroTimer() },
            onReset = { viewModel.resetPomodoroTimer() },
            onExtend = { viewModel.extendPomodoroTimer(300) },
            onSkip = { viewModel.skipPomodoroTimer() },
            onSelectDuration = { viewModel.setPomodoroDurationMinutes(it) },
            onSelectSessionType = { viewModel.setPomodoroSessionType(it) },
            onSelectBoundTask = { viewModel.setPomodoroBoundTask(it) },
            onToggleAutoMarkComplete = { viewModel.setPomodoroAutoMarkComplete(it) },
            onCompleteEarly = { elapsedMinutes ->
                viewModel.completeFocusSession(
                    durationMinutes = elapsedMinutes,
                    sessionType = uiState.pomodoroTimerState.sessionType,
                    boundTask = uiState.pomodoroTimerState.boundTask,
                    autoMarkTaskComplete = uiState.pomodoroTimerState.autoMarkComplete
                )
                viewModel.resetPomodoroTimer()
            },
            onDismissCelebration = { viewModel.dismissPomodoroCelebration() },
            onDismiss = { viewModel.closeFocusTimer() },
            hapticFeedbackEnabled = uiState.appSettings.hapticFeedbackEnabled
        )
    }

    // Productivity Analytics & Streaks Dialog
    if (uiState.isAnalyticsDialogOpen) {
        ProductivityAnalyticsDialog(
            stats = uiState.productivityStats,
            onDismiss = { viewModel.closeAnalyticsDialog() },
            onStartFocusSession = {
                viewModel.closeAnalyticsDialog()
                viewModel.openFocusTimer()
            }
        )
    }

    // Habit Add / Edit Dialog
    if (isAddEditHabitDialogOpen) {
        AddEditHabitDialog(
            habitToEdit = habitToEdit,
            onDismiss = {
                isAddEditHabitDialogOpen = false
                habitToEdit = null
            },
            onSave = { habit ->
                if (habitToEdit == null) {
                    viewModel.insertHabit(habit)
                } else {
                    viewModel.updateHabit(habit)
                }
                isAddEditHabitDialogOpen = false
                habitToEdit = null
            }
        )
    }
}

@Composable
fun TasksEmptyState(
    isSearching: Boolean,
    isStarredView: Boolean,
    isCompletedView: Boolean = false,
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.size(160.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            com.example.ui.util.ResourceEfficientImage(
                resId = R.drawable.tasks_empty_illustration,
                contentDescription = "No tasks illustration",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        val title = when {
            isSearching -> "No results found"
            isCompletedView -> "No completed tasks"
            isStarredView -> "No starred tasks"
            else -> "A fresh start"
        }

        val subtitle = when {
            isSearching -> "Try searching for something else"
            isCompletedView -> "Tasks you complete will be cleanly archived here."
            isStarredView -> "Star important tasks to find them easily here."
            else -> "Anything to add? Tap + to create your first task."
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
