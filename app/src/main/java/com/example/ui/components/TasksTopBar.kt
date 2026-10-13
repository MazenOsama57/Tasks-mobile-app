package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoDelete
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskList
import com.example.ui.SortOrder
import com.example.ui.theme.StarActiveColor
import com.example.ui.theme.TaskBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTopBar(
    currentListName: String,
    lists: List<TaskList>,
    selectedListId: Long?,
    isStarredView: Boolean,
    isCompletedView: Boolean,
    isUpcomingRecurringView: Boolean = false,
    isCalendarView: Boolean = false,
    sortOrder: SortOrder,
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: (Boolean) -> Unit,
    onSelectList: (Long) -> Unit,
    onSelectStarred: () -> Unit,
    onSelectCompleted: () -> Unit,
    onSelectUpcomingRecurring: () -> Unit = {},
    onSelectCalendarView: () -> Unit = {},
    onOpenCreateList: () -> Unit,
    onOpenRenameList: (TaskList) -> Unit,
    onOpenDeleteList: (TaskList) -> Unit,
    onOpenSortDialog: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onOpenAutoClearDialog: () -> Unit,
    onOpenReminderDialog: () -> Unit = {},
    onOpenSettingsDialog: () -> Unit = {},
    onOpenFocusTimer: () -> Unit = {},
    onOpenAnalytics: () -> Unit = {},
    onClearCompleted: () -> Unit,
    // Batch Selection Mode
    isBatchSelectionMode: Boolean = false,
    selectedTaskCount: Int = 0,
    onClearBatchSelection: () -> Unit = {},
    onSelectAllBatch: () -> Unit = {},
    onMoveBatch: () -> Unit = {},
    onDeleteBatch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    val currentSelectedList = lists.find { it.id == selectedListId }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier.fillMaxWidth()) {
        if (isBatchSelectionMode) {
            // Contextual Action Bar for Multi-Selection Mode
            TopAppBar(
                title = {
                    Text(
                        text = "$selectedTaskCount selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClearBatchSelection,
                        modifier = Modifier.testTag("exit_selection_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Exit selection")
                    }
                },
                actions = {
                    // Select All
                    IconButton(
                        onClick = onSelectAllBatch,
                        modifier = Modifier.testTag("select_all_btn")
                    ) {
                        Icon(imageVector = Icons.Outlined.SelectAll, contentDescription = "Select all")
                    }

                    // Move Selected
                    IconButton(
                        onClick = onMoveBatch,
                        enabled = selectedTaskCount > 0,
                        modifier = Modifier.testTag("move_selected_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Outlined.DriveFileMove, contentDescription = "Move tasks")
                    }

                    // Delete Selected
                    IconButton(
                        onClick = onDeleteBatch,
                        enabled = selectedTaskCount > 0,
                        modifier = Modifier.testTag("delete_selected_btn")
                    ) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete selected tasks")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            )
        } else {
            // Standard Top App Bar
            TopAppBar(
                title = {
                    if (isSearching) {
                        TextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search your tasks...") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_tasks_input")
                        )
                    } else {
                        Text(
                            text = "Tasks",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            modifier = Modifier.testTag("current_list_title")
                        )
                    }
                },
                navigationIcon = {
                    if (isSearching) {
                        IconButton(
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onToggleSearch(false)
                                onSearchQueryChange("")
                            },
                            modifier = Modifier.testTag("close_search_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Close search")
                        }
                    }
                },
                actions = {
                    if (!isSearching) {
                        // Search Button
                        IconButton(
                            onClick = { onToggleSearch(true) },
                            modifier = Modifier.testTag("open_search_button")
                        ) {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = "Search tasks")
                        }

                        // Sort Button with active badge indicator if not default
                        IconButton(
                            onClick = onOpenSortDialog,
                            modifier = Modifier.testTag("open_sort_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (sortOrder != SortOrder.MY_ORDER) {
                                        Badge(containerColor = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Outlined.Sort, contentDescription = "Sort tasks")
                            }
                        }

                        // Clean Overflow Menu (No duplicate subjects)
                        Box {
                            IconButton(
                                onClick = { showOverflowMenu = true },
                                modifier = Modifier.testTag("overflow_menu_button")
                            ) {
                                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More options")
                            }

                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Focus timer") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.HourglassTop,
                                            contentDescription = null,
                                            tint = TaskBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onOpenFocusTimer()
                                    },
                                    modifier = Modifier.testTag("menu_focus_timer")
                                )

                                DropdownMenuItem(
                                    text = { Text("Productivity & streaks") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = Color(0xFFF97316),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onOpenAnalytics()
                                    },
                                    modifier = Modifier.testTag("menu_productivity_analytics")
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                if (!isStarredView && !isCompletedView && !isUpcomingRecurringView && !isCalendarView && currentSelectedList != null) {
                                    DropdownMenuItem(
                                        text = { Text("Rename list") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = null,
                                                tint = TaskBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            onOpenRenameList(currentSelectedList)
                                        },
                                        modifier = Modifier.testTag("menu_rename_list")
                                    )

                                    if (lists.size > 1) {
                                        DropdownMenuItem(
                                            text = { Text("Delete list") },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            onClick = {
                                                showOverflowMenu = false
                                                onOpenDeleteList(currentSelectedList)
                                            },
                                            modifier = Modifier.testTag("menu_delete_list")
                                        )
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                }

                                DropdownMenuItem(
                                    text = { Text("Clear completed tasks") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteSweep,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onClearCompleted()
                                    },
                                    modifier = Modifier.testTag("menu_clear_completed")
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                DropdownMenuItem(
                                    text = { Text("Settings & personalization") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Settings,
                                            contentDescription = null,
                                            tint = TaskBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onOpenSettingsDialog()
                                    },
                                    modifier = Modifier.testTag("menu_settings_and_personalization")
                                )
                            }
                        }
                    }
                }
            )
        }

        // List Tabs (Horizontal list tabs)
        if (!isSearching && !isBatchSelectionMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Starred View Tab
                FilterChip(
                    selected = isStarredView,
                    onClick = onSelectStarred,
                    label = { Text("Starred", maxLines = 1, softWrap = false) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isStarredView) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = null,
                            tint = if (isStarredView) StarActiveColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("tab_starred")
                )

                // Completed View Tab
                FilterChip(
                    selected = isCompletedView,
                    onClick = onSelectCompleted,
                    label = { Text("Completed", maxLines = 1, softWrap = false) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isCompletedView) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (isCompletedView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("tab_completed")
                )

                // Upcoming Repeating View Tab (Dedicated list tab so repeating tasks are isolated & never forgotten)
                FilterChip(
                    selected = isUpcomingRecurringView,
                    onClick = onSelectUpcomingRecurring,
                    label = { Text("Repeating", maxLines = 1, softWrap = false) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Repeat,
                            contentDescription = null,
                            tint = if (isUpcomingRecurringView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("tab_repeating")
                )

                // Calendar Grid View Tab
                FilterChip(
                    selected = isCalendarView,
                    onClick = onSelectCalendarView,
                    label = { Text("Calendar", maxLines = 1, softWrap = false) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = null,
                            tint = if (isCalendarView) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("tab_calendar")
                )

                // Regular Lists
                lists.forEach { list ->
                    val isSelected = !isStarredView && !isCompletedView && !isUpcomingRecurringView && !isCalendarView && selectedListId == list.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectList(list.id) },
                        label = { Text(list.name, maxLines = 1, softWrap = false) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.FormatListBulleted,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("tab_list_${list.id}")
                    )
                }

                // Add New List Button
                AssistChip(
                    onClick = onOpenCreateList,
                    label = { Text("New list", maxLines = 1, softWrap = false) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "New list",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("tab_new_list_btn")
                )
            }
        }
    }
}
