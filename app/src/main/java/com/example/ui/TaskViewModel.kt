package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val tasksWithSubtasks: StateFlow<List<TaskWithSubtasks>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(title: String, description: String, priority: String, category: String, dueDate: Long?, subtasks: List<String>) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                priority = priority,
                category = category,
                dueDate = dueDate,
                status = "todo",
                completed = false
            )
            val subtaskList = subtasks.filter { it.isNotBlank() }.map { Subtask(taskId = 0, title = it, completed = false) }
            repository.insertTask(task, subtaskList)
        }
    }

    fun updateTask(taskId: Int, title: String, description: String, status: String, priority: String, category: String, dueDate: Long?, subtasks: List<Subtask>, completed: Boolean = false) {
        viewModelScope.launch {
            val resolvedCompleted = completed || status == "done"
            val task = Task(
                id = taskId,
                title = title,
                description = description,
                status = status,
                priority = priority,
                category = category,
                dueDate = dueDate,
                completed = resolvedCompleted
            )
            val subtaskList = subtasks.filter { it.title.isNotBlank() }
            repository.updateTask(task, subtaskList)
        }
    }

    fun updateTaskStatus(task: Task, newStatus: String) {
        viewModelScope.launch {
            val isCompleted = newStatus == "done"
            val updated = task.copy(status = newStatus, completed = isCompleted)
            repository.updateTaskOnly(updated)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val newCompleted = !task.completed
            val newStatus = if (newCompleted) "done" else "todo"
            val updated = task.copy(completed = newCompleted, status = newStatus)
            repository.updateTaskOnly(updated)
        }
    }

    fun toggleSubtaskCompletion(subtask: Subtask) {
        viewModelScope.launch {
            val updated = subtask.copy(completed = !subtask.completed)
            repository.updateSubtask(updated)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
