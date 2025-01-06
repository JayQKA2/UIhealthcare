package vn.edu.usth.uihealthcare.ui.theme.activity

import vn.edu.usth.uihealthcare.heartrate.OutputAnalyzer
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vn.edu.usth.uihealthcare.R
import vn.edu.usth.uihealthcare.sensor.CameraService
import vn.edu.usth.uihealthcare.utils.HealthConnectManager
import java.time.Instant
import java.time.ZonedDateTime

class HeartActivity : AppCompatActivity() {
    private var analyzer: OutputAnalyzer? = null
    private var sessionStartTime: ZonedDateTime? = null
    private lateinit var toolbar: Toolbar
    private lateinit var healthConnectManager: HealthConnectManager
    private var sessionEndTime: ZonedDateTime? = null
    private var heart_value: TextView? = null



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
                    view.findViewById<TextView>(R.id.pulse_value).text = msg.obj.toString()
                    setViewState(ViewState.SHOW_RESULTS)
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

    private fun checkpermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("CAMERA", "Permission granted! ")
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 100)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_heart)
        heart_value = findViewById(R.id.heartnumber)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        checkpermission()

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val startButton: Button = findViewById(R.id.floatingActionButton)
        startButton.setOnClickListener { onClickNewMeasurement() }

        healthConnectManager = HealthConnectManager(this)
        fetchHeartRate()
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
        setViewState(ViewState.MEASUREMENT)

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


    fun setViewState(state: ViewState) {
        when (state) {
            ViewState.MEASUREMENT -> {
                findViewById<View>(R.id.floatingActionButton)?.visibility = View.INVISIBLE
            }
            ViewState.SHOW_RESULTS -> {
                findViewById<View>(R.id.floatingActionButton)?.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun fetchHeartRate() {
        lifecycleScope.launch {
            try {
                val endTime = Instant.now()
                val startTime = endTime.minusSeconds(60 * 60 * 24)

                val heartRateRecord = healthConnectManager.readHeartRateRecords(startTime, endTime)
                if (heartRateRecord.isNotEmpty()) {
                    val latestHeartRate = heartRateRecord.first()

                    val pulse = latestHeartRate.samples.first().beatsPerMinute

                    heart_value!!.text = "Weight: $pulse bpm"
                } else {
                    heart_value!!.text = "No weight data found."
                }
            } catch (e: Exception) {
                heart_value!!.text = "Failed to fetch weight data: ${e.message}"
            }
        }
    }


    companion object {
        const val MESSAGE_UPDATE_REALTIME = 1
        const val MESSAGE_UPDATE_FINAL = 2
        const val MESSAGE_CAMERA_NOT_AVAILABLE = 3

    }

    enum class ViewState {
        MEASUREMENT,
        SHOW_RESULTS
    }
}
