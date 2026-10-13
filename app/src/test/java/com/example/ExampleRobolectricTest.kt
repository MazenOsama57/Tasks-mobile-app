package com.example

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.Converters
import com.example.data.model.Priority
import com.example.data.model.RecurrenceFrequency
import com.example.data.model.Task
import com.example.ui.theme.ThemePresets
import com.example.ui.util.CoachTone
import com.example.ui.util.MotivationalCoach
import com.example.ui.util.TaskDateUtils
import com.example.widget.TasksWidgetProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Tasks", appName)
  }

  @Test
  fun `priority enum converters test`() {
    val converters = Converters()
    assertEquals("HIGH", converters.fromPriority(Priority.HIGH))
    assertEquals(Priority.HIGH, converters.toPriority("HIGH"))
    assertEquals(Priority.NONE, converters.toPriority("UNKNOWN_VALUE"))
  }

  @Test
  fun `theme presets count test`() {
    val presets = ThemePresets.AllPresets
    val darkPresets = presets.filter { it.isDark }
    assertTrue(darkPresets.size >= 10)
  }

  @Test
  fun `motivational coach tone evaluation test`() {
    val feedbackSuccess = MotivationalCoach.evaluateDay(completedCount = 4, totalCount = 4, overdueCount = 0)
    assertEquals(CoachTone.SUCCESS, feedbackSuccess.tone)
    assertEquals(1.0f, feedbackSuccess.progress, 0.01f)

    val feedbackOverdue = MotivationalCoach.evaluateDay(completedCount = 1, totalCount = 3, overdueCount = 2)
    assertEquals(CoachTone.DISAPPOINTED_OVERDUE, feedbackOverdue.tone)

    val feedbackZero = MotivationalCoach.evaluateDay(completedCount = 0, totalCount = 3, overdueCount = 0)
    assertEquals(CoachTone.DISAPPOINTED_ZERO, feedbackZero.tone)
  }

  @Test
  fun `widget provider instantiation and broadcast test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val provider = TasksWidgetProvider()
    assertNotNull(provider)

    // Verify broadcast receiver does not crash on refresh
    val refreshIntent = Intent(context, TasksWidgetProvider::class.java).apply {
      action = TasksWidgetProvider.ACTION_REFRESH
    }
    provider.onReceive(context, refreshIntent)
  }

  @Test
  fun `recurrence days calculation and future detection test`() {
    val futureCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }
    assertTrue(TaskDateUtils.isFutureDate(futureCal.timeInMillis))

    val todayCal = Calendar.getInstance()
    assertTrue(!TaskDateUtils.isFutureDate(todayCal.timeInMillis))

    // Weekly with specific days (e.g. Wednesday and Friday)
    val nextWeekly = TaskDateUtils.calculateNextDueDate(
      currentDueDate = System.currentTimeMillis(),
      frequency = RecurrenceFrequency.WEEKLY,
      interval = 1,
      timesPerPeriod = 1,
      daysOfWeek = "3,5" // Tue=3, Thu=5 or Calendar.TUESDAY, Calendar.THURSDAY
    )
    assertTrue(nextWeekly > System.currentTimeMillis())

    // Monthly with specific days (e.g. 1st and 15th)
    val nextMonthly = TaskDateUtils.calculateNextDueDate(
      currentDueDate = System.currentTimeMillis(),
      frequency = RecurrenceFrequency.MONTHLY,
      interval = 1,
      timesPerPeriod = 1,
      daysOfMonth = "1,15"
    )
    assertTrue(nextMonthly > System.currentTimeMillis())
  }

  @Test
  fun `recurring task completion duplicate prevention test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val repo = com.example.data.repository.TaskRepository(
      database.taskListDao(),
      database.taskDao(),
      database.settingsDao(),
      context
    )

    val listId = database.taskListDao().insertList(com.example.data.model.TaskList(name = "Test List"))
    val task = Task(
      listId = listId,
      title = "Daily Recurring Task",
      recurrenceFrequency = RecurrenceFrequency.DAILY,
      recurrenceInterval = 1,
      dueDateMillis = System.currentTimeMillis()
    )
    val taskId = repo.insertTask(task)

    // Complete task -> exactly 1 new instance spawned
    repo.setTaskCompleted(taskId, true)
    val completedTask = repo.getTaskById(taskId)
    assertNotNull(completedTask)
    assertTrue(completedTask!!.isCompleted)
    val spawnedId = completedTask.spawnedNextTaskId
    assertNotNull(spawnedId)

    val spawnedTask = repo.getTaskById(spawnedId!!)
    assertNotNull(spawnedTask)
    assertEquals(false, spawnedTask!!.isCompleted)

    // Un-complete task -> spawned uncompleted instance is removed
    repo.setTaskCompleted(taskId, false)
    val uncompletedOriginal = repo.getTaskById(taskId)
    assertNotNull(uncompletedOriginal)
    assertEquals(false, uncompletedOriginal!!.isCompleted)
    assertEquals(null, uncompletedOriginal.spawnedNextTaskId)

    val deletedSpawned = repo.getTaskById(spawnedId)
    assertEquals(null, deletedSpawned)

    // Re-complete task -> generates exactly 1 new instance, no duplicates
    repo.setTaskCompleted(taskId, true)
    val reCompletedOriginal = repo.getTaskById(taskId)
    assertNotNull(reCompletedOriginal)
    assertTrue(reCompletedOriginal!!.isCompleted)
    val newSpawnedId = reCompletedOriginal.spawnedNextTaskId
    assertNotNull(newSpawnedId)
    val newSpawnedTask = repo.getTaskById(newSpawnedId!!)
    assertNotNull(newSpawnedTask)
  }

  @Test
  fun `repository in-memory caching layer test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val repo = com.example.data.repository.TaskRepository(
      database.taskListDao(),
      database.taskDao(),
      database.settingsDao(),
      context
    )

    val listId1 = database.taskListDao().insertList(com.example.data.model.TaskList(name = "List 1"))
    val listId2 = database.taskListDao().insertList(com.example.data.model.TaskList(name = "List 2"))

    repo.insertTask(Task(listId = listId1, title = "Task in List 1"))
    repo.insertTask(Task(listId = listId2, title = "Task in List 2"))

    // Let the background collector populate cache
    kotlinx.coroutines.delay(200)

    repo.setCurrentListId(listId1)
    val cachedList1 = repo.getCurrentListData()
    assertTrue(cachedList1.any { it.task.title == "Task in List 1" })
    assertEquals(cachedList1, repo.currentListTasks.value)

    repo.setCurrentListId(listId2)
    val cachedList2 = repo.getCurrentListData()
    assertTrue(cachedList2.any { it.task.title == "Task in List 2" })
    assertEquals(cachedList2, repo.currentListTasks.value)
  }

  @Test
  fun `repository due date filtering system test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val repo = com.example.data.repository.TaskRepository(
      database.taskListDao(),
      database.taskDao(),
      database.settingsDao(),
      context
    )

    val listId = database.taskListDao().insertList(com.example.data.model.TaskList(name = "Due Filter List"))
    val todayCal = java.util.Calendar.getInstance()
    val todayMillis = todayCal.timeInMillis

    val futureCal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 4) }
    val futureMillis = futureCal.timeInMillis

    // 1. Task due today
    repo.insertTask(Task(listId = listId, title = "Task Due Today", dueDateMillis = todayMillis))
    // 2. Recurring task due today
    repo.insertTask(Task(
      listId = listId,
      title = "Recurring Due Today",
      dueDateMillis = todayMillis,
      recurrenceFrequency = RecurrenceFrequency.DAILY
    ))
    // 3. Recurring task due in 4 days (future)
    repo.insertTask(Task(
      listId = listId,
      title = "Future Recurring Task",
      dueDateMillis = futureMillis,
      recurrenceFrequency = RecurrenceFrequency.WEEKLY
    ))

    kotlinx.coroutines.delay(200)

    val allTasks = repo.getCurrentListData().ifEmpty {
      database.taskDao().getTasksForList(listId)
    }

    val todayFiltered = repo.filterTasksByDueDate(
      database.taskDao().getPendingTasksForWidget(System.currentTimeMillis() + 8640000000L),
      com.example.data.repository.TaskRepository.DueDateFilter.TODAY
    )

    // Recurring task due today appears
    assertTrue(todayFiltered.any { it.task.title == "Recurring Due Today" })
    assertTrue(todayFiltered.any { it.task.title == "Task Due Today" })
    // Future recurring task must NOT appear in today's filter!
    assertTrue(todayFiltered.none { it.task.title == "Future Recurring Task" })
  }

  @Test
  fun `image utils downsampling calculation test`() {
    val options = android.graphics.BitmapFactory.Options().apply {
      outWidth = 1024
      outHeight = 1024
    }
    val sampleSize = com.example.ui.util.ImageUtils.calculateInSampleSize(options, 256, 256)
    assertTrue(sampleSize >= 4)
  }

  @Test
  fun `pomodoro timer background persistence and control test`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = AppDatabase.getDatabase(context)
    val repo = com.example.data.repository.TaskRepository(
      database.taskListDao(),
      database.taskDao(),
      database.settingsDao(),
      context,
      database.focusSessionDao()
    )
    val viewModel = com.example.ui.TaskViewModel(repo)

    // Initially timer is not running
    assertTrue(!viewModel.pomodoroState.value.isRunning)
    assertEquals(25 * 60, viewModel.pomodoroState.value.secondsRemaining)

    // Open timer and start
    viewModel.openFocusTimer()
    viewModel.startPomodoroTimer()
    assertTrue(viewModel.pomodoroState.value.isRunning)

    // Exit/close dialog - timer MUST remain running and not reset!
    viewModel.closeFocusTimer()
    assertTrue(viewModel.pomodoroState.value.isRunning)

    // Extend by 5 min
    val remainingBefore = viewModel.pomodoroState.value.secondsRemaining
    viewModel.extendPomodoroTimer(300)
    assertEquals(remainingBefore + 300, viewModel.pomodoroState.value.secondsRemaining)

    // Pause timer
    viewModel.pausePomodoroTimer()
    assertTrue(!viewModel.pomodoroState.value.isRunning)

    // Reopen dialog - timer state is preserved at paused second
    viewModel.openFocusTimer()
    assertEquals(remainingBefore + 300, viewModel.pomodoroState.value.secondsRemaining)
  }

  @Test
  fun `manifest supportsRtl configuration test`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
    val supportsRtl = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SUPPORTS_RTL) != 0
    assertTrue(!supportsRtl)
  }
}
