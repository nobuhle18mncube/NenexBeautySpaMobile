package com.example.nenexbeautyspa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class Admin : AppCompatActivity() {

    private val TAG = "AdminLogin"
    private lateinit var roleSpinner: Spinner
    private lateinit var adminTokenInput: EditText
    private lateinit var loginBtn: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)

        // Ensure Firebase SDK is initialized before using Firestore
        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        // Optional: adapt padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        roleSpinner = findViewById(R.id.roleSpinner)
        adminTokenInput = findViewById(R.id.adminToken)
        loginBtn = findViewById(R.id.adminLoginBtn)

        // Load spinner items from strings.xml
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.admin_roles_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        // Handle login
        loginBtn.setOnClickListener {
            attemptAdminLogin()
        }
    }

    private fun attemptAdminLogin() {
        val selectedLabel = roleSpinner.selectedItem as String
        if (selectedLabel == "Select role") {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show()
            return
        }

        val enteredToken = adminTokenInput.text.toString().trim()
        if (enteredToken.isEmpty()) {
            adminTokenInput.error = "Please enter your admin token"
            adminTokenInput.requestFocus()
            return
        }

        loginBtn.isEnabled = false
        loginBtn.text = "Checking..."

        // Firestore fetch (reads the document admin_tokens/tokens)
        db.collection("admin_tokens")
            .document("tokens")
            .get()
            .addOnSuccessListener { doc ->
                loginBtn.isEnabled = true
                loginBtn.text = "Login as Admin"

                if (doc != null && doc.exists()) {
                    val roleKey = roleLabelToKey(selectedLabel)
                    val serverToken = doc.getString(roleKey)

                    if (serverToken == null) {
                        Toast.makeText(this, "No token configured for $selectedLabel", Toast.LENGTH_LONG).show()
                        Log.w(TAG, "Missing field for roleKey=$roleKey in admin_tokens/tokens")
                        return@addOnSuccessListener
                    }

                    if (enteredToken == serverToken) {
                        Toast.makeText(this, "Admin access granted: $selectedLabel", Toast.LENGTH_SHORT).show()
                        navigateToRoleDashboard(selectedLabel)
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid token for $selectedLabel", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Token data not found in Firestore", Toast.LENGTH_LONG).show()
                    Log.w(TAG, "admin_tokens/tokens doc missing")
                }
            }
            .addOnFailureListener { e ->
                loginBtn.isEnabled = true
                loginBtn.text = "Login as Admin"
                Toast.makeText(this, "Error checking token: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                Log.e(TAG, "Firestore get failed", e)
            }
    }

    private fun roleLabelToKey(label: String): String {
        return when (label) {
            "Hairstylist" -> "hairstylist"
            "Barber" -> "barber"
            "Nail Tech", "NailTech" -> "nailtech"
            else -> label.lowercase().replace(" ", "")
        }
    }

    private fun navigateToRoleDashboard(label: String) {
        val intent = Intent(this, AdminDashboard::class.java)
        intent.putExtra("adminRole", label) // pass role for dashboard use
        startActivity(intent)
    }
}
