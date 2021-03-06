package com.example.androiddevchallenge.ui.views

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androiddevchallenge.model.CountDown
import com.example.androiddevchallenge.ui.TimerState
import com.example.androiddevchallenge.ui.TimerViewModel
import com.example.androiddevchallenge.ui.theme.purple200
import com.example.androiddevchallenge.ui.views.MutableClockSegment.*
import com.example.androiddevchallenge.utils.theta
import com.example.androiddevchallenge.utils.toTwoDigits
import com.example.androiddevchallenge.utils.rotateToTop
import kotlin.math.*

@ExperimentalAnimationApi
@Composable
fun TimerApp(timerViewModel: TimerViewModel = viewModel()) {
    val timerState: TimerState by timerViewModel.timerState.observeAsState(TimerState.FINISHED)
    val countDown: CountDown by timerViewModel.countDown.observeAsState(CountDown())
    var activeSegment by remember { mutableStateOf(NONE) }
    Surface(
        color = MaterialTheme.colors.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MutableClock(
                timerViewModel = timerViewModel,
                countDown = countDown,
                enabled = timerState == TimerState.FINISHED,
                activeSegment = activeSegment,
                updateActiveSegment = { newSegment ->
                    activeSegment = if (activeSegment != newSegment) newSegment else NONE
                }
            )
            Spacer(modifier = Modifier.padding(bottom = 12.dp))
            ButtonRow(
                timerState,
                onStopClick = { timerViewModel.stopTimer() },
                onStartPauseClick = {
                    activeSegment = NONE
                    timerViewModel.onStartStopClick()
                },
                startButtonEnabled = countDown.isNotZero,
                showResetButton = countDown.isNotZero || timerState != TimerState.FINISHED
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun MutableClock(
    timerViewModel: TimerViewModel,
    countDown: CountDown,
    enabled: Boolean,
    activeSegment: MutableClockSegment,
    updateActiveSegment: (MutableClockSegment) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            ClockSegment(
                label = "h",
                time = countDown.hours.toTwoDigits(),
                active = activeSegment == HOURS,
                onSegmentClick = { if (enabled) updateActiveSegment(HOURS) }
            )
            Spacer(modifier = Modifier.width(16.dp))
            ClockSegment(
                label = "m",
                time = countDown.minutes.toTwoDigits(),
                active = activeSegment == MINUTES,
                onSegmentClick = { if (enabled) updateActiveSegment(MINUTES) }
            )
            Spacer(modifier = Modifier.width(16.dp))
            ClockSegment(
                label = "s",
                time = countDown.seconds.toTwoDigits(),
                active = activeSegment == SECONDS,
                onSegmentClick = { if (enabled) updateActiveSegment(SECONDS) }
            )
        }
        AnimatedVisibility(visible = activeSegment == HOURS) {
            Wheel(
                numberOfTicks = 24,
                activeTicks = countDown.hours,
                onChange = { activeTicks -> timerViewModel.setNewTime(hours = activeTicks) },
                modifier = Modifier.width(300.dp)
            )
        }
        AnimatedVisibility(visible = activeSegment == MINUTES) {
            Wheel(
                numberOfTicks = 60,
                activeTicks = countDown.minutes,
                onChange = { activeTicks -> timerViewModel.setNewTime(minutes = activeTicks) },
                modifier = Modifier.width(300.dp)
            )
        }
        AnimatedVisibility(visible = activeSegment == SECONDS) {
            Wheel(
                numberOfTicks = 60,
                activeTicks = countDown.seconds,
                onChange = { activeTicks -> timerViewModel.setNewTime(seconds = activeTicks) },
                modifier = Modifier.width(300.dp)
            )
        }
    }
}

@Composable
fun ClockSegment(
    label: String,
    time: String,
    active: Boolean,
    onSegmentClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(text = label, color = Color(0xFF666666), modifier = Modifier.padding(end = 4.dp))
        Text(
            text = time,
            style = TextStyle(
                color = if (active) MaterialTheme.colors.primary else MaterialTheme.colors.secondary,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Light,
                fontSize = 80.sp
            ),
            modifier = Modifier.clickable { onSegmentClick() }
        )
    }
}

enum class MutableClockSegment {
    HOURS, MINUTES, SECONDS, NONE
}

@Composable
fun Wheel(
    numberOfTicks: Int,
    activeTicks: Int,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var position by remember { mutableStateOf(Offset.Zero) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onSizeChanged {
                origin = it.center.toOffset()
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        position = offset - origin
                    },
                    onDragEnd = {
                        position = Offset.Zero
                    },
                    onDragCancel = {
                        position = Offset.Zero
                    },
                    onDrag = { change, dragAmount ->
                        position += dragAmount
                        val angle = position
                            .theta()
                            .rotateToTop()
                        val tickDistance = 2 * PI / numberOfTicks
                        onChange(ceil(angle / tickDistance).toInt())
                        change.consumeAllChanges()
                    }
                )
            }
    ) {
        WheelSegment(numberOfTicks = numberOfTicks, activeTicks = activeTicks)
    }
}

@Composable
fun WheelSegment(
    numberOfTicks: Int,
    activeTicks: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                for (i in 0 until numberOfTicks) {
                    val angleOfTick = i * 2 * PI.toFloat() / numberOfTicks
                    val startRadius = size.width / 2 * 0.45f
                    val endRadius = size.width / 2 * 0.65f
                    val startPosition = Offset(
                        x = cos(angleOfTick.rotateToTop() - PI.toFloat()) * startRadius,
                        y = sin(angleOfTick.rotateToTop() - PI.toFloat()) * startRadius
                    )
                    val endPosition = Offset(
                        x = cos(angleOfTick.rotateToTop() - PI.toFloat()) * endRadius,
                        y = sin(angleOfTick.rotateToTop() - PI.toFloat()) * endRadius
                    )

                    drawLine(
                        color = if (i < activeTicks) purple200 else Color.White.copy(alpha = 0.2f),
                        start = center + startPosition,
                        end = center + endPosition,
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )
                }
            }
    )
}


@ExperimentalAnimationApi
@Composable
fun ButtonRow(
    timerState: TimerState,
    onStopClick: () -> Unit,
    onStartPauseClick: () -> Unit,
    startButtonEnabled: Boolean,
    showResetButton: Boolean
) {
    Row {
        AnimatedVisibility(showResetButton) { ResetButton { onStopClick() } }
        StartPauseButton(timerState, startButtonEnabled) { onStartPauseClick() }
    }
}

@Composable
fun StartPauseButton(timerState: TimerState, enabled: Boolean, onButtonClick: () -> Unit) {
    Button(
        enabled = enabled,
        onClick = {
            onButtonClick()
        }
    ) {
        Text(
            if (timerState == TimerState.RUNNING) "PAUSE" else "START"
        )
    }
}

@Composable
fun ResetButton(onButtonClick: () -> Unit) {
    TextButton(
        onClick = { onButtonClick() },
        modifier = Modifier.padding(end = 16.dp)
    ) {
        Text("RESET")
    }
}

