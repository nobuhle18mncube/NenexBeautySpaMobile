package com.example.nenexbeautyspa

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val editTextEmail: EditText = findViewById(R.id.editTextEmail)
        val editTextPassword: EditText = findViewById(R.id.editTextPassword)

        // Login Button
        val loginbtn: Button = findViewById(R.id.loginbtn)
        loginbtn.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            // Validate fields
            if (email.isEmpty()) {
                editTextEmail.error = "Email cannot be empty"
                return@setOnClickListener
            }

            // Check for "@" symbol explicitly
            if (!email.contains("@")) {
                editTextEmail.error = "Email must contain '@'"
                return@setOnClickListener
            }

            if (!isValidEmail(email)) {
                editTextEmail.error = "Invalid email format"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                editTextPassword.error = "Password cannot be empty"
                return@setOnClickListener
            }

            // If email and password are valid, proceed to the next activity (Dashboard)
            val intent = Intent(this,DashBoard::class.java)   //  direct to Dashboard
            startActivity(intent)


        }
        // signing up to the account Button
        val signUpBtn: Button = findViewById(R.id.signUpBtn)
        signUpBtn.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }

        // Function to validate email format


    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}