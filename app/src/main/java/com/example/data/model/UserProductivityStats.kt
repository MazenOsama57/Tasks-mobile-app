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
     * Level calculation designed for longevity:
     * Easily supports daily active users completing 10+ tasks/day over 5+ years.
     * Scales across 100+ levels with motivating multi-year milestones.
     */
    fun calculateLevel(): LevelInfo {
        var currentLevel = 1
        var xpRequiredForCurrent = 0
        var xpRequiredForNext = 100

        // Cap at 200 to support beyond 5 years of daily execution
        while (totalXp >= xpRequiredForNext && currentLevel < 200) {
            currentLevel++
            xpRequiredForCurrent = xpRequiredForNext
            val step = when {
                currentLevel < 15 -> 50
                currentLevel < 35 -> 75
                currentLevel < 70 -> 100
                currentLevel < 100 -> 125
                else -> 150
            }
            xpRequiredForNext += 100 + (currentLevel * step)
        }

        val xpInCurrentLevel = totalXp - xpRequiredForCurrent
        val xpRange = xpRequiredForNext - xpRequiredForCurrent
        val progress = if (xpRange > 0) (xpInCurrentLevel.toFloat() / xpRange).coerceIn(0f, 1f) else 1f

        val (title, tier) = when {
            currentLevel == 1 -> "Novice Organizer" to "Bronze"
            currentLevel == 2 -> "Task Apprentice" to "Bronze"
            currentLevel == 3 -> "Focus Initiate" to "Bronze"
            currentLevel == 4 -> "Productivity Builder" to "Bronze"
            currentLevel in 5..7 -> "Habit Architect" to "Silver"
            currentLevel in 8..11 -> "Momentum Driver" to "Silver"
            currentLevel in 12..15 -> "Workflow Specialist" to "Silver"
            currentLevel in 16..20 -> "Focus Maestro" to "Gold"
            currentLevel in 21..25 -> "Efficiency Virtuoso" to "Gold"
            currentLevel in 26..30 -> "Deep Work Pioneer" to "Gold"
            currentLevel in 31..37 -> "Titan of Execution (Year 1)" to "Platinum"
            currentLevel in 38..45 -> "Master of Momentum" to "Platinum"
            currentLevel in 46..55 -> "Centurion Grandmaster" to "Platinum"
            currentLevel in 56..65 -> "Apex Achiever (Year 2)" to "Diamond"
            currentLevel in 66..75 -> "Unstoppable Force" to "Diamond"
            currentLevel in 76..85 -> "Legendary Champion (Year 3)" to "Mythic"
            currentLevel in 86..95 -> "Mythic Architect (Year 4)" to "Mythic"
            currentLevel in 96..99 -> "Productivity Demigod" to "Mythic"
            currentLevel in 100..120 -> "Five-Year Eternal Sovereign (Year 5)" to "5-Year Eternal"
            currentLevel in 121..150 -> "Cosmic Paragon of Time" to "5-Year Eternal"
            else -> "Transcendent Immortal of Focus" to "5-Year Eternal"
        }

        return LevelInfo(
            level = currentLevel,
            title = title,
            tier = tier,
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
    val progress: Float,
    val tier: String = "Bronze"
)

enum class AchievementCategory(val label: String) {
    TASKS("Tasks"),
    HABITS("Habits & Streaks"),
    FOCUS("Focus & Pomodoro"),
    WORKFLOWS("Matrix & Kanban")
}

enum class AchievementTier(val label: String, val colorHex: Long) {
    BRONZE("Bronze", 0xFFCD7F32),
    SILVER("Silver", 0xFF94A3B8),
    GOLD("Gold", 0xFFF59E0B),
    PLATINUM("Platinum", 0xFF38BDF8),
    DIAMOND("Diamond", 0xFF818CF8),
    MYTHIC("Mythic", 0xFFA855F7),
    ETERNAL("5-Year Eternal", 0xFFF43F5E)
}

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val category: AchievementCategory,
    val iconName: String,
    val targetCount: Int,
    val currentCountExtractor: (UserProductivityStats) -> Int,
    val tier: AchievementTier = AchievementTier.BRONZE
)

