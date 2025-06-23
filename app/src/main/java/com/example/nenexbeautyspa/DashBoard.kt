package com.example.nenexbeautyspa

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DashBoard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dash_board)

        val cardBookManicure = findViewById<CardView>(R.id.cardBookManicure)
        val cardBookPedicure = findViewById<CardView>(R.id.cardBookPedicure)
        val cardHaircut = findViewById<CardView>(R.id.cardHaircut)
        val cardBraids = findViewById<CardView>(R.id.cardBraids)
        val cardMyAppointments = findViewById<CardView>(R.id.cardMyAppointments)
        val cardProfile = findViewById<CardView>(R.id.cardProfile)


        cardBookManicure.setOnClickListener {
            val intent = Intent(this, Manicure::class.java)
            startActivity(intent)
        }
        cardBookPedicure.setOnClickListener{
            val intent = Intent (this,Pedicure::class.java)
            startActivity(intent)
        }
        cardHaircut.setOnClickListener {
            val intent = Intent(this, Haircut::class.java)
            startActivity(intent)
        }

        cardBraids.setOnClickListener {
            val intent = Intent(this, Braids::class.java)
            startActivity(intent)
        }

        //cardAppointments.setOnClickListener {
           // val intent = Intent(this, MyAppointmentsActivity::class.java)
         //   startActivity(intent)
       // }

       // cardProfile.setOnClickListener {
         //   val intent = Intent(this, ProfileActivity::class.java)
           // startActivity(intent)
        //}
    }

}