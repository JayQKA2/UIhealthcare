package vn.edu.usth.uihealthcare.model

import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList

class MeasureStore {

    private val measurements = CopyOnWriteArrayList<Measurement<Int>>()
    private var minimum = Int.MAX_VALUE
    private var maximum = Int.MIN_VALUE


    private val rollingAverageSize = 4

    fun add(measurement: Int) {
        val measurementWithDate = Measurement(Date(), measurement)

        measurements.add(measurementWithDate)
        if (measurement < minimum) minimum = measurement
        if (measurement > maximum) maximum = measurement
    }

    fun getStdValues(): CopyOnWriteArrayList<Measurement<Float>> {
        val stdValues = CopyOnWriteArrayList<Measurement<Float>>()

        for (i in measurements.indices) {
            var sum = 0
            for (rollingAverageCounter in 0 until rollingAverageSize) {
                sum += measurements.getOrElse(i - rollingAverageCounter) { measurements[0] }.measurement
            }

            val stdValue = Measurement(
                measurements[i].timestamp,
                ((sum.toFloat() / rollingAverageSize - minimum) / (maximum - minimum))
            )
            stdValues.add(stdValue)
        }

        return stdValues
    }

    fun getLastStdValues(count: Int): CopyOnWriteArrayList<Measurement<Int>> {
        return if (count < measurements.size) {
            CopyOnWriteArrayList(measurements.subList(measurements.size - 1 - count, measurements.size))
        } else {
            measurements
        }
    }

    fun getLastTimestamp(): Date {
        return measurements.last().timestamp
    }
}