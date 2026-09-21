package com.example.alarm.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.data.entity.WaterLogEntity
import java.time.LocalDate
import java.util.Locale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalDensity

@Composable
fun WaterBarChart(
    data: List<Triple<String, Int, Int>>,
    selectedDate: LocalDate,
    chartAnchorDate: LocalDate,
    onDateSelected: (Int) -> Unit, // Index of selected item
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val density = LocalDensity.current

    // Calculate scaling
    val maxIntake = data.maxOf { it.second }.toFloat()
    val avgGoal = data.map { it.third }.average().toFloat()
    val yMax = maxOf(avgGoal * 1.3f, maxIntake).coerceAtLeast(1000f)

    val scrollState = rememberScrollState()
    
    // Derived selected index from selectedDate & chartAnchorDate
    val selectedIndex = remember(selectedDate, chartAnchorDate, data) {
        if (data.size == 7 || data.size == 30) {
            val diff = java.time.temporal.ChronoUnit.DAYS.between(selectedDate, chartAnchorDate).toInt()
            val index = (data.size - 1) - diff
            index.coerceIn(-1, data.size - 1)
        } else if (data.size > 0 && data[0].first.startsWith("W")) {
            // Quarter view aggregation
            val diff = java.time.temporal.ChronoUnit.DAYS.between(selectedDate, chartAnchorDate).toInt()
            val index = (data.size - 1) - (diff / 7)
            index.coerceIn(-1, data.size - 1)
        } else {
            -1
        }
    }
    
    // Tooltip state
    var tooltipIndex by remember { mutableIntStateOf(-1) }
    
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val chartHeight = maxHeight - 40.dp
        
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(chartHeight)
            ) {
                // Background Grid & Goal Line
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val goalY = size.height * (1f - (avgGoal / yMax))
                    
                    // Baseline
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    // Goal Line (Dashed)
                    drawLine(
                        color = Color(0xFF2196F3).copy(alpha = 0.5f),
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
                
                // Goal Label - Moved to left and top of line to avoid overlap
                Text(
                    text = "Goal ${"%.1f".format(avgGoal / 1000f)}L",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = with(density) { (chartHeight.toPx() * (1f - (avgGoal / yMax))).toDp() - 18.dp }.coerceAtLeast(0.dp))
                        .padding(start = 8.dp)
                )

                // The Bars
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(scrollState)
                        .pointerInput(data) {
                            detectTapGestures { offset ->
                                val barWidthPx = with(density) { 40.dp.toPx() }
                                val spacingPx = with(density) { 8.dp.toPx() }
                                val totalBarWidthPx: Float = if (data.size <= 7) size.width / data.size.toFloat() else barWidthPx + spacingPx
                                
                                val index = (offset.x / totalBarWidthPx).toInt().coerceIn(0, data.size - 1)
                                tooltipIndex = index
                                onDateSelected(index)
                            }
                        },
                    horizontalArrangement = if (data.size <= 7) Arrangement.SpaceEvenly else Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    val barWidth = if (data.size <= 7) (this@BoxWithConstraints.maxWidth / 7) - 12.dp else 40.dp
                    
                    data.forEachIndexed { index, (label, amount, goal) ->
                        BarItem(
                            amount = amount,
                            goal = goal,
                            yMax = yMax,
                            label = label,
                            isSelected = selectedIndex == index,
                            modifier = Modifier
                                .width(barWidth)
                                .fillMaxHeight()
                        )
                    }
                }
            }
            
            // X-Axis Labels
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = if (data.size <= 7) Arrangement.SpaceEvenly else Arrangement.spacedBy(8.dp)
            ) {
                val barWidth = if (data.size <= 7) (this@BoxWithConstraints.maxWidth / 7) - 12.dp else 40.dp
                data.forEachIndexed { index, (label, _, _) ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(barWidth),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        // Tooltip
        if (tooltipIndex != -1) {
            val item = data[tooltipIndex]
            TooltipPopup(
                label = item.first,
                amount = item.second,
                goal = item.third,
                onDismiss = { tooltipIndex = -1 },
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    LaunchedEffect(data, selectedIndex) {
        if (data.size > 7) {
            // Scroll to end (which is our selectedDate)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }
}

@Composable
fun BarItem(
    amount: Int,
    goal: Int,
    yMax: Float,
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = (amount.toFloat() / yMax).coerceIn(0f, 1f)
    
    // Minimum height for non-zero values (approx 4dp)
    val animatedProgress by animateFloatAsState(
        targetValue = if (amount > 0) progress.coerceAtLeast(0.02f) else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "bar_height"
    )

    Column(
        modifier = modifier.semantics {
            contentDescription = "$label: $amount ml of $goal ml goal"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight(animatedProgress)
                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else if (amount >= goal) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                )
        )
        if (amount == 0) {
            // Faint baseline for 0ml
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun TooltipPopup(
    label: String,
    amount: Int,
    goal: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = (amount.toFloat() / goal.toFloat() * 100).toInt()
    val displayAmount = if (amount >= 1000) "%.2f L".format(amount / 1000f) else "$amount ml"
    
    Surface(
        modifier = modifier
            .padding(top = 8.dp)
            .clickable { onDismiss() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
        tonalElevation = 8.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (label.startsWith("W")) "Week $label" else label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = displayAmount,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$percentage% of goal",
                style = MaterialTheme.typography.labelSmall,
                color = if (amount >= goal) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
