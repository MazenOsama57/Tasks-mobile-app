package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FocusSession
import com.example.data.model.TaskWithSubtasks
import com.example.ui.theme.TaskBlue
import java.text.SimpleDateFormat
import java.util.*

data class DayCompletionStat(
    val dayLabel: String,
    val completedCount: Int,
    val focusMinutes: Int,
    val isToday: Boolean
)

data class ProductivityStats(
    val currentStreakDays: Int,
    val longestStreakDays: Int,
    val completedTodayCount: Int,
    val totalCompletedCount: Int,
    val todayFocusMinutes: Int,
    val totalFocusMinutes: Int,
    val totalFocusSessions: Int,
    val weeklyVelocity: List<DayCompletionStat>,
    val recentSessions: List<FocusSession>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityAnalyticsDialog(
    stats: ProductivityStats,
    onDismiss: () -> Unit,
    onStartFocusSession: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .testTag("productivity_analytics_screen"),
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("analytics_close_button")
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Close analytics")
                    }

                    Text(
                        text = "Productivity & Streaks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = onStartFocusSession,
                        modifier = Modifier.testTag("analytics_start_focus_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HourglassTop,
                            contentDescription = "Start Focus",
                            tint = TaskBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Streak Banner Card
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (isDark) Color(0xFF2E1708) else Color(0xFFFFF7ED),
                    border = BorderStroke(1.5.dp, Color(0xFFF97316).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("streak_banner_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF97316).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalFireDepartment,
                                        contentDescription = "Streak Fire",
                                        tint = Color(0xFFF97316),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${stats.currentStreakDays} Day Streak",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color(0xFFFFEDD5) else Color(0xFF9A3412)
                                    )
                                    Text(
                                        text = "Best record: ${stats.longestStreakDays} days",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isDark) Color(0xFFFDBA74) else Color(0xFFC2410C)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = if (stats.currentStreakDays > 0) {
                                    "Keep the flame burning! Complete tasks daily to increase your streak."
                                } else {
                                    "Complete a task today to ignite your completion streak!"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFFFFEDD5).copy(alpha = 0.85f) else Color(0xFF7C2D12)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2x2 Metric Cards Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Metric 1: Tasks Today
                    MetricCard(
                        title = "Completed Today",
                        value = "${stats.completedTodayCount}",
                        subtitle = "All time: ${stats.totalCompletedCount}",
                        icon = Icons.Outlined.CheckCircle,
                        accentColor = TaskBlue,
                        modifier = Modifier.weight(1f)
                    )

                    // Metric 2: Focus Minutes Today
                    MetricCard(
                        title = "Focus Time Today",
                        value = "${stats.todayFocusMinutes}m",
                        subtitle = "Total: ${stats.totalFocusMinutes}m logged",
                        icon = Icons.Outlined.HourglassTop,
                        accentColor = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Weekly Velocity Chart Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_velocity_chart")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "7-Day Completion Velocity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TaskBlue.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Tasks & Focus",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TaskBlue,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val maxTasks = stats.weeklyVelocity.maxOfOrNull { it.completedCount }?.coerceAtLeast(4) ?: 4

                        // Bar Chart Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            stats.weeklyVelocity.forEach { dayStat ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val barHeightRatio = (dayStat.completedCount.toFloat() / maxTasks.toFloat()).coerceIn(0.08f, 1f)
                                    val barColor = if (dayStat.isToday) TaskBlue else TaskBlue.copy(alpha = 0.4f)

                                    Text(
                                        text = "${dayStat.completedCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (dayStat.isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (dayStat.isToday) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height((75 * barHeightRatio).dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(barColor)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = dayStat.dayLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (dayStat.isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = if (dayStat.isToday) TaskBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Milestone Badges
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Milestones & Achievements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Badges list
                        AchievementBadgeItem(
                            title = "Deep Focus Initiate",
                            description = "Complete your first Pomodoro session",
                            isUnlocked = stats.totalFocusSessions >= 1,
                            icon = Icons.Filled.HourglassFull
                        )

                        AchievementBadgeItem(
                            title = "Consistency Spark",
                            description = "Achieve a 3-day completion streak",
                            isUnlocked = stats.longestStreakDays >= 3,
                            icon = Icons.Filled.ElectricBolt
                        )

                        AchievementBadgeItem(
                            title = "Habit Master",
                            description = "Reach an unbroken 7-day streak",
                            isUnlocked = stats.longestStreakDays >= 7,
                            icon = Icons.Filled.EmojiEvents
                        )

                        AchievementBadgeItem(
                            title = "Centurion Focus",
                            description = "Log over 60 minutes of deep focus",
                            isUnlocked = stats.totalFocusMinutes >= 60,
                            icon = Icons.Filled.MilitaryTech
                        )

                        AchievementBadgeItem(
                            title = "Task Master",
                            description = "Complete 20 tasks in total",
                            isUnlocked = stats.totalCompletedCount >= 20,
                            icon = Icons.Filled.Star
                        )
                    }
                }

                // Recent Focus Sessions History
                if (stats.recentSessions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Recent Focus Sessions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

                            stats.recentSessions.take(5).forEach { session ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(TaskBlue)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (session.taskTitle.isNotBlank()) session.taskTitle else "Free Focus Work",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = dateFormat.format(Date(session.completedAt)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = TaskBlue.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "+${session.durationMinutes}m",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TaskBlue,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun AchievementBadgeItem(
    title: String,
    description: String,
    isUnlocked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        if (isUnlocked) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF10B981).copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Unlocked",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        } else {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Locked",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
