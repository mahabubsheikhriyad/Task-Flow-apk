package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.data.TaskWithSubtasks

@Composable
fun KanbanScreen(
    tasks: List<TaskWithSubtasks>,
    onUpdateStatus: (Task, String) -> Unit,
    onEditTask: (TaskWithSubtasks) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val swimlanes = listOf(
        Triple("todo", "To Do", Color(0xFF6366F1)),
        Triple("in_progress", "In Progress", Color(0xFFF59E0B)),
        Triple("review", "In Review", Color(0xFFA78BFA)),
        Triple("done", "Completed", Color(0xFF10B981))
    )

    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(scrollState)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.width(4.dp)) // Padding at start

        swimlanes.forEach { (statusCode, columnTitle, columnColor) ->
            val columnTasks = remember(tasks, statusCode) {
                tasks.filter { it.task.status == statusCode }
            }

            KanbanColumn(
                title = columnTitle,
                color = columnColor,
                tasks = columnTasks,
                statusValue = statusCode,
                onMoveLeft = { task ->
                    val prevStatus = when (statusCode) {
                        "in_progress" -> "todo"
                        "review" -> "in_progress"
                        "done" -> "review"
                        else -> "todo"
                    }
                    onUpdateStatus(task, prevStatus)
                },
                onMoveRight = { task ->
                    val nextStatus = when (statusCode) {
                        "todo" -> "in_progress"
                        "in_progress" -> "review"
                        "review" -> "done"
                        else -> "done"
                    }
                    onUpdateStatus(task, nextStatus)
                },
                onUpdateStatusDirectSelect = { task, newStatus ->
                    onUpdateStatus(task, newStatus)
                },
                onEdit = onEditTask,
                onDelete = onDeleteTask
            )
        }

        Spacer(modifier = Modifier.width(4.dp)) // Padding at end
    }
}

@Composable
fun KanbanColumn(
    title: String,
    color: Color,
    tasks: List<TaskWithSubtasks>,
    statusValue: String,
    onMoveLeft: (Task) -> Unit,
    onMoveRight: (Task) -> Unit,
    onUpdateStatusDirectSelect: (Task, String) -> Unit,
    onEdit: (TaskWithSubtasks) -> Unit,
    onDelete: (Task) -> Unit
) {
    Box(
        modifier = Modifier
            .width(290.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(20.dp))
            .padding(14.dp)
            .testTag("kanban_column_${statusValue}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Column Header Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                // Count Pill badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tasks.size.toString(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = color,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Divider matching color theme
            Divider(color = color.copy(alpha = 0.2f), thickness = 2.dp)

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LowPriority,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = Color.White.copy(alpha = 0.08f)
                        )
                        Text(
                            text = "Column is empty",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 60.dp)
                ) {
                    items(tasks, key = { it.task.id }) { taskWithSub ->
                        KanbanCardItem(
                            taskWithSub = taskWithSub,
                            cardBorderColor = color,
                            onMoveLeft = { onMoveLeft(taskWithSub.task) },
                            onMoveRight = { onMoveRight(taskWithSub.task) },
                            onUpdateStatusDirectSelect = { status -> onUpdateStatusDirectSelect(taskWithSub.task, status) },
                            canMoveLeft = statusValue != "todo",
                            canMoveRight = statusValue != "done",
                            onEdit = { onEdit(taskWithSub) },
                            onDelete = { onDelete(taskWithSub.task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanCardItem(
    taskWithSub: TaskWithSubtasks,
    cardBorderColor: Color,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onUpdateStatusDirectSelect: (String) -> Unit,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val task = taskWithSub.task
    val subtasks = taskWithSub.subtasks
    val completedSubtaskCount = subtasks.filter { it.completed }.size
    val totalSubtasks = subtasks.size

    var showMenu by remember { mutableStateOf(false) }

    val isHighPriority = task.priority.lowercase() == "high"

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if (isHighPriority) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 1.dp, vertical = 1.dp)
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(
                                Color(0x1AEF4444),
                                Color(0x0AF59E0B)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isHighPriority) 1.dp else 0.dp)
                .testTag("kanban_task_card_${task.id}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0x0EFFFFFF)
            ),
            border = BorderStroke(
                1.dp,
                if (isHighPriority) Color(0xFFEF4444).copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Row metadata (Priority tag & drop controls)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority bullet tag
                    val prioColor = when (task.priority.lowercase()) {
                        "high" -> Color(0xFFEF4444)
                        "medium" -> Color(0xFFF59E0B)
                        else -> Color(0xFF10B981)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(prioColor)
                        )
                        Text(
                            text = task.priority.uppercase(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = prioColor,
                                fontSize = 8.sp
                            )
                        )
                    }

                    // Settings popover to quick status switch or edit/delete
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp).testTag("kanban_card_menu_${task.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color(0xFF111218))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Task Details", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White) },
                                onClick = { onEdit(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Move: To Do", color = Color.White) },
                                onClick = { onUpdateStatusDirectSelect("todo"); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Move: In Progress", color = Color.White) },
                                onClick = { onUpdateStatusDirectSelect("in_progress"); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Move: In Review", color = Color.White) },
                                onClick = { onUpdateStatusDirectSelect("review"); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Move: Completed", color = Color.White) },
                                onClick = { onUpdateStatusDirectSelect("done"); showMenu = false }
                            )
                            Divider(color = Color.White.copy(alpha = 0.08f))
                            DropdownMenuItem(
                                text = { Text("Delete task", color = Color(0xFFEF4444)) },
                                leadingIcon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color(0xFFEF4444)) },
                                onClick = { onDelete(); showMenu = false }
                            )
                        }
                    }
                }

            // Task title
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Optional description snippet
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.45f)
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Subtask summary indicator
            if (totalSubtasks > 0) {
                LinearProgressIndicator(
                    progress = completedSubtaskCount.toFloat() / totalSubtasks.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = cardBorderColor,
                    trackColor = Color.White.copy(alpha = 0.06f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtasks progress",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, color = Color.White.copy(alpha = 0.35f))
                    )
                    Text(
                        text = "$completedSubtaskCount/$totalSubtasks",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, color = cardBorderColor, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Divider(color = Color.White.copy(alpha = 0.04f))

            // Footer actions: Left relocator arrow, category name tag, Right relocator arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Arrow
                IconButton(
                    onClick = onMoveLeft,
                    enabled = canMoveLeft,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("kanban_left_arrow_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Move Left Column",
                        tint = if (canMoveLeft) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Category mini indicator
                val catIcon = when (task.category.lowercase()) {
                    "work" -> Icons.Default.Work
                    "personal" -> Icons.Default.Star
                    "shopping" -> Icons.Default.ShoppingCart
                    else -> Icons.Default.Category
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = catIcon,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        text = task.category.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = Color.White.copy(alpha = 0.35f))
                    )
                }

                // Right Arrow
                IconButton(
                    onClick = onMoveRight,
                    enabled = canMoveRight,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("kanban_right_arrow_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Move Right Column",
                        tint = if (canMoveRight) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
}
