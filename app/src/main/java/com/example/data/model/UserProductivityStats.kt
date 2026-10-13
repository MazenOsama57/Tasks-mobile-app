package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_productivity_stats")
data class UserProductivityStats(
    @PrimaryKey
    val id: Long = 1,
    val totalXp: Int = 120, // Initial starter XP
    val tasksCompletedCount: Int = 1,
    val habitsCompletedCount: Int = 0,
    val focusMinutesTotal: Int = 0,
    val focusSessionsCompleted: Int = 0,
    val longestStreakDays: Int = 0,
    val unlockedBadgeIds: String = "FIRST_STEP", // Comma separated IDs
    val lastActiveDate: String = ""
) {
    val unlockedBadgesSet: Set<String> by lazy {
        if (unlockedBadgeIds.isBlank()) emptySet()
        else unlockedBadgeIds.split(",").map { it.trim() }.toSet()
    }

    /**
     * Level calculation:
     * Level 1: 0 - 99 XP
     * Level 2: 100 - 249 XP (+150)
     * Level 3: 250 - 449 XP (+200)
     * Level 4: 450 - 699 XP (+250)
     * etc.
     */
    fun calculateLevel(): LevelInfo {
        var currentLevel = 1
        var xpRequiredForCurrent = 0
        var xpRequiredForNext = 100

        while (totalXp >= xpRequiredForNext && currentLevel < 50) {
            currentLevel++
            xpRequiredForCurrent = xpRequiredForNext
            xpRequiredForNext += 100 + (currentLevel * 50)
        }

        val xpInCurrentLevel = totalXp - xpRequiredForCurrent
        val xpRange = xpRequiredForNext - xpRequiredForCurrent
        val progress = if (xpRange > 0) (xpInCurrentLevel.toFloat() / xpRange).coerceIn(0f, 1f) else 1f

        val title = when (currentLevel) {
            1 -> "Novice Organizer"
            2 -> "Task Apprentice"
            3 -> "Focus Initiate"
            4 -> "Productivity Builder"
            5 -> "Habit Architect"
            6 -> "Momentum Driver"
            7 -> "Workflow Specialist"
            8 -> "Focus Master"
            9 -> "Efficiency Virtuoso"
            10 -> "Deep Work Pioneer"
            11 -> "Streak Legend"
            12 -> "Grandmaster of Focus"
            else -> "Productivity Sovereign"
        }

        return LevelInfo(
            level = currentLevel,
            title = title,
            currentLevelXp = xpInCurrentLevel,
            nextLevelXpRequired = xpRange,
            totalLevelXpTarget = xpRequiredForNext,
            progress = progress
        )
    }
}

data class LevelInfo(
    val level: Int,
    val title: String,
    val currentLevelXp: Int,
    val nextLevelXpRequired: Int,
    val totalLevelXpTarget: Int,
    val progress: Float
)

enum class AchievementCategory(val label: String) {
    TASKS("Tasks"),
    HABITS("Habits & Streaks"),
    FOCUS("Focus & Pomodoro"),
    WORKFLOWS("Matrix & Kanban")
}

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val category: AchievementCategory,
    val iconName: String,
    val targetCount: Int,
    val currentCountExtractor: (UserProductivityStats) -> Int
)

object AchievementRegistry {
    val ALL_ACHIEVEMENTS = listOf(
        AchievementDef(
            id = "FIRST_STEP",
            title = "First Step",
            description = "Complete your very first task",
            xpReward = 50,
            category = AchievementCategory.TASKS,
            iconName = "check",
            targetCount = 1,
            currentCountExtractor = { it.tasksCompletedCount }
        ),
        AchievementDef(
            id = "TASK_EXPLORER",
            title = "Task Sprinter",
            description = "Complete 10 tasks",
            xpReward = 100,
            category = AchievementCategory.TASKS,
            iconName = "bolt",
            targetCount = 10,
            currentCountExtractor = { it.tasksCompletedCount }
        ),
        AchievementDef(
            id = "TASK_CENTURION",
            title = "Century Club",
            description = "Complete 50 tasks",
            xpReward = 250,
            category = AchievementCategory.TASKS,
            iconName = "stars",
            targetCount = 50,
            currentCountExtractor = { it.tasksCompletedCount }
        ),
        AchievementDef(
            id = "HABIT_STARTER",
            title = "Habit Starter",
            description = "Complete your first daily habit check-in",
            xpReward = 50,
            category = AchievementCategory.HABITS,
            iconName = "fire",
            targetCount = 1,
            currentCountExtractor = { it.habitsCompletedCount }
        ),
        AchievementDef(
            id = "STREAK_CHAMPION",
            title = "Streak Champion",
            description = "Build a 3-day continuous habit streak",
            xpReward = 150,
            category = AchievementCategory.HABITS,
            iconName = "local_fire",
            targetCount = 3,
            currentCountExtractor = { it.longestStreakDays }
        ),
        AchievementDef(
            id = "STREAK_LEGEND",
            title = "Unstoppable Force",
            description = "Build a 7-day continuous habit streak",
            xpReward = 300,
            category = AchievementCategory.HABITS,
            iconName = "diamond",
            targetCount = 7,
            currentCountExtractor = { it.longestStreakDays }
        ),
        AchievementDef(
            id = "FOCUS_INITIATE",
            title = "Deep Work Disciple",
            description = "Finish your first Pomodoro focus session",
            xpReward = 75,
            category = AchievementCategory.FOCUS,
            iconName = "timer",
            targetCount = 1,
            currentCountExtractor = { it.focusSessionsCompleted }
        ),
        AchievementDef(
            id = "ZEN_MASTER",
            title = "Zen Master",
            description = "Accumulate 100 minutes of pure focus time",
            xpReward = 200,
            category = AchievementCategory.FOCUS,
            iconName = "self_improvement",
            targetCount = 100,
            currentCountExtractor = { it.focusMinutesTotal }
        ),
        AchievementDef(
            id = "MATRIX_STRATEGIST",
            title = "Matrix Strategist",
            description = "Organize and conquer tasks in the Eisenhower Matrix",
            xpReward = 80,
            category = AchievementCategory.WORKFLOWS,
            iconName = "grid_view",
            targetCount = 5,
            currentCountExtractor = { it.tasksCompletedCount }
        ),
        AchievementDef(
            id = "KANBAN_FLOW",
            title = "Kanban Master",
            description = "Flow tasks seamlessly through Kanban columns",
            xpReward = 80,
            category = AchievementCategory.WORKFLOWS,
            iconName = "view_column",
            targetCount = 5,
            currentCountExtractor = { it.tasksCompletedCount }
        )
    )
}
