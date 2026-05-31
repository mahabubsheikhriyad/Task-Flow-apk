package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Subtask
import com.example.data.Task
import com.example.data.TaskWithSubtasks
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodoListScreen(
    tasks: List<TaskWithSubtasks>,
    onToggleTask: (Task) -> Unit,
    onToggleSubtask: (Subtask) -> Unit,
    onEditTask: (TaskWithSubtasks) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var priorityFilter by remember { mutableStateOf("all") }
    var categoryFilter by remember { mutableStateOf("all") }

    val filteredTasks = remember(tasks, searchQuery, priorityFilter, categoryFilter) {
        tasks.filter { item ->
            val matchesSearch = item.task.title.contains(searchQuery, ignoreCase = true) ||
                    item.task.description.contains(searchQuery, ignoreCase = true)
            val matchesPriority = priorityFilter == "all" || item.task.priority == priorityFilter
            val matchesCategory = categoryFilter == "all" || item.task.category == categoryFilter
            matchesSearch && matchesPriority && matchesCategory
        }
    }

    // Keep track of which tasks have subtasks list expanded
    val expandedTasks = remember { mutableStateMapOf<Int, Boolean>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search and Filter Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter flow streams...", color = Color.White.copy(alpha = 0.4f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White.copy(alpha = 0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF6366F1),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                cursorColor = Color(0xFF6366F1),
                focusedContainerColor = Color(0x09FFFFFF),
                unfocusedContainerColor = Color(0x05FFFFFF)
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Horizontal filter selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Priority selector dropdown
            var showPriorityMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { showPriorityMenu = true },
                    modifier = Modifier.fillMaxWidth().testTag("priority_filter_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Text(
                        text = if (priorityFilter == "all") "All Priorities" else "Priority: " + priorityFilter.replaceFirstChar { it.uppercase() },
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                DropdownMenu(
                    expanded = showPriorityMenu,
                    onDismissRequest = { showPriorityMenu = false },
                    modifier = Modifier.background(Color(0xFF111218))
                ) {
                    DropdownMenuItem(
                        text = { Text("All Priorities", color = Color.White) },
                        onClick = { priorityFilter = "all"; showPriorityMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("🔴 High", color = Color(0xFFEF4444)) },
                        onClick = { priorityFilter = "high"; showPriorityMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("🟡 Medium", color = Color(0xFFF59E0B)) },
                        onClick = { priorityFilter = "medium"; showPriorityMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("🟢 Low", color = Color(0xFF10B981)) },
                        onClick = { priorityFilter = "low"; showPriorityMenu = false }
                    )
                }
            }

            // Category selector dropdown
            var showCategoryMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                Button(
                    onClick = { showCategoryMenu = true },
                    modifier = Modifier.fillMaxWidth().testTag("category_filter_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Text(
                        text = if (categoryFilter == "all") "All Categories" else "Cat: " + categoryFilter.replaceFirstChar { it.uppercase() },
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false },
                    modifier = Modifier.background(Color(0xFF111218))
                ) {
                    DropdownMenuItem(
                        text = { Text("All Categories", color = Color.White) },
                        onClick = { categoryFilter = "all"; showCategoryMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Work", color = Color(0xFF6366F1)) },
                        onClick = { categoryFilter = "work"; showCategoryMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Personal", color = Color(0xFFA78BFA)) },
                        onClick = { categoryFilter = "personal"; showCategoryMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Shopping", color = Color(0xFFF59E0B)) },
                        onClick = { categoryFilter = "shopping"; showCategoryMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Other", color = Color(0xFF10B981)) },
                        onClick = { categoryFilter = "other"; showCategoryMenu = false }
                    )
                }
            }
        }

        // List View
        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInbox,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.15f)
                    )
                    Text(
                        text = "No active task streams found",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    )
                    Text(
                        text = "Schedule a deadline or create a new flow card to start.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.25f)
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTasks, key = { it.task.id }) { taskWithSub ->
                    TaskCard(
                        taskWithSub = taskWithSub,
                        isExpanded = expandedTasks[taskWithSub.task.id] ?: false,
                        onToggleExpand = { expandedTasks[taskWithSub.task.id] = !(expandedTasks[taskWithSub.task.id] ?: false) },
                        onToggleTask = onToggleTask,
                        onToggleSubtask = onToggleSubtask,
                        onEdit = onEditTask,
                        onDelete = onDeleteTask
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    taskWithSub: TaskWithSubtasks,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleTask: (Task) -> Unit,
    onToggleSubtask: (Subtask) -> Unit,
    onEdit: (TaskWithSubtasks) -> Unit,
    onDelete: (Task) -> Unit
) {
    val task = taskWithSub.task
    val subtasks = taskWithSub.subtasks
    val completedSubtaskCount = subtasks.filter { it.completed }.size
    val totalSubtasks = subtasks.size

    val checkboxTint by animateColorAsState(
        targetValue = if (task.completed) Color(0xFF10B981) else Color.White.copy(alpha = 0.3f)
    )

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val hasGlow = task.priority.lowercase() == "high"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        if (hasGlow) {
            // High priority glow backing shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 2.dp, vertical = 2.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(
                                Color(0x1AEF4444),
                                Color(0x0AF59E0B)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (hasGlow) 1.5.dp else 0.dp)
                .testTag("task_id_${task.id}"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0x0EFFFFFF) // Glassmorphic translucent bg (white/5)
            ),
            border = BorderStroke(
                1.dp,
                if (hasGlow) Color(0xFFEF4444).copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Main row (checkbox, title, status dots, toggle, edit, delete buttons)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Interactive Status Checkbox
                    IconButton(
                        onClick = { onToggleTask(task) },
                        modifier = Modifier.testTag("toggle_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = if (task.completed) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
                            contentDescription = "Toggle Complete",
                            tint = checkboxTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Title and deadline
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onToggleExpand() }
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (task.completed) Color.White.copy(alpha = 0.35f) else Color.White,
                                textDecoration = if (task.completed) TextDecoration.LineThrough else null
                            )
                        )
                        
                        // Display deadline alarm if set
                        if (task.dueDate != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = Color(0xFF6366F1).copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Due on " + dateFormatter.format(Date(task.dueDate)),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = Color(0xFF818CF8)
                                    )
                                )
                            }
                        }
                    }

                    // Edit Button
                    IconButton(
                        onClick = { onEdit(taskWithSub) },
                        modifier = Modifier.testTag("edit_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Task",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Delete Button
                    IconButton(
                        onClick = { onDelete(task) },
                        modifier = Modifier.testTag("delete_task_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Task",
                            tint = Color(0xFFEF4444).copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Metas and expanded information (Subtask metrics progress bar, category, priority pill)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 48.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority & Category Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Priority tag
                        val (prioColor, prioBg) = when (task.priority.lowercase()) {
                            "high" -> Pair(Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.15f))
                            "medium" -> Pair(Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.15f))
                            else -> Pair(Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.15f))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(prioBg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = task.priority.uppercase(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = prioColor
                                )
                            )
                        }

                        // Category tag
                        val catColor = when (task.category.lowercase()) {
                            "work" -> Color(0xFF6366F1)
                            "personal" -> Color(0xFFA78BFA)
                            "shopping" -> Color(0xFFF59E0B)
                            else -> Color(0xFF10B981)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(catColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = task.category.uppercase(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = catColor
                                )
                            )
                        }
                    }

                    // Subtask checklist progress tag
                    if (totalSubtasks > 0) {
                        Text(
                            text = "$completedSubtaskCount/$totalSubtasks checklist steps completed",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.clickable { onToggleExpand() }
                        )
                    } else {
                        // Toggle description expansion action hint
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = Color(0xFF818CF8).copy(alpha = 0.7f),
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.clickable { onToggleExpand() }
                        )
                    }
                }

            // Expanding Drawer Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 16.dp, end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Description Block
                    if (task.description.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.03f))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Task Objectives:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.4f),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        lineHeight = 18.sp
                                    )
                                )
                            }
                        }
                    }

                    // Checklist Column
                    if (subtasks.isNotEmpty()) {
                        Divider(color = Color.White.copy(alpha = 0.05f))
                        Text(
                            text = "Checklist Tasks:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.4f),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        subtasks.forEach { subtask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleSubtask(subtask) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (subtask.completed) Icons.Default.CheckCircleOutline else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (subtask.completed) Color(0xFF10B981) else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = subtask.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (subtask.completed) Color.White.copy(alpha = 0.35f) else Color.White,
                                        textDecoration = if (subtask.completed) TextDecoration.LineThrough else null
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
}
