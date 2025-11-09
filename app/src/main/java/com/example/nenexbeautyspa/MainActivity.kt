package com.example.nenexbeautyspa

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    // Views
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var loginBtn: Button
    private lateinit var createBtn: Button
    private lateinit var textForgotPassword: TextView
    private lateinit var textLoginAsAdmin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // find views
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        loginBtn = findViewById(R.id.loginbtn)
        createBtn = findViewById(R.id.createBtn)
        textForgotPassword = findViewById(R.id.textForgotPassword)
        textLoginAsAdmin = findViewById(R.id.textLoginAsAdmin)

        Log.d(TAG, "views -> email:${editTextEmail != null} pwd:${editTextPassword != null} login:${loginBtn != null} signup:${createBtn!= null}")

        // Login click
        loginBtn.setOnClickListener {
            performLogin()
        }

        // Sign-up button opens the SignUp activity
        createBtn.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        // Forgot password click
        textForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        // Login as Admin click
        textLoginAsAdmin.setOnClickListener {
            // Open AdminLogin activity
            startActivity(Intent(this, Admin::class.java))
        }
    }

    private fun performLogin() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString()

        // Validate fields
        if (email.isEmpty()) {
            editTextEmail.error = "Email cannot be empty"
            editTextEmail.requestFocus()
            return
        }

        // Check for "@" symbol explicitly (your earlier request)
        if (!email.contains("@")) {
            editTextEmail.error = "Email must contain '@'"
            editTextEmail.requestFocus()
            return
        }

        if (!isValidEmail(email)) {
            editTextEmail.error = "Invalid email format"
            editTextEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            editTextPassword.error = "Password cannot be empty"
            editTextPassword.requestFocus()
            return
        }

        // Disable button while signing in
        loginBtn.isEnabled = false

        // Firebase sign in
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                loginBtn.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    // Navigate to Dashboard (ensure DashBoard exists)
                    startActivity(Intent(this, DashBoard::class.java))
                    finish()
                } else {
                    val msg = task.exception?.localizedMessage ?: "Authentication failed"
                    Toast.makeText(this, "Login failed: $msg", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "signIn failed", task.exception)
                }
            }
            .addOnFailureListener { e ->
                loginBtn.isEnabled = true
                Toast.makeText(this, "Sign-in error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "signInWithEmailAndPassword failure", e)
            }
    }

    private fun showForgotPasswordDialog() {
        val existingEmail = editTextEmail.text.toString().trim()

        if (existingEmail.isNotEmpty() && isValidEmail(existingEmail)) {
            // ask for confirmation to send reset to existing email in field
            AlertDialog.Builder(this)
                .setTitle("Reset password")
                .setMessage("Send password reset email to $existingEmail ?")
                .setPositiveButton("Send") { _, _ ->
                    sendPasswordReset(existingEmail)
                }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }

        // If there is no valid email in the field, prompt user to enter one
        val input = EditText(this).apply {
            hint = "Enter your email"
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle("Forgot password")
            .setView(input)
            .setPositiveButton("Send") { _, _ ->
                val email = input.text.toString().trim()
                if (email.isEmpty()) {
                    Toast.makeText(this, "Please enter an email", Toast.LENGTH_SHORT).show()
                } else if (!isValidEmail(email)) {
                    Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
                } else {
                    sendPasswordReset(email)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordReset(email: String) {
        // disable login button while sending (small UX improvement)
        loginBtn.isEnabled = false
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                loginBtn.isEnabled = true
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                } else {
                    val msg = task.exception?.localizedMessage ?: "Failed to send reset email"
                    Toast.makeText(this, "Error: $msg", Toast.LENGTH_LONG).show()
                    Log.e(TAG, "sendPasswordResetEmail failed", task.exception)
                }
            }
            .addOnFailureListener { e ->
                loginBtn.isEnabled = true
                Toast.makeText(this, "Error sending reset: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "sendPasswordResetEmail onFailure", e)
            }
    }

    // Email validation using Android built-in pattern
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
