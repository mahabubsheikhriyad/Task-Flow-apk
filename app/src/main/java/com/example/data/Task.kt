package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val completed: Boolean = false,
    val status: String = "todo", // "todo", "in_progress", "review", "done"
    val priority: String = "medium", // "low", "medium", "high"
    val category: String = "other", // "work", "personal", "shopping", "other"
    val dueDate: Long? = null // Unix timestamp (ms)
)
