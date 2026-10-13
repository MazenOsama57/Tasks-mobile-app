package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class Priority(
    val displayName: String,
    val weight: Int
) {
    NONE("None", 0),
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3);

    fun getColor(isDarkTheme: Boolean): Color {
        return when (this) {
            NONE -> if (isDarkTheme) Color(0xFF9AA0A6) else Color(0xFF5F6368)
            LOW -> if (isDarkTheme) PriorityLowDark else PriorityLowLight
            MEDIUM -> if (isDarkTheme) PriorityMediumDark else PriorityMediumLight
            HIGH -> if (isDarkTheme) PriorityHighDark else PriorityHighLight
        }
    }

    fun getContainerColor(isDarkTheme: Boolean): Color {
        return when (this) {
            NONE -> if (isDarkTheme) AppSurfaceVariantDark else AppSurfaceVariantLight
            LOW -> if (isDarkTheme) PriorityLowContainerDark else PriorityLowContainerLight
            MEDIUM -> if (isDarkTheme) PriorityMediumContainerDark else PriorityMediumContainerLight
            HIGH -> if (isDarkTheme) PriorityHighContainerDark else PriorityHighContainerLight
        }
    }
}
