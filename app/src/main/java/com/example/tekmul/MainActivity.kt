package com.example.tekmul

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var scoreTextView: TextView
    private lateinit var tts: TextToSpeech
    private var score = 0

    private val questions = listOf(
        "Apa ibukota Indonesia?" to listOf("Jakarta", "Bandung"),
        "Berapakah hasil 2 + 2?" to listOf("4", "5")
    )

    private var currentQuestionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        scoreTextView = findViewById(R.id.scoreTextView)

        // Inisialisasi TTS
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale("id", "ID"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Bahasa tidak didukung!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Inisialisasi TTS gagal!", Toast.LENGTH_SHORT).show()
            }
        }

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            if (currentQuestionIndex < questions.size) {
                createQuizNode(hitResult.createAnchor())
            } else {
                Toast.makeText(this, "Semua pertanyaan selesai!", Toast.LENGTH_SHORT).show()
                tts.speak("Semua pertanyaan selesai!", TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    private fun createQuizNode(anchor: Anchor) {
        ViewRenderable.builder()
            .setView(this, R.layout.quiz_question_view)
            .build()
            .thenAccept { renderable ->
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment.arSceneView.scene)

                val quizNode = TransformableNode(arFragment.transformationSystem)
                quizNode.setParent(anchorNode)
                quizNode.renderable = renderable
                quizNode.select()

                val questionTextView = renderable.view.findViewById<TextView>(R.id.questionTextView)
                val btnAnswer1 = renderable.view.findViewById<TextView>(R.id.btnAnswer1)
                val btnAnswer2 = renderable.view.findViewById<TextView>(R.id.btnAnswer2)

                val (question, answers) = questions[currentQuestionIndex]
                questionTextView.text = question
                btnAnswer1.text = answers[0]
                btnAnswer2.text = answers[1]

                // Bacakan pertanyaan
                tts.speak(question, TextToSpeech.QUEUE_FLUSH, null, null)

                btnAnswer1.setOnClickListener {
                    handleAnswer(answers[0])
                    quizNode.setParent(null)
                }

                btnAnswer2.setOnClickListener {
                    handleAnswer(answers[1])
                    quizNode.setParent(null)
                }
            }
    }

    private fun handleAnswer(selectedAnswer: String) {
        val correctAnswers = listOf("Jakarta", "4")
        val correct = selectedAnswer == correctAnswers[currentQuestionIndex]

        val response = if (correct) {
            score++
            scoreTextView.text = "Score: $score"
            Toast.makeText(this, "Benar!", Toast.LENGTH_SHORT).show()
            "Jawaban kamu benar!"
        } else {
            Toast.makeText(this, "Salah!", Toast.LENGTH_SHORT).show()
            "Jawaban kamu salah!"
        }

        // Bacakan respons
        tts.speak("Jawabanmu: $selectedAnswer. $response", TextToSpeech.QUEUE_FLUSH, null, null)
        currentQuestionIndex++
    }

    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
