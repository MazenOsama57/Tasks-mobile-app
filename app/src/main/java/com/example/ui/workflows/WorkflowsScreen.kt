package com.example.ui.workflows

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.Task
import com.example.data.model.TaskList
import com.example.data.model.TaskWithSubtasks
import com.example.ui.theme.TaskBlue
import com.example.ui.util.TaskDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkflowsScreen(
    tasks: List<TaskWithSubtasks>,
    lists: List<TaskList>,
    selectedListId: Long?,
    onSelectList: (Long?) -> Unit,
    activeFocusTaskId: Long?,
    onTaskClick: (TaskWithSubtasks) -> Unit,
    onToggleTaskComplete: (Task) -> Unit,
    onMoveTaskToQuadrant: (Task, EisenhowerQuadrant) -> Unit,
    onMoveTaskKanban: (Task, KanbanColumn) -> Unit,
    onStartFocusSession: (TaskWithSubtasks) -> Unit,
    onQuickAddTaskToQuadrant: (EisenhowerQuadrant) -> Unit,
    onQuickAddTaskToKanban: (KanbanColumn) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(WorkflowViewMode.EISENHOWER_MATRIX) }

    // Filter tasks by selected list if specified
    val filteredTasks = remember(tasks, selectedListId) {
        if (selectedListId == null) tasks
        else tasks.filter { it.task.listId == selectedListId }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("workflows_screen")
    ) {
        // Mode Selector: Segmented Button / Tabs
        PrimaryTabRow(
            selectedTabIndex = if (viewMode == WorkflowViewMode.EISENHOWER_MATRIX) 0 else 1,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = viewMode == WorkflowViewMode.EISENHOWER_MATRIX,
                onClick = { viewMode = WorkflowViewMode.EISENHOWER_MATRIX },
                text = { Text("Eisenhower Matrix", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("tab_matrix")
            )
            Tab(
                selected = viewMode == WorkflowViewMode.KANBAN_BOARD,
                onClick = { viewMode = WorkflowViewMode.KANBAN_BOARD },
                text = { Text("Kanban Board", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.ViewColumn,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.testTag("tab_kanban")
            )
        }

        // List Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = selectedListId == null,
                onClick = { onSelectList(null) },
                label = { Text("All Lists") },
                shape = RoundedCornerShape(10.dp)
            )

            lists.forEach { list ->
                FilterChip(
                    selected = selectedListId == list.id,
                    onClick = { onSelectList(list.id) },
                    label = { Text(list.name) },
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        when (viewMode) {
            WorkflowViewMode.EISENHOWER_MATRIX -> {
                EisenhowerMatrixView(
                    tasks = filteredTasks,
                    onTaskClick = onTaskClick,
                    onToggleComplete = onToggleTaskComplete,
                    onMoveQuadrant = onMoveTaskToQuadrant,
                    onQuickAdd = onQuickAddTaskToQuadrant
                )
            }
            WorkflowViewMode.KANBAN_BOARD -> {
                KanbanBoardView(
                    tasks = filteredTasks,
                    activeFocusTaskId = activeFocusTaskId,
                    onTaskClick = onTaskClick,
                    onToggleComplete = onToggleTaskComplete,
                    onMoveColumn = onMoveTaskKanban,
                    onStartFocusSession = onStartFocusSession,
                    onQuickAdd = onQuickAddTaskToKanban
                )
            }
        }
    }
}

@Composable
private fun EisenhowerMatrixView(
    tasks: List<TaskWithSubtasks>,
    onTaskClick: (TaskWithSubtasks) -> Unit,
    onToggleComplete: (Task) -> Unit,
    onMoveQuadrant: (Task, EisenhowerQuadrant) -> Unit,
    onQuickAdd: (EisenhowerQuadrant) -> Unit
) {
    // Only classify active/uncompleted tasks in the matrix
    val activeTasks = remember(tasks) { tasks.filter { !it.task.isCompleted } }

    val q1Tasks = remember(activeTasks) { activeTasks.filter { EisenhowerQuadrant.classify(it.task) == EisenhowerQuadrant.DO_FIRST } }
    val q2Tasks = remember(activeTasks) { activeTasks.filter { EisenhowerQuadrant.classify(it.task) == EisenhowerQuadrant.SCHEDULE } }
    val q3Tasks = remember(activeTasks) { activeTasks.filter { EisenhowerQuadrant.classify(it.task) == EisenhowerQuadrant.DELEGATE } }
    val q4Tasks = remember(activeTasks) { activeTasks.filter { EisenhowerQuadrant.classify(it.task) == EisenhowerQuadrant.ELIMINATE } }

    LazyColumn(
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            QuadrantCard(
                quadrant = EisenhowerQuadrant.DO_FIRST,
                tasks = q1Tasks,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onMoveQuadrant = onMoveQuadrant,
                onQuickAdd = { onQuickAdd(EisenhowerQuadrant.DO_FIRST) }
            )
        }
        item {
            QuadrantCard(
                quadrant = EisenhowerQuadrant.SCHEDULE,
                tasks = q2Tasks,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onMoveQuadrant = onMoveQuadrant,
                onQuickAdd = { onQuickAdd(EisenhowerQuadrant.SCHEDULE) }
            )
        }
        item {
            QuadrantCard(
                quadrant = EisenhowerQuadrant.DELEGATE,
                tasks = q3Tasks,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onMoveQuadrant = onMoveQuadrant,
                onQuickAdd = { onQuickAdd(EisenhowerQuadrant.DELEGATE) }
            )
        }
        item {
            QuadrantCard(
                quadrant = EisenhowerQuadrant.ELIMINATE,
                tasks = q4Tasks,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onMoveQuadrant = onMoveQuadrant,
                onQuickAdd = { onQuickAdd(EisenhowerQuadrant.ELIMINATE) }
            )
        }
    }
}

@Composable
private fun QuadrantCard(
    quadrant: EisenhowerQuadrant,
    tasks: List<TaskWithSubtasks>,
    onTaskClick: (TaskWithSubtasks) -> Unit,
    onToggleComplete: (Task) -> Unit,
    onMoveQuadrant: (Task, EisenhowerQuadrant) -> Unit,
    onQuickAdd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = quadrant.containerColor.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, quadrant.primaryColor.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quadrant_${quadrant.name}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(quadrant.primaryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = quadrant.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = quadrant.primaryColor.copy(alpha = 0.15f),
                        contentColor = quadrant.primaryColor
                    ) {
                        Text(
                            text = "${tasks.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onQuickAdd,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add task to ${quadrant.title}",
                        tint = quadrant.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = "${quadrant.subtitle} • ${quadrant.description}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (tasks.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No tasks in this quadrant",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    tasks.forEach { item ->
                        MatrixTaskCard(
                            taskWithSubtasks = item,
                            quadrant = quadrant,
                            onClick = { onTaskClick(item) },
                            onToggleComplete = { onToggleComplete(item.task) },
                            onMoveQuadrant = { targetQ -> onMoveQuadrant(item.task, targetQ) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixTaskCard(
    taskWithSubtasks: TaskWithSubtasks,
    quadrant: EisenhowerQuadrant,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onMoveQuadrant: (EisenhowerQuadrant) -> Unit
) {
    val task = taskWithSubtasks.task
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggleComplete() },
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.dueDateMillis != null) {
                    val dateStr = TaskDateUtils.formatDueDate(task.dueDateMillis)
                    Text(
                        text = "📅 $dateStr",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (TaskDateUtils.isOverdue(task.dueDateMillis)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Move to another quadrant menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = "Move quadrant",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    EisenhowerQuadrant.values().filter { it != quadrant }.forEach { target ->
                        DropdownMenuItem(
                            text = { Text("Move to ${target.title}") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(target.primaryColor)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onMoveQuadrant(target)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KanbanBoardView(
    tasks: List<TaskWithSubtasks>,
    activeFocusTaskId: Long?,
    onTaskClick: (TaskWithSubtasks) -> Unit,
    onToggleComplete: (Task) -> Unit,
    onMoveColumn: (Task, KanbanColumn) -> Unit,
    onStartFocusSession: (TaskWithSubtasks) -> Unit,
    onQuickAdd: (KanbanColumn) -> Unit
) {
    val todoTasks = remember(tasks, activeFocusTaskId) {
        tasks.filter { KanbanColumn.classify(it, activeFocusTaskId) == KanbanColumn.TODO }
    }
    val inProgressTasks = remember(tasks, activeFocusTaskId) {
        tasks.filter { KanbanColumn.classify(it, activeFocusTaskId) == KanbanColumn.IN_PROGRESS }
    }
    val doneTasks = remember(tasks, activeFocusTaskId) {
        tasks.filter { KanbanColumn.classify(it, activeFocusTaskId) == KanbanColumn.DONE }
    }

    LazyRow(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            KanbanColumnContainer(
                column = KanbanColumn.TODO,
                tasks = todoTasks,
                activeFocusTaskId = activeFocusTaskId,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onMoveColumn = onMoveColumn,
                onStartFocusSession = onStartFocusSession,
                onQuickAdd = { onQuickAdd(KanbanColumn.TODO) }
            )
        }
        item {
            KanbanColumnContainer(
                column = KanbanColumn.IN_PROGRESS,
                tasks = inProgressTasks,
                activeFocusTaskId = activeFocusTaskId,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onMoveColumn = onMoveColumn,
                onStartFocusSession = onStartFocusSession,
                onQuickAdd = { onQuickAdd(KanbanColumn.IN_PROGRESS) }
            )
        }
        item {
            KanbanColumnContainer(
                column = KanbanColumn.DONE,
                tasks = doneTasks,
                activeFocusTaskId = activeFocusTaskId,
                onTaskClick = onTaskClick,
                onToggleComplete = onToggleComplete,
                onMoveColumn = onMoveColumn,
                onStartFocusSession = onStartFocusSession,
                onQuickAdd = { onQuickAdd(KanbanColumn.DONE) }
            )
        }
    }
}

@Composable
private fun KanbanColumnContainer(
    column: KanbanColumn,
    tasks: List<TaskWithSubtasks>,
    activeFocusTaskId: Long?,
    onTaskClick: (TaskWithSubtasks) -> Unit,
    onToggleComplete: (Task) -> Unit,
    onMoveColumn: (Task, KanbanColumn) -> Unit,
    onStartFocusSession: (TaskWithSubtasks) -> Unit,
    onQuickAdd: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .testTag("kanban_col_${column.name}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(column.accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = column.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = column.accentColor.copy(alpha = 0.15f),
                        contentColor = column.accentColor
                    ) {
                        Text(
                            text = "${tasks.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onQuickAdd,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add to ${column.title}",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (tasks.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Empty column",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                } else {
                    items(tasks, key = { it.task.id }) { item ->
                        KanbanTaskCard(
                            taskWithSubtasks = item,
                            column = column,
                            isFocusActive = item.task.id == activeFocusTaskId,
                            onClick = { onTaskClick(item) },
                            onToggleComplete = { onToggleComplete(item.task) },
                            onMoveColumn = { targetCol -> onMoveColumn(item.task, targetCol) },
                            onStartFocusSession = { onStartFocusSession(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KanbanTaskCard(
    taskWithSubtasks: TaskWithSubtasks,
    column: KanbanColumn,
    isFocusActive: Boolean,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onMoveColumn: (KanbanColumn) -> Unit,
    onStartFocusSession: () -> Unit
) {
    val task = taskWithSubtasks.task

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete() },
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            if (task.dueDateMillis != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📅 " + TaskDateUtils.formatDueDate(task.dueDateMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (TaskDateUtils.isOverdue(task.dueDateMillis)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Buttons
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (column) {
                    KanbanColumn.TODO -> {
                        Button(
                            onClick = { onMoveColumn(KanbanColumn.IN_PROGRESS) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Start →", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    KanbanColumn.IN_PROGRESS -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilledTonalButton(
                                onClick = onStartFocusSession,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Outlined.HourglassTop, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Focus", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onMoveColumn(KanbanColumn.DONE) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Done ✓", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    KanbanColumn.DONE -> {
                        TextButton(
                            onClick = { onMoveColumn(KanbanColumn.TODO) },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("← Move to To Do", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
