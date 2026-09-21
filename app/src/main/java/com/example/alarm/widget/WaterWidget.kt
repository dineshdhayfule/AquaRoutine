package com.example.alarm.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.alarm.data.AppDatabase
import com.example.alarm.data.HydrationUtils
import com.example.alarm.data.UserPreferences
import com.example.alarm.data.repository.WaterRepository
import java.time.LocalDate
import java.time.ZoneId

class WaterWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.waterLogDao()
        val dailyGoalOverrideDao = db.dailyGoalOverrideDao()
        val userPrefs = UserPreferences(context)
        val repository = WaterRepository(dao, dailyGoalOverrideDao, userPrefs)

        // Calculate today's range
        val today = LocalDate.now()
        val start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        
        // Range for streak (last 30 days)
        val startStreak = today.minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        provideContent {
            val totalConsumed by dao.getTotalConsumedForDay(start, end).collectAsState(initial = 0)
            val dailyGoal by repository.getDailyGoalFlow(today).collectAsState(initial = UserPreferences.DEFAULT_GOAL_ML)
            val logs by dao.getLogsInRange(startStreak, end).collectAsState(initial = emptyList())
            
            val currentTotal = totalConsumed ?: 0
            val streak = HydrationUtils.calculateStreak(logs, dailyGoal)
            
            val size = LocalSize.current
            
            WidgetContent(
                currentTotal = currentTotal,
                dailyGoal = dailyGoal,
                streak = streak,
                size = size
            )
        }
    }

    @Composable
    private fun WidgetContent(
        currentTotal: Int,
        dailyGoal: Int,
        streak: Int,
        size: DpSize
    ) {
        val progress = if (dailyGoal > 0) currentTotal.toFloat() / dailyGoal else 0f
        val percentage = (progress * 100).toInt()
        
        val status = when {
            progress >= 1.0f -> "Goal Reached 🎉"
            progress >= 0.8f -> "Almost There"
            progress >= 0.3f -> "On Track"
            else -> "Let's get started"
        }

        val context = LocalContext.current

        GlanceTheme {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .cornerRadius(28.dp)
                    .background(GlanceTheme.colors.surface)
                    .clickable(
                        androidx.glance.appwidget.action.actionStartActivity(
                            android.content.Intent(context, com.example.alarm.MainActivity::class.java).apply {
                                action = "com.example.alarm.VIEW_STATS"
                                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        )
                    )
                    .padding(12.dp)
            ) {
                when {
                    size.width < 120.dp -> SmallLayout(percentage)
                    size.width < 240.dp -> MediumLayout(currentTotal, dailyGoal, percentage, status)
                    else -> LargeLayout(currentTotal, dailyGoal, percentage, status, streak)
                }
            }
        }
    }

    @Composable
    private fun SmallLayout(percentage: Int) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing(percentage, size = 60.dp)
        }
    }

    @Composable
    private fun MediumLayout(currentTotal: Int, dailyGoal: Int, percentage: Int, status: String) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Text(
                text = "\uD83D\uDCA7 Hydration",
                modifier = GlanceModifier.padding(horizontal = 4.dp, vertical = 2.dp),
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
            
            Spacer(modifier = GlanceModifier.size(12.dp))
            
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier.size(80.dp)
                ) {
                    ProgressRing(percentage, size = 80.dp)
                }
                
                Spacer(modifier = GlanceModifier.size(16.dp))
                
                Column {
                    Text(
                        text = java.util.Locale.US.let { 
                            String.format(it, "%.2f / %.2f L", currentTotal / 1000f, dailyGoal / 1000f) 
                        },
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                    Text(
                        text = status,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
            
            QuickAddButtons()
        }
    }

    @Composable
    private fun LargeLayout(currentTotal: Int, dailyGoal: Int, percentage: Int, status: String, streak: Int) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)) {
                Text(
                    text = "\uD83D\uDCA7 Hydration",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.onSurface
                    )
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                if (streak > 0) {
                    Text(
                        text = "\uD83D\uDD25 $streak Day Streak",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = GlanceTheme.colors.secondary
                        )
                    )
                }
            }
            
            Spacer(modifier = GlanceModifier.size(16.dp))
            
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier.size(100.dp)
                ) {
                    ProgressRing(percentage, size = 100.dp)
                }
                
                Spacer(modifier = GlanceModifier.size(24.dp))
                
                Column {
                    Text(
                        text = java.util.Locale.US.let { 
                            String.format(it, "%.2f / %.2f L", currentTotal / 1000f, dailyGoal / 1000f) 
                        },
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurface
                        )
                    )
                    Text(
                        text = status,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                    
                    Spacer(modifier = GlanceModifier.size(8.dp))
                    
                    val remaining = maxOf(0, dailyGoal - currentTotal)
                    Text(
                        text = "Remaining: ${java.util.Locale.US.let { String.format(it, "%.2f L", remaining / 1000f) }}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = GlanceTheme.colors.primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
            
            QuickAddButtons()
        }
    }

    @Composable
    private fun ProgressRing(percentage: Int, size: Dp) {
        val context = LocalContext.current
        val progress = (percentage / 100f).coerceIn(0f, 1f)
        
        val colorProvider = GlanceTheme.colors.primary
        val trackColorProvider = GlanceTheme.colors.primaryContainer
        
        val bitmap = remember(progress, size, colorProvider, trackColorProvider) {
            createProgressBitmap(context, progress, size, colorProvider, trackColorProvider)
        }
        
        Box(
            modifier = GlanceModifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = "Hydration Progress: $percentage%",
                modifier = GlanceModifier.fillMaxSize()
            )
            
            Text(
                text = "$percentage%",
                style = TextStyle(
                    fontSize = (size.value * 0.22).sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
    }

    private fun createProgressBitmap(
        context: Context,
        progress: Float,
        sizeDp: Dp,
        colorProvider: ColorProvider,
        trackColorProvider: ColorProvider
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp.value * density).toInt()
        val bitmap = Bitmap.createBitmap(maxOf(1, sizePx), maxOf(1, sizePx), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Resolve colors
        val colorInt = colorProvider.getColor(context).toArgb()
        val trackColorInt = trackColorProvider.getColor(context).toArgb()
        
        val strokeWidthPx = 6f * density
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            strokeCap = Paint.Cap.ROUND
        }
        
        val margin = strokeWidthPx / 2
        val rect = RectF(margin, margin, sizePx - margin, sizePx - margin)
        
        // Track
        paint.color = trackColorInt
        canvas.drawOval(rect, paint)
        
        // Progress
        if (progress > 0.01f) {
            paint.color = colorInt
            canvas.drawArc(rect, -90f, progress * 360f, false, paint)
        }
        
        return bitmap
    }

    @Composable
    private fun QuickAddButtons() {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AddButton("+100ml", 100)
            Spacer(modifier = GlanceModifier.size(8.dp))
            AddButton("+250ml", 250)
            Spacer(modifier = GlanceModifier.size(8.dp))
            AddButton("+500ml", 500)
        }
    }

    @Composable
    private fun AddButton(text: String, amount: Int) {
        Box(
            modifier = GlanceModifier
                .cornerRadius(12.dp)
                .background(GlanceTheme.colors.primary)
                .clickable(
                    actionRunCallback<AddWaterAction>(
                        actionParametersOf(AddWaterAction.AmountKey to amount)
                    )
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onPrimary
                )
            )
        }
    }
}
