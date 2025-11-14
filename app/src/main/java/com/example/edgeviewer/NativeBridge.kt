package com.example.edgeviewer

object NativeBridge {
    init {
        System.loadLibrary("native-lib")
    }

    external fun stringFromJNI(): String
    external fun processFrame(input: ByteArray, width: Int, height: Int): ByteArray
}
