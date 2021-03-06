/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge.model

import androidx.lifecycle.MutableLiveData

data class CountDown(
    var hours: Int = 0,
    var minutes: Int = 0,
    var seconds: Int = 0
) {
    fun toMillis(): Long {
        return this.hours * 3600000L + this.minutes * 60000L + this.seconds * 1000
    }

    val isNotZero: Boolean get() = hours > 0 || minutes > 0 || seconds > 0
}

fun MutableLiveData<CountDown>.update(
    hours: Int? = null,
    minutes: Int? = null,
    seconds: Int? = null,
    millis: Long? = null
) {
    var value = this.value?.copy() ?: CountDown()

    hours?.let { value.hours = it }
    minutes?.let { value.minutes = it }
    seconds?.let { value.seconds = it }
    millis?.let { value = it.toCountDown() }

    this.value = value
}

fun MutableLiveData<CountDown>.reset() {
    this.value = CountDown()
}

fun Long.toCountDown(): CountDown {
    val hours = this / 3600000L
    val minutes = (this - hours * 3600000L) / 60000L
    val seconds = (this - minutes * 60000L - hours * 3600000L) / 1000L
    return CountDown(hours.toInt(), minutes.toInt(), seconds.toInt())
}
