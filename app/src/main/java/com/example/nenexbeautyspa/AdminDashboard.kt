package com.example.nenexbeautyspa

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout

class AdminDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView
    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingAdapter: BookingAdapter
    private var bookingsList = mutableListOf<Booking>()

    // Summary cards
    private lateinit var tvTotalBookings: TextView
    private lateinit var tvPendingBookings: TextView
    private lateinit var tvApprovedBookings: TextView

    private val db = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.adminNavView)
        bookingsRecyclerView = findViewById(R.id.rvBookings)
        tvTotalBookings = findViewById(R.id.tvTotalBookings)
        tvPendingBookings = findViewById(R.id.tvPendingBookings)
        tvApprovedBookings = findViewById(R.id.tvApprovedBookings)

        val toolbar: Toolbar = findViewById(R.id.adminToolbar)
        setSupportActionBar(toolbar)

        // Drawer toggle
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Navigation listener
        navView.setNavigationItemSelectedListener { menuItem ->
            handleNavigation(menuItem)
            true
        }

        // RecyclerView setup
        bookingsRecyclerView.layoutManager = LinearLayoutManager(this)
        bookingAdapter = BookingAdapter(bookingsList) { booking, action ->
            when (action) {
                "approve" -> updateBookingStatus(booking, "Approved")
                "decline" -> updateBookingStatus(booking, "Declined")
            }
        }
        bookingsRecyclerView.adapter = bookingAdapter

        // Load bookings from Firebase
        loadBookings()
    }

    private fun handleNavigation(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.navBookings -> bookingsRecyclerView.visibility = RecyclerView.VISIBLE
            R.id.navInventory -> Toast.makeText(this, "Inventory clicked", Toast.LENGTH_SHORT).show()
            R.id.navLogout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, Admin::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun loadBookings() {
        db.collection("bookings")
            .get()
            .addOnSuccessListener { result ->
                bookingsList.clear()
                for (doc in result) {
                    val booking = doc.toObject(Booking::class.java)
                    booking.id = doc.id
                    bookingsList.add(booking)
                }
                bookingAdapter.notifyDataSetChanged()
                updateSummaryCards()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load bookings", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateBookingStatus(booking: Booking, status: String) {
        db.collection("bookings").document(booking.id)
            .update("status", status)
            .addOnSuccessListener {
                booking.status = status
                bookingAdapter.notifyDataSetChanged()
                updateSummaryCards()
            }
    }

    private fun updateSummaryCards() {
        val total = bookingsList.size
        val pending = bookingsList.count { it.status == "Pending" }
        val approved = bookingsList.count { it.status == "Approved" }

        tvTotalBookings.text = total.toString()
        tvPendingBookings.text = pending.toString()
        tvApprovedBookings.text = approved.toString()
    }

    }
