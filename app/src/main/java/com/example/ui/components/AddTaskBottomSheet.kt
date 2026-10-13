package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.data.model.Priority
import com.example.data.model.RecurrenceFrequency
import com.example.data.model.TaskList
import com.example.ui.theme.TaskBlue
import com.example.ui.theme.StarActiveColor
import com.example.ui.util.TaskDateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    lists: List<TaskList>,
    currentListId: Long?,
    initialDueDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onSaveTask: (
        title: String,
        details: String,
        dueDateMillis: Long?,
        hasTime: Boolean,
        priority: Priority,
        isStarred: Boolean,
        targetListId: Long,
        recurrenceFrequency: RecurrenceFrequency,
        recurrenceInterval: Int,
        recurrenceTimesPerPeriod: Int,
        recurrenceDaysOfWeek: String,
        recurrenceDaysOfMonth: String,
        tags: String
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var showDetailsInput by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(initialDueDateMillis) }
    var selectedPriority by remember { mutableStateOf(Priority.NONE) }
    var isStarred by remember { mutableStateOf(false) }
    var tagsString by remember { mutableStateOf("") }
    var customTagInput by remember { mutableStateOf("") }
    var showCustomTagInput by remember { mutableStateOf(false) }
    val presetTags = remember { listOf("Work", "Personal", "Urgent", "Study", "Health", "Finance") }
    var selectedListId by remember {
        mutableStateOf(currentListId ?: lists.firstOrNull()?.id ?: 1L)
    }

    // Recurrence
    var recurrenceFrequency by remember { mutableStateOf(RecurrenceFrequency.NONE) }
    var recurrenceInterval by remember { mutableIntStateOf(1) }
    var recurrenceTimesPerPeriod by remember { mutableIntStateOf(1) }
    var recurrenceDaysOfWeek by remember { mutableStateOf("") }
    var recurrenceDaysOfMonth by remember { mutableStateOf("") }
    var showRecurrenceDialog by remember { mutableStateOf(false) }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showListMenu by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(250)
        try {
            focusRequester.requestFocus()
        } catch (_: Exception) {}
    }

    fun handleDismiss() {
        keyboardController?.hide()
        focusManager.clearFocus()
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = ::handleDismiss,
        sheetState = sheetState,
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .imePadding()
                .navigationBarsPadding()
        ) {
            // Title Input
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = {
                    Text(
                        text = "New task",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                singleLine = false,
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .testTag("new_task_title_input"),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
            )

            // Smart NLP Parser Real-Time Preview
            val parsedResult = remember(title, lists) {
                com.example.ui.util.SmartTaskParser.parse(title, lists)
            }

            AnimatedVisibility(visible = parsedResult.tokens.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Smart Parser Detected",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            parsedResult.tokens.forEach { token ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val icon = when (token.type) {
                                            com.example.ui.util.ParsedTokenType.DATE_TIME -> Icons.Outlined.Event
                                            com.example.ui.util.ParsedTokenType.PRIORITY -> Icons.Outlined.Flag
                                            com.example.ui.util.ParsedTokenType.LIST -> Icons.Outlined.Folder
                                            com.example.ui.util.ParsedTokenType.TAG -> Icons.Outlined.Label
                                        }
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = token.displayLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = {
                                                // Remove this token from title
                                                title = title.replace(token.rawText, " ").replace(Regex("""\s+"""), " ").trim()
                                            },
                                            modifier = Modifier.size(14.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Remove token",
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Details Input (expandable)
            AnimatedVisibility(visible = showDetailsInput) {
                TextField(
                    value = details,
                    onValueChange = { details = it },
                    placeholder = {
                        Text(
                            text = "Add details",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .testTag("new_task_details_input")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action chips row 1: Date quick chips & Recurrence
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Details button toggle
                if (!showDetailsInput) {
                    AssistChip(
                        onClick = { showDetailsInput = true },
                        label = { Text("Details") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Notes,
                                contentDescription = "Add details",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("toggle_details_btn")
                    )
                }

                // Date Chip / Picker
                if (selectedDateMillis == null) {
                    AssistChip(
                        onClick = { showDatePickerDialog = true },
                        label = { Text("Set date") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Event,
                                contentDescription = "Date",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("set_date_btn")
                    )

                    FilterChip(
                        selected = false,
                        onClick = { selectedDateMillis = TaskDateUtils.getTodayMillis() },
                        label = { Text("Today") }
                    )

                    FilterChip(
                        selected = false,
                        onClick = { selectedDateMillis = TaskDateUtils.getTomorrowMillis() },
                        label = { Text("Tomorrow") }
                    )
                } else {
                    FilterChip(
                        selected = true,
                        onClick = { showDatePickerDialog = true },
                        label = { Text(TaskDateUtils.formatDueDate(selectedDateMillis)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Event,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { selectedDateMillis = null },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear date",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    )
                }

                // Recurrence Chip
                if (recurrenceFrequency == RecurrenceFrequency.NONE) {
                    AssistChip(
                        onClick = { showRecurrenceDialog = true },
                        label = { Text("Repeat") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = "Repeat",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("repeat_task_chip")
                    )
                } else {
                    FilterChip(
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
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { recurrenceFrequency = RecurrenceFrequency.NONE },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Clear repeat",
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action chips row 2: Priority selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Priority:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Priority.entries.forEach { priority ->
                    val isSelected = selectedPriority == priority
                    val chipColor = priority.getColor(isDark)

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPriority = priority },
                        label = {
                            Text(
                                text = priority.displayName,
                                color = if (isSelected && priority != Priority.NONE) chipColor else Color.Unspecified
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (priority != Priority.NONE) priority.getContainerColor(isDark) else MaterialTheme.colorScheme.secondaryContainer
                        ),
                        modifier = Modifier.testTag("priority_chip_${priority.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action chips row 3: Smart Tags & Categorization
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tags:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val activeTagList = tagsString.split(",")
                    .map { it.trim().removePrefix("#") }
                    .filter { it.isNotEmpty() }

                // Preset tags
                presetTags.forEach { preset ->
                    val isTagSelected = activeTagList.contains(preset)
                    FilterChip(
                        selected = isTagSelected,
                        onClick = {
                            val newTags = if (isTagSelected) {
                                activeTagList.filter { it != preset }
                            } else {
                                activeTagList + preset
                            }
                            tagsString = newTags.joinToString(",")
                        },
                        label = { Text("#$preset", style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tag_preset_${preset.lowercase()}")
                    )
                }

                // Custom tags already selected
                activeTagList.filter { it !in presetTags }.forEach { custom ->
                    FilterChip(
                        selected = true,
                        onClick = {
                            val newTags = activeTagList.filter { it != custom }
                            tagsString = newTags.joinToString(",")
                        },
                        label = { Text("#$custom", style = MaterialTheme.typography.labelSmall) },
                        trailingIcon = {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = "Remove tag", modifier = Modifier.size(12.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Add custom tag chip
                if (!showCustomTagInput) {
                    AssistChip(
                        onClick = { showCustomTagInput = true },
                        label = { Text("+ Tag", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("add_custom_tag_chip")
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
                        placeholder = { Text("e.g. ProjectX", style = MaterialTheme.typography.bodySmall) },
                        prefix = { Text("#", style = MaterialTheme.typography.bodySmall) },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("custom_tag_input")
                    )
                    Button(
                        onClick = {
                            val clean = customTagInput.trim().removePrefix("#")
                            if (clean.isNotEmpty()) {
                                val current = tagsString.split(",").map { it.trim().removePrefix("#") }.filter { it.isNotEmpty() }
                                if (!current.contains(clean)) {
                                    tagsString = (current + clean).joinToString(",")
                                }
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

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom bar: List selection & Star & Save Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // List Picker Dropdown
                Box {
                    val currentListName = lists.find { it.id == selectedListId }?.name ?: "My Tasks"
                    TextButton(
                        onClick = { showListMenu = true },
                        modifier = Modifier.testTag("list_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentListName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.ArrowDropDown,
                            contentDescription = "Select list",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showListMenu,
                        onDismissRequest = { showListMenu = false }
                    ) {
                        lists.forEach { list ->
                            DropdownMenuItem(
                                text = { Text(list.name) },
                                onClick = {
                                    selectedListId = list.id
                                    showListMenu = false
                                }
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Star toggle
                    IconButton(
                        onClick = { isStarred = !isStarred },
                        modifier = Modifier.testTag("new_task_star_button")
                    ) {
                        Icon(
                            imageVector = if (isStarred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Star task",
                            tint = if (isStarred) StarActiveColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                keyboardController?.hide()
                                focusManager.clearFocus()

                                val finalTitle = if (parsedResult.cleanTitle.isNotBlank()) parsedResult.cleanTitle else title
                                val finalDueDate = selectedDateMillis ?: parsedResult.dueDateMillis
                                val finalPriority = if (selectedPriority != Priority.NONE) selectedPriority else (parsedResult.priority ?: Priority.NONE)
                                val finalListId = if (selectedListId != (currentListId ?: lists.firstOrNull()?.id ?: 1L)) selectedListId else (parsedResult.targetListId ?: selectedListId)

                                val combinedTags = buildList {
                                    if (tagsString.isNotBlank()) {
                                        addAll(tagsString.split(",").map { it.trim() })
                                    }
                                    addAll(parsedResult.tags)
                                }.distinct().joinToString(",")

                                onSaveTask(
                                    finalTitle,
                                    details,
                                    finalDueDate,
                                    parsedResult.hasTime,
                                    finalPriority,
                                    isStarred,
                                    finalListId,
                                    recurrenceFrequency,
                                    recurrenceInterval,
                                    recurrenceTimesPerPeriod,
                                    recurrenceDaysOfWeek,
                                    recurrenceDaysOfMonth,
                                    combinedTags
                                )
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = TaskBlue),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("save_task_button")
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
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

    // Recurrence Selector Dialog
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
            showRecurrenceDialog = false
        }
    )
}
