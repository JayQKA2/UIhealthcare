package vn.edu.usth.uihealthcare.heartrate

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.TextureView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.data.HeartData
import vn.edu.usth.uihealthcare.sensor.CameraService
import vn.edu.usth.uihealthcare.MainActivity
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.model.MeasureStore
import vn.edu.usth.uihealthcare.ui.theme.activity.HeartActivity
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.ceil

class OutputAnalyzer(
    private val context: Context,
    private val heartRateBar: TextureView,
    private val handler: Handler? = null
) {
    private var onDataReceivedListener: ((HeartData) -> Unit)? = null

    fun setOnDataReceivedListener(listener: (HeartData) -> Unit) {
        onDataReceivedListener = listener
    }

    private val chartDrawer: ChartDrawer = ChartDrawer(heartRateBar)
    private var store: MeasureStore? = null

    private val measurementInterval = 33
    private val measurementLength = 30000
    private val clipLength = 3500

    private var detectedValleys = 0
    private var ticksPassed = 0

    private val valleys = CopyOnWriteArrayList<Long>()
    private var timer: CountDownTimer? = null
    private val healthConnectManager = HealthConnectManager(context)

    private fun detectValley(): Boolean {
        val valleyDetectionWindowSize = 13
        val subList = store?.getLastStdValues(valleyDetectionWindowSize) ?: return false

        if (subList.size < valleyDetectionWindowSize) {
            return false
        } else {
            val referenceValue = subList[(ceil((valleyDetectionWindowSize / 2f).toDouble()) - 1).toInt()].measurement
            for (measurement in subList) {
                if (measurement.measurement < referenceValue) return false
            }

            return subList[(ceil((valleyDetectionWindowSize / 2f).toDouble()) - 1).toInt()].measurement !=
                    subList[(ceil((valleyDetectionWindowSize / 2f).toDouble()) - 2).toInt()].measurement
        }
    }

    fun measurePulse(textureView: TextureView, cameraService: CameraService) {
        store = MeasureStore()
        detectedValleys = 0

        timer = object : CountDownTimer(measurementLength.toLong(), measurementInterval.toLong()) {
            override fun onTick(millisUntilFinished: Long) {
                if (clipLength > (++ticksPassed * measurementInterval)) return

                Thread {
                    val currentBitmap = textureView.bitmap
                    val pixelCount = textureView.width * textureView.height
                    var measurement = 0
                    val pixels = IntArray(pixelCount)

                    currentBitmap?.getPixels(pixels, 0, textureView.width, 0, 0, textureView.width, textureView.height)

                    for (pixelIndex in pixels.indices) {
                        val red = (pixels[pixelIndex] shr 16) and 0xff
                        val green = (pixels[pixelIndex] shr 8) and 0xff
                        val blue = pixels[pixelIndex] and 0xff
                        measurement += (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
                    }
                    measurement /= pixels.size

                    store?.add(measurement)

                    if (detectValley()) {
                        detectedValleys += 1
                        valleys.add(store?.getLastTimestamp()?.time ?: 0)
                        val currentValue = String.format(
                            Locale.getDefault(),
                            context.resources.getQuantityString(R.plurals.measurement_output_template, detectedValleys),
                            if (valleys.size == 1) {
                                60f * detectedValleys / (1f.coerceAtLeast((measurementLength - millisUntilFinished - clipLength) / 1000f))
                            } else {
                                60f * (detectedValleys - 1) / (1f.coerceAtLeast((valleys.last() - valleys.first()) / 1000f))
                            },
                            detectedValleys,
                            1f * (measurementLength - millisUntilFinished - clipLength) / 1000f
                        )

                        sendMessage(HeartActivity.MESSAGE_UPDATE_REALTIME, currentValue)

                        onDataReceivedListener?.invoke(HeartData(
                            ZonedDateTime.now().toLocalDate().toString(),
                            ZonedDateTime.now().toLocalTime().toString(),
                            currentValue
                        ))
                    }

                    Thread { chartDrawer.draw(store?.getStdValues() ?: CopyOnWriteArrayList()) }.start()
                }.start()
            }

            override fun onFinish() {
                store?.getStdValues() ?: return

                if (valleys.isEmpty()) {
                    handler?.sendMessage(
                        Message.obtain(handler, MainActivity.MESSAGE_CAMERA_NOT_AVAILABLE, "No valleys detected - there may be an issue when accessing the camera.")
                    )
                    return
                }

                val averageHeartRate = 60f * (detectedValleys - 1) / (1f.coerceAtLeast((valleys.last() - valleys.first()) / 1000f))

                Log.d("Average Heart Rate", averageHeartRate.toString())
                val currentValue = String.format(
                    Locale.getDefault(),
                    "%.1f bpm",
                    averageHeartRate
                )

                sendMessage(HeartActivity.MESSAGE_UPDATE_FINAL, currentValue)

                val sessionStartTime = ZonedDateTime.now(ZoneOffset.UTC)
                val sessionEndTime = ZonedDateTime.now().plusSeconds(45)

                CoroutineScope(Dispatchers.Main).launch {
                    healthConnectManager.writeHeartRateSession(
                        averageHeartRate,
                        sessionStartTime,
                        sessionEndTime
                    )
                }
                cameraService.stop()
            }
        }

        timer?.start()
    }

    fun stop() {
        timer?.cancel()
    }

    private fun sendMessage(what: Int, message: Any) {
        val msg = Message()
        msg.what = what
        msg.obj = message
        handler?.sendMessage(msg)
    }
}