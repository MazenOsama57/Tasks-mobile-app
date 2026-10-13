package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.Subtask
import com.example.ui.util.HapticFeedbackHelper

@Composable
fun SubtaskItem(
    subtask: Subtask,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskCheckCircle(
            isCompleted = subtask.isCompleted,
            isLocked = isLocked,
            onClick = {
                if (!isLocked) {
                    HapticFeedbackHelper.performTaskCompleteHaptic(context, hapticFeedback)
                    onToggleComplete()
                }
            },
            testTag = "subtask_check_${subtask.id}"
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = subtask.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = if (subtask.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            color = if (subtask.isCompleted || isLocked) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isLocked) 0.5f else 0.6f)
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = !isLocked) {
                    HapticFeedbackHelper.performTaskCompleteHaptic(context, hapticFeedback)
                    onToggleComplete()
                }
        )

        IconButton(
            onClick = {
                HapticFeedbackHelper.performTaskDeleteHaptic(context, hapticFeedback)
                onDelete()
            },
            modifier = Modifier
                .size(36.dp)
                .testTag("delete_subtask_${subtask.id}")
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Delete subtask",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
