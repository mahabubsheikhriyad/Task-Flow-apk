package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Flow alarms and notifications registered successfully!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enable notification permissions to receive task due date reminders.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Bootstrap database, repository, & viewModel manually to avoid complex DI overhead
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(applicationContext, database.taskDao)
        val viewModel: TaskViewModel by viewModels {
            TaskViewModel.Factory(repository)
        }

        checkAndRequestNotifications()

        setContent {
            MyApplicationTheme {
                val tasksState by viewModel.tasksWithSubtasks.collectAsState()

                var selectedTab by remember { mutableStateOf(0) }
                var showAddDialog by remember { mutableStateOf(false) }
                var taskToEdit by remember { mutableStateOf<TaskWithSubtasks?>(null) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = { showAddDialog = true },
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .padding(bottom = 12.dp)
                                .testTag("fab_add_task"),
                            icon = { Icon(Icons.Default.Add, contentDescription = "Add Task") },
                            text = { Text("New Flow Card", fontWeight = FontWeight.Bold) }
                        )
                    },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF08090D),
                                        Color(0xFF0A0B10),
                                        Color(0xFF0C0D14)
                                    )
                                )
                            )
                            .padding(innerPadding)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Top App Bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    FlowTaskLogo(modifier = Modifier.size(52.dp))
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "FlowTask",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = (-0.5).sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "LOCAL-FIRST WORKSPACE",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF94A3B8), // Slate400
                                                fontWeight = FontWeight.SemiBold,
                                                letterSpacing = 1.sp
                                            )
                                        )
                                    }
                                }

                                // Glowing premium Workspace badge
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF6366F1).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFF818CF8).copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(Color(0xFF10B981), CircleShape)
                                    )
                                }
                            }

                            // Custom Premium Pill-shaped Switcher
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .background(Color(0x0DFFFFFF), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(16.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                val tabsList = listOf(
                                    Triple(0, "List Space", Icons.Default.FormatListBulleted),
                                    Triple(1, "Kanban Board", Icons.Default.Dashboard),
                                    Triple(2, "Metrics Hub", Icons.Default.Analytics)
                                )
                                tabsList.forEach { (index, title, icon) ->
                                    val isSelected = selectedTab == index
                                    val bgActive = if (isSelected) Color(0x1AFFFFFF) else Color.Transparent
                                    val textColor = if (isSelected) Color.White else Color(0xFF94A3B8)
                                    val iconColor = if (isSelected) Color(0xFF6366F1) else Color(0xFF64748B)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(bgActive, RoundedCornerShape(12.dp))
                                            .clickable { selectedTab = index }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = iconColor,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = title,
                                                color = textColor,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Content Swapper according to selection tab index
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                when (selectedTab) {
                                    0 -> TodoListScreen(
                                        tasks = tasksState,
                                        onToggleTask = { task -> viewModel.toggleTaskCompletion(task) },
                                        onToggleSubtask = { sub -> viewModel.toggleSubtaskCompletion(sub) },
                                        onEditTask = { item -> taskToEdit = item },
                                        onDeleteTask = { task -> viewModel.deleteTask(task) }
                                    )
                                    1 -> KanbanScreen(
                                        tasks = tasksState,
                                        onUpdateStatus = { task, status -> viewModel.updateTaskStatus(task, status) },
                                        onEditTask = { item -> taskToEdit = item },
                                        onDeleteTask = { task -> viewModel.deleteTask(task) }
                                    )
                                    2 -> AnalyticsScreen(
                                        tasks = tasksState
                                    )
                                }
                            }
                        }

                        // Save dialog component launcher
                        if (showAddDialog) {
                            AddEditTaskDialog(
                                onDismiss = { showAddDialog = false },
                                onSave = { title, desc, prio, cat, due, subs ->
                                    viewModel.addTask(title, desc, prio, cat, due, subs)
                                },
                                onUpdate = { _, _, _, _, _, _, _, _, _ -> }
                            )
                        }

                        // Edit dialog component launcher
                        if (taskToEdit != null) {
                            AddEditTaskDialog(
                                taskToEdit = taskToEdit,
                                onDismiss = { taskToEdit = null },
                                onSave = { _, _, _, _, _, _ -> },
                                onUpdate = { taskId, title, desc, status, prio, cat, due, subs, completed ->
                                    viewModel.updateTask(taskId, title, desc, status, prio, cat, due, subs, completed)
                                    taskToEdit = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun FlowTaskLogo(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing central circle background
        Box(
            modifier = Modifier
                .fillMaxSize(0.85f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1E3A8A),
                            Color(0xFF0F172A)
                        )
                    ),
                    shape = CircleShape
                )
        )

        // Custom drawn trending arrow
        Canvas(modifier = Modifier.fillMaxSize(0.6f)) {
            val w = size.width
            val h = size.height

            val path = Path().apply {
                moveTo(w * 0.15f, h * 0.82f)
                cubicTo(
                    w * 0.35f, h * 0.46f,
                    w * 0.50f, h * 0.85f,
                    w * 0.82f, h * 0.22f
                )
            }

            // Draw outer shadow glow
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x3000D2FF),
                        Color(0x3010B981)
                    )
                ),
                style = Stroke(
                    width = 8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw bright core arrow stroke
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF00D2FF),
                        Color(0xFF10B981)
                    )
                ),
                style = Stroke(
                    width = 3.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Arrow head
            val arrowHead = Path().apply {
                moveTo(w * 0.82f, h * 0.22f)
                lineTo(w * 0.58f, h * 0.25f)
                lineTo(w * 0.79f, h * 0.46f)
                close()
            }
            drawPath(
                path = arrowHead,
                color = Color(0xFF10B981)
            )
        }
    }
}
