package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Button // Assuming you have a Button to trigger generation
import android.widget.TextView // Assuming you have a TextView to display the result
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope // For lifecycle-aware coroutines
import androidx.privacysandbox.tools.core.generator.build
import com.google.firebase.FirebaseApp // Import for FirebaseApp initialization check
import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.client.generativeai.GenerativeBackend
import com.google.firebase.ai.client.generativeai.GenerativeModel
import com.google.firebase.ai.client.generativeai.type.Content
import com.google.firebase.ai.client.generativeai.type.GenerateContentResponse
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var generativeModel: GenerativeModel
    private lateinit var resultTextView: TextView // Example TextView
    private lateinit var generateButton: Button // Example Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main) // Make sure this layout has a Button and TextView

        // Initialize Firebase App (good practice, though often handled by FirebaseInitProvider)
        FirebaseApp.initializeApp(this)

        // Initialize Views (assuming you have them in your R.layout.activity_main)
        // Replace with your actual view IDs if using ViewBinding or direct findViewById
        resultTextView = findViewById(R.id.resultTextView) // Example ID
        generateButton = findViewById(R.id.generateButton)   // Example ID

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase AI
        initializeFirebaseAI()

        // Set up a button click listener to generate content
        generateButton.setOnClickListener {
            generateStoryAboutMagicBackpack()
        }
    }

    private fun initializeFirebaseAI() {
        try {
            generativeModel = FirebaseAI.getInstance(GenerativeBackend.googleAI())
                .generativeModel("gemini-1.5-flash") // Or your preferred model
            Log.d("MainActivity", "Firebase AI Model Initialized")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error initializing Firebase AI: ${e.localizedMessage}", e)
            resultTextView.text = "Error initializing AI Model: ${e.localizedMessage}"
        }
    }

    private fun generateStoryAboutMagicBackpack() {
        if (!::generativeModel.isInitialized) {
            resultTextView.text = "AI Model not initialized. Please try again."
            Log.e("MainActivity", "generativeModel not initialized before calling generateStory")
            return
        }

        val promptText = "Write a short story about a magic backpack."
        resultTextView.text = "Generating story..." // Provide feedback to the user

        lifecycleScope.launch { // Launch a coroutine in the Activity's lifecycle scope
            try {
                val prompt = Content.Builder()
                    .addText(promptText)
                    .build()

                val response: GenerateContentResponse = generativeModel.generateContent(prompt)
                val resultText = response.text

                if (resultText != null) {
                    Log.d("MainActivity", "Generated Story: $resultText")
                    this@MainActivity.resultTextView.text = resultText // Update UI
                } else {
                    Log.e("MainActivity", "Response text is null")
                    this@MainActivity.resultTextView.text = "Received an empty response."
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Error generating content: ${e.localizedMessage}", e)
                this@MainActivity.resultTextView.text = "Error: ${e.localizedMessage}"
            }
        }
    }
}