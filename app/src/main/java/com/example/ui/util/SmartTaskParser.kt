package com.example.ui.util

import com.example.data.model.Priority
import com.example.data.model.TaskList
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

enum class ParsedTokenType {
    DATE_TIME,
    PRIORITY,
    LIST,
    TAG
}

data class ParsedTokenItem(
    val type: ParsedTokenType,
    val rawText: String,
    val displayLabel: String,
    val data: Any? = null
)

data class ParsedTaskResult(
    val originalInput: String,
    val cleanTitle: String,
    val dueDateMillis: Long? = null,
    val hasTime: Boolean = false,
    val priority: Priority? = null,
    val targetListId: Long? = null,
    val targetListName: String? = null,
    val tags: List<String> = emptyList(),
    val tokens: List<ParsedTokenItem> = emptyList()
)

object SmartTaskParser {

    private val TIME_REGEX = Pattern.compile(
        """\b(?:at\s+)?(\d{1,2})(?::(\d{2}))?\s*(am|pm)\b|\b(?:at\s+)(\d{1,2}):(\d{2})\b""",
        Pattern.CASE_INSENSITIVE
    )

    private val PRIORITY_HIGH_REGEX = Pattern.compile("""\b!(?:high|p1|urgent)\b""", Pattern.CASE_INSENSITIVE)
    private val PRIORITY_MED_REGEX = Pattern.compile("""\b!(?:med|medium|p2|normal)\b""", Pattern.CASE_INSENSITIVE)
    private val PRIORITY_LOW_REGEX = Pattern.compile("""\b!(?:low|p3)\b""", Pattern.CASE_INSENSITIVE)

    private val LIST_REGEX = Pattern.compile("""#([A-Za-z0-9_\-]+)""")
    private val TAG_REGEX = Pattern.compile("""@([A-Za-z0-9_\-]+)""")

    fun parse(input: String, availableLists: List<TaskList> = emptyList()): ParsedTaskResult {
        if (input.isBlank()) {
            return ParsedTaskResult(originalInput = input, cleanTitle = "")
        }

        var workingText = input
        val tokens = mutableListOf<ParsedTokenItem>()
        var detectedPriority: Priority? = null
        var detectedListId: Long? = null
        var detectedListName: String? = null
        val detectedTags = mutableListOf<String>()

        // 1. Check Priority
        val highMatcher = PRIORITY_HIGH_REGEX.matcher(workingText)
        if (highMatcher.find()) {
            val raw = highMatcher.group()
            detectedPriority = Priority.HIGH
            tokens.add(ParsedTokenItem(ParsedTokenType.PRIORITY, raw, "High Priority", Priority.HIGH))
            workingText = highMatcher.replaceFirst(" ")
        } else {
            val medMatcher = PRIORITY_MED_REGEX.matcher(workingText)
            if (medMatcher.find()) {
                val raw = medMatcher.group()
                detectedPriority = Priority.MEDIUM
                tokens.add(ParsedTokenItem(ParsedTokenType.PRIORITY, raw, "Medium Priority", Priority.MEDIUM))
                workingText = medMatcher.replaceFirst(" ")
            } else {
                val lowMatcher = PRIORITY_LOW_REGEX.matcher(workingText)
                if (lowMatcher.find()) {
                    val raw = lowMatcher.group()
                    detectedPriority = Priority.LOW
                    tokens.add(ParsedTokenItem(ParsedTokenType.PRIORITY, raw, "Low Priority", Priority.LOW))
                    workingText = lowMatcher.replaceFirst(" ")
                }
            }
        }

        // 2. Check List (#ListName)
        val listMatcher = LIST_REGEX.matcher(workingText)
        while (listMatcher.find()) {
            val listTag = listMatcher.group(1) ?: ""
            val matchedList = availableLists.find { it.name.equals(listTag, ignoreCase = true) }
            if (matchedList != null) {
                detectedListId = matchedList.id
                detectedListName = matchedList.name
                tokens.add(ParsedTokenItem(ParsedTokenType.LIST, listMatcher.group(), "#${matchedList.name}", matchedList.id))
                workingText = workingText.replace(listMatcher.group(), " ")
                break
            }
        }

        // 3. Check Tags (@tagName)
        val tagMatcher = TAG_REGEX.matcher(workingText)
        while (tagMatcher.find()) {
            val tag = tagMatcher.group(1) ?: ""
            if (tag.isNotBlank()) {
                detectedTags.add(tag)
                tokens.add(ParsedTokenItem(ParsedTokenType.TAG, tagMatcher.group(), "@$tag", tag))
                workingText = workingText.replace(tagMatcher.group(), " ")
            }
        }

        // 4. Date & Time Parsing
        var hasTime = false
        var targetHour: Int? = null
        var targetMinute: Int? = null

        val timeMatcher = TIME_REGEX.matcher(workingText)
        var rawTimeMatch: String? = null
        if (timeMatcher.find()) {
            rawTimeMatch = timeMatcher.group()
            hasTime = true
            val h1 = timeMatcher.group(1)
            val m1 = timeMatcher.group(2)
            val ampm = timeMatcher.group(3)

            val h2 = timeMatcher.group(4)
            val m2 = timeMatcher.group(5)

            if (h1 != null) {
                var hour = h1.toIntOrNull() ?: 12
                val min = m1?.toIntOrNull() ?: 0
                if (ampm != null) {
                    if (ampm.equals("pm", ignoreCase = true) && hour < 12) hour += 12
                    if (ampm.equals("am", ignoreCase = true) && hour == 12) hour = 0
                }
                targetHour = hour
                targetMinute = min
            } else if (h2 != null) {
                targetHour = h2.toIntOrNull() ?: 12
                targetMinute = m2?.toIntOrNull() ?: 0
            }
            workingText = workingText.replace(rawTimeMatch, " ")
        }

        // Check relative / specific dates
        val cal = Calendar.getInstance()
        var dateFound = false
        var rawDateMatch: String? = null
        var dateDisplay = ""

        val lower = workingText.lowercase(Locale.US)

        if (lower.contains("tomorrow") || lower.contains("tmrw")) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            dateFound = true
            rawDateMatch = if (lower.contains("tomorrow")) "tomorrow" else "tmrw"
            dateDisplay = "Tomorrow"
        } else if (lower.contains("today") || lower.contains("tonight")) {
            dateFound = true
            rawDateMatch = if (lower.contains("tonight")) "tonight" else "today"
            dateDisplay = "Today"
            if (lower.contains("tonight") && targetHour == null) {
                targetHour = 20
                targetMinute = 0
                hasTime = true
            }
        } else {
            // Check day of week (e.g. "next monday", "this friday", "sunday")
            val daysMap = mapOf(
                "sunday" to Calendar.SUNDAY,
                "monday" to Calendar.MONDAY,
                "tuesday" to Calendar.TUESDAY,
                "wednesday" to Calendar.WEDNESDAY,
                "thursday" to Calendar.THURSDAY,
                "friday" to Calendar.FRIDAY,
                "saturday" to Calendar.SATURDAY
            )

            for ((dayName, dayConst) in daysMap) {
                val nextDayRegex = Pattern.compile("""\b(?:next\s+|this\s+)?$dayName\b""", Pattern.CASE_INSENSITIVE)
                val m = nextDayRegex.matcher(workingText)
                if (m.find()) {
                    rawDateMatch = m.group()
                    dateFound = true
                    dateDisplay = rawDateMatch.replaceFirstChar { it.uppercase() }

                    val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                    var daysToAdd = dayConst - currentDayOfWeek
                    if (daysToAdd <= 0 || rawDateMatch.lowercase().startsWith("next")) {
                        daysToAdd += 7
                    }
                    cal.add(Calendar.DAY_OF_YEAR, daysToAdd)
                    break
                }
            }
        }

