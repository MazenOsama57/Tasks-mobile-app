# Tasks App ProGuard and R8 rules

# Preserve Room database models and DAOs
-keep class com.example.data.model.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep ViewModels and StateFlow entities
-keep class com.example.ui.TaskViewModel { *; }
-keep class com.example.ui.TaskViewModelFactory { *; }

# Keep AppWidget and Receiver components
-keep class com.example.widget.TasksWidgetProvider { *; }
-keep class com.example.widget.TasksWidgetService { *; }
-keep class com.example.reminder.BootReceiver { *; }
-keep class com.example.reminder.DailyReminderReceiver { *; }

# Retain line number attributes for debugging production crash reports
-keepattributes SourceFile,LineNumberTable
