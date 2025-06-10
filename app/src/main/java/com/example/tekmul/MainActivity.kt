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
        "Berapakah hasil dari 3x + 2 jika x = 2?" to listOf("8", "10"),
        "Berapakah nilai dari 20% dari 150?" to listOf("30", "20"),
        "Hasil dari (5 + 3) × 2 adalah?" to listOf("16", "13"),
        "Jika f(x) = x², maka f(4) = ?" to listOf("16", "8"),
        "Berapakah akar kuadrat dari 81?" to listOf("9", "8"),
        "Sebuah balok memiliki panjang 10 cm, lebar 4 cm, dan tinggi 3 cm. Berapakah volumenya?" to listOf("120 cm³", "140 cm³"),
        "Berapakah hasil dari penyelesaian x² - 9 = 0?" to listOf("x = 3", "x = ±3"),
        "Selesaikan SPLDV: 2x + y = 7 dan x - y = 2. Nilai x adalah?" to listOf("x = 2", "x = 3"),
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
        val correctAnswers = listOf("8", "30", "16", "16", "9", "120 cm³", "x = ±3", "3")
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
