package com.example.voiceai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var resultText: TextView
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech

    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        resultText = findViewById(R.id.resultText)

        val button: Button = findViewById(R.id.voiceButton)

        // Text To Speech
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {

                val result = textToSpeech.setLanguage(Locale.US)

                ttsReady =
                    result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }

        // بررسی وجود سرویس تشخیص صدا
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            resultText.text =
                "Speech recognition is not available on this phone."

            button.isEnabled = false

            return
        }

        // ساخت Speech Recognizer
        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer.setRecognitionListener(
            object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {

                    resultText.text = "Listening..."
                }

                override fun onBeginningOfSpeech() {

                    resultText.text = "I'm listening..."
                }

                override fun onRmsChanged(rmsdB: Float) {
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                }

                override fun onEndOfSpeech() {

                    resultText.text = "Processing..."
                }

                override fun onError(error: Int) {

                    resultText.text =
                        "Speech error: ${getErrorMessage(error)}"
                }

                override fun onResults(results: Bundle?) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val spokenText =
                        matches?.firstOrNull()

                    if (spokenText.isNullOrBlank()) {

                        resultText.text =
                            "I didn't hear anything."

                        return
                    }

                    // نمایش متن تشخیص داده شده
                    resultText.text = spokenText

                    // پاسخ صوتی
                    if (ttsReady) {

                        textToSpeech.speak(
                            spokenText,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "voice"
                        )
                    }
                }

                override fun onPartialResults(
                    partialResults: Bundle?
                ) {
                }

                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {
                }
            }
        )

        // دکمه Talk to AI
        button.setOnClickListener {

            if (
                checkSelfPermission(
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                requestPermissions(
                    arrayOf(
                        Manifest.permission.RECORD_AUDIO
                    ),
                    100
                )

            } else {

                startListening()
            }
        }

        // درخواست اجازه میکروفون
        if (
            checkSelfPermission(
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                100
            )
        }
    }

    private fun startListening() {

        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.US.toLanguageTag()
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PARTIAL_RESULTS,
            true
        )

        resultText.text = "Starting..."

        speechRecognizer.startListening(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == 100) {

            if (
                grantResults.isNotEmpty() &&
                grantResults[0] ==
                PackageManager.PERMISSION_GRANTED
            ) {

                resultText.text =
                    "Microphone permission granted. Tap Talk to AI."

            } else {

                resultText.text =
                    "Microphone permission was denied."
            }
        }
    }

    private fun getErrorMessage(error: Int): String {

        return when (error) {

            SpeechRecognizer.ERROR_AUDIO ->
                "audio recording problem"

            SpeechRecognizer.ERROR_CLIENT ->
                "client problem"

            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                "microphone permission denied"

            SpeechRecognizer.ERROR_NETWORK ->
                "network problem"

            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "network timeout"

            SpeechRecognizer.ERROR_NO_MATCH ->
                "no speech recognized"

            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                "recognizer is busy"

            SpeechRecognizer.ERROR_SERVER ->
                "speech service server problem"

            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                "no speech detected"

            else ->
                "unknown error (code $error)"
        }
    }

    override fun onDestroy() {

        if (::speechRecognizer.isInitialized) {

            speechRecognizer.destroy()
        }

        if (::textToSpeech.isInitialized) {

            textToSpeech.shutdown()
        }

        super.onDestroy()
    }
}
