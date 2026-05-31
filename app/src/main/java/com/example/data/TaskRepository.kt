package com.example.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.receiver.ReminderReceiver
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val context: Context, private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskWithSubtasks>> = taskDao.getTasksWithSubtasks()

    suspend fun insertTask(task: Task, subtasks: List<Subtask>): Long {
        val taskId = taskDao.insertTask(task).toInt()
        val mappedSubtasks = subtasks.map { it.copy(taskId = taskId) }
        taskDao.insertSubtasks(mappedSubtasks)
        if (task.dueDate != null) {
            scheduleAlarm(taskId, task.title, task.priority, task.dueDate)
        }
        return taskId.toLong()
    }

    suspend fun updateTask(task: Task, subtasks: List<Subtask>) {
        taskDao.updateTask(task)
        taskDao.deleteSubtasksByTaskId(task.id)
        val mappedSubtasks = subtasks.map { it.copy(taskId = task.id) }
        taskDao.insertSubtasks(mappedSubtasks)

        if (task.dueDate != null) {
            scheduleAlarm(task.id, task.title, task.priority, task.dueDate)
        } else {
            cancelAlarm(task.id)
        }
    }

    suspend fun updateTaskOnly(task: Task) {
        taskDao.updateTask(task)
        if (task.completed || task.status == "done") {
            cancelAlarm(task.id)
        } else if (task.dueDate != null) {
            scheduleAlarm(task.id, task.title, task.priority, task.dueDate)
        }
    }

    suspend fun updateSubtask(subtask: Subtask) {
        taskDao.updateSubtask(subtask)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
        cancelAlarm(task.id)
    }

    private fun scheduleAlarm(taskId: Int, title: String, priority: String, dueDateMs: Long) {
        if (dueDateMs <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("TASK_ID", taskId)
            putExtra("TASK_TITLE", title)
            putExtra("TASK_PRIORITY", priority)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDateMs, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, dueDateMs, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dueDateMs, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("TaskRepository", "Failed to schedule exact alarm, using standard", e)
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, dueDateMs, pendingIntent)
            } catch (ex: Exception) {
                Log.e("TaskRepository", "Failed to schedule standard alarm", ex)
            }
        }
    }

    private fun cancelAlarm(taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
