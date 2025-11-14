package com.example.edgeviewer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.edgeviewer.camera.CameraController

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQ_CAMERA = 101
    }

    private var camera: CameraController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(android.R.layout.simple_list_item_1)

        // JNI test (keep it)
        val s = NativeBridge.stringFromJNI()
        Log.d("JNI", "Message from native: $s")

        // Check permission first, request if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            Log.d("Perm", "Camera permission already granted")
            startCameraController()
        } else {
            Log.d("Perm", "Requesting camera permission")
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQ_CAMERA)
        }
    }

    private fun startCameraController() {
        camera = CameraController(this, 640, 480) { frameBytes ->

            val processed = NativeBridge.processFrame(frameBytes, 640, 480)

            Log.d("Native", "Processed frame returned = ${processed.size}")

        }

        camera?.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CAMERA) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Perm", "Camera permission granted by user")
                startCameraController()
            } else {
                Log.w("Perm", "Camera permission denied")
                // show dialog to open app settings
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

    override fun onDestroy() {
        super.onDestroy()
        // stop or cleanup camera thread if you add a stop method later
    }
}
