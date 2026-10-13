package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskList::class,
        Task::class,
        Subtask::class,
        AppSettings::class,
        FocusSession::class,
        Habit::class,
        HabitLog::class,
        UserProductivityStats::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskListDao(): TaskListDao
    abstract fun taskDao(): TaskDao
    abstract fun settingsDao(): SettingsDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun habitDao(): HabitDao
    abstract fun statsDao(): StatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tasks_database.db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(DatabaseCallback(context.applicationContext, scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val appContext: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch {
                    val database = getDatabase(appContext)
                    populateInitialData(
                        database.taskListDao(),
                        database.taskDao(),
                        database.settingsDao(),
                        database.habitDao(),
                        database.statsDao()
                    )
                }
            }
        }

        private suspend fun populateInitialData(
            taskListDao: TaskListDao,
            taskDao: TaskDao,
            settingsDao: SettingsDao,
            habitDao: HabitDao,
            statsDao: StatsDao
        ) {
            settingsDao.insertOrUpdate(AppSettings())
            statsDao.insertOrUpdate(
                UserProductivityStats(
                    id = 1,
                    totalXp = 150,
                    tasksCompletedCount = 1,
                    habitsCompletedCount = 1,
                    focusMinutesTotal = 25,
                    focusSessionsCompleted = 1,
                    longestStreakDays = 2,
                    unlockedBadgeIds = "FIRST_STEP,HABIT_STARTER",
                    lastActiveDate = HabitWithLogs.getTodayDateString()
                )
            )

            // Populate starter habits
            val habit1Id = habitDao.insertHabit(
                Habit(
                    title = "Morning Sunlight & Hydration",
                    description = "Drink 500ml water and get 10 minutes of direct sunlight",
                    frequency = HabitFrequency.DAILY,
                    colorHex = 0xFFF59E0B, // Amber
                    iconName = "wb_sunny",
                    orderIndex = 0
                )
            )
            // Log today's completion for the first habit
            habitDao.insertLog(
                HabitLog(
                    habitId = habit1Id,
                    dateString = HabitWithLogs.getTodayDateString(),
                    completedAt = System.currentTimeMillis()
                )
            )

            habitDao.insertHabit(
                Habit(
                    title = "25-min Deep Focus Session",
                    description = "Eliminate phone distractions and focus on your top task",
                    frequency = HabitFrequency.DAILY,
                    colorHex = 0xFF6366F1, // Indigo
                    iconName = "hourglass_top",
                    orderIndex = 1
                )
            )

            habitDao.insertHabit(
                Habit(
                    title = "Daily Evening Review",
                    description = "Review Eisenhower Matrix and plan 3 priorities for tomorrow",
                    frequency = HabitFrequency.DAILY,
                    colorHex = 0xFF10B981, // Emerald
                    iconName = "fact_check",
                    orderIndex = 2
                )
            )

            val defaultListId = taskListDao.insertList(
                TaskList(name = "My Tasks", orderIndex = 0)
            )
            taskListDao.insertList(
                TaskList(name = "Personal", orderIndex = 1)
            )
            taskListDao.insertList(
                TaskList(name = "Work", orderIndex = 2)
            )

            val now = System.currentTimeMillis()

            // Task 1: Due today to populate daily progress!
            val task1Id = taskDao.insertTask(
                Task(
                    listId = defaultListId,
                    title = "Welcome to Tasks!",
                    details = "Check off tasks to see your daily progress bar and motivational feedback!",
                    dueDateMillis = now, // Due Today!
                    priority = Priority.HIGH,
                    isStarred = true,
                    recurrenceFrequency = RecurrenceFrequency.DAILY,
                    orderIndex = 0
                )
            )

            taskDao.insertSubtask(
                Subtask(
                    taskId = task1Id,
                    title = "Tap the arrow to expand/collapse subtasks",
                    isCompleted = false,
                    orderIndex = 0
                )
            )
            taskDao.insertSubtask(
                Subtask(
                    taskId = task1Id,
                    title = "Check off subtasks inline from the main list",
                    isCompleted = false,
                    orderIndex = 1
                )
            )

            // Task 2: Another active task due today
            taskDao.insertTask(
                Task(
                    listId = defaultListId,
                    title = "Explore the 10 Dark Themes",
                    details = "Tap the palette icon in the top bar to try out OLED Midnight, Cyberpunk, Emerald Forest, and more, or create your own custom theme!",
                    dueDateMillis = now,
                    priority = Priority.MEDIUM,
                    isStarred = true,
                    orderIndex = 1
                )
            )

            // Task 3: A completed task to demonstrate the new Completed list tab!
            taskDao.insertTask(
                Task(
                    listId = defaultListId,
                    title = "Install and set up Tasks app",
                    details = "Completed task appearing in your new dedicated Completed list tab.",
                    dueDateMillis = now - 3600000L,
                    isCompleted = true,
                    completedAt = now - 1800000L,
                    priority = Priority.LOW,
                    orderIndex = 2
                )
            )
        }
    }
}
