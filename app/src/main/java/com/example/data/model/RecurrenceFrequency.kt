package com.example.data.model

enum class RecurrenceFrequency(val displayName: String) {
    NONE("Does not repeat"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    fun getSummaryText(interval: Int, timesPerPeriod: Int): String {
        if (this == NONE) return displayName
        val intervalPrefix = if (interval > 1) "Every $interval " else ""
        return when (this) {
            NONE -> displayName
            DAILY -> {
                if (interval > 1) "Every $interval days" else "Every day"
            }
            WEEKLY -> {
                val period = if (interval > 1) "$interval weeks" else "week"
                if (timesPerPeriod > 1) {
                    "Every $period ($timesPerPeriod times a week)"
                } else {
                    if (interval > 1) "Every $interval weeks" else "Weekly"
                }
            }
            MONTHLY -> {
                val period = if (interval > 1) "$interval months" else "month"
                if (timesPerPeriod > 1) {
                    "Every $period ($timesPerPeriod times a month)"
                } else {
                    if (interval > 1) "Every $interval months" else "Monthly"
                }
            }
            YEARLY -> {
                val period = if (interval > 1) "$interval years" else "year"
                if (timesPerPeriod > 1) {
                    "Every $period ($timesPerPeriod times a year)"
                } else {
                    if (interval > 1) "Every $interval years" else "Yearly"
                }
            }
        }
    }
}