object AchievementRegistry {
    val ALL_ACHIEVEMENTS: List<AchievementDef> = listOf(
        // ==========================================
        // TASKS CATEGORY (Day 1 to 5+ Years = 20,000 tasks)
        // ==========================================
        AchievementDef(
            id = "FIRST_STEP",
            title = "First Step",
            description = "Complete your very first task",
            xpReward = 50,
            category = AchievementCategory.TASKS,
            iconName = "check",
            targetCount = 1,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "TASK_SPRINTER",
            title = "Task Sprinter",
            description = "Complete 10 tasks (Day 1 milestone)",
            xpReward = 100,
            category = AchievementCategory.TASKS,
            iconName = "bolt",
            targetCount = 10,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "WEEKLY_WARRIOR",
            title = "Weekly Warrior",
            description = "Complete 50 tasks (Week 1 milestone)",
            xpReward = 200,
            category = AchievementCategory.TASKS,
            iconName = "flash_on",
            targetCount = 50,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "TASK_CENTURION",
            title = "Century Club",
            description = "Complete 100 tasks (~10 days milestone)",
            xpReward = 350,
            category = AchievementCategory.TASKS,
            iconName = "stars",
            targetCount = 100,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "EXECUTION_ENGINE",
            title = "Execution Engine",
            description = "Complete 250 tasks (~1 month milestone)",
            xpReward = 500,
            category = AchievementCategory.TASKS,
            iconName = "speed",
            targetCount = 250,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "PRODUCTIVITY_POWERHOUSE",
            title = "Productivity Powerhouse",
            description = "Complete 500 tasks (~2 months of daily action)",
            xpReward = 800,
            category = AchievementCategory.TASKS,
            iconName = "military_tech",
            targetCount = 500,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "TASK_MILLENNIUM",
            title = "The 1,000 Milestone",
            description = "Complete 1,000 tasks (~3 months of consistency)",
            xpReward = 1500,
            category = AchievementCategory.TASKS,
            iconName = "workspace_premium",
            targetCount = 1000,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.GOLD
        ),
        AchievementDef(
            id = "QUARTERLY_CONQUEROR",
            title = "Quarterly Conqueror",
            description = "Complete 2,500 tasks (~8 months of daily output)",
            xpReward = 2500,
            category = AchievementCategory.TASKS,
            iconName = "emoji_events",
            targetCount = 2500,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.GOLD
        ),
        AchievementDef(
            id = "ANNUAL_TITAN",
            title = "Annual Titan",
            description = "Complete 3,650 tasks (1 Full Year of 10 tasks/day!)",
            xpReward = 4500,
            category = AchievementCategory.TASKS,
            iconName = "auto_awesome",
            targetCount = 3650,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.PLATINUM
        ),
        AchievementDef(
            id = "FIVE_THOUSAND_FORCE",
            title = "Five-Thousand Force",
            description = "Complete 5,000 tasks (~1.5 Years of relentless dedication)",
            xpReward = 6000,
            category = AchievementCategory.TASKS,
            iconName = "shield",
            targetCount = 5000,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.PLATINUM
        ),
        AchievementDef(
            id = "BIENNIAL_SENTINEL",
            title = "Two-Year Sentinel",
            description = "Complete 7,300 tasks (2 Full Years of 10 tasks/day!)",
            xpReward = 8500,
            category = AchievementCategory.TASKS,
            iconName = "diamond",
            targetCount = 7300,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.DIAMOND
        ),
        AchievementDef(
            id = "DECA_THOUSAND_VANGUARD",
            title = "Ten-Thousand Vanguard",
            description = "Complete 10,000 tasks (~3 Years of unmatched discipline)",
            xpReward = 12000,
            category = AchievementCategory.TASKS,
            iconName = "whatshot",
            targetCount = 10000,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.DIAMOND
        ),
        AchievementDef(
            id = "FOUR_YEAR_CHAMPION",
            title = "Four-Year Champion",
            description = "Complete 14,600 tasks (4 Full Years of 10 tasks/day!)",
            xpReward = 16000,
            category = AchievementCategory.TASKS,
            iconName = "military_tech",
            targetCount = 14600,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.MYTHIC
        ),
        AchievementDef(
            id = "FIVE_YEAR_ODYSSEY",
            title = "The Five-Year Odyssey",
            description = "Complete 18,250 tasks (5 Full Years of 10 tasks/day!)",
            xpReward = 25000,
            category = AchievementCategory.TASKS,
            iconName = "emoji_events",
            targetCount = 18250,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.ETERNAL
        ),
        AchievementDef(
            id = "ETERNAL_TASK_SOVEREIGN",
            title = "Eternal Task Sovereign",
            description = "Complete 20,000 tasks (Ultimate Lifetime Legacy)",
            xpReward = 30000,
            category = AchievementCategory.TASKS,
            iconName = "workspace_premium",
            targetCount = 20000,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.ETERNAL
        ),

        // ==========================================
        // HABITS & STREAKS (1 to 1,825 days = 5 Years)
        // ==========================================
        AchievementDef(
            id = "HABIT_STARTER",
            title = "Habit Starter",
            description = "Complete your first daily habit check-in",
            xpReward = 50,
            category = AchievementCategory.HABITS,
            iconName = "fire",
            targetCount = 1,
            currentCountExtractor = { it.habitsCompletedCount },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "STREAK_CHAMPION",
            title = "Streak Sparks",
            description = "Build a 3-day continuous habit streak",
            xpReward = 150,
            category = AchievementCategory.HABITS,
            iconName = "local_fire",
            targetCount = 3,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "STREAK_LEGEND",
            title = "Week of Willpower",
            description = "Build a 7-day continuous habit streak",
            xpReward = 300,
            category = AchievementCategory.HABITS,
            iconName = "diamond",
            targetCount = 7,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "FORTNIGHT_FORTITUDE",
            title = "Fortnight of Fortitude",
            description = "Maintain a 14-day continuous habit streak",
            xpReward = 500,
            category = AchievementCategory.HABITS,
            iconName = "shield",
            targetCount = 14,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "MONTHLY_MASTERY",
            title = "Monthly Mastery",
            description = "Maintain a 30-day unbroken habit streak",
            xpReward = 1000,
            category = AchievementCategory.HABITS,
            iconName = "stars",
            targetCount = 30,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "TWO_MONTH_DYNAMO",
            title = "Two-Month Dynamo",
            description = "Achieve a 60-day unbroken habit streak",
            xpReward = 1800,
            category = AchievementCategory.HABITS,
            iconName = "flash_on",
            targetCount = 60,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.GOLD
        ),
        AchievementDef(
            id = "CENTURION_STREAK",
            title = "Triple-Digit Centurion",
            description = "Achieve a 100-day uninterrupted habit streak",
            xpReward = 3000,
            category = AchievementCategory.HABITS,
            iconName = "workspace_premium",
            targetCount = 100,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.GOLD
        ),
        AchievementDef(
            id = "HALF_YEAR_UNBROKEN",
            title = "Half-Year Unbroken",
            description = "Achieve a 180-day unbroken streak (~6 months)",
            xpReward = 5000,
            category = AchievementCategory.HABITS,
            iconName = "auto_awesome",
            targetCount = 180,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.PLATINUM
        ),
        AchievementDef(
            id = "ONE_YEAR_ETERNAL_FLAME",
            title = "One-Year Eternal Flame",
            description = "Achieve a 365-day unbroken streak (1 Full Year!)",
            xpReward = 10000,
            category = AchievementCategory.HABITS,
            iconName = "whatshot",
            targetCount = 365,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.DIAMOND
        ),
        AchievementDef(
            id = "TWO_YEAR_LEGEND",
            title = "Two-Year Habit Legend",
            description = "Achieve a 730-day unbroken streak (2 Full Years!)",
            xpReward = 16000,
            category = AchievementCategory.HABITS,
            iconName = "diamond",
            targetCount = 730,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.MYTHIC
        ),
        AchievementDef(
            id = "THREE_YEAR_TITAN",
            title = "Three-Year Iron Will",
            description = "Achieve a 1,095-day unbroken streak (3 Full Years!)",
            xpReward = 22000,
            category = AchievementCategory.HABITS,
            iconName = "military_tech",
            targetCount = 1095,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.MYTHIC
        ),
        AchievementDef(
            id = "FIVE_YEAR_DIAMOND_STREAK",
            title = "Five-Year Diamond Streak",
            description = "Achieve a 1,825-day unbroken streak (5 Full Years!)",
            xpReward = 35000,
            category = AchievementCategory.HABITS,
            iconName = "emoji_events",
            targetCount = 1825,
            currentCountExtractor = { it.longestStreakDays },
            tier = AchievementTier.ETERNAL
        ),
        AchievementDef(
            id = "ROUTINE_BUILDER",
            title = "Routine Builder",
            description = "Log 50 total habit check-ins",
            xpReward = 250,
            category = AchievementCategory.HABITS,
            iconName = "check_circle",
            targetCount = 50,
            currentCountExtractor = { it.habitsCompletedCount },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "HABIT_CENTURION",
            title = "Habit Centurion",
            description = "Log 100 total habit check-ins",
            xpReward = 400,
            category = AchievementCategory.HABITS,
            iconName = "stars",
            targetCount = 100,
            currentCountExtractor = { it.habitsCompletedCount },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "QUARTER_YEAR_RITUAL",
            title = "Quarter-Year Ritualist",
            description = "Log 250 total habit check-ins",
            xpReward = 750,
            category = AchievementCategory.HABITS,
            iconName = "self_improvement",
            targetCount = 250,
            currentCountExtractor = { it.habitsCompletedCount },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "UNSHAKEABLE_RITUALS",
            title = "Unshakeable Rituals",
            description = "Log 500 total habit check-ins",
            xpReward = 1200,
            category = AchievementCategory.HABITS,
            iconName = "shield",
            targetCount = 500,
            currentCountExtractor = { it.habitsCompletedCount },
            tier = AchievementTier.GOLD
        ),
        AchievementDef(
            id = "THOUSAND_HABITS_CLUB",
            title = "Millennium Ritualist",
            description = "Log 1,000 total habit check-ins (~1 year)",
            xpReward = 2500,
            category = AchievementCategory.HABITS,
            iconName = "workspace_premium",
            targetCount = 1000,
            currentCountExtractor = { it.habitsCompletedCount },
            tier = AchievementTier.PLATINUM
        ),
        AchievementDef(
            id = "TWO_YEAR_RITUAL_MASTER",
            title = "Two-Year Habit Master",
            description = "Log 2,500 total habit check-ins (~2 years)",
            xpReward = 5500,
            category = AchievementCategory.HABITS,
            iconName = "diamond",
            targetCount = 2500,
            currentCountExtractor = { it.habitsCompletedCount },
            tier = AchievementTier.DIAMOND
        ),
        AchievementDef(
            id = "FIVE_THOUSAND_RITUALS",
            title = "Five-Thousand Rituals",
            description = "Log 5,000 total habit check-ins (~3.5 years)",
            xpReward = 9500,
            category = AchievementCategory.HABITS,
            iconName = "auto_awesome",
            targetCount = 5000,
            currentCountExtractor = { it.habitsCompletedCount },
            tier = AchievementTier.MYTHIC
        ),
        AchievementDef(
            id = "TEN_THOUSAND_HABIT_SAGE",
            title = "Ten-Thousand Ritual Sage",
            description = "Log 10,000 total habit check-ins (5+ Years of habit mastery)",
            xpReward = 20000,
            category = AchievementCategory.HABITS,
            iconName = "emoji_events",
            targetCount = 10000,
            currentCountExtractor = { it.habitsCompletedCount },
            tier = AchievementTier.ETERNAL
        ),

        // ==========================================
        // FOCUS & POMODORO (Sessions & Minutes over 5 Years)
        // ==========================================
        AchievementDef(
            id = "FOCUS_INITIATE",
            title = "Deep Work Disciple",
            description = "Finish your first Pomodoro focus session",
            xpReward = 75,
            category = AchievementCategory.FOCUS,
            iconName = "timer",
            targetCount = 1,
            currentCountExtractor = { it.focusSessionsCompleted },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "FOCUS_RHYTHM",
            title = "Focus Rhythm",
            description = "Complete 10 Pomodoro focus sessions",
            xpReward = 200,
            category = AchievementCategory.FOCUS,
            iconName = "hourglass_top",
            targetCount = 10,
            currentCountExtractor = { it.focusSessionsCompleted },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "FOCUS_ARCHITECT",
            title = "Focus Architect",
            description = "Complete 50 Pomodoro focus sessions",
            xpReward = 500,
            category = AchievementCategory.FOCUS,
            iconName = "self_improvement",
            targetCount = 50,
            currentCountExtractor = { it.focusSessionsCompleted },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "FOCUS_CENTURION",
            title = "Deep Work Centurion",
            description = "Complete 100 Pomodoro focus sessions",
            xpReward = 1000,
            category = AchievementCategory.FOCUS,
            iconName = "stars",
            targetCount = 100,
            currentCountExtractor = { it.focusSessionsCompleted },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "FLOW_STATE_RUNNER",
            title = "Flow State Runner",
            description = "Complete 250 Pomodoro focus sessions",
            xpReward = 2000,
            category = AchievementCategory.FOCUS,
            iconName = "speed",
            targetCount = 250,
            currentCountExtractor = { it.focusSessionsCompleted },
            tier = AchievementTier.GOLD
        ),
        AchievementDef(
            id = "FIVE_HUNDRED_SESSIONS",
            title = "Five-Hundred Flow Sessions",
            description = "Complete 500 focus sessions (~1 year of deep work)",
            xpReward = 4000,
            category = AchievementCategory.FOCUS,
            iconName = "workspace_premium",
            targetCount = 500,
            currentCountExtractor = { it.focusSessionsCompleted },
            tier = AchievementTier.PLATINUM
        ),
        AchievementDef(
            id = "THOUSAND_SESSIONS_TITAN",
            title = "Thousand Flow Sessions",
            description = "Complete 1,000 focus sessions (~2.5 years)",
            xpReward = 8000,
            category = AchievementCategory.FOCUS,
            iconName = "diamond",
            targetCount = 1000,
            currentCountExtractor = { it.focusSessionsCompleted },
            tier = AchievementTier.DIAMOND
        ),
        AchievementDef(
            id = "TWO_THOUSAND_SESSIONS",
            title = "Two-Thousand Sessions Master",
            description = "Complete 2,000 focus sessions (~4 years)",
            xpReward = 14000,
            category = AchievementCategory.FOCUS,
            iconName = "auto_awesome",
            targetCount = 2000,
            currentCountExtractor = { it.focusSessionsCompleted },
            tier = AchievementTier.MYTHIC
        ),
        AchievementDef(
            id = "FIVE_YEAR_FOCUS_SAGE",
            title = "Five-Year Focus Sage",
            description = "Complete 3,000 focus sessions (5+ Years of dedicated flow!)",
            xpReward = 25000,
            category = AchievementCategory.FOCUS,
            iconName = "emoji_events",
            targetCount = 3000,
            currentCountExtractor = { it.focusSessionsCompleted },
            tier = AchievementTier.ETERNAL
        ),
        AchievementDef(
            id = "HOUR_OF_POWER",
            title = "Hour of Power",
            description = "Accumulate 60 minutes of pure focus time",
            xpReward = 150,
            category = AchievementCategory.FOCUS,
            iconName = "timer",
            targetCount = 60,
            currentCountExtractor = { it.focusMinutesTotal },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "ZEN_MASTER",
            title = "Zen Master",
            description = "Accumulate 250 minutes of pure focus time",
            xpReward = 350,
            category = AchievementCategory.FOCUS,
            iconName = "self_improvement",
            targetCount = 250,
            currentCountExtractor = { it.focusMinutesTotal },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "TEN_HOUR_FLOW",
            title = "Ten-Hour Flow State",
            description = "Accumulate 600 minutes (10 hours) of focus",
            xpReward = 750,
            category = AchievementCategory.FOCUS,
            iconName = "schedule",
            targetCount = 600,
            currentCountExtractor = { it.focusMinutesTotal },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "FULL_DAY_OF_FOCUS",
            title = "Twenty-Four Hours in the Zone",
            description = "Accumulate 1,440 minutes (24 hours) of focus",
            xpReward = 1500,
            category = AchievementCategory.FOCUS,
            iconName = "stars",
            targetCount = 1440,
            currentCountExtractor = { it.focusMinutesTotal },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "FIFTY_HOUR_DEEP_WORK",
            title = "Fifty-Hour Deep Work",
            description = "Accumulate 3,000 minutes (50 hours) of focus",
            xpReward = 3000,
            category = AchievementCategory.FOCUS,
            iconName = "military_tech",
            targetCount = 3000,
            currentCountExtractor = { it.focusMinutesTotal },
            tier = AchievementTier.GOLD
        ),
        AchievementDef(
            id = "CENTURY_OF_DEEP_WORK",
            title = "Century of Deep Work",
            description = "Accumulate 6,000 minutes (100 hours) of pure focus",
            xpReward = 5000,
            category = AchievementCategory.FOCUS,
            iconName = "workspace_premium",
            targetCount = 6000,
            currentCountExtractor = { it.focusMinutesTotal },
            tier = AchievementTier.PLATINUM
        ),
        AchievementDef(
            id = "TWO_HUNDRED_FIFTY_HOURS",
            title = "Quarter-Thousand Hours",
            description = "Accumulate 15,000 minutes (250 hours) of focus (~1 year)",
            xpReward = 9000,
            category = AchievementCategory.FOCUS,
            iconName = "diamond",
            targetCount = 15000,
            currentCountExtractor = { it.focusMinutesTotal },
            tier = AchievementTier.DIAMOND
        ),
        AchievementDef(
            id = "FIVE_HUNDRED_HOURS_FLOW",
            title = "Five-Hundred Hour Grandmaster",
            description = "Accumulate 30,000 minutes (500 hours) of focus (~2.5 years)",
            xpReward = 16000,
            category = AchievementCategory.FOCUS,
            iconName = "auto_awesome",
            targetCount = 30000,
            currentCountExtractor = { it.focusMinutesTotal },
            tier = AchievementTier.MYTHIC
        ),
        AchievementDef(
            id = "THOUSAND_HOURS_TRANSCENDENT",
            title = "One-Thousand Hour Transcendent",
            description = "Accumulate 60,000 minutes (1,000 hours of deep work! 5-Year mastery)",
            xpReward = 30000,
            category = AchievementCategory.FOCUS,
            iconName = "emoji_events",
            targetCount = 60000,
            currentCountExtractor = { it.focusMinutesTotal },
            tier = AchievementTier.ETERNAL
        ),

        // ==========================================
        // WORKFLOWS (Matrix & Kanban Strategy over 5 Years)
        // ==========================================
        AchievementDef(
            id = "MATRIX_STRATEGIST",
            title = "Matrix Strategist",
            description = "Conquer 5 tasks in prioritized workflows",
            xpReward = 80,
            category = AchievementCategory.WORKFLOWS,
            iconName = "grid_view",
            targetCount = 5,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "KANBAN_FLOW",
            title = "Kanban Master",
            description = "Advance 10 tasks through your workflow pipeline",
            xpReward = 100,
            category = AchievementCategory.WORKFLOWS,
            iconName = "view_column",
            targetCount = 10,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "PRIORITY_MAESTRO",
            title = "Priority Maestro",
            description = "Execute 50 tasks with tactical organization",
            xpReward = 250,
            category = AchievementCategory.WORKFLOWS,
            iconName = "flag",
            targetCount = 50,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.BRONZE
        ),
        AchievementDef(
            id = "STRATEGIC_OPERATOR",
            title = "Strategic Operator",
            description = "Execute 200 tasks in structured workflows",
            xpReward = 600,
            category = AchievementCategory.WORKFLOWS,
            iconName = "check_circle",
            targetCount = 200,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.SILVER
        ),
        AchievementDef(
            id = "TACTICAL_VIRTUOSO",
            title = "Tactical Virtuoso",
            description = "Complete 500 workflow-managed tasks",
            xpReward = 1200,
            category = AchievementCategory.WORKFLOWS,
            iconName = "military_tech",
            targetCount = 500,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.GOLD
        ),
        AchievementDef(
            id = "SYSTEMIZED_EXECUTIVE",
            title = "Systemized Executive",
            description = "Complete 1,500 workflow-managed tasks (~1 year)",
            xpReward = 2800,
            category = AchievementCategory.WORKFLOWS,
            iconName = "workspace_premium",
            targetCount = 1500,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.PLATINUM
        ),
        AchievementDef(
            id = "THREE_YEAR_WORKFLOW_TITAN",
            title = "Three-Year Workflow Titan",
            description = "Complete 5,000 workflow tasks (~3 years of systematic execution)",
            xpReward = 8000,
            category = AchievementCategory.WORKFLOWS,
            iconName = "auto_awesome",
            targetCount = 5000,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.DIAMOND
        ),
        AchievementDef(
            id = "FIVE_YEAR_GRAND_STRATEGIST",
            title = "Five-Year Grand Strategist",
            description = "Complete 10,000 workflow tasks (5 Years of systematic mastery!)",
            xpReward = 20000,
            category = AchievementCategory.WORKFLOWS,
            iconName = "emoji_events",
            targetCount = 10000,
            currentCountExtractor = { it.tasksCompletedCount },
            tier = AchievementTier.ETERNAL
        )
    )
}