        // Clean raw date match from text
        if (rawDateMatch != null) {
            val pattern = Pattern.compile("""\b""" + Pattern.quote(rawDateMatch) + """\b""", Pattern.CASE_INSENSITIVE)
            workingText = pattern.matcher(workingText).replaceFirst(" ")
        }

        var dueDateMillis: Long? = null
        if (dateFound || hasTime) {
            if (targetHour != null && targetMinute != null) {
                cal.set(Calendar.HOUR_OF_DAY, targetHour)
                cal.set(Calendar.MINUTE, targetMinute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            } else {
                // Default to end of day if no specific time
                cal.set(Calendar.HOUR_OF_DAY, 18)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            dueDateMillis = cal.timeInMillis

            val display = buildString {
                if (dateDisplay.isNotEmpty()) append(dateDisplay)
                else append("Today")
                if (hasTime && targetHour != null && targetMinute != null) {
                    append(", %d:%02d %s".format(
                        if (targetHour % 12 == 0) 12 else targetHour % 12,
                        targetMinute,
                        if (targetHour >= 12) "PM" else "AM"
                    ))
                }
            }

            val combinedRaw = listOfNotNull(rawDateMatch, rawTimeMatch).joinToString(" ")
            tokens.add(
                ParsedTokenItem(
                    type = ParsedTokenType.DATE_TIME,
                    rawText = combinedRaw,
                    displayLabel = display,
                    data = dueDateMillis
                )
            )
        }

        // Clean up title: remove duplicate spaces, trim
        val cleanTitle = workingText.replace(Regex("""\s+"""), " ").trim()

        return ParsedTaskResult(
            originalInput = input,
            cleanTitle = if (cleanTitle.isNotBlank()) cleanTitle else input.trim(),
            dueDateMillis = dueDateMillis,
            hasTime = hasTime,
            priority = detectedPriority,
            targetListId = detectedListId,
            targetListName = detectedListName,
            tags = detectedTags,
            tokens = tokens
        )
    }
}
