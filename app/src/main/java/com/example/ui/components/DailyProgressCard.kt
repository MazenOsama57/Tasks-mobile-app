package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.TaskBlue
import com.example.ui.util.CoachTone
import com.example.ui.util.DailyCoachFeedback

@Composable
fun DailyProgressCard(
    feedback: DailyCoachFeedback,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = feedback.progress,
        animationSpec = tween(durationMillis = 600),
        label = "dailyProgressAnim"
    )

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val (cardBg, accentColor, icon) = when (feedback.tone) {
        CoachTone.SUCCESS -> if (isDark) {
            Triple(Color(0xFF0F3822), Color(0xFF34D399), Icons.Filled.EmojiEvents)
        } else {
            Triple(Color(0xFFE6F4EA), Color(0xFF137333), Icons.Filled.EmojiEvents)
        }
        CoachTone.DISAPPOINTED_OVERDUE -> if (isDark) {
            Triple(Color(0xFF3B1219), Color(0xFFFB7185), Icons.Filled.Warning)
        } else {
            Triple(Color(0xFFFCE8E6), Color(0xFFC5221F), Icons.Filled.Warning)
        }
        CoachTone.DISAPPOINTED_ZERO -> if (isDark) {
            Triple(Color(0xFF2E1A0F), Color(0xFFFBBF24), Icons.Filled.HourglassEmpty)
        } else {
            Triple(Color(0xFFFEF7E0), Color(0xFFB06000), Icons.Filled.HourglassEmpty)
        }
        CoachTone.ENCOURAGING_HIGH -> if (isDark) {
            Triple(Color(0xFF0C2B3E), Color(0xFF38BDF8), Icons.Filled.Bolt)
        } else {
            Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), Icons.Filled.Bolt)
        }
        CoachTone.ENCOURAGING_MID -> if (isDark) {
            Triple(Color(0xFF1E1B38), Color(0xFFA78BFA), Icons.AutoMirrored.Filled.TrendingUp)
        } else {
            Triple(Color(0xFFF3E8FF), Color(0xFF7E22CE), Icons.AutoMirrored.Filled.TrendingUp)
        }
        CoachTone.ENCOURAGING_LOW -> if (isDark) {
            Triple(Color(0xFF182230), Color(0xFF60A5FA), Icons.Filled.Flag)
        } else {
            Triple(Color(0xFFE8F0FE), Color(0xFF1A73E8), Icons.Filled.Flag)
        }
        CoachTone.CLEAR_DAY -> if (isDark) {
            Triple(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), TaskBlue, Icons.Filled.CheckCircle)
        } else {
            Triple(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), TaskBlue, Icons.Filled.CheckCircle)
        }
    }

    val primaryTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
    val trackBgColor = if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("daily_progress_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon + Headline + Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = feedback.headline,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryTextColor
                        )
                        Text(
                            text = "${(feedback.progress * 100).toInt()}% • ${feedback.completedCount} of ${feedback.totalCount} completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated Linear Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .testTag("daily_progress_bar"),
                color = accentColor,
                trackColor = trackBgColor
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Motivational Feedback Message
            Text(
                text = feedback.message,
                style = MaterialTheme.typography.bodySmall,
                color = primaryTextColor.copy(alpha = if (isDark) 0.9f else 0.85f),
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}
