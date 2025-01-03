package vn.edu.usth.uihealthcare.ui.theme

import OutputAnalyzer
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.sensor.CameraService
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.time.ZonedDateTime


class HeartActivity : AppCompatActivity() {
    private var analyzer: OutputAnalyzer? = null
    private var sessionStartTime: ZonedDateTime? = null
    private var sessionEndTime: ZonedDateTime? = null

    private val mainHandler = @SuppressLint("HandlerLeak")
    object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val view = window.decorView
            when (msg.what) {
                MESSAGE_UPDATE_REALTIME -> {
                    view.findViewById<TextView>(R.id.pulse_value).text = msg.obj.toString()
                }
                MESSAGE_UPDATE_FINAL -> {
                    view.findViewById<TextView>(R.id.pulse_value).setText(msg.obj.toString())
                    setViewState(VIEW_STATE.SHOW_RESULTS)
                    stopCamera()
                    sessionEndTime = ZonedDateTime.now()

                }
                MESSAGE_CAMERA_NOT_AVAILABLE -> {
                    view.findViewById<TextView>(R.id.pulse_value).setText(R.string.camera_not_found)
                    analyzer?.stop()
                    stopCamera()
                }
            }
        }
    }

    private val cameraService = CameraService(this, mainHandler)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart)
        val startButton: Button = findViewById(R.id.floatingActionButton)
        startButton.setOnClickListener { onClickNewMeasurement() }
    }

    override fun onResume() {
        super.onResume()
        analyzer = OutputAnalyzer(this, findViewById(R.id.heart_rate_bar), mainHandler)
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
        analyzer?.stop()
    }

    fun onClickNewMeasurement() {
        analyzer = OutputAnalyzer(this, findViewById(R.id.heart_rate_bar), mainHandler)
        findViewById<TextView>(R.id.pulse_value).text = ""
        setViewState(VIEW_STATE.MEASUREMENT)

        sessionStartTime = ZonedDateTime.now()


        val cameraTextureView = findViewById<TextureView>(R.id.camera)
        val previewSurfaceTexture = cameraTextureView?.surfaceTexture
        previewSurfaceTexture?.let {
            val previewSurface = Surface(it)
            cameraService.start(previewSurface)
            analyzer?.measurePulse(cameraTextureView, cameraService)
        }
    }

    private fun stopCamera() {
        cameraService.stop()
    }


    fun setViewState(state: VIEW_STATE) {
        val appMenu = findViewById<Toolbar>(R.id.toolbar)?.menu
        when (state) {
            VIEW_STATE.MEASUREMENT -> {
                appMenu?.getItem(MENU_INDEX_NEW_MEASUREMENT)?.isVisible = false
                appMenu?.getItem(MENU_INDEX_EXPORT_RESULT)?.isVisible = false
                appMenu?.getItem(MENU_INDEX_EXPORT_DETAILS)?.isVisible = false
                findViewById<View>(R.id.floatingActionButton)?.visibility = View.INVISIBLE
            }
            VIEW_STATE.SHOW_RESULTS -> {
                findViewById<View>(R.id.floatingActionButton)?.visibility = View.VISIBLE
                appMenu?.getItem(MENU_INDEX_EXPORT_RESULT)?.isVisible = true
                appMenu?.getItem(MENU_INDEX_EXPORT_DETAILS)?.isVisible = true
                appMenu?.getItem(MENU_INDEX_NEW_MEASUREMENT)?.isVisible = true
            }
        }
    }

    companion object {
        const val MESSAGE_UPDATE_REALTIME = 1
        const val MESSAGE_UPDATE_FINAL = 2
        const val MESSAGE_CAMERA_NOT_AVAILABLE = 3
        private const val MENU_INDEX_NEW_MEASUREMENT = 0
        private const val MENU_INDEX_EXPORT_RESULT = 1
        private const val MENU_INDEX_EXPORT_DETAILS = 2
    }

    enum class VIEW_STATE {
        MEASUREMENT,
        SHOW_RESULTS
    }
}
