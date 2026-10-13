package com.example.ui.util

import androidx.compose.ui.graphics.Color

enum class CoachTone {
    SUCCESS,
    DISAPPOINTED_OVERDUE,
    ENCOURAGING_HIGH,
    ENCOURAGING_MID,
    ENCOURAGING_LOW,
    DISAPPOINTED_ZERO,
    CLEAR_DAY
}

data class DailyCoachFeedback(
    val progress: Float,
    val completedCount: Int,
    val totalCount: Int,
    val overdueCount: Int,
    val headline: String,
    val message: String,
    val tone: CoachTone
)

object MotivationalCoach {

    fun evaluateDay(
        completedCount: Int,
        totalCount: Int,
        overdueCount: Int
    ): DailyCoachFeedback {
        val progress = if (totalCount > 0) {
            (completedCount.toFloat() / totalCount).coerceIn(0f, 1f)
        } else {
            0f
        }

        return when {
            // Case 1: Overdue tasks exist (Disappointment / accountability)
            overdueCount > 0 -> {
                val overdueMsg = if (overdueCount == 1) {
                    "You have 1 task past its due date. Procrastination is stealing your momentum — don't let it slip any further, tackle it right now!"
                } else {
                    "You have $overdueCount tasks past their due date. Falling behind schedule is disappointing, but excuses won't clear your plate. Focus up and handle them!"
                }
                DailyCoachFeedback(
                    progress = progress,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    overdueCount = overdueCount,
                    headline = "Overdue Deadlines Alert!",
                    message = overdueMsg,
                    tone = CoachTone.DISAPPOINTED_OVERDUE
                )
            }

            // Case 2: 100% completed today (All caught up! Maximum praise & celebration)
            totalCount > 0 && completedCount >= totalCount -> {
                DailyCoachFeedback(
                    progress = 1.0f,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    overdueCount = 0,
                    headline = "Unstoppable! 100% Crushed!",
                    message = "Outstanding performance! You completed every single task scheduled for today. Take a deep breath and enjoy your victory — you earned it!",
                    tone = CoachTone.SUCCESS
                )
            }

            // Case 3: High progress (70% - 99%)
            progress >= 0.70f -> {
                DailyCoachFeedback(
                    progress = progress,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    overdueCount = 0,
                    headline = "Almost to the Finish Line!",
                    message = "Fantastic momentum! You're in the final stretch for today. Give it one more focused push and seal a perfect 100% day!",
                    tone = CoachTone.ENCOURAGING_HIGH
                )
            }

            // Case 4: Moderate progress (35% - 69%)
            progress >= 0.35f -> {
                DailyCoachFeedback(
                    progress = progress,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    overdueCount = 0,
                    headline = "Solid Momentum!",
                    message = "Halfway through today's list! Stay locked in and keep this steady rhythm going before the day winds down.",
                    tone = CoachTone.ENCOURAGING_MID
                )
            }

            // Case 5: Low progress (1% - 34%)
            progress > 0f -> {
                DailyCoachFeedback(
                    progress = progress,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    overdueCount = 0,
                    headline = "First Steps Complete!",
                    message = "You've broken the friction and made your first marks. Don't slow down now — carry that spark into the next task!",
                    tone = CoachTone.ENCOURAGING_LOW
                )
            }

            // Case 6: 0% completed with active tasks today (Disappointed / urgent nudge)
            totalCount > 0 -> {
                DailyCoachFeedback(
                    progress = 0f,
                    completedCount = 0,
                    totalCount = totalCount,
                    overdueCount = 0,
                    headline = "Zero Tasks Done Yet",
                    message = "The day is slipping away and your scorecard is completely empty. Disappointing start, but action cures fear. Pick one task and start right now!",
                    tone = CoachTone.DISAPPOINTED_ZERO
                )
            }

            // Case 7: No tasks due today
            else -> {
                DailyCoachFeedback(
                    progress = 1.0f,
                    completedCount = 0,
                    totalCount = 0,
                    overdueCount = 0,
                    headline = "All Clear Today!",
                    message = "No pending deadlines for today! Enjoy your free time or plan ahead by tapping + New task.",
                    tone = CoachTone.CLEAR_DAY
                )
            }
        }
    }
}
