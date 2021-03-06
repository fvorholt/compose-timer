package com.example.androiddevchallenge.utils

fun Int.toTwoDigits(): String {
    return "$this".padStart(2, '0')
}