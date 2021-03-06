package com.example.androiddevchallenge.ui

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androiddevchallenge.model.CountDown
import com.example.androiddevchallenge.model.reset
import com.example.androiddevchallenge.model.update

class TimerViewModel : ViewModel() {

    private lateinit var timer: CountDownTimer

    private var _countDown = MutableLiveData(CountDown())
    val countDown: LiveData<CountDown> = _countDown

    private var _timerState = MutableLiveData(TimerState.FINISHED)
    val timerState: LiveData<TimerState> = _timerState

    fun onStartStopClick() {
        if (_timerState.value == TimerState.RUNNING) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    fun setNewTime(hours: Int? = null, minutes: Int? = null, seconds: Int? = null) {
        hours?.let { _countDown.update(hours = it) }
        minutes?.let { _countDown.update(minutes = it) }
        seconds?.let { _countDown.update(seconds = it) }
    }

    private fun startTimer() {
        // start new one
        timer = object : CountDownTimer(_countDown.value?.toMillis() ?: 0L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _countDown.update(millis = millisUntilFinished)
            }

            override fun onFinish() {
                _timerState.value = TimerState.FINISHED
            }
        }.start()
        _timerState.value = TimerState.RUNNING
    }

    fun stopTimer() {
        if (_timerState.value == TimerState.RUNNING) {
            timer.cancel()
        }
        _countDown.reset()
        _timerState.value = TimerState.FINISHED
    }

    private fun pauseTimer() {
        timer.cancel()
        _timerState.value = TimerState.PAUSED
    }
}

enum class TimerState {
    RUNNING, PAUSED, FINISHED
}
