package com.example.tekmul

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class MainActivity : AppCompatActivity() {

    private lateinit var arFragment: ArFragment
    private lateinit var scoreTextView: TextView
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

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            if (currentQuestionIndex < questions.size) {
                createQuizNode(hitResult.createAnchor())
            } else {
                Toast.makeText(this, "Semua pertanyaan selesai!", Toast.LENGTH_SHORT).show()
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

                btnAnswer1.setOnClickListener {
                    checkAnswer(answers[0])
                    quizNode.setParent(null)  // Hapus node setelah dijawab
                }
                btnAnswer2.setOnClickListener {
                    checkAnswer(answers[1])
                    quizNode.setParent(null)
                }
            }
    }

    private fun checkAnswer(answer: String) {
        val correctAnswers = listOf("Jakarta", "4")
        if (answer == correctAnswers[currentQuestionIndex]) {
            score++
            scoreTextView.text = "Score: $score"
            Toast.makeText(this, "Benar!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Salah!", Toast.LENGTH_SHORT).show()
        }
        currentQuestionIndex++
    }
}
