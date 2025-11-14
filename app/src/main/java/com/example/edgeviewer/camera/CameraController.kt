package com.example.edgeviewer.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log

class CameraController(
    private val context: Context,
    private val width: Int,
    private val height: Int,
    private val onFrame: (ByteArray) -> Unit
) {

    private lateinit var cameraDevice: CameraDevice
    private lateinit var session: CameraCaptureSession
    private lateinit var imageReader: ImageReader

    private val bgThread = HandlerThread("CameraBG").apply { start() }
    private val bgHandler = Handler(bgThread.looper)

    @SuppressLint("MissingPermission")
    fun start() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList[0]

        imageReader = ImageReader.newInstance(width, height, ImageFormat.YUV_420_888, 2)
        imageReader.setOnImageAvailableListener({ reader ->
            val img = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            val bytes = imageToNV21(img)
            img.close()
            onFrame(bytes)
        }, bgHandler)

        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) {
                cameraDevice = device
                createSession()
            }
            override fun onDisconnected(device: CameraDevice) {}
            override fun onError(device: CameraDevice, error: Int) {}
        }, bgHandler)
    }

    private fun createSession() {
        val surfaces = listOf(imageReader.surface)

        cameraDevice.createCaptureSession(
            surfaces,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(ses: CameraCaptureSession) {
                    session = ses
                    val req = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    req.addTarget(imageReader.surface)
                    ses.setRepeatingRequest(req.build(), null, bgHandler)
                }

                override fun onConfigureFailed(ses: CameraCaptureSession) {
                    Log.e("CameraController", "Session config failed")
                }
            },
            bgHandler
        )
    }

    // Convert YUV_420_888 → NV21
    private fun imageToNV21(image: Image): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val output = ByteArray(ySize + uSize + vSize)

        yBuffer.get(output, 0, ySize)
        vBuffer.get(output, ySize, vSize)
        uBuffer.get(output, ySize + vSize, uSize)

        return output
    }
}
