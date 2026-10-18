package com.example.ui.achievements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementCategory
import com.example.data.model.AchievementDef
import com.example.data.model.AchievementRegistry
import com.example.data.model.AchievementTier
import com.example.data.model.UserProductivityStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    userStats: UserProductivityStats,
    modifier: Modifier = Modifier
) {
    val levelInfo = remember(userStats.totalXp) { userStats.calculateLevel() }
    var selectedCategory by remember { mutableStateOf<AchievementCategory?>(null) }
    var showRoadmap by remember { mutableStateOf(false) }

    val filteredAchievements = remember(selectedCategory) {
        if (selectedCategory == null) AchievementRegistry.ALL_ACHIEVEMENTS
        else AchievementRegistry.ALL_ACHIEVEMENTS.filter { it.category == selectedCategory }
    }

    val categoryCounts = remember {
        AchievementCategory.values().associateWith { cat ->
            AchievementRegistry.ALL_ACHIEVEMENTS.count { it.category == cat }
        }
    }

    // Achievement Badges List (precomputed and memoized to eliminate scroll lag)
    val badgeUiModels = remember(filteredAchievements, userStats) {
        val unlockedSet = userStats.unlockedBadgesSet
        filteredAchievements.map { badge ->
            val isUnlocked = unlockedSet.contains(badge.id)
            val currentCount = badge.currentCountExtractor(userStats)
            val progressFraction = (currentCount.toFloat() / badge.targetCount.toFloat()).coerceIn(0f, 1f)
            BadgeUiModel(badge, isUnlocked, currentCount, progressFraction)
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("achievements_screen")
    ) {
        // Hero Level Banner
        item {
            val tierGradient = when (levelInfo.tier) {
                "5-Year Eternal" -> listOf(Color(0xFFFDA4AF), Color(0xFFE11D48), Color(0xFF9F1239))
                "Mythic" -> listOf(Color(0xFFE9D5FF), Color(0xFFA855F7), Color(0xFF7E22CE))
                "Diamond" -> listOf(Color(0xFFC7D2FE), Color(0xFF6366F1), Color(0xFF4338CA))
                "Platinum" -> listOf(Color(0xFFBAE6FD), Color(0xFF0284C7), Color(0xFF0369A1))
                "Gold" -> listOf(Color(0xFFFDE68A), Color(0xFFF59E0B), Color(0xFFB45309))
                "Silver" -> listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8), Color(0xFF475569))
                else -> listOf(Color(0xFFFED7AA), Color(0xFFEA580C), Color(0xFF9A3412)) // Bronze
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Level Avatar Badge with Tier-reflective gradient
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(colors = tierGradient))
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (levelInfo.tier) {
                                    "5-Year Eternal" -> Icons.Filled.EmojiEvents
                                    "Mythic" -> Icons.Filled.AutoAwesome
                                    "Diamond" -> Icons.Filled.Diamond
                                    "Platinum" -> Icons.Filled.WorkspacePremium
                                    "Gold" -> Icons.Filled.Stars
                                    else -> Icons.Filled.MilitaryTech
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "LVL ${levelInfo.level}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title & Tier Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = levelInfo.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${levelInfo.tier} Tier  •  ${String.format("%,d", userStats.totalXp)} Total XP",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // XP Progress to next level
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Next Level (LVL ${levelInfo.level + 1}):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = "${String.format("%,d", levelInfo.currentLevelXp)} / ${String.format("%,d", levelInfo.nextLevelXpRequired)} XP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { levelInfo.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Stats Overview Grid (4 items)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    title = "Tasks Done",
                    value = String.format("%,d", userStats.tasksCompletedCount),
                    icon = Icons.Filled.CheckCircle,
                    accentColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Habits Logged",
                    value = String.format("%,d", userStats.habitsCompletedCount),
                    icon = Icons.Filled.LocalFireDepartment,
                    accentColor = Color(0xFFF97316),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val focusHours = userStats.focusMinutesTotal / 60
                val focusDisplay = if (focusHours > 0) {
                    val rem = userStats.focusMinutesTotal % 60
                    "${focusHours}h ${rem}m"
                } else {
                    "${userStats.focusMinutesTotal}m"
                }
                StatMetricCard(
                    title = "Deep Focus",
                    value = focusDisplay,
                    icon = Icons.Filled.HourglassTop,
                    accentColor = Color(0xFF6366F1),
                    modifier = Modifier.weight(1f)
                )
                StatMetricCard(
                    title = "Best Streak",
                    value = "${userStats.longestStreakDays} days",
                    icon = Icons.Filled.Bolt,
                    accentColor = Color(0xFFEAB308),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 5-Year Motivational Roadmap Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRoadmap = !showRoadmap }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "5-Year Longevity Roadmap",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (showRoadmap) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (showRoadmap) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Designed for 10+ daily tasks across 5+ years of lifelong motivation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    AnimatedVisibility(visible = showRoadmap) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            RoadmapMilestoneRow("Day 1 - Week 1", "10 - 50 Tasks", "Novice to Apprentice", Color(0xFFCD7F32))
                            RoadmapMilestoneRow("Year 1 (~10/day)", "3,650 Tasks · 365d Streak", "LVL 35+ Titan of Execution", Color(0xFF0284C7))
                            RoadmapMilestoneRow("Year 2", "7,300 Tasks · 730d Streak", "LVL 60+ Apex Achiever", Color(0xFF6366F1))
                            RoadmapMilestoneRow("Year 3", "10,000 Tasks · 1,095d Streak", "LVL 75+ Legendary Champion", Color(0xFFA855F7))
                            RoadmapMilestoneRow("Year 4", "14,600 Tasks · 2,000 Focus", "LVL 90+ Mythic Architect", Color(0xFFA855F7))
                            RoadmapMilestoneRow("Year 5 Odyssey", "18,250 Tasks · 1,825d Streak", "LVL 100+ Eternal Sovereign", Color(0xFFE11D48))
                            RoadmapMilestoneRow("Lifetime Legacy", "20,000+ Tasks", "LVL 150+ Cosmic Immortal", Color(0xFFF59E0B))
                        }
                    }
                }
            }
        }

        // Badges Section Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Achievement Badges",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${userStats.unlockedBadgesSet.size} of ${AchievementRegistry.ALL_ACHIEVEMENTS.size} unlocked",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All (${AchievementRegistry.ALL_ACHIEVEMENTS.size})") },
                    shape = RoundedCornerShape(10.dp)
                )

                AchievementCategory.values().forEach { cat ->
                    val count = categoryCounts[cat] ?: 0
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text("${cat.label} ($count)") },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Achievement Badges List
        items(badgeUiModels, key = { it.badge.id }) { item ->
            AchievementCardItem(
                badge = item.badge,
                isUnlocked = item.isUnlocked,
                currentCount = item.currentCount,
                progressFraction = item.progressFraction
            )
        }
    }
}

private data class BadgeUiModel(
    val badge: AchievementDef,
    val isUnlocked: Boolean,
    val currentCount: Int,
    val progressFraction: Float
)

@Composable
private fun RoadmapMilestoneRow(
    period: String,
    target: String,
    title: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = period,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = target,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = accentColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
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
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AchievementCardItem(
    badge: AchievementDef,
    isUnlocked: Boolean,
    currentCount: Int,
    progressFraction: Float,
    modifier: Modifier = Modifier
) {
    val tierColor = Color(badge.tier.colorHex)

    val badgeIcon = when (badge.iconName) {
        "check", "check_circle" -> Icons.Filled.CheckCircle
        "bolt", "flash_on" -> Icons.Filled.Bolt
        "stars" -> Icons.Filled.Stars
        "speed" -> Icons.Filled.Speed
        "military_tech" -> Icons.Filled.MilitaryTech
        "workspace_premium" -> Icons.Filled.WorkspacePremium
        "emoji_events" -> Icons.Filled.EmojiEvents
        "auto_awesome" -> Icons.Filled.AutoAwesome
        "shield" -> Icons.Filled.Shield
        "diamond" -> Icons.Filled.Diamond
        "whatshot", "fire", "local_fire" -> Icons.Filled.Whatshot
        "timer", "schedule", "hourglass_top" -> Icons.Filled.HourglassTop
        "self_improvement" -> Icons.Filled.SelfImprovement
        "grid_view" -> Icons.Filled.GridView
        "view_column" -> Icons.Filled.ViewColumn
        "flag" -> Icons.Filled.Flag
        else -> Icons.Filled.Stars
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = if (isUnlocked) BorderStroke(1.5.dp, tierColor.copy(alpha = 0.5f)) else null,
        modifier = modifier
            .fillMaxWidth()
            .testTag("badge_${badge.id}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge Icon with Tier tint
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) Brush.radialGradient(listOf(tierColor.copy(alpha = 0.3f), tierColor))
                        else Brush.radialGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isUnlocked) badgeIcon else Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) Color.White else Color(0xFF475569),
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Title and XP Reward
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = badge.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUnlocked) tierColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isUnlocked) tierColor else MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = "+${String.format("%,d", badge.xpReward)} XP",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Tier indicator and description
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = tierColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = badge.tier.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = tierColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isUnlocked) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Unlocked",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = tierColor,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${String.format("%,d", currentCount)} / ${String.format("%,d", badge.targetCount)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
