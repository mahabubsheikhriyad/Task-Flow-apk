package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Subtask
import com.example.data.TaskWithSubtasks
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    taskToEdit: TaskWithSubtasks? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        priority: String,
        category: String,
        dueDate: Long?,
        subtasks: List<String>
    ) -> Unit,
    onUpdate: (
        taskId: Int,
        title: String,
        description: String,
        status: String,
        priority: String,
        category: String,
        dueDate: Long?,
        subtasks: List<Subtask>,
        completed: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditMode = taskToEdit != null

    var title by remember { mutableStateOf(taskToEdit?.task?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.task?.description ?: "") }
    var priority by remember { mutableStateOf(taskToEdit?.task?.priority ?: "medium") }
    var category by remember { mutableStateOf(taskToEdit?.task?.category ?: "other") }
    var dueDate by remember { mutableStateOf<Long?>(taskToEdit?.task?.dueDate) }
    var status = taskToEdit?.task?.status ?: "todo"
    var completed = taskToEdit?.task?.completed ?: false

    // Maintain subtask list (can grow/shrink)
    var subtaskInputs = remember {
        mutableStateListOf<String>().apply {
            if (isEditMode && taskToEdit != null) {
                addAll(taskToEdit.subtasks.map { it.title })
            } else {
                add("") // Initial empty subtask
            }
        }
    }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F1015),
                            Color(0xFF07080A)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditMode) "Edit Sprint Task" else "Create Task Flow",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dialog_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                // Task Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title", color = Color.White.copy(alpha = 0.6f)) },
                    placeholder = { Text("E.g. Refactor API calls", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = Color(0xFF6366F1)
                    ),
                    singleLine = true
                )

                // Task Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description (Optional)", color = Color.White.copy(alpha = 0.6f)) },
                    placeholder = { Text("Describe outcomes and objectives...", color = Color.White.copy(alpha = 0.3f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_desc_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = Color(0xFF6366F1)
                    ),
                    maxLines = 3
                )

                // Priority Selection Pills
                Text(
                    text = "Flow Priority Level",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("low", "medium", "high").forEach { level ->
                        val isSelected = priority == level
                        val activeColor = when (level) {
                            "high" -> Color(0xFFEF4444)     // Glassy Red
                            "medium" -> Color(0xFFF59E0B)   // Amber Orange
                            else -> Color(0xFF10B981)       // Teal Green
                        }
                        
                        val pillBg = if (isSelected) activeColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.04f)
                        val pillBorder = if (isSelected) activeColor else Color.White.copy(alpha = 0.1f)
                        val textColor = if (isSelected) activeColor else Color.White.copy(alpha = 0.5f)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(pillBg)
                                .clickable { priority = level }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            )
                        }
                    }
                }

                // Category Selection Cards
                Text(
                    text = "Category Bucket",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        Triple("work", Icons.Default.Work, Color(0xFF6366F1)),
                        Triple("personal", Icons.Default.Star, Color(0xFFA78BFA)),
                        Triple("shopping", Icons.Default.ShoppingCart, Color(0xFFF59E0B)),
                        Triple("other", Icons.Default.Category, Color(0xFF10B981))
                    )

                    categories.forEach { (catName, iconVec, specColor) ->
                        val isSelected = category == catName
                        val pillBg = if (isSelected) specColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f)
                        val pillBorder = if (isSelected) specColor else Color.White.copy(alpha = 0.1f)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(pillBg)
                                .clickable { category = catName }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = catName,
                                tint = if (isSelected) specColor else Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = catName.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                // Due Date Deadline (DatePicker Dialog)
                Text(
                    text = "Sprint Deadline & Alarm",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                )

                Surface(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        dueDate?.let { calendar.timeInMillis = it }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    set(Calendar.HOUR_OF_DAY, 9) // Default: 9:00 AM alarm
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                dueDate = selectedCal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Due Date Calendar",
                                tint = Color(0xFF6366F1)
                            )
                            Column {
                                Text(
                                    text = if (dueDate == null) "Set Flow Due Date" else "Due on:",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                )
                                if (dueDate != null) {
                                    Text(
                                        text = dateFormatter.format(Date(dueDate!!)) + " (9:00 AM Alarm)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                        if (dueDate != null) {
                            IconButton(onClick = { dueDate = null }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Clear Deadline",
                                     tint = Color(0xFFEF4444)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                // Subtask Composer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtasks Checklist",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                    TextButton(
                        onClick = { subtaskInputs.add("") },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF6366F1))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Checklist Step", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                }

                subtaskInputs.forEachIndexed { index, subtaskTitle ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SubdirectoryArrowRight,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                        OutlinedTextField(
                            value = subtaskTitle,
                            onValueChange = { subtaskInputs[index] = it },
                            placeholder = { Text("E.g. Code database schema", color = Color.White.copy(alpha = 0.25f)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("subtask_${index}_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6366F1).copy(alpha = 0.6f),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(
                            onClick = {
                                if (subtaskInputs.size > 1) {
                                    subtaskInputs.removeAt(index)
                                } else {
                                    subtaskInputs[0] = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Subtask Input",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions Save / Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_cancel_button"),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                if (isEditMode && taskToEdit != null) {
                                    val finalSubtasks = subtaskInputs.filter { it.isNotBlank() }.map { subTitle ->
                                        // If previously existing subtask had this title, reuse it to retain completion state
                                        val existing = taskToEdit.subtasks.find { it.title == subTitle }
                                        existing ?: Subtask(
                                            taskId = taskToEdit.task.id,
                                            title = subTitle,
                                            completed = false
                                        )
                                    }
                                    onUpdate(
                                        taskToEdit.task.id,
                                        title,
                                        description,
                                        status,
                                        priority,
                                        category,
                                        dueDate,
                                        finalSubtasks,
                                        completed
                                    )
                                } else {
                                    onSave(title, description, priority, category, dueDate, subtaskInputs)
                                }
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("dialog_save_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = title.isNotBlank()
                    ) {
                        Text(
                            text = if (isEditMode) "Save Changes" else "Initialize Flow",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
