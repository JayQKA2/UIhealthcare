package vn.edu.usth.uihealthcare.sensor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.view.Surface
import androidx.core.app.ActivityCompat
import vn.edu.usth.uihealthcare.MainActivity
import java.util.Collections

class CameraService(private val activity: Activity, private val handler: Handler) {
    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var previewSession: CameraCaptureSession? = null
    private var previewCaptureRequestBuilder: CaptureRequest.Builder? = null

    fun start(previewSurface: Surface) {
        val cameraManager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull()
        } catch (e: CameraAccessException) {
            Log.e("camera", "No access to camera", e)
            handler.sendMessage(Message.obtain(handler, MainActivity.MESSAGE_CAMERA_NOT_AVAILABLE, "No access to camera...."))
        } catch (e: NullPointerException) {
            Log.e("camera", "Null pointer exception", e)
        } catch (e: ArrayIndexOutOfBoundsException) {
            Log.e("camera", "Array index out of bounds", e)
        }

        try {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e("camera", "No permission to take photos")
                handler.sendMessage(Message.obtain(handler, MainActivity.MESSAGE_CAMERA_NOT_AVAILABLE, "No permission to take photos"))
                return
            }

            if (cameraId == null) {
                return
            }

            cameraManager.openCamera(cameraId!!, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    val stateCallback = object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            previewSession = session
                            try {
                                previewCaptureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                                previewCaptureRequestBuilder!!.addTarget(previewSurface)
                                previewCaptureRequestBuilder!!.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)

                                val thread = HandlerThread("CameraPreview")
                                thread.start()

                                previewSession!!.setRepeatingRequest(previewCaptureRequestBuilder!!.build(), null, null)

                            } catch (e: CameraAccessException) {
                                e.message?.let { Log.e("camera", it) }
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e("camera", "Session configuration failed")
                        }
                    }

                    try {
                        // Deprecated in API 30, but keeping it for backward compatibility.
                        camera.createCaptureSession(Collections.singletonList(previewSurface), stateCallback, null)
                    } catch (e: CameraAccessException) {
                        e.message?.let { Log.e("camera", it) }
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {}

                override fun onError(camera: CameraDevice, error: Int) {}
            }, null)
        } catch (e: CameraAccessException) {
            e.message?.let {
                Log.e("camera", it)
                handler.sendMessage(Message.obtain(handler, MainActivity.MESSAGE_CAMERA_NOT_AVAILABLE, it))
            }
        } catch (e: SecurityException) {
            e.message?.let {
                Log.e("camera", it)
                handler.sendMessage(Message.obtain(handler, MainActivity.MESSAGE_CAMERA_NOT_AVAILABLE, it))
            }
        }
    }

    fun stop() {
        try {
            cameraDevice?.close()
        } catch (e: Exception) {
            Log.e("camera", "Cannot close camera device: ${e.message}")
        }
    }
}
