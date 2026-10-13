package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.TaskRepository
import com.example.ui.util.DailyCoachFeedback
import com.example.ui.util.MotivationalCoach
import com.example.ui.util.TaskDateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class SortOrder(val label: String) {
    MY_ORDER("My order"),
    DUE_DATE("Date"),
    PRIORITY("Priority"),
    STARRED("Starred recently")
}

data class TasksUiState(
    val lists: List<TaskList> = emptyList(),
    val selectedListId: Long? = null,
    val isStarredView: Boolean = false,
    val isCompletedView: Boolean = false,
    val isUpcomingRecurringView: Boolean = false,
    val currentListName: String = "My Tasks",
    val activeTasks: List<TaskWithSubtasks> = emptyList(),
    val completedTasks: List<TaskWithSubtasks> = emptyList(),
    val upcomingRecurringTasks: List<TaskWithSubtasks> = emptyList(),
    val allTasks: List<TaskWithSubtasks> = emptyList(),
    val isCalendarView: Boolean = false,
    val selectedCalendarDateMillis: Long = System.currentTimeMillis(),
    val selectedTag: String? = null,
    val availableTags: List<String> = emptyList(),
    val isUpcomingRecurringExpanded: Boolean = true,
    val userMessage: String? = null,
    val sortOrder: SortOrder = SortOrder.MY_ORDER,
    val dueDateFilter: TaskRepository.DueDateFilter = TaskRepository.DueDateFilter.ALL,
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isCompletedExpanded: Boolean = true,
    val expandedTaskIds: Set<Long> = emptySet(),
    val isAddTaskSheetOpen: Boolean = false,
    val initialDueDateForNewTask: Long? = null,
    val selectedTaskForDetail: TaskWithSubtasks? = null,
    val isCreateListDialogOpen: Boolean = false,
    val listToRename: TaskList? = null,
    val listToDelete: TaskList? = null,
    val isSortDialogOpen: Boolean = false,
    val isThemeDialogOpen: Boolean = false,
    val isAutoClearDialogOpen: Boolean = false,
    val isMoveTasksDialogOpen: Boolean = false,
    val isReminderDialogOpen: Boolean = false,
    val isSettingsDialogOpen: Boolean = false,
    val isBatchSelectionMode: Boolean = false,
    val selectedTaskIdsForBatch: Set<Long> = emptySet(),
    val appSettings: AppSettings = AppSettings(),
    val dailyCoachFeedback: DailyCoachFeedback = MotivationalCoach.evaluateDay(0, 0, 0),
    val isFocusTimerOpen: Boolean = false,
    val taskForFocusSession: TaskWithSubtasks? = null,
    val pomodoroTimerState: PomodoroTimerState = PomodoroTimerState(),
    val isAnalyticsDialogOpen: Boolean = false,
    val productivityStats: com.example.ui.components.ProductivityStats = com.example.ui.components.ProductivityStats(
        currentStreakDays = 0,
        longestStreakDays = 0,
        completedTodayCount = 0,
        totalCompletedCount = 0,
        todayFocusMinutes = 0,
        totalFocusMinutes = 0,
        totalFocusSessions = 0,
        weeklyVelocity = emptyList(),
        recentSessions = emptyList()
    ),
    val currentBottomTab: com.example.ui.workflows.AppBottomTab = com.example.ui.workflows.AppBottomTab.TASKS,
    val habits: List<com.example.data.model.HabitWithLogs> = emptyList(),
    val userProductivityStats: com.example.data.model.UserProductivityStats = com.example.data.model.UserProductivityStats()
)

data class PomodoroTimerState(
    val isRunning: Boolean = false,
    val sessionType: FocusSessionType = FocusSessionType.WORK,
    val selectedDurationMinutes: Int = 25,
    val totalSeconds: Int = 25 * 60,
    val secondsRemaining: Int = 25 * 60,
    val boundTask: TaskWithSubtasks? = null,
    val autoMarkComplete: Boolean = true,
    val pomodoroCount: Int = 1,
    val sessionFinishedCelebration: Boolean = false
)

private data class FilterState(
    val sortOrder: SortOrder,
    val dueDateFilter: TaskRepository.DueDateFilter,
    val searchQuery: String,
    val isSearching: Boolean,
    val isCompletedExpanded: Boolean,
    val isUpcomingRecurringExpanded: Boolean,
    val userMessage: String?,
    val expandedTaskIds: Set<Long>,
    val selectedTag: String?
)

private data class DialogsState(
    val isAddTaskSheetOpen: Boolean = false,
    val initialDueDateForNewTask: Long? = null,
    val isCreateListDialogOpen: Boolean = false,
    val listToRename: TaskList? = null,
    val listToDelete: TaskList? = null,
    val isSortDialogOpen: Boolean = false,
    val isThemeDialogOpen: Boolean = false,
    val isAutoClearDialogOpen: Boolean = false,
    val isMoveTasksDialogOpen: Boolean = false,
    val isReminderDialogOpen: Boolean = false,
    val isSettingsDialogOpen: Boolean = false,
    val isBatchSelectionMode: Boolean = false,
    val selectedTaskIdsForBatch: Set<Long> = emptySet(),
    val isFocusTimerOpen: Boolean = false,
    val taskForFocusSession: TaskWithSubtasks? = null,
    val pomodoroTimerState: PomodoroTimerState = PomodoroTimerState(),
    val isAnalyticsDialogOpen: Boolean = false,
    val isCalendarView: Boolean = false,
    val selectedCalendarDateMillis: Long = System.currentTimeMillis(),
    val currentBottomTab: com.example.ui.workflows.AppBottomTab = com.example.ui.workflows.AppBottomTab.TASKS
)

