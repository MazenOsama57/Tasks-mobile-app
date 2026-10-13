package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FocusSessionType
import com.example.data.model.TaskWithSubtasks
import com.example.ui.PomodoroTimerState
import com.example.ui.theme.TaskBlue
import com.example.ui.util.HapticFeedbackHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroTimerDialog(
    timerState: PomodoroTimerState,
    activeTasks: List<TaskWithSubtasks>,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onExtend: () -> Unit,
    onSkip: () -> Unit,
    onSelectDuration: (Int) -> Unit,
    onSelectSessionType: (FocusSessionType) -> Unit,
    onSelectBoundTask: (TaskWithSubtasks?) -> Unit,
    onToggleAutoMarkComplete: (Boolean) -> Unit,
    onCompleteEarly: (durationMinutes: Int) -> Unit,
    onDismissCelebration: () -> Unit,
    onDismiss: () -> Unit,
    hapticFeedbackEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val selectedSessionType = timerState.sessionType
    val selectedDurationMinutes = timerState.selectedDurationMinutes
    val totalSeconds = timerState.totalSeconds
    val secondsRemaining = timerState.secondsRemaining
    val isRunning = timerState.isRunning
    val boundTask = timerState.boundTask
    val autoMarkComplete = timerState.autoMarkComplete
    val pomodoroCount = timerState.pomodoroCount
    val sessionFinishedCelebration = timerState.sessionFinishedCelebration

    var showTaskPicker by remember { mutableStateOf(false) }

    // Active color scheme based on session mode
    val activeColor by animateColorAsState(
        targetValue = when (selectedSessionType) {
            FocusSessionType.WORK -> TaskBlue
            FocusSessionType.SHORT_BREAK -> Color(0xFF10B981) // Emerald green
            FocusSessionType.LONG_BREAK -> Color(0xFF8B5CF6) // Purple
        },
        label = "activeTimerColor"
    )

    val progress = if (totalSeconds > 0) {
        (totalSeconds - secondsRemaining).toFloat() / totalSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "ringProgressAnim"
    )

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = "%02d:%02d".format(minutes, seconds)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .testTag("pomodoro_timer_screen"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("pomodoro_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close focus timer",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Session title
                    Text(
                        text = if (selectedSessionType == FocusSessionType.WORK) "Focus Session #$pomodoroCount" else selectedSessionType.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Reset Timer Button
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.testTag("pomodoro_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Reset timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Tabs (Work, Short Break, Long Break)
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pomodoro_mode_tabs")
                ) {
                    FocusSessionType.values().forEachIndexed { index, type ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = FocusSessionType.values().size),
                            onClick = {
                                if (!isRunning) {
                                    onSelectSessionType(type)
                                }
                            },
                            selected = selectedSessionType == type,
                            label = {
                                Text(
                                    text = type.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedSessionType == type) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                // If in Work mode and not running, duration presets (15, 25, 45, 60 min)
                if (selectedSessionType == FocusSessionType.WORK && !isRunning) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        listOf(15, 25, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = selectedDurationMinutes == mins,
                                onClick = { onSelectDuration(mins) },
                                label = { Text("${mins}m") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TaskBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = TaskBlue
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Circular Progress Ring & Timer Display
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val trackColor = if (isDark) Color(0xFF232528) else Color(0xFFE5E7EB)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        // Background track
                        drawCircle(
                            color = trackColor,
                            style = Stroke(width = strokeWidth)
                        )
                        // Active animated progress arc
                        drawArc(
                            color = activeColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 48.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("pomodoro_time_display")
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = activeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isRunning) "Deep in focus..." else if (sessionFinishedCelebration) "Interval Completed! 🎉" else "Ready to Focus",
                                style = MaterialTheme.typography.labelMedium,
                                color = activeColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary Play / Pause & Quick Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // +5 Minutes Extension Button
                    FilledTonalIconButton(
                        onClick = onExtend,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pomodoro_add_5m_button")
                    ) {
                        Text("+5m", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Big Start / Pause Play Action Button
                    Button(
                        onClick = {
                            HapticFeedbackHelper.performSelectionToggleHaptic(context, enabled = hapticFeedbackEnabled)
                            if (isRunning) onPause() else onStart()
                        },
                        modifier = Modifier
                            .height(64.dp)
                            .width(140.dp)
                            .testTag("pomodoro_play_pause_button"),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = activeColor)
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Start",
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "Pause" else "Start",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Skip to Next Phase Button
                    FilledTonalIconButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("pomodoro_skip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Skip to next phase",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bound Task Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pomodoro_bound_task_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Assignment,
                                    contentDescription = null,
                                    tint = TaskBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Current Focus Task",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            TextButton(
                                onClick = { showTaskPicker = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (boundTask != null) "Change" else "Select", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (boundTask != null) {
                            Text(
                                text = boundTask.task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (boundTask.task.details.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = boundTask.task.details,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mark task complete on finish",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Switch(
                                    checked = autoMarkComplete,
                                    onCheckedChange = onToggleAutoMarkComplete,
                                    modifier = Modifier.testTag("pomodoro_auto_complete_switch")
                                )
                            }
                        } else {
                            Text(
                                text = "Free Focus Session (No task bound)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // If finished early or user wants to manually record session
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        val elapsedMinutes = ((totalSeconds - secondsRemaining) / 60).coerceAtLeast(1)
                        onCompleteEarly(elapsedMinutes)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pomodoro_complete_early_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Early & Log Minutes")
                }

                // Session Finished Celebration banner
                AnimatedVisibility(visible = sessionFinishedCelebration) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Session Completed! 🎉",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$selectedDurationMinutes minutes logged toward your daily streak.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onDismissCelebration,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("Continue to Next Interval")
                            }
                        }
                    }
                }
            }
        }
    }

    // Task Picker Dialog
    if (showTaskPicker) {
        AlertDialog(
            onDismissRequest = { showTaskPicker = false },
            title = { Text("Select Task for Focus") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Option to clear binding
                    Surface(
                        onClick = {
                            onSelectBoundTask(null)
                            showTaskPicker = false
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Free focus (No bound task)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    activeTasks.forEach { taskWithSubs ->
                        Surface(
                            onClick = {
                                onSelectBoundTask(taskWithSubs)
                                showTaskPicker = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = if (boundTask?.task?.id == taskWithSubs.task.id) {
                                TaskBlue.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = taskWithSubs.task.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTaskPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
