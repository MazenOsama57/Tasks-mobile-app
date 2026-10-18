package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.repository.TaskRepository
import com.example.ui.TaskViewModel
import com.example.ui.TaskViewModelFactory
import com.example.ui.TasksScreen
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(
            database.taskListDao(),
            database.taskDao(),
            database.settingsDao(),
            applicationContext,
            database.focusSessionDao(),
            database.habitDao(),
            database.statsDao()
        )
        TaskViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appSettings by viewModel.settings.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                MyApplicationTheme(settings = appSettings) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        TasksScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
