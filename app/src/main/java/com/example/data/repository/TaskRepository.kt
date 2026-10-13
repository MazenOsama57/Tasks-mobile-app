package com.example.data.repository

import android.content.Context
import com.example.data.local.SettingsDao
import com.example.data.local.TaskDao
import com.example.data.local.TaskListDao
import com.example.data.model.*
import com.example.ui.util.TaskDateUtils
import com.example.widget.TasksWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class TaskRepository(
    private val taskListDao: TaskListDao,
    private val taskDao: TaskDao,
    private val settingsDao: SettingsDao,
    private val context: Context? = null,
    private val focusSessionDao: com.example.data.local.FocusSessionDao? = null,
    private val habitDao: com.example.data.local.HabitDao? = null,
    private val statsDao: com.example.data.local.StatsDao? = null
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // In-memory cache layer: holds the current list data in a local variable and exposes via StateFlow
    // to prevent redundant re-fetching from Room when navigating between task lists and improve UI responsiveness.
    private var currentListData: List<TaskWithSubtasks> = emptyList()
    private var currentListId: Long? = null
    private val _currentListTasks = MutableStateFlow<List<TaskWithSubtasks>>(emptyList())
    val currentListTasks: StateFlow<List<TaskWithSubtasks>> = _currentListTasks.asStateFlow()

    // Thread-safe map holding tasks grouped by list for instant zero-lag switching
    private val tasksCacheByList = ConcurrentHashMap<Long, List<TaskWithSubtasks>>()
    private var cachedAllTasksList: List<TaskWithSubtasks> = emptyList()

    init {
        repositoryScope.launch {
            taskDao.getAllTasks().collect { allTasks ->
                cachedAllTasksList = allTasks
                val grouped = allTasks.groupBy { it.task.listId }
                tasksCacheByList.clear()
                tasksCacheByList.putAll(grouped)

                // Update current list data cache if list is active
                currentListId?.let { activeId ->
                    val updatedList = tasksCacheByList[activeId] ?: emptyList()
                    currentListData = updatedList
                    _currentListTasks.value = updatedList
                }
            }
        }
    }

    val allLists: Flow<List<TaskList>> = taskListDao.getAllLists()

    fun setCurrentListId(listId: Long) {
        currentListId = listId
        val cached = tasksCacheByList[listId] ?: emptyList()
        currentListData = cached
        _currentListTasks.value = cached
    }

    fun getCurrentListData(): List<TaskWithSubtasks> = currentListData

    fun getTasksForList(listId: Long): Flow<List<TaskWithSubtasks>> {
        setCurrentListId(listId)
        return _currentListTasks
    }

    val starredTasks: Flow<List<TaskWithSubtasks>> = taskDao.getStarredTasks()

    val completedTasks: Flow<List<TaskWithSubtasks>> = taskDao.getAllCompletedTasks()

    val allTasks: Flow<List<TaskWithSubtasks>> = taskDao.getAllTasks()

    /**
     * Due date filter criteria.
     */
    enum class DueDateFilter(val label: String) {
        ALL("All Tasks"),
        TODAY("Due Today"),
        TOMORROW("Due Tomorrow"),
        THIS_WEEK("This Week"),
        OVERDUE("Overdue"),
        UPCOMING("Upcoming")
    }

    /**
     * Filters tasks specifically by their due date, ensuring recurring tasks only appear
     * when they are actually due on that scheduled day.
     */
    fun filterTasksByDueDate(
        tasks: List<TaskWithSubtasks>,
        filter: DueDateFilter
    ): List<TaskWithSubtasks> {
        if (filter == DueDateFilter.ALL) return tasks

        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val todayStart = cal.timeInMillis
        val todayEnd = todayStart + 86400000L - 1L
        val tomorrowStart = todayStart + 86400000L
        val tomorrowEnd = tomorrowStart + 86400000L - 1L

        val weekEndCal = java.util.Calendar.getInstance().apply {
            timeInMillis = todayStart
            add(java.util.Calendar.DAY_OF_YEAR, 7)
        }
        val weekEnd = weekEndCal.timeInMillis - 1L

        return tasks.filter { taskWithSubs ->
            val task = taskWithSubs.task
            val due = task.dueDateMillis ?: if (task.recurrenceFrequency != RecurrenceFrequency.NONE) {
                TaskDateUtils.computeInitialOccurrenceDate(
                    task.recurrenceFrequency,
                    task.recurrenceDaysOfWeek,
                    task.recurrenceDaysOfMonth
                )
            } else null

            when (filter) {
                DueDateFilter.ALL -> true
                DueDateFilter.TODAY -> {
                    // Recurring task only appears if its active occurrence is due today!
                    due != null && due in todayStart..todayEnd
                }
                DueDateFilter.TOMORROW -> {
                    // Recurring task only appears if its active occurrence is due tomorrow!
                    due != null && due in tomorrowStart..tomorrowEnd
                }
                DueDateFilter.OVERDUE -> {
                    !task.isCompleted && due != null && due < todayStart
                }
                DueDateFilter.THIS_WEEK -> {
                    due != null && due in todayStart..weekEnd
                }
                DueDateFilter.UPCOMING -> {
                    due != null && due > todayEnd
                }
            }
        }
    }

    /**
     * Returns a Flow of tasks filtered specifically by due date.
     */
    fun getTasksFilteredByDueDate(
        filter: DueDateFilter,
        listId: Long? = null
    ): Flow<List<TaskWithSubtasks>> {
        val baseFlow = if (listId != null) getTasksForList(listId) else allTasks
        return baseFlow.map { tasks ->
            filterTasksByDueDate(tasks, filter)
        }
    }

    /**
     * Returns tasks due on a specific calendar day range, ensuring recurring tasks
     * only appear on that date if their active instance is actually due then.
     */
    fun getTasksForDateRange(startOfDayMillis: Long, endOfDayMillis: Long): Flow<List<TaskWithSubtasks>> {
        return allTasks.map { all ->
            all.filter { taskWithSubs ->
                val task = taskWithSubs.task
                val due = task.dueDateMillis
                due != null && due in startOfDayMillis..endOfDayMillis
            }
        }
    }

    val settings: Flow<AppSettings> = settingsDao.getSettings().map { it ?: AppSettings() }

    fun getTaskWithSubtasks(taskId: Long): Flow<TaskWithSubtasks?> =
        taskDao.getTaskWithSubtasksById(taskId)

    suspend fun getTaskById(taskId: Long): Task? = taskDao.getTaskById(taskId)

    private fun notifyWidget() {
        context?.let {
            try {
                TasksWidgetProvider.updateAllWidgets(it)
            } catch (e: Exception) {
                // Ignore widget notification failure if not initialized
            }
        }
    }

    suspend fun ensureDefaultList(): Long {
        val count = taskListDao.getListCount()
        if (count == 0) {
            return taskListDao.insertList(TaskList(name = "My Tasks", orderIndex = 0))
        }
        return -1
    }

    suspend fun insertList(name: String): Long {
        return taskListDao.insertList(TaskList(name = name.trim()))
    }

    suspend fun updateList(list: TaskList) {
        taskListDao.updateList(list)
    }

    suspend fun deleteList(list: TaskList) {
        taskListDao.deleteList(list)
        ensureDefaultList()
        notifyWidget()
    }

    suspend fun insertTask(task: Task): Long {
        val id = taskDao.insertTask(task)
        notifyWidget()
        return id
    }

    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
        notifyWidget()
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        notifyWidget()
    }

    suspend fun restoreTaskWithSubtasks(taskWithSubtasks: TaskWithSubtasks): Long {
        val newTaskId = taskDao.insertTask(taskWithSubtasks.task.copy(id = 0))
        if (taskWithSubtasks.subtasks.isNotEmpty()) {
            val subtasksToRestore = taskWithSubtasks.subtasks.map { sub ->
                sub.copy(id = 0, taskId = newTaskId)
            }
            taskDao.insertSubtasks(subtasksToRestore)
        }
        notifyWidget()
        return newTaskId
    }

    suspend fun setTaskCompleted(taskId: Long, isCompleted: Boolean) {
        val task = taskDao.getTaskById(taskId) ?: return
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        taskDao.setTaskCompleted(taskId, isCompleted, completedAt)

        // If marking completed and task has recurrence, schedule exactly one next occurrence
        if (isCompleted && task.recurrenceFrequency != RecurrenceFrequency.NONE) {
            val alreadySpawned = task.spawnedNextTaskId?.let { spawnedId ->
                taskDao.getTaskById(spawnedId)
            }
            if (alreadySpawned == null) {
                val nextDueDate = TaskDateUtils.calculateNextDueDate(
                    task.dueDateMillis,
                    task.recurrenceFrequency,
                    task.recurrenceInterval,
                    task.recurrenceTimesPerPeriod,
                    task.recurrenceDaysOfWeek,
                    task.recurrenceDaysOfMonth
                )
                val nextTask = task.copy(
                    id = 0,
                    dueDateMillis = nextDueDate,
                    isCompleted = false,
                    completedAt = null,
                    createdAt = System.currentTimeMillis(),
                    spawnedNextTaskId = null,
                    recurringOriginalDueDate = nextDueDate
                )
                val newTaskId = taskDao.insertTask(nextTask)
                taskDao.setSpawnedNextTaskId(taskId, newTaskId)

                val existingSubtasks = taskDao.getSubtasksForTaskId(taskId)
                if (existingSubtasks.isNotEmpty()) {
                    val clonedSubtasks = existingSubtasks.map {
                        it.copy(id = 0, taskId = newTaskId, isCompleted = false)
                    }
                    taskDao.insertSubtasks(clonedSubtasks)
                }
            }
        } else if (!isCompleted && task.recurrenceFrequency != RecurrenceFrequency.NONE) {
            // Upon un-completion, delete the spawned uncompleted recurrence instance if it hasn't been completed yet
            task.spawnedNextTaskId?.let { spawnedId ->
                val spawnedTask = taskDao.getTaskById(spawnedId)
                if (spawnedTask != null && !spawnedTask.isCompleted) {
                    taskDao.deleteUncompletedTaskById(spawnedId)
                    taskDao.setSpawnedNextTaskId(taskId, null)
                }
            }
        }

        // Check auto-clear
        checkAndTriggerAutoClear()
        notifyWidget()
    }

    suspend fun setTaskStarred(taskId: Long, isStarred: Boolean) {
        taskDao.setTaskStarred(taskId, isStarred)
        notifyWidget()
    }

    suspend fun deleteCompletedTasks(listId: Long?) {
        if (listId != null && listId > 0) {
            taskDao.deleteCompletedTasksForList(listId)
        } else {
            taskDao.deleteAllCompletedTasks()
        }
        notifyWidget()
    }

    suspend fun deleteTasks(taskIds: Collection<Long>) {
        if (taskIds.isEmpty()) return
        taskDao.deleteTasksByIds(taskIds.toList())
        notifyWidget()
    }

    suspend fun moveTasksToList(taskIds: Collection<Long>, targetListId: Long) {
        if (taskIds.isEmpty()) return
        taskDao.moveTasksToList(taskIds.toList(), targetListId)
        notifyWidget()
    }

    suspend fun checkAndTriggerAutoClear() {
        val currentSettings = settingsDao.getSettingsOnce() ?: return
        if (currentSettings.autoClearEnabled) {
            val freqHours = currentSettings.autoClearFrequencyHours
            val threshold = if (freqHours <= 0) {
                System.currentTimeMillis() + 1000L
            } else {
                System.currentTimeMillis() - (freqHours.toLong() * 3600000L)
            }
            taskDao.deleteCompletedTasksOlderThan(threshold)
            notifyWidget()
        }
    }

    suspend fun updateSettings(settings: AppSettings) {
        settingsDao.insertOrUpdate(settings)
        checkAndTriggerAutoClear()
        context?.let { ctx ->
            com.example.reminder.DailyReminderScheduler.scheduleReminders(
                context = ctx,
                morningEnabled = settings.morningReminderEnabled,
                morningHour = settings.morningReminderHour,
                morningMinute = settings.morningReminderMinute,
                eveningEnabled = settings.eveningReminderEnabled,
                eveningHour = settings.eveningReminderHour,
                eveningMinute = settings.eveningReminderMinute
            )
        }
    }

    suspend fun addSubtask(taskId: Long, title: String, parentSubtaskId: Long? = null): Long {
        val id = taskDao.insertSubtask(
            Subtask(
                taskId = taskId,
                parentSubtaskId = parentSubtaskId,
                title = title.trim(),
                isCompleted = false
            )
        )
        notifyWidget()
        return id
    }

    suspend fun setSubtaskCompleted(subtaskId: Long, isCompleted: Boolean) {
        taskDao.setSubtaskCompleted(subtaskId, isCompleted)
        notifyWidget()
    }

    suspend fun deleteSubtask(subtaskId: Long) {
        taskDao.deleteSubtaskById(subtaskId)
        notifyWidget()
    }

    // Pomodoro Focus Sessions
    val focusSessions: Flow<List<FocusSession>> = focusSessionDao?.getAllSessions()
        ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun recordFocusSession(session: FocusSession): Long {
        return focusSessionDao?.insertSession(session) ?: -1L
    }

    suspend fun deleteFocusSession(id: Long) {
        focusSessionDao?.deleteSessionById(id)
    }

    suspend fun clearFocusSessions() {
        focusSessionDao?.clearAllSessions()
    }

    // Habits & Streaks
    val habitsWithLogs: Flow<List<HabitWithLogs>> = habitDao?.getActiveHabitsWithLogs()?.map { list ->
        list.map { it.toModel() }
    } ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertHabit(habit: Habit): Long {
        return habitDao?.insertHabit(habit) ?: -1L
    }

    suspend fun updateHabit(habit: Habit) {
        habitDao?.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: Habit) {
        habitDao?.deleteHabit(habit)
    }

    suspend fun toggleHabitLog(habitId: Long, dateString: String, isCurrentlyCompleted: Boolean) {
        if (isCurrentlyCompleted) {
            habitDao?.deleteLog(habitId, dateString)
        } else {
            habitDao?.insertLog(
                HabitLog(
                    habitId = habitId,
                    dateString = dateString,
                    completedAt = System.currentTimeMillis()
                )
            )
            // Record habit completion XP
            recordHabitCompletedStats()
        }
    }

    suspend fun toggleHabitToday(habitId: Long, isCurrentlyCompleted: Boolean) {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        toggleHabitLog(habitId, todayStr, isCurrentlyCompleted)
    }

    val userProductivityStats: Flow<UserProductivityStats> get() = userStats

    // Gamification & User Stats
    val userStats: Flow<UserProductivityStats> = statsDao?.getStats()?.map { it ?: UserProductivityStats() }
        ?: kotlinx.coroutines.flow.flowOf(UserProductivityStats())

    suspend fun recordTaskCompletedStats(isPriorityHigh: Boolean = false) {
        val xpGain = if (isPriorityHigh) 25 else 15
        statsDao?.recordTaskCompleted(xpGain = xpGain)
        checkBadges()
    }

    suspend fun recordHabitCompletedStats() {
        statsDao?.recordHabitCompleted(xpGain = 20)
        checkBadges()
    }

    suspend fun recordFocusSessionCompletedStats(durationMinutes: Int) {
        val xpGain = (durationMinutes * 2).coerceAtLeast(20)
        statsDao?.recordFocusSession(minutes = durationMinutes, xpGain = xpGain)
        checkBadges()
    }

    suspend fun addBonusXp(amount: Int) {
        statsDao?.addXp(amount)
        checkBadges()
    }

    private suspend fun checkBadges() {
        val current = statsDao?.getStatsDirect() ?: return
        val currentUnlocked = current.unlockedBadgesSet.toMutableSet()
        var newlyUnlocked = false
        var bonusXp = 0

        for (achievement in AchievementRegistry.ALL_ACHIEVEMENTS) {
            if (!currentUnlocked.contains(achievement.id)) {
                val count = achievement.currentCountExtractor(current)
                if (count >= achievement.targetCount) {
                    currentUnlocked.add(achievement.id)
                    newlyUnlocked = true
                    bonusXp += achievement.xpReward
                }
            }
        }

        if (newlyUnlocked) {
            val updated = current.copy(
                totalXp = current.totalXp + bonusXp,
                unlockedBadgeIds = currentUnlocked.joinToString(",")
            )
            statsDao?.insertOrUpdate(updated)
        }
    }
}
