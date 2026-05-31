package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val taskId = intent.getIntExtra("TASK_ID", -1)
        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val taskPriority = intent.getStringExtra("TASK_PRIORITY") ?: "medium"

        if (action == ACTION_COMPLETE_TASK && taskId != -1) {
            // Mark task as complete directly from notification shade!
            CoroutineScope(Dispatchers.IO).launch {
                val database = AppDatabase.getDatabase(context)
                val dao = database.taskDao
                val taskWithSubtasks = dao.getTaskWithSubtasksById(taskId)
                taskWithSubtasks?.let {
                    val updatedTask = it.task.copy(completed = true, status = "done")
                    dao.updateTask(updatedTask)
                }
                
                // Dismiss the notification
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(taskId)
            }
            return
        }

        if (taskId != -1) {
            showNotification(context, taskId, taskTitle, taskPriority)
        }
    }

    private fun showNotification(context: Context, taskId: Int, title: String, priority: String) {
        val channelId = "flowtask_reminders"
        val channelName = "Task Reminders"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent reminders for outstanding tasks"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Tap notification to open app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // "Complete" action button
        val completeIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra("TASK_ID", taskId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId + 100000, // Safe offset for PendingIntent ID uniqueness
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priorityText = when (priority.lowercase()) {
            "high" -> "🔴 High Priority"
            "low" -> "🟢 Low Priority"
            else -> "🟡 Medium Priority"
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System standard fallback drawable
            .setContentTitle("Task Due: $title")
            .setContentText("Priority: $priorityText. Tap to view details.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Complete Task",
                completePendingIntent
            )

        try {
            notificationManager.notify(taskId, builder.build())
        } catch (e: SecurityException) {
            Log.e("ReminderReceiver", "Cannot post notification: permission not granted", e)
        }
    }

    companion object {
        const val ACTION_COMPLETE_TASK = "com.example.receiver.ACTION_COMPLETE_TASK"
    }
}
