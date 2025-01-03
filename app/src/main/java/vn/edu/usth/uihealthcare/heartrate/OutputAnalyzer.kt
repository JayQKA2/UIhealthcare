import android.os.CountDownTimer
import android.os.Handler
import android.os.Message
import android.view.TextureView
import vn.edu.usth.uihealthcare.sensor.CameraService
import vn.edu.usth.uihealthcare.heartrate.ChartDrawer
import vn.edu.usth.uihealthcare.MainActivity
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.model.MeasureStore
import vn.edu.usth.uihealthcare.ui.theme.HeartActivity
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class OutputAnalyzer(private val activity: HeartActivity, graphTextureView: TextureView, private val mainHandler: Handler) {

    private val chartDrawer: ChartDrawer = ChartDrawer(graphTextureView)
    private var store: MeasureStore? = null

    private val measurementInterval = 45
    private val measurementLength = 30000
    private val clipLength = 3500

    private var detectedValleys = 0
    private var ticksPassed = 0

    private val valleys = CopyOnWriteArrayList<Long>()
    private var timer: CountDownTimer? = null

    private fun detectValley(): Boolean {
        val valleyDetectionWindowSize = 13
        val subList = store?.getLastStdValues(valleyDetectionWindowSize) ?: return false

        if (subList.size < valleyDetectionWindowSize) {
            return false
        } else {
            val referenceValue = subList[(Math.ceil((valleyDetectionWindowSize / 2f).toDouble()) - 1).toInt()].measurement
            for (measurement in subList) {
                if (measurement.measurement < referenceValue) return false
            }

            return subList[(Math.ceil((valleyDetectionWindowSize / 2f).toDouble()) - 1).toInt()].measurement !=
                    subList[(Math.ceil((valleyDetectionWindowSize / 2f).toDouble()) - 2).toInt()].measurement
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
                        measurement += (pixels[pixelIndex] shr 16) and 0xff
                    }

                    store?.add(measurement)

                    if (detectValley()) {
                        detectedValleys += 1
                        valleys.add(store?.getLastTimestamp()?.time ?: 0)
                        val currentValue = String.format(
                            Locale.getDefault(),
                            activity.resources.getQuantityString(R.plurals.measurement_output_template, detectedValleys),
                            if (valleys.size == 1) {
                                60f * detectedValleys / (Math.max(1f, (measurementLength - millisUntilFinished - clipLength) / 1000f))
                            } else {
                                60f * (detectedValleys - 1) / (Math.max(1f, (valleys.last() - valleys.first()) / 1000f))
                            },
                            detectedValleys,
                            1f * (measurementLength - millisUntilFinished - clipLength) / 1000f
                        )

                        sendMessage(HeartActivity.MESSAGE_UPDATE_REALTIME, currentValue)
                    }

                    Thread { chartDrawer.draw(store?.getStdValues() ?: CopyOnWriteArrayList()) }.start()
                }.start()
            }

            override fun onFinish() {
                store?.getStdValues() ?: return

                if (valleys.isEmpty()) {
                    mainHandler.sendMessage(
                        Message.obtain(mainHandler, MainActivity.MESSAGE_CAMERA_NOT_AVAILABLE, "No valleys detected - there may be an issue when accessing the camera.")
                    )
                    return
                }

                val averageHeartRate = 60f * (detectedValleys - 1) / (Math.max(1f, (valleys.last() - valleys.first()) / 1000f))

                val currentValue = String.format(
                    Locale.getDefault(),
                    "%.1f bpm",
                    averageHeartRate
                )

                sendMessage(HeartActivity.MESSAGE_UPDATE_FINAL, currentValue)

                cameraService.stop()
            }
        }

        activity.setViewState(HeartActivity.ViewState.MEASUREMENT)
        timer?.start()
    }

    fun stop() {
        timer?.cancel()
    }

    private fun sendMessage(what: Int, message: Any) {
        val msg = Message()
        msg.what = what
        msg.obj = message
        mainHandler.sendMessage(msg)
    }
}
