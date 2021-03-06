package com.example.androiddevchallenge.utils

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.atan2

fun Offset.theta(): Float {
    val theta = atan2(y.toDouble(), x.toDouble()).toFloat()
    return if (theta >= 0) {
        theta
    } else {
        (theta + 2 * PI).toFloat()
    }
}

fun Float.rotateToTop(): Float {
    return when {
        this in 0.0..1.5f * PI -> {
            this + 0.5f * PI.toFloat()
        }
        this in 1.5f * PI..2 * PI -> {
            this - 1.5f * PI.toFloat()
        }
        else -> {
            error("This should not happen")
        }
    }
}