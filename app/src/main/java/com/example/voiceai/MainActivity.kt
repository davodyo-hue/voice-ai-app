package com.example.voiceai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var resultText: TextView
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resultText = TextView(this)
        resultText.text = "Voice AI\n\nPress the button and speak."
        resultText.textSize = 20f
        resultText.setPadding(30, 50, 30, 30)

        val button = Button(this)
        button.text = "🎤 Speak"

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(30, 30, 30, 30)

        layout.addView(resultText)
        layout.addView(button)

        setContentView(layout)

        textToSpeech = TextToSpeech(this) {
            textToSpeech.language = Locale.US
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        button.setOnClickListener {
            startListening()
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.US
        )

        speechRecognizer.setRecognitionListener(
            object : android.speech.RecognitionListener {

                override fun onResults(results: Bundle?) {
                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    if (!matches.isNullOrEmpty()) {
                        val spokenText = matches[0]
                        resultText.text = spokenText
                        textToSpeech.speak(
                            spokenText,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "voice"
                        )
                    }
                }

                override fun onReadyForSpeech(params: Bundle?) {
                    resultText.text = "Listening..."
                }

                override fun onError(error: Int) {
                    resultText.text = "Could not understand. Try again."
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            }
        )

        speechRecognizer.startListening(intent)
    }

    override fun onDestroy() {
        speechRecognizer.destroy()
        textToSpeech.shutdown()
        super.onDestroy()
    }
}
