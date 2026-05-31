package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskWithSubtasks

@Composable
fun AnalyticsScreen(
    tasks: List<TaskWithSubtasks>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Calculate aggregated metrics
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.task.completed || it.task.status == "done" }
    val completionRate = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks) else 0f

    // Priority breakdown
    val highCount = tasks.count { it.task.priority.lowercase() == "high" }
    val mediumCount = tasks.count { it.task.priority.lowercase() == "medium" }
    val lowCount = tasks.count { it.task.priority.lowercase() == "low" }
    val maxPriorityCount = maxOf(1, maxOf(highCount, maxOf(mediumCount, lowCount)))

    // Category breakdown
    val workCount = tasks.count { it.task.category.lowercase() == "work" }
    val personalCount = tasks.count { it.task.category.lowercase() == "personal" }
    val shoppingCount = tasks.count { it.task.category.lowercase() == "shopping" }
    val otherCount = tasks.count { it.task.category.lowercase() == "other" }
    val totalCategoryPoints = workCount + personalCount + shoppingCount + otherCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("analytics_viewport"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (totalTasks == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.White.copy(alpha = 0.1f)
                    )
                    Text(
                        text = "Analytics data unavailable",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    )
                    Text(
                        text = "Populate tasks to construct performance metrics.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.2f),
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        } else {
            // Widget 1: Circular Progress Gauge Card
            CompletionWidget(
                completionRate = completionRate,
                completedCount = completedTasks,
                totalCount = totalTasks
            )

            // Widget 2: Priority Bar Graph Widget Card
            PriorityWidget(
                high = highCount,
                medium = mediumCount,
                low = lowCount,
                maxVal = maxPriorityCount
            )

            // Widget 3: Category Donut Chart Widget Card
            CategoryWidget(
                work = workCount,
                personal = personalCount,
                shopping = shoppingCount,
                other = otherCount,
                total = totalCategoryPoints
            )

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CompletionWidget(
    completionRate: Float,
    completedCount: Int,
    totalCount: Int
) {
    val complianceAnim by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(durationMillis = 800),
        label = "circularRateAnimation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Sprint Completion Metrics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Drawing the Canvas Circular gauge
                val ringCol = Color(0xFF6366F1)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Track circle background
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 14.dp.toPx())
                    )

                    // Active progress ring arc
                    val sweepAngle = complianceAnim * 360f
                    drawArc(
                        color = ringCol,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(complianceAnim * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 32.sp
                        )
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Text(
                text = "$completedCount out of $totalCount flow cards resolved",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

@Composable
fun PriorityWidget(
    high: Int,
    medium: Int,
    low: Int,
    maxVal: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Prioritization Load Distribution",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            // Dynamic heights for the vertical bars
            val animHighHeight by animateFloatAsState(targetValue = high.toFloat() / maxVal, animationSpec = tween(700), label = "h")
            val animMedHeight by animateFloatAsState(targetValue = medium.toFloat() / maxVal, animationSpec = tween(700), label = "m")
            val animLowHeight by animateFloatAsState(targetValue = low.toFloat() / maxVal, animationSpec = tween(700), label = "l")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val paddingBottom = 20.dp.toPx()
                    val graphHeight = h - paddingBottom
                    val barWidth = 40.dp.toPx()
                    val itemSpacing = w / 4f

                    // Color tokens
                    val colHigh = Color(0xFFEF4444)
                    val colMed = Color(0xFFF59E0B)
                    val colLow = Color(0xFF10B981)

                    // Draw High Priority Bar
                    val highBarH = graphHeight * animHighHeight
                    drawRoundRect(
                        color = colHigh,
                        topLeft = Offset(itemSpacing - barWidth / 2, graphHeight - highBarH),
                        size = Size(barWidth, highBarH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw Medium Priority Bar
                    val medBarH = graphHeight * animMedHeight
                    drawRoundRect(
                        color = colMed,
                        topLeft = Offset(itemSpacing * 2f - barWidth / 2, graphHeight - medBarH),
                        size = Size(barWidth, medBarH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )

                    // Draw Low Priority Bar
                    val lowBarH = graphHeight * animLowHeight
                    drawRoundRect(
                        color = colLow,
                        topLeft = Offset(itemSpacing * 3f - barWidth / 2, graphHeight - lowBarH),
                        size = Size(barWidth, lowBarH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }

                // Standard Labels overlay placed underneath
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("High ($high)", color = Color(0xFFEF4444), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Medium ($medium)", color = Color(0xFFF59E0B), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Low ($low)", color = Color(0xFF10B981), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp))
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryWidget(
    work: Int,
    personal: Int,
    shopping: Int,
    other: Int,
    total: Int
) {
    val totalCountSafe = if (total == 0) 1 else total

    val sweepWork by animateFloatAsState(targetValue = (work.toFloat() / totalCountSafe) * 360f, animationSpec = tween(600), label = "w")
    val sweepPersonal by animateFloatAsState(targetValue = (personal.toFloat() / totalCountSafe) * 360f, animationSpec = tween(600), label = "p")
    val sweepShopping by animateFloatAsState(targetValue = (shopping.toFloat() / totalCountSafe) * 360f, animationSpec = tween(600), label = "s")
    val sweepOther by animateFloatAsState(targetValue = (other.toFloat() / totalCountSafe) * 360f, animationSpec = tween(600), label = "o")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Category Demand Allocation",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Segmented Donut Chart Canvas (Left)
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val thickness = 12.dp.toPx()
                        val ringSize = size.minDimension - thickness

                        var currentStartAngle = -90f

                        // Draw Work arc
                        if (sweepWork > 0.01f) {
                            drawArc(
                                color = Color(0xFF6366F1),
                                startAngle = currentStartAngle,
                                sweepAngle = sweepWork,
                                useCenter = false,
                                style = Stroke(width = thickness)
                            )
                            currentStartAngle += sweepWork
                        }

                        // Draw Personal arc
                        if (sweepPersonal > 0.01f) {
                            drawArc(
                                color = Color(0xFFA78BFA),
                                startAngle = currentStartAngle,
                                sweepAngle = sweepPersonal,
                                useCenter = false,
                                style = Stroke(width = thickness)
                            )
                            currentStartAngle += sweepPersonal
                        }

                        // Draw Shopping arc
                        if (sweepShopping > 0.01f) {
                            drawArc(
                                color = Color(0xFFF59E0B),
                                startAngle = currentStartAngle,
                                sweepAngle = sweepShopping,
                                useCenter = false,
                                style = Stroke(width = thickness)
                            )
                            currentStartAngle += sweepShopping
                        }

                        // Draw Other arc
                        if (sweepOther > 0.01f) {
                            drawArc(
                                color = Color(0xFF10B981),
                                startAngle = currentStartAngle,
                                sweepAngle = sweepOther,
                                useCenter = false,
                                style = Stroke(width = thickness)
                            )
                        }
                    }

                    // Donut center cut info
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalCountSafe.toString(),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Tasks",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Legend List View (Right)
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    LegendItem(name = "Work", count = work, total = totalCountSafe, color = Color(0xFF6366F1))
                    LegendItem(name = "Personal", count = personal, total = totalCountSafe, color = Color(0xFFA78BFA))
                    LegendItem(name = "Shopping", count = shopping, total = totalCountSafe, color = Color(0xFFF59E0B))
                    LegendItem(name = "Other", count = other, total = totalCountSafe, color = Color(0xFF10B981))
                }
            }
        }
    }
}

@Composable
fun LegendItem(name: String, count: Int, total: Int, color: Color) {
    val percent = (count.toFloat() / total * 100f).toInt()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(
            text = "$name: $count ($percent%)",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        )
    }
}
