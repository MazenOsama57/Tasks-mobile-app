package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val autoClearEnabled: Boolean = false,
    val autoClearFrequencyHours: Int = 24, // 0 = Immediately, 1 = 1h, 24 = 1d, 72 = 3d, 168 = 7d, 720 = 30d
    val themePresetId: String = "modern_dark",
    val customPrimaryHex: String = "#8AB4F8",
    val customBackgroundHex: String = "#121212",
    val customSurfaceHex: String = "#1E1E1E",
    val isCustomThemeLight: Boolean = false,
    val morningReminderEnabled: Boolean = true,
    val morningReminderHour: Int = 8,
    val morningReminderMinute: Int = 0,
    val eveningReminderEnabled: Boolean = true,
    val eveningReminderHour: Int = 20,
    val eveningReminderMinute: Int = 0,
    val hapticFeedbackEnabled: Boolean = true
)
