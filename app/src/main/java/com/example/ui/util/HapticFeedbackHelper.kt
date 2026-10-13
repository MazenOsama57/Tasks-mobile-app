package com.example.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

object HapticFeedbackHelper {

    @Suppress("DEPRECATION")
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Tactile feedback triggered when a user marks a task or subtask as complete.
     */
    fun performTaskCompleteHaptic(
        context: Context,
        hapticFeedback: HapticFeedback? = null,
        enabled: Boolean = true
    ) {
        if (!enabled) return
        try {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(45, 200))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(45)
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile feedback triggered when a user swipes to delete a task or confirms deletion.
     */
    fun performTaskDeleteHaptic(
        context: Context,
        hapticFeedback: HapticFeedback? = null,
        enabled: Boolean = true
    ) {
        if (!enabled) return
        try {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.LongPress)
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 40, 60), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 35, 40, 60), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile feedback triggered when selecting tasks or entering multi-select mode.
     */
    fun performSelectionToggleHaptic(
        context: Context,
        hapticFeedback: HapticFeedback? = null,
        enabled: Boolean = true
    ) {
        if (!enabled) return
        try {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, 120))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile feedback triggered when swiping reaches dismissal threshold.
     */
    fun performSwipeThresholdHaptic(
        context: Context,
        hapticFeedback: HapticFeedback? = null,
        enabled: Boolean = true
    ) {
        if (!enabled) return
        try {
            hapticFeedback?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, 100))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    /**
     * Celebration pulse pattern triggered when a Pomodoro focus interval or break completes.
     */
    fun performTimerCompleteHaptic(
        context: Context,
        enabled: Boolean = true
    ) {
        if (!enabled) return
        try {
            val vibrator = getVibrator(context) ?: return
            if (!vibrator.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Two distinct celebratory pulses: wait 0ms, 120ms vibrate, 80ms rest, 200ms vibrate
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 120, 80, 200), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 120, 80, 200), -1)
            }
        } catch (_: Exception) {}
    }
}
