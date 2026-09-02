package com.example.voiceai

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this)
        text.text = "Voice AI"
        text.textSize = 28f
        text.setPadding(40, 80, 40, 40)

        setContentView(text)
    }
}
