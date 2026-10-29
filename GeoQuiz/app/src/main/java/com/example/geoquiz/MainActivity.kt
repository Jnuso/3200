package com.example.geoquiz

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.example.geoquiz.databinding.ActivityMainBinding
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val quizViewModel : QuizViewModel by viewModels()


    private val cheatLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            quizViewModel.isCheater =
                result.data?.getBooleanExtra(EXTRA_ANSWER_SHOWN, false) ?: false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Got a QuizViewModel: $quizViewModel")

        binding.questionTextView.setOnClickListener {
            quizViewModel.moveToNext()
            updateQuestion()
        }

        binding.trueButton.setOnClickListener { view ->
            checkAnswer(true)
        }

        binding.falseButton.setOnClickListener { view ->
            checkAnswer(false)
        }

        binding.nextButton.setOnClickListener { view ->
            quizViewModel.moveToNext()
            updateQuestion()

        }

        binding.lastButton.setOnClickListener {
            quizViewModel.moveToLast()
            updateQuestion()
        }

        binding.cheatButton.setOnClickListener{
            //start cheat activity
            val AnswerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this, AnswerIsTrue)
            cheatLauncher.launch(intent)


        }
        //get start question state
        updateQuestion()


    }

    override fun onStart(){
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }


    private fun updateQuestion(){
        val questionTextResId = quizViewModel.currentQuestionText
        binding.questionTextView.setText(questionTextResId)
    }

    private fun checkAnswer(userAnswer : Boolean){
        val correctAnswer = quizViewModel.currentQuestionAnswer

        val messageResId = when {
            quizViewModel.isCheater -> R.string.judgment_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
    }

}