private data class CombinedBase(
    val allLists: List<TaskList>,
    val tasks: List<TaskWithSubtasks>,
    val selectedListId: Long?,
    val isStarred: Boolean,
    val isCompleted: Boolean
)

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _selectedListId = MutableStateFlow<Long?>(null)
    private val _isStarredView = MutableStateFlow(false)
    private val _isCompletedView = MutableStateFlow(false)
    private val _isUpcomingRecurringView = MutableStateFlow(false)
    private val _sortOrder = MutableStateFlow(SortOrder.MY_ORDER)
    private val _dueDateFilter = MutableStateFlow(TaskRepository.DueDateFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isSearching = MutableStateFlow(false)
    private val _isCompletedExpanded = MutableStateFlow(true)
    private val _isUpcomingRecurringExpanded = MutableStateFlow(true)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _expandedTaskIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _isAddTaskSheetOpen = MutableStateFlow(false)
    private val _selectedTaskId = MutableStateFlow<Long?>(null)
    private val _isCreateListDialogOpen = MutableStateFlow(false)
    private val _listToRename = MutableStateFlow<TaskList?>(null)
    private val _listToDelete = MutableStateFlow<TaskList?>(null)
    private val _isSortDialogOpen = MutableStateFlow(false)
    private val _isThemeDialogOpen = MutableStateFlow(false)
    private val _isAutoClearDialogOpen = MutableStateFlow(false)
    private val _isMoveTasksDialogOpen = MutableStateFlow(false)
    private val _isReminderDialogOpen = MutableStateFlow(false)
    private val _isSettingsDialogOpen = MutableStateFlow(false)
    private val _isBatchSelectionMode = MutableStateFlow(false)
    private val _selectedTaskIdsForBatch = MutableStateFlow<Set<Long>>(emptySet())
    private val _isFocusTimerOpen = MutableStateFlow(false)
    private val _taskForFocusSession = MutableStateFlow<TaskWithSubtasks?>(null)
    private val _pomodoroState = MutableStateFlow(PomodoroTimerState())
    val pomodoroState: StateFlow<PomodoroTimerState> = _pomodoroState.asStateFlow()
    private var pomodoroTimerJob: kotlinx.coroutines.Job? = null
    private val _isAnalyticsDialogOpen = MutableStateFlow(false)
    private val _isCalendarView = MutableStateFlow(false)
    private val _selectedCalendarDateMillis = MutableStateFlow(System.currentTimeMillis())
    private val _selectedTag = MutableStateFlow<String?>(null)
    private val _initialDueDateForNewTask = MutableStateFlow<Long?>(null)
    private val _currentBottomTab = MutableStateFlow(com.example.ui.workflows.AppBottomTab.TASKS)

    init {
        viewModelScope.launch {
            repository.ensureDefaultList()
            repository.checkAndTriggerAutoClear()
        }
    }

    val lists: StateFlow<List<TaskList>> = repository.allLists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    // Stream of tasks based on selected list, starred view, completed view, or upcoming recurring view
    private val rawTasksFlow: Flow<List<TaskWithSubtasks>> = combine(
        lists,
        _selectedListId,
        _isStarredView,
        _isCompletedView,
        _isUpcomingRecurringView
    ) { allLists, selId, isStarred, isCompleted, isUpcomingRecurring ->
        CombinedViewSelection(allLists, selId, isStarred, isCompleted, isUpcomingRecurring)
    }.flatMapLatest { selection ->
        when {
            selection.isCompleted -> repository.completedTasks
            selection.isStarred -> repository.starredTasks
            selection.isUpcomingRecurring -> repository.allTasks.map { all ->
                all.filter { !it.task.isCompleted && it.task.recurrenceFrequency != RecurrenceFrequency.NONE }
            }
            selection.selId != null && selection.allLists.any { it.id == selection.selId } -> repository.getTasksForList(selection.selId)
            selection.allLists.isNotEmpty() -> {
                val firstListId = selection.allLists.first().id
                if (_selectedListId.value != firstListId) {
                    _selectedListId.value = firstListId
                }
                repository.getTasksForList(firstListId)
            }
            else -> repository.allTasks
        }
    }

    // Combined coach feedback and productivity analytics/streaks
    private val metricsFlow: Flow<Pair<DailyCoachFeedback, com.example.ui.components.ProductivityStats>> = combine(
        repository.allTasks,
        repository.focusSessions
    ) { allTasksList, sessions ->
        val coach = computeDailyCoachFeedback(allTasksList)
        val stats = computeProductivityStats(allTasksList, sessions)
        Pair(coach, stats)
    }

    val dailyProgressFlow: Flow<DailyCoachFeedback> = metricsFlow.map { it.first }

    private fun computeDailyCoachFeedback(allTasksList: List<TaskWithSubtasks>): DailyCoachFeedback {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val endOfToday = cal.timeInMillis

        var completedTodayCount = 0
        var activeDueTodayCount = 0
        var overdueCount = 0

        allTasksList.forEach { taskWithSubs ->
            val task = taskWithSubs.task
            if (task.isCompleted) {
                if (task.completedAt != null && task.completedAt >= startOfToday) {
                    completedTodayCount++
                }
            } else {
                val dueDate = task.dueDateMillis
                if (dueDate != null) {
                    if (dueDate < startOfToday) {
                        overdueCount++
                    } else if (dueDate in startOfToday..<endOfToday) {
                        activeDueTodayCount++
                    }
                }
            }
        }

        val totalToday = completedTodayCount + activeDueTodayCount + overdueCount
        return MotivationalCoach.evaluateDay(completedTodayCount, totalToday, overdueCount)
    }

    private fun computeProductivityStats(
        allTasksList: List<TaskWithSubtasks>,
        sessions: List<FocusSession>
    ): com.example.ui.components.ProductivityStats {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        val completedTasks = allTasksList.filter { it.task.isCompleted && it.task.completedAt != null }
        val completedTodayCount = completedTasks.count { (it.task.completedAt ?: 0L) >= startOfToday }

        val completionDates = completedTasks
            .mapNotNull { it.task.completedAt }
            .map { dateFormat.format(java.util.Date(it)) }
            .toSet()

        var currentStreak = 0
        val checkCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStr = dateFormat.format(checkCal.time)
        checkCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = dateFormat.format(checkCal.time)

        if (completionDates.contains(todayStr)) {
            currentStreak = 1
            val prevCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -1)
            }
            while (completionDates.contains(dateFormat.format(prevCal.time))) {
                currentStreak++
                prevCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        } else if (completionDates.contains(yesterdayStr)) {
            currentStreak = 1
            val prevCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -2)
            }
            while (completionDates.contains(dateFormat.format(prevCal.time))) {
                currentStreak++
                prevCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        var longestStreak = currentStreak
        val sortedDates = completionDates.sorted()
        var tempStreak = 0
        var lastDayCal: Calendar? = null
        for (dateStr in sortedDates) {
            val dCal = Calendar.getInstance()
            try {
                dCal.time = dateFormat.parse(dateStr) ?: continue
                if (lastDayCal == null) {
                    tempStreak = 1
                } else {
                    val diffDays = ((dCal.timeInMillis - lastDayCal.timeInMillis) / 86400000L).toInt()
                    if (diffDays == 1) {
                        tempStreak++
                    } else if (diffDays > 1) {
                        tempStreak = 1
                    }
                }
                lastDayCal = dCal
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak
                }
            } catch (_: Exception) {}
        }

        val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        val weeklyVelocity = (6 downTo 0).map { dayOffset ->
            val dayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, -dayOffset)
            }
            val dayStart = dayCal.timeInMillis
            val dayEnd = dayStart + 86400000L - 1L
            val dayLabel = if (dayOffset == 0) "Today" else dayFormat.format(dayCal.time)
            val dayCompleted = completedTasks.count { (it.task.completedAt ?: 0L) in dayStart..dayEnd }
            val dayFocus = sessions
                .filter { it.completedAt in dayStart..dayEnd }
                .sumOf { it.durationMinutes }

            com.example.ui.components.DayCompletionStat(
                dayLabel = dayLabel,
                completedCount = dayCompleted,
                focusMinutes = dayFocus,
                isToday = dayOffset == 0
            )
        }

        val todayFocusMinutes = sessions
            .filter { it.completedAt >= startOfToday }
            .sumOf { it.durationMinutes }

        val totalFocusMinutes = sessions.sumOf { it.durationMinutes }

        return com.example.ui.components.ProductivityStats(
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak.coerceAtLeast(currentStreak),
            completedTodayCount = completedTodayCount,
            totalCompletedCount = completedTasks.size,
            todayFocusMinutes = todayFocusMinutes,
            totalFocusMinutes = totalFocusMinutes,
            totalFocusSessions = sessions.size,
            weeklyVelocity = weeklyVelocity,
            recentSessions = sessions.take(10)
        )
    }

    // Detail task live flow
    val selectedTaskDetail: StateFlow<TaskWithSubtasks?> = _selectedTaskId
        .flatMapLatest { taskId ->
            if (taskId != null) repository.getTaskWithSubtasks(taskId) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val filterStateFlow: Flow<FilterState> = combine(
        combine(_sortOrder, _searchQuery, _isSearching) { sort, query, searching ->
            Triple(sort, query, searching)
        },
        combine(_isCompletedExpanded, _isUpcomingRecurringExpanded) { compExp, recExp ->
            Pair(compExp, recExp)
        },
        _dueDateFilter,
        _userMessage,
        combine(_expandedTaskIds, _selectedTag) { taskIds, tag ->
            Pair(taskIds, tag)
        }
    ) { (sort, query, searching), (compExp, recExp), dueFilter, message, (taskIds, tag) ->
        FilterState(
            sortOrder = sort,
            dueDateFilter = dueFilter,
            searchQuery = query,
            isSearching = searching,
            isCompletedExpanded = compExp,
            isUpcomingRecurringExpanded = recExp,
            userMessage = message,
            expandedTaskIds = taskIds,
            selectedTag = tag
        )
    }

    private val dialogsStateFlow: Flow<DialogsState> = combine(
        combine(_isAddTaskSheetOpen, _initialDueDateForNewTask, _isCreateListDialogOpen, _listToRename) { addTask, initDue, createList, renameList ->
            listOf(addTask, initDue, createList, renameList)
        },
        combine(_listToDelete, _isSortDialogOpen, _isThemeDialogOpen) { deleteList, sortDialog, themeOpen ->
            Triple(deleteList, sortDialog, themeOpen)
        },
        combine(_isAutoClearDialogOpen, _isMoveTasksDialogOpen, _isReminderDialogOpen, _isSettingsDialogOpen) { autoClear, moveTasks, reminderOpen, settingsOpen ->
            listOf(autoClear, moveTasks, reminderOpen, settingsOpen)
        },
        combine(_isBatchSelectionMode, _selectedTaskIdsForBatch) { batchMode, selectedIds ->
            Pair(batchMode, selectedIds)
        },
        combine(
            combine(_isFocusTimerOpen, _pomodoroState, _isAnalyticsDialogOpen) { focusOpen, pomoState, analyticsOpen ->
                Triple(focusOpen, pomoState, analyticsOpen)
            },
            _isCalendarView,
            _selectedCalendarDateMillis,
            _currentBottomTab
        ) { (focusOpen, pomoState, analyticsOpen), calView, calDate, bottomTab ->
            listOf(focusOpen, pomoState, analyticsOpen, calView, calDate, bottomTab)
        }
    ) { addSheetData, (deleteList, sortDialog, themeOpen), settingsList, (batchMode, selectedIds), focusAndCalList ->
        val addTask = addSheetData[0] as Boolean
        val initDue = addSheetData[1] as Long?
        val createList = addSheetData[2] as Boolean
        val renameList = addSheetData[3] as TaskList?
        val autoClear = settingsList[0] as Boolean
        val moveTasks = settingsList[1] as Boolean
        val reminderOpen = settingsList[2] as Boolean
        val settingsOpen = settingsList[3] as Boolean
        val focusOpen = focusAndCalList[0] as Boolean
        val pomoState = focusAndCalList[1] as PomodoroTimerState
        val analyticsOpen = focusAndCalList[2] as Boolean
        val calView = focusAndCalList[3] as Boolean
        val calDate = focusAndCalList[4] as Long
        val currentTab = focusAndCalList[5] as com.example.ui.workflows.AppBottomTab
        DialogsState(
            isAddTaskSheetOpen = addTask,
            initialDueDateForNewTask = initDue,
            isCreateListDialogOpen = createList,
            listToRename = renameList,
            listToDelete = deleteList,
            isSortDialogOpen = sortDialog,
            isThemeDialogOpen = themeOpen,
            isAutoClearDialogOpen = autoClear,
            isMoveTasksDialogOpen = moveTasks,
            isReminderDialogOpen = reminderOpen,
            isSettingsDialogOpen = settingsOpen,
            isBatchSelectionMode = batchMode,
            selectedTaskIdsForBatch = selectedIds,
            isFocusTimerOpen = focusOpen,
            taskForFocusSession = pomoState.boundTask,
            pomodoroTimerState = pomoState,
            isAnalyticsDialogOpen = analyticsOpen,
            isCalendarView = calView,
            selectedCalendarDateMillis = calDate,
            currentBottomTab = currentTab
        )
    }

    private val baseTasksUiState: Flow<TasksUiState> = combine(
        lists,
        rawTasksFlow,
        _selectedListId,
        _isStarredView,
        _isCompletedView
    ) { allLists, tasks, selectedListId, isStarred, isCompleted ->
        CombinedBase(allLists, tasks, selectedListId, isStarred, isCompleted)
    }.combine(_isUpcomingRecurringView) { base, isUpcomingRecurring ->
        val validSelectedListId = if (base.selectedListId != null && base.allLists.any { it.id == base.selectedListId }) {
            base.selectedListId
        } else {
            base.allLists.firstOrNull()?.id
        }

        val listName = when {
            isUpcomingRecurring -> "Upcoming Repeating"
            base.isCompleted -> "Completed"
            base.isStarred -> "Starred"
            validSelectedListId != null -> base.allLists.find { it.id == validSelectedListId }?.name ?: "Tasks"
            base.allLists.isNotEmpty() -> base.allLists.first().name
            else -> "My Tasks"
        }

        val active: List<TaskWithSubtasks>
        val upcoming: List<TaskWithSubtasks>
        val completed: List<TaskWithSubtasks>

        when {
            isUpcomingRecurring -> {
                // Repeating view:
                // Repeating tasks whose date has arrived (today or overdue or no future constraint) are active and checkable!
                // Future repeating tasks whose date hasn't arrived are in upcoming and locked!
                active = base.tasks.filter {
                    !it.task.isCompleted &&
                    (it.task.dueDateMillis == null || !TaskDateUtils.isFutureDate(it.task.dueDateMillis))
                }
                upcoming = base.tasks.filter {
                    !it.task.isCompleted &&
                    TaskDateUtils.isFutureDate(it.task.dueDateMillis)
                }
                completed = emptyList()
            }
            base.isCompleted -> {
                active = emptyList()
                upcoming = emptyList()
                completed = base.tasks
            }
            else -> {
                // In standard list view or starred view:
                // Active tasks ONLY (recurring tasks only appear when they are actually due today or overdue).
                // Upcoming repeated tasks (due in future) only appear in the "Upcoming Repeating" view,
                // not in this list, and not in any other one in the end.
                active = base.tasks.filter {
                    !it.task.isCompleted &&
                    (it.task.recurrenceFrequency == RecurrenceFrequency.NONE || !TaskDateUtils.isFutureDate(it.task.dueDateMillis))
                }
                upcoming = emptyList()
                completed = base.tasks.filter { it.task.isCompleted }
            }
        }

        TasksUiState(
            lists = base.allLists,
            selectedListId = validSelectedListId,
            isStarredView = base.isStarred,
            isCompletedView = base.isCompleted,
            isUpcomingRecurringView = isUpcomingRecurring,
            currentListName = listName,
            activeTasks = active,
            upcomingRecurringTasks = upcoming,
            completedTasks = completed
        )
    }.combine(filterStateFlow) { state, filter ->
        val filteredActive = filterAndSortTasks(state.activeTasks, filter.searchQuery, filter.sortOrder, filter.dueDateFilter, filter.selectedTag)
        val filteredUpcoming = filterAndSortTasks(state.upcomingRecurringTasks, filter.searchQuery, filter.sortOrder, filter.dueDateFilter, filter.selectedTag)
        val filteredCompleted = filterAndSortTasks(state.completedTasks, filter.searchQuery, filter.sortOrder, filter.dueDateFilter, filter.selectedTag)

        state.copy(
            activeTasks = filteredActive,
            upcomingRecurringTasks = filteredUpcoming,
            completedTasks = filteredCompleted,
            selectedTag = filter.selectedTag,
            sortOrder = filter.sortOrder,
            dueDateFilter = filter.dueDateFilter,
            searchQuery = filter.searchQuery,
            isSearching = filter.isSearching,
            isCompletedExpanded = filter.isCompletedExpanded,
            isUpcomingRecurringExpanded = filter.isUpcomingRecurringExpanded,
            userMessage = filter.userMessage,
            expandedTaskIds = filter.expandedTaskIds
        )
    }.combine(repository.allTasks) { state, allTasksList ->
        val availableTags = allTasksList.flatMap { it.task.getTagList() }.distinct().sorted()
        state.copy(
            allTasks = allTasksList,
            availableTags = availableTags
        )
    }

    private val coreUiStateFlow: Flow<TasksUiState> = combine(
        baseTasksUiState,
        dialogsStateFlow,
        selectedTaskDetail,
        settings,
        metricsFlow
    ) { baseState, dialogs, detail, appSettings, (coachFeedback, stats) ->
        baseState.copy(
            isAddTaskSheetOpen = dialogs.isAddTaskSheetOpen,
            initialDueDateForNewTask = dialogs.initialDueDateForNewTask,
            isCreateListDialogOpen = dialogs.isCreateListDialogOpen,
            listToRename = dialogs.listToRename,
            listToDelete = dialogs.listToDelete,
            isSortDialogOpen = dialogs.isSortDialogOpen,
            isThemeDialogOpen = dialogs.isThemeDialogOpen,
            isAutoClearDialogOpen = dialogs.isAutoClearDialogOpen,
            isMoveTasksDialogOpen = dialogs.isMoveTasksDialogOpen,
            isReminderDialogOpen = dialogs.isReminderDialogOpen,
            isSettingsDialogOpen = dialogs.isSettingsDialogOpen,
            isBatchSelectionMode = dialogs.isBatchSelectionMode,
            selectedTaskIdsForBatch = dialogs.selectedTaskIdsForBatch,
            isFocusTimerOpen = dialogs.isFocusTimerOpen,
            taskForFocusSession = dialogs.taskForFocusSession,
            pomodoroTimerState = dialogs.pomodoroTimerState,
            isAnalyticsDialogOpen = dialogs.isAnalyticsDialogOpen,
            isCalendarView = dialogs.isCalendarView,
            selectedCalendarDateMillis = dialogs.selectedCalendarDateMillis,
            currentBottomTab = dialogs.currentBottomTab,
            selectedTaskForDetail = detail,
            appSettings = appSettings,
            dailyCoachFeedback = coachFeedback,
            productivityStats = stats
        )
    }

    val uiState: StateFlow<TasksUiState> = combine(
        coreUiStateFlow,
        repository.habitsWithLogs,
        repository.userProductivityStats
    ) { coreState, habitsList, userStats ->
        coreState.copy(
            habits = habitsList,
            userProductivityStats = userStats
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState()
    )

    private fun filterAndSortTasks(
        tasks: List<TaskWithSubtasks>,
        query: String,
        order: SortOrder,
        dueDateFilter: TaskRepository.DueDateFilter = TaskRepository.DueDateFilter.ALL,
        selectedTag: String? = null
    ): List<TaskWithSubtasks> {
        val dateFiltered = repository.filterTasksByDueDate(tasks, dueDateFilter)
        val tagFiltered = if (!selectedTag.isNullOrBlank()) {
            dateFiltered.filter { it.task.getTagList().contains(selectedTag) }
        } else {
            dateFiltered
        }
        val filtered = if (query.isNotBlank()) {
            tagFiltered.filter { taskWithSubtasks ->
                taskWithSubtasks.task.title.contains(query, ignoreCase = true) ||
                taskWithSubtasks.task.details.contains(query, ignoreCase = true) ||
                taskWithSubtasks.task.tags.contains(query, ignoreCase = true) ||
                taskWithSubtasks.subtasks.any { sub -> sub.title.contains(query, ignoreCase = true) }
            }
        } else {
            tagFiltered
        }

        return when (order) {
            SortOrder.MY_ORDER -> filtered.sortedBy { it.task.orderIndex }
            SortOrder.DUE_DATE -> filtered.sortedWith(
                compareBy<TaskWithSubtasks> { it.task.dueDateMillis == null }
                    .thenBy { it.task.dueDateMillis ?: Long.MAX_VALUE }
                    .thenByDescending { it.task.priority.weight }
            )
            SortOrder.PRIORITY -> filtered.sortedWith(
                compareByDescending<TaskWithSubtasks> { it.task.priority.weight }
                    .thenBy { it.task.dueDateMillis ?: Long.MAX_VALUE }
            )
            SortOrder.STARRED -> filtered.sortedWith(
                compareByDescending<TaskWithSubtasks> { it.task.isStarred }
                    .thenByDescending { it.task.priority.weight }
                    .thenBy { it.task.dueDateMillis ?: Long.MAX_VALUE }
            )
        }
    }

    fun toggleTaskExpanded(taskId: Long) {
        val current = _expandedTaskIds.value.toMutableSet()
        if (current.contains(taskId)) {
            current.remove(taskId)
        } else {
            current.add(taskId)
        }
        _expandedTaskIds.value = current
    }

    // List Navigation
    fun selectList(listId: Long?) {
        _isStarredView.value = false
        _isCompletedView.value = false
        _isUpcomingRecurringView.value = false
        _isCalendarView.value = false
        _selectedListId.value = listId
    }

    fun selectStarredView() {
        _isStarredView.value = true
        _isCompletedView.value = false
        _isUpcomingRecurringView.value = false
        _isCalendarView.value = false
    }

    fun selectCompletedView() {
        _isCompletedView.value = true
        _isStarredView.value = false
        _isUpcomingRecurringView.value = false
        _isCalendarView.value = false
    }

    fun selectUpcomingRecurringView() {
        _isUpcomingRecurringView.value = true
        _isStarredView.value = false
        _isCompletedView.value = false
        _isCalendarView.value = false
    }

    fun selectCalendarView() {
        _isStarredView.value = false
        _isCompletedView.value = false
        _isUpcomingRecurringView.value = false
        _isCalendarView.value = true
    }

    fun setSelectedCalendarDate(dateMillis: Long) {
        _selectedCalendarDateMillis.value = dateMillis
    }

    fun selectTag(tag: String?) {
        _selectedTag.value = if (_selectedTag.value == tag) null else tag
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
        _isSortDialogOpen.value = false
    }

    fun setDueDateFilter(filter: TaskRepository.DueDateFilter) {
        _dueDateFilter.value = filter
        _isSortDialogOpen.value = false
    }

    fun toggleSortDialog(open: Boolean) {
        _isSortDialogOpen.value = open
    }

    fun toggleThemeDialog(open: Boolean) {
        _isThemeDialogOpen.value = open
    }

    fun toggleAutoClearDialog(open: Boolean) {
        _isAutoClearDialogOpen.value = open
    }

    fun toggleCompletedSection() {
        _isCompletedExpanded.value = !_isCompletedExpanded.value
    }

    fun toggleUpcomingRecurringSection() {
        _isUpcomingRecurringExpanded.value = !_isUpcomingRecurringExpanded.value
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Search
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearching(searching: Boolean) {
        _isSearching.value = searching
        if (!searching) {
            _searchQuery.value = ""
        }
    }

    // Quick Add
    fun openAddTaskSheet(initialDueDate: Long? = null) {
        _initialDueDateForNewTask.value = initialDueDate
        _isAddTaskSheetOpen.value = true
    }

    fun closeAddTaskSheet() {
        _isAddTaskSheetOpen.value = false
        _initialDueDateForNewTask.value = null
    }

    fun createTask(
        title: String,
        details: String = "",
        dueDateMillis: Long? = null,
        hasTime: Boolean = false,
        priority: Priority = Priority.NONE,
        isStarred: Boolean = false,
        targetListId: Long? = null,
        recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
        recurrenceInterval: Int = 1,
        recurrenceTimesPerPeriod: Int = 1,
        recurrenceDaysOfWeek: String = "",
        recurrenceDaysOfMonth: String = "",
        tags: String = ""
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val listId = targetListId
                ?: _selectedListId.value
                ?: lists.value.firstOrNull()?.id
                ?: 1L

            val effectiveDueDate = dueDateMillis ?: if (recurrenceFrequency != RecurrenceFrequency.NONE) {
                TaskDateUtils.computeInitialOccurrenceDate(
                    recurrenceFrequency,
                    recurrenceDaysOfWeek,
                    recurrenceDaysOfMonth
                )
            } else null

            val newTask = Task(
                listId = listId,
                title = title.trim(),
                details = details.trim(),
                dueDateMillis = effectiveDueDate,
                hasTime = hasTime,
                priority = priority,
                recurrenceFrequency = recurrenceFrequency,
                recurrenceInterval = recurrenceInterval,
                recurrenceTimesPerPeriod = recurrenceTimesPerPeriod,
                recurrenceDaysOfWeek = recurrenceDaysOfWeek,
                recurrenceDaysOfMonth = recurrenceDaysOfMonth,
                isStarred = isStarred || _isStarredView.value,
                orderIndex = 0,
                tags = tags.trim()
            )
            repository.insertTask(newTask)
            _isAddTaskSheetOpen.value = false
            _initialDueDateForNewTask.value = null
        }
    }

    // Task Actions
    fun toggleTaskComplete(taskWithSubtasks: TaskWithSubtasks) {
        val task = taskWithSubtasks.task
        // Prevent completing upcoming repeating tasks before their scheduled day
        if (!task.isCompleted && task.recurrenceFrequency != RecurrenceFrequency.NONE && TaskDateUtils.isFutureDate(task.dueDateMillis)) {
            val dateText = TaskDateUtils.formatDueDate(task.dueDateMillis, task.hasTime)
            _userMessage.value = "Scheduled for $dateText. Upcoming repeating tasks can only be completed when that day arrives."
            return
        }

        viewModelScope.launch {
            val willBeCompleted = !taskWithSubtasks.task.isCompleted
            repository.setTaskCompleted(
                taskWithSubtasks.task.id,
                willBeCompleted
            )
            if (willBeCompleted) {
                repository.recordTaskCompletedStats(task.priority == Priority.HIGH)
            }
        }
    }

    fun toggleTaskStarred(taskWithSubtasks: TaskWithSubtasks) {
        viewModelScope.launch {
            repository.setTaskStarred(
                taskWithSubtasks.task.id,
                !taskWithSubtasks.task.isStarred
            )
        }
    }

    fun openTaskDetail(taskWithSubtasks: TaskWithSubtasks) {
        _selectedTaskId.value = taskWithSubtasks.task.id
    }

    fun closeTaskDetail() {
        _selectedTaskId.value = null
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            val existing = repository.getTaskById(task.id)
            if (existing != null && existing.isCompleted != task.isCompleted) {
                repository.updateTask(task)
                repository.setTaskCompleted(task.id, task.isCompleted)
            } else {
                repository.updateTask(task)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            if (_selectedTaskId.value == task.id) {
                _selectedTaskId.value = null
            }
        }
    }

    fun restoreTask(taskWithSubtasks: TaskWithSubtasks) {
        viewModelScope.launch {
            repository.restoreTaskWithSubtasks(taskWithSubtasks)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            val listId = if (_isStarredView.value || _isCompletedView.value) null else _selectedListId.value
            repository.deleteCompletedTasks(listId)
        }
    }

    // Subtasks
    fun addSubtask(taskId: Long, title: String, parentSubtaskId: Long? = null) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addSubtask(taskId, title, parentSubtaskId)
            _expandedTaskIds.value = _expandedTaskIds.value + taskId
        }
    }

    fun toggleSubtaskComplete(subtask: Subtask) {
        viewModelScope.launch {
            val parentTask = repository.getTaskById(subtask.taskId)
            if (parentTask != null && !parentTask.isCompleted &&
                parentTask.recurrenceFrequency != RecurrenceFrequency.NONE &&
                TaskDateUtils.isFutureDate(parentTask.dueDateMillis)
            ) {
                val dateText = TaskDateUtils.formatDueDate(parentTask.dueDateMillis, parentTask.hasTime)
                _userMessage.value = "Scheduled for $dateText. Subtasks of upcoming repeating tasks can only be completed when that day arrives."
                return@launch
            }
            repository.setSubtaskCompleted(subtask.id, !subtask.isCompleted)
        }
    }

    fun deleteSubtask(subtaskId: Long) {
        viewModelScope.launch {
            repository.deleteSubtask(subtaskId)
        }
    }

    // List Management
    fun openCreateListDialog() {
        _isCreateListDialogOpen.value = true
    }

    fun closeCreateListDialog() {
        _isCreateListDialogOpen.value = false
    }

    fun createList(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = repository.insertList(name)
            _isCreateListDialogOpen.value = false
            selectList(newId)
        }
    }

    fun openRenameListDialog(list: TaskList) {
        _listToRename.value = list
    }

    fun closeRenameListDialog() {
        _listToRename.value = null
    }

    fun renameList(list: TaskList, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateList(list.copy(name = newName.trim()))
            _listToRename.value = null
        }
    }

    fun openDeleteListDialog(list: TaskList) {
        _listToDelete.value = list
    }

    fun closeDeleteListDialog() {
        _listToDelete.value = null
    }

    fun deleteList(list: TaskList) {
        viewModelScope.launch {
            repository.deleteList(list)
            _listToDelete.value = null
            val fallback = lists.value.firstOrNull { it.id != list.id }?.id
            _selectedListId.value = fallback ?: 1L
        }
    }

    // Multi-Select / Batch Mode
    fun enterBatchSelection(taskId: Long) {
        _isBatchSelectionMode.value = true
        _selectedTaskIdsForBatch.value = setOf(taskId)
    }

    fun toggleTaskSelection(taskId: Long) {
        val current = _selectedTaskIdsForBatch.value.toMutableSet()
        if (current.contains(taskId)) {
            current.remove(taskId)
            if (current.isEmpty()) {
                _isBatchSelectionMode.value = false
            }
        } else {
            current.add(taskId)
        }
        _selectedTaskIdsForBatch.value = current
    }

    fun selectAllTasks(taskIds: List<Long>) {
        if (_selectedTaskIdsForBatch.value.size == taskIds.size) {
            _selectedTaskIdsForBatch.value = emptySet()
            _isBatchSelectionMode.value = false
        } else {
            _selectedTaskIdsForBatch.value = taskIds.toSet()
            _isBatchSelectionMode.value = true
        }
    }

    fun clearBatchSelection() {
        _isBatchSelectionMode.value = false
        _selectedTaskIdsForBatch.value = emptySet()
    }

    fun openMoveTasksDialog() {
        _isMoveTasksDialogOpen.value = true
    }

    fun closeMoveTasksDialog() {
        _isMoveTasksDialogOpen.value = false
    }

    fun moveSelectedTasks(targetListId: Long) {
        val taskIds = _selectedTaskIdsForBatch.value
        if (taskIds.isEmpty()) return
        viewModelScope.launch {
            repository.moveTasksToList(taskIds, targetListId)
            _selectedTaskIdsForBatch.value = emptySet()
            _isBatchSelectionMode.value = false
            _isMoveTasksDialogOpen.value = false
            _userMessage.value = "Moved ${taskIds.size} task(s)"
        }
    }

    fun deleteSelectedTasks() {
        val taskIds = _selectedTaskIdsForBatch.value
        if (taskIds.isEmpty()) return
        viewModelScope.launch {
            repository.deleteTasks(taskIds)
            _selectedTaskIdsForBatch.value = emptySet()
            _isBatchSelectionMode.value = false
            _userMessage.value = "Deleted ${taskIds.size} task(s)"
        }
    }

    // Reminders
    fun openSettingsDialog() {
        _isSettingsDialogOpen.value = true
    }

    fun closeSettingsDialog() {
        _isSettingsDialogOpen.value = false
    }

    fun toggleHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(hapticFeedbackEnabled = enabled))
        }
    }

    fun openReminderDialog() {
        _isReminderDialogOpen.value = true
    }

    fun closeReminderDialog() {
        _isReminderDialogOpen.value = false
    }

    fun updateReminderSettings(
        morningEnabled: Boolean,
        morningHour: Int,
        morningMinute: Int,
        eveningEnabled: Boolean,
        eveningHour: Int,
        eveningMinute: Int
    ) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    morningReminderEnabled = morningEnabled,
                    morningReminderHour = morningHour,
                    morningReminderMinute = morningMinute,
                    eveningReminderEnabled = eveningEnabled,
                    eveningReminderHour = eveningHour,
                    eveningReminderMinute = eveningMinute
                )
            )
            _isReminderDialogOpen.value = false
            _userMessage.value = "Reminder settings saved"
        }
    }

    fun sendTestReminder(context: android.content.Context) {
        com.example.reminder.DailyReminderScheduler.triggerTestNotification(context)
    }

    // Theme & Settings
    fun updateThemePreset(presetId: String) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(current.copy(themePresetId = presetId))
        }
    }

    fun updateCustomTheme(primaryHex: String, bgHex: String, surfaceHex: String, isLight: Boolean = false) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    themePresetId = "custom",
                    customPrimaryHex = primaryHex,
                    customBackgroundHex = bgHex,
                    customSurfaceHex = surfaceHex,
                    isCustomThemeLight = isLight
                )
            )
        }
    }

    fun updateAutoClear(enabled: Boolean, frequencyHours: Int) {
        viewModelScope.launch {
            val current = settings.value
            repository.updateSettings(
                current.copy(
                    autoClearEnabled = enabled,
                    autoClearFrequencyHours = frequencyHours
                )
            )
        }
    }

    // Pomodoro Focus Timer & Productivity Analytics
    fun openFocusTimer(taskWithSubtasks: TaskWithSubtasks? = null) {
        if (taskWithSubtasks != null && !_pomodoroState.value.isRunning) {
            _pomodoroState.update { it.copy(boundTask = taskWithSubtasks) }
        }
        _isFocusTimerOpen.value = true
    }

    fun closeFocusTimer() {
        _isFocusTimerOpen.value = false
        // Notice: Timer keeps running in background; state and task are preserved!
    }

    fun startPomodoroTimer() {
        if (_pomodoroState.value.isRunning) return
        _pomodoroState.update { it.copy(isRunning = true, sessionFinishedCelebration = false) }
        pomodoroTimerJob?.cancel()
        pomodoroTimerJob = viewModelScope.launch {
            while (_pomodoroState.value.secondsRemaining > 0 && _pomodoroState.value.isRunning) {
                kotlinx.coroutines.delay(1000L)
                if (!_pomodoroState.value.isRunning) break
                val newRemaining = _pomodoroState.value.secondsRemaining - 1
                _pomodoroState.update { it.copy(secondsRemaining = newRemaining) }
                if (newRemaining <= 0) {
                    onPomodoroTimerFinished()
                    break
                }
            }
        }
    }

    private fun onPomodoroTimerFinished() {
        val current = _pomodoroState.value
        _pomodoroState.update {
            it.copy(
                isRunning = false,
                sessionFinishedCelebration = true
            )
        }
        completeFocusSession(
            durationMinutes = current.selectedDurationMinutes,
            sessionType = current.sessionType,
            boundTask = current.boundTask,
            autoMarkTaskComplete = current.autoMarkComplete
        )
        // Transition next session type automatically
        val nextType = when (current.sessionType) {
            com.example.data.model.FocusSessionType.WORK -> {
                val newCount = current.pomodoroCount + 1
                if (newCount % 4 == 0) com.example.data.model.FocusSessionType.LONG_BREAK else com.example.data.model.FocusSessionType.SHORT_BREAK
            }
            com.example.data.model.FocusSessionType.SHORT_BREAK, com.example.data.model.FocusSessionType.LONG_BREAK -> com.example.data.model.FocusSessionType.WORK
        }
        val nextMinutes = when (nextType) {
            com.example.data.model.FocusSessionType.WORK -> 25
            com.example.data.model.FocusSessionType.SHORT_BREAK -> 5
            com.example.data.model.FocusSessionType.LONG_BREAK -> 15
        }
        _pomodoroState.update {
            it.copy(
                sessionType = nextType,
                selectedDurationMinutes = nextMinutes,
                totalSeconds = nextMinutes * 60,
                secondsRemaining = nextMinutes * 60,
                pomodoroCount = if (current.sessionType == com.example.data.model.FocusSessionType.WORK) current.pomodoroCount + 1 else current.pomodoroCount
            )
        }
    }

    fun pausePomodoroTimer() {
        pomodoroTimerJob?.cancel()
        _pomodoroState.update { it.copy(isRunning = false) }
    }

    fun resetPomodoroTimer() {
        pomodoroTimerJob?.cancel()
        _pomodoroState.update {
            it.copy(
                isRunning = false,
                sessionFinishedCelebration = false,
                secondsRemaining = it.totalSeconds
            )
        }
    }

    fun extendPomodoroTimer(additionalSeconds: Int = 300) {
        _pomodoroState.update {
            it.copy(
                secondsRemaining = it.secondsRemaining + additionalSeconds,
                totalSeconds = it.totalSeconds + additionalSeconds
            )
        }
    }

    fun skipPomodoroTimer() {
        pomodoroTimerJob?.cancel()
        val current = _pomodoroState.value
        val nextType = when (current.sessionType) {
            com.example.data.model.FocusSessionType.WORK -> {
                if (current.pomodoroCount % 4 == 0) com.example.data.model.FocusSessionType.LONG_BREAK else com.example.data.model.FocusSessionType.SHORT_BREAK
            }
            com.example.data.model.FocusSessionType.SHORT_BREAK, com.example.data.model.FocusSessionType.LONG_BREAK -> com.example.data.model.FocusSessionType.WORK
        }
        val nextMinutes = when (nextType) {
            com.example.data.model.FocusSessionType.WORK -> 25
            com.example.data.model.FocusSessionType.SHORT_BREAK -> 5
            com.example.data.model.FocusSessionType.LONG_BREAK -> 15
        }
        _pomodoroState.update {
            it.copy(
                isRunning = false,
                sessionType = nextType,
                selectedDurationMinutes = nextMinutes,
                totalSeconds = nextMinutes * 60,
                secondsRemaining = nextMinutes * 60,
                pomodoroCount = if (current.sessionType == com.example.data.model.FocusSessionType.WORK) current.pomodoroCount + 1 else current.pomodoroCount,
                sessionFinishedCelebration = false
            )
        }
    }

    fun setPomodoroDurationMinutes(minutes: Int) {
        if (_pomodoroState.value.isRunning) return
        _pomodoroState.update {
            it.copy(
                selectedDurationMinutes = minutes,
                totalSeconds = minutes * 60,
                secondsRemaining = minutes * 60,
                sessionFinishedCelebration = false
            )
        }
    }

    fun setPomodoroSessionType(type: com.example.data.model.FocusSessionType) {
        if (_pomodoroState.value.isRunning) return
        val defaultMinutes = when (type) {
            com.example.data.model.FocusSessionType.WORK -> 25
            com.example.data.model.FocusSessionType.SHORT_BREAK -> 5
            com.example.data.model.FocusSessionType.LONG_BREAK -> 15
        }
        _pomodoroState.update {
            it.copy(
                sessionType = type,
                selectedDurationMinutes = defaultMinutes,
                totalSeconds = defaultMinutes * 60,
                secondsRemaining = defaultMinutes * 60,
                sessionFinishedCelebration = false
            )
        }
    }

    fun setPomodoroBoundTask(task: TaskWithSubtasks?) {
        _pomodoroState.update { it.copy(boundTask = task) }
    }

    fun setPomodoroAutoMarkComplete(auto: Boolean) {
        _pomodoroState.update { it.copy(autoMarkComplete = auto) }
    }

    fun dismissPomodoroCelebration() {
        _pomodoroState.update { it.copy(sessionFinishedCelebration = false) }
    }

    fun completeFocusSession(
        durationMinutes: Int,
        sessionType: com.example.data.model.FocusSessionType,
        boundTask: TaskWithSubtasks?,
        autoMarkTaskComplete: Boolean
    ) {
        viewModelScope.launch {
            val session = com.example.data.model.FocusSession(
                taskId = boundTask?.task?.id,
                taskTitle = boundTask?.task?.title ?: "",
                durationMinutes = durationMinutes,
                sessionType = sessionType,
                completedAt = System.currentTimeMillis()
            )
            repository.recordFocusSession(session)

            if (autoMarkTaskComplete && boundTask != null && !boundTask.task.isCompleted) {
                toggleTaskComplete(boundTask)
            }
            repository.recordFocusSessionCompletedStats(durationMinutes)

            _userMessage.value = "Great work! $durationMinutes min of ${sessionType.label} logged."
        }
    }

    fun openAnalyticsDialog() {
        _isAnalyticsDialogOpen.value = true
    }

    fun closeAnalyticsDialog() {
        _isAnalyticsDialogOpen.value = false
    }

    // Bottom Navigation
    fun selectBottomTab(tab: com.example.ui.workflows.AppBottomTab) {
        _currentBottomTab.value = tab
    }

    // Habits Operations
    fun toggleHabitToday(habitId: Long, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitToday(habitId, isCurrentlyCompleted)
        }
    }

    fun insertHabit(habit: Habit) {
        viewModelScope.launch {
            repository.insertHabit(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Workflows / Eisenhower Matrix / Kanban
    fun moveTaskToQuadrant(task: Task, quadrant: com.example.ui.workflows.EisenhowerQuadrant) {
        viewModelScope.launch {
            val updated = when (quadrant) {
                com.example.ui.workflows.EisenhowerQuadrant.DO_FIRST -> {
                    task.copy(
                        priority = Priority.HIGH,
                        dueDateMillis = TaskDateUtils.getTodayMillis()
                    )
                }
                com.example.ui.workflows.EisenhowerQuadrant.SCHEDULE -> {
                    task.copy(
                        priority = Priority.HIGH,
                        dueDateMillis = TaskDateUtils.getNextWeekMillis()
                    )
                }
                com.example.ui.workflows.EisenhowerQuadrant.DELEGATE -> {
                    task.copy(
                        priority = Priority.LOW,
                        dueDateMillis = TaskDateUtils.getTodayMillis()
                    )
                }
                com.example.ui.workflows.EisenhowerQuadrant.ELIMINATE -> {
                    task.copy(
                        priority = Priority.LOW,
                        dueDateMillis = null
                    )
                }
            }
            repository.updateTask(updated)
            _userMessage.value = "Moved to ${quadrant.title}"
        }
    }

    fun moveTaskKanban(task: Task, column: com.example.ui.workflows.KanbanColumn) {
        viewModelScope.launch {
            when (column) {
                com.example.ui.workflows.KanbanColumn.TODO -> {
                    repository.setTaskCompleted(task.id, false)
                }
                com.example.ui.workflows.KanbanColumn.IN_PROGRESS -> {
                    repository.setTaskCompleted(task.id, false)
                    // If no focus session running, optionally prompt focus
                    val taskWithSubs = uiState.value.allTasks.find { it.task.id == task.id }
                    if (taskWithSubs != null && !_pomodoroState.value.isRunning) {
                        openFocusTimer(taskWithSubs)
                    }
                }
                com.example.ui.workflows.KanbanColumn.DONE -> {
                    if (!task.isCompleted) {
                        repository.setTaskCompleted(task.id, true)
                        repository.recordTaskCompletedStats(task.priority == Priority.HIGH)
                    }
                }
            }
        }
    }

    fun quickAddTaskToQuadrant(quadrant: com.example.ui.workflows.EisenhowerQuadrant) {
        val (dueMillis, priority) = when (quadrant) {
            com.example.ui.workflows.EisenhowerQuadrant.DO_FIRST -> Pair(TaskDateUtils.getTodayMillis(), Priority.HIGH)
            com.example.ui.workflows.EisenhowerQuadrant.SCHEDULE -> Pair(TaskDateUtils.getNextWeekMillis(), Priority.HIGH)
            com.example.ui.workflows.EisenhowerQuadrant.DELEGATE -> Pair(TaskDateUtils.getTodayMillis(), Priority.LOW)
            com.example.ui.workflows.EisenhowerQuadrant.ELIMINATE -> Pair(null, Priority.LOW)
        }
        _initialDueDateForNewTask.value = dueMillis
        _isAddTaskSheetOpen.value = true
    }
}

private data class CombinedViewSelection(
    val allLists: List<TaskList>,
    val selId: Long?,
    val isStarred: Boolean,
    val isCompleted: Boolean,
    val isUpcomingRecurring: Boolean
)

class TaskViewModelFactory(
    private val repository: TaskRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
