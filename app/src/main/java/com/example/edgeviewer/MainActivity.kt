package com.example.edgeviewer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(android.R.layout.simple_list_item_1)

        val s = NativeBridge.stringFromJNI()
        Log.d("JNI", "Message from native: $s")
    }
}
