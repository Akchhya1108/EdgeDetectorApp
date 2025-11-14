package com.example.edgeviewer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.opengl.GLSurfaceView
import com.example.edgeviewer.camera.CameraController
import com.example.edgeviewer.gl.GLRenderer
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQ_CAMERA = 101
    }

    private var camera: CameraController? = null
    private lateinit var glView: GLSurfaceView
    private var renderer: GLRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // create GLSurfaceView programmatically and set as content view
        glView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
        }

        renderer = GLRenderer(640, 480)
        glView.setRenderer(renderer)
        glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY

        // Wrap view in a FrameLayout so we can later overlay controls if needed
        val root = FrameLayout(this)
        root.addView(glView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER
        ))
        setContentView(root)

        // JNI test
        val s = NativeBridge.stringFromJNI()
        Log.d("JNI", "Message from native: $s")

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCameraController()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA)
        }
    }

    private fun startCameraController() {
        camera = CameraController(this, 640, 480) { frameBytes ->
            // Camera callback runs on a background handler (not UI). We will:
            // 1) send bytes to native for processing (also fast native code)
            // 2) receive processed RGBA bytes and pass to renderer
            thread {
                try {
                    val processed = NativeBridge.processFrame(frameBytes, 640, 480)
                    // processed should be RGBA (width*height*4)
                    if (processed != null && processed.isNotEmpty()) {
                        // Pass to renderer on GL thread using queueEvent
                        glView.queueEvent {
                            renderer?.updateFrame(processed, 640, 480)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "native processing failed: ${e.message}")
                }
            }
        }

        camera?.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraController()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Camera permission required")
                    .setMessage("This app needs camera permission to capture frames. Please enable it in Settings.")
                    .setPositiveButton("Open Settings") { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }
}
