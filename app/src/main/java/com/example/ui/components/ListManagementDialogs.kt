package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.TaskList
import com.example.ui.SortOrder
import com.example.ui.theme.TaskBlue

@Composable
fun CreateListDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCreateList: (String) -> Unit
) {
    if (!isOpen) return

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var listTitle by remember { mutableStateOf("") }

    fun handleDismiss() {
        keyboardController?.hide()
        focusManager.clearFocus()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = ::handleDismiss,
        title = { Text("Create new list", fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = listTitle,
                onValueChange = { listTitle = it },
                label = { Text("Enter list title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_list_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (listTitle.isNotBlank()) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onCreateList(listTitle)
                        listTitle = ""
                    }
                },
                enabled = listTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TaskBlue),
                modifier = Modifier.testTag("confirm_create_list_button")
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(
                onClick = ::handleDismiss,
                modifier = Modifier.testTag("cancel_create_list_button")
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameListDialog(
    list: TaskList?,
    onDismiss: () -> Unit,
    onRenameList: (TaskList, String) -> Unit
) {
    if (list == null) return

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var newTitle by remember(list) { mutableStateOf(list.name) }

    fun handleDismiss() {
        keyboardController?.hide()
        focusManager.clearFocus()
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = ::handleDismiss,
        title = { Text("Rename list", fontWeight = FontWeight.SemiBold) },
        text = {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("List title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rename_list_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newTitle.isNotBlank()) {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onRenameList(list, newTitle)
                    }
                },
                enabled = newTitle.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TaskBlue),
                modifier = Modifier.testTag("confirm_rename_list_button")
            ) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = ::handleDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteListDialog(
    list: TaskList?,
    onDismiss: () -> Unit,
    onDeleteList: (TaskList) -> Unit
) {
    if (list == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete list?", fontWeight = FontWeight.SemiBold) },
        text = {
            Text("All tasks in '${list.name}' will be permanently deleted.")
        },
        confirmButton = {
            TextButton(
                onClick = { onDeleteList(list) },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_delete_list_button")
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortOptionsBottomSheet(
    currentSort: SortOrder,
    onSelectSort: (SortOrder) -> Unit,
    currentDueDateFilter: com.example.data.repository.TaskRepository.DueDateFilter = com.example.data.repository.TaskRepository.DueDateFilter.ALL,
    onSelectDueDateFilter: (com.example.data.repository.TaskRepository.DueDateFilter) -> Unit = {},
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            SortOrder.entries.forEach { option ->
                val isSelected = option == currentSort
                Surface(
                    onClick = { onSelectSort(option) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) TaskBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .testTag("sort_option_${option.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val icon = when (option) {
                                SortOrder.MY_ORDER -> Icons.Outlined.Reorder
                                SortOrder.DUE_DATE -> Icons.Outlined.Event
                                SortOrder.PRIORITY -> Icons.Outlined.Flag
                                SortOrder.STARRED -> Icons.Outlined.Star
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) TaskBlue else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = TaskBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Filter by due date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            com.example.data.repository.TaskRepository.DueDateFilter.entries.forEach { filterOption ->
                val isSelected = filterOption == currentDueDateFilter
                Surface(
                    onClick = { onSelectDueDateFilter(filterOption) },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) TaskBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .testTag("due_date_filter_${filterOption.name.lowercase()}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DateRange,
                                contentDescription = null,
                                tint = if (isSelected) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = filterOption.label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) TaskBlue else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = TaskBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
