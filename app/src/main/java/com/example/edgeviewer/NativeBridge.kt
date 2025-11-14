package com.example.edgeviewer

object NativeBridge {
    init {
        // This loads the 'native-lib' library when this object is first used.
        System.loadLibrary("native-lib")
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    /**
     * A native method that processes a frame (like from a camera)
     * and returns the processed frame.
     */
    external fun processFrame(input: ByteArray, width: Int, height: Int): ByteArray

}
