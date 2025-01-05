package vn.edu.usth.uihealthcare.model

import java.util.Date

data class Measurement<T>(
    val timestamp: Date,
    val measurement: T
)