package com.example.waterfillchallenge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

data class TaskResult(val target: Int, val actual: Int, val score: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WaterFillApp() }
    }
}

@Composable
fun WaterFillApp() {
    var level by remember { mutableIntStateOf(1) }
    var task by remember { mutableIntStateOf(1) }
    var totalTasks by remember { mutableIntStateOf(tasksForLevel(1)) }
    var target by remember { mutableIntStateOf(randomTarget(1)) }
    var amount by remember { mutableFloatStateOf(0f) }
    var filling by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(false) }
    var levelScore by remember { mutableIntStateOf(0) }
    var totalScore by remember { mutableIntStateOf(0) }
    var completedLevels by remember { mutableIntStateOf(0) }
    var results by remember { mutableStateOf(listOf<TaskResult>()) }
    var showSummary by remember { mutableStateOf(false) }
    var success by remember { mutableStateOf(false) }

    fun resetTask() {
        target = randomTarget(level)
        amount = 0f
        locked = false
        filling = false
    }

    fun retryLevel() {
        task = 1
        totalTasks = tasksForLevel(level)
        levelScore = 0
        results = emptyList()
        showSummary = false
        resetTask()
    }

    fun nextLevel() {
        level++
        task = 1
        totalTasks = tasksForLevel(level)
        levelScore = 0
        results = emptyList()
        showSummary = false
        resetTask()
    }

    LaunchedEffect(filling) {
        if (filling) {
            while (filling && !locked) {
                delay(70)
                amount += 5.5f + level * 0.35f
                if (amount > target * 1.45f) {
                    filling = false
                    locked = true
                    success = false
                    showSummary = true
                }
            }
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5FBFE)) {
            if (showSummary) {
                SummaryScreen(
                    level = level,
                    results = results,
                    levelScore = levelScore,
                    success = success,
                    completedLevels = completedLevels,
                    onRetry = { retryLevel() },
                    onNext = { nextLevel() }
                )
            } else {
                GameScreen(
                    level = level,
                    task = task,
                    totalTasks = totalTasks,
                    target = target,
                    amount = amount,
                    totalScore = totalScore,
                    completedLevels = completedLevels,
                    filling = filling,
                    onStart = {
                        if (!locked) filling = true
                    },
                    onStop = {
                        if (filling && !locked) {
                            filling = false
                            val tolerance = maxOf(
                                5f,
                                target * if (level <= 2) .02f else if (level <= 4) .012f else .008f
                            )
                            val ok = abs(amount - target) <= tolerance
                            val score = if (ok) {
                                (100f * (1f - (abs(amount - target) / target).coerceAtMost(1f))).toInt()
                            } else 0

                            locked = true
                            success = ok
                            levelScore += score
                            totalScore += score
                            results = results + TaskResult(target, amount.toInt(), score)

                            if (!ok) {
                                showSummary = true
                            } else if (task == totalTasks) {
                                completedLevels++
                                showSummary = true
                            } else {
                                task++
                                resetTask()
                            }
                        }
                    },
                    onRestart = { retryLevel() }
                )
            }
        }
    }
}

fun tasksForLevel(level: Int): Int =
    when (level) {
        1 -> 1
        2 -> 3
        3 -> 5
        4 -> 7
        else -> minOf(10 + (level - 5) * 2, 25)
    }

fun randomTarget(level: Int): Int {
    val min = minOf(300 + level * 30, 600)
    val max = minOf(600 + level * 35, 950)
    return ((min + Random.nextInt(max - min + 1)) / 10) * 10
}

@Composable
fun GameScreen(
    level: Int, task: Int, totalTasks: Int, target: Int, amount: Float,
    totalScore: Int, completedLevels: Int, filling: Boolean,
    onStart: () -> Unit, onStop: () -> Unit, onRestart: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("💧 Water Fill Challenge", fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text("One continuous pull. Stop as close as possible.", color = Color.Gray, fontSize = 13.sp)
            }
            Text("LEVEL $level", fontWeight = FontWeight.Bold)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoPill("Task $task/$totalTasks")
            InfoPill("Levels $completedLevels")
            InfoPill("Score $totalScore")
        }

        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
            Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.fillMaxWidth().height(330.dp).background(Color(0xFFE8F8FF), RoundedCornerShape(18.dp))) {
                    Text("Target: $target ml", Modifier.align(Alignment.TopStart).padding(12.dp), fontWeight = FontWeight.Bold)
                    Text("Current: ${amount.toInt()} ml", Modifier.align(Alignment.TopEnd).padding(12.dp), fontWeight = FontWeight.Bold)

                    Canvas(Modifier.fillMaxSize()) {
                        val glassW = 125.dp.toPx()
                        val glassH = 225.dp.toPx()
                        val left = (size.width - glassW) / 2
                        val top = size.height - glassH - 30.dp.toPx()
                        drawRoundRect(Color(0xFF657786), Offset(left, top), Size(glassW, glassH), 28.dp.toPx())
                        val waterH = (glassH * (amount / target).coerceIn(0f, 1f))
                        drawRect(Color(0xFF1689D0), Offset(left + 6, top + glassH - waterH - 6), Size(glassW - 12, waterH))
                        drawLine(Color.Red, Offset(left, top + glassH * .22f), Offset(left + glassW, top + glassH * .22f), 3.dp.toPx())
                    }
                }

                Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onStart,
                        Modifier.weight(1f).pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onStart()
                                    tryAwaitRelease()
                                    onStop()
                                }
                            )
                        }
                    ) { Text("🚰 HOLD TO FILL") }

                    OutlinedButton(onClick = onStop) { Text("STOP") }
                }

                Text(
                    if (filling) "💧 Filling…" else "Hold and release near the red target line.",
                    Modifier.padding(top = 10.dp),
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(onClick = onRestart, Modifier.padding(top = 4.dp)) {
                    Text("↻ Restart Level")
                }
            }
        }
    }
}

@Composable
fun InfoPill(text: String) {
    Text(text, Modifier.border(1.dp, Color(0xFFD5E5EE), RoundedCornerShape(12.dp)).padding(9.dp), fontSize = 12.sp)
}

@Composable
fun SummaryScreen(
    level: Int, results: List<TaskResult>, levelScore: Int,
    success: Boolean, completedLevels: Int, onRetry: () -> Unit, onNext: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (success) "🏆 Level $level Complete!" else "❌ Level $level Failed", fontSize = 27.sp, fontWeight = FontWeight.Bold)
        Text(if (success) "You completed all ${results.size} tasks." else "Retry the level to continue.", color = Color.Gray)
        Text("$levelScore / ${results.size * 100}", Modifier.fillMaxWidth(), fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
        Text("LEVEL SCORE", Modifier.align(Alignment.CenterHorizontally), color = Color.Gray)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                results.forEachIndexed { index, r ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Task ${index + 1}: ${r.target} → ${r.actual} ml")
                        Text("${r.score}/100", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onRetry, Modifier.weight(1f)) { Text("↻ Retry Level") }
            if (success) Button(onClick = onNext, Modifier.weight(1f)) { Text("Next Level →") }
        }
    }
}
