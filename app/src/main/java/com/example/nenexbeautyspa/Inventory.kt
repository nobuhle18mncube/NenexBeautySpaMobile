package com.example.nenexbeautyspa

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.FirebaseFirestore

class Inventory : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var inventoryRecyclerView: RecyclerView
    private lateinit var inventoryAdapter: InventoryAdapter
    private val inventoryList = mutableListOf<InventoryItem>()

    private lateinit var tvTotalItems: TextView
    private lateinit var tvLowStock: TextView
    private lateinit var fabAdd: FloatingActionButton

    private val db = FirebaseFirestore.getInstance()
    private val collectionName = "inventory" // Firestore collection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inventory)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById<DrawerLayout>(R.id.inventoryDrawerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // find views (IDs must match activity_inventory.xml)
        drawerLayout = findViewById(R.id.inventoryDrawerLayout)
        navView = findViewById(R.id.inventoryNavView)
        inventoryRecyclerView = findViewById(R.id.rvInventory)
        tvTotalItems = findViewById(R.id.tvTotalItems)
        tvLowStock = findViewById(R.id.tvLowStock)

        // optional: add a FloatingActionButton in your activity_inventory.xml with id fabAdd
        // If you don't have it, you can create one or comment out the lines that reference it below.
        fabAdd = try {
            findViewById(R.id.fabAdd)
        } catch (e: Exception) {
            null as FloatingActionButton
        }

        val toolbar: Toolbar = findViewById(R.id.inventoryToolbar)
        setSupportActionBar(toolbar)

        // set up drawer toggle
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navBookings -> {
                    // implement navigation to bookings/admin dashboard if required
                    Toast.makeText(this, "Open Bookings (implement)", Toast.LENGTH_SHORT).show()
                }
                R.id.navInventory -> { /* already here */ }
                R.id.navLogout -> {
                    // implement logout behavior if necessary
                    Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // RecyclerView + adapter
        inventoryRecyclerView.layoutManager = LinearLayoutManager(this)
        inventoryAdapter = InventoryAdapter(inventoryList) { item, action ->
            when (action) {
                "edit" -> showAddEditDialog(item)
                "delete" -> confirmDelete(item)
            }
        }
        inventoryRecyclerView.adapter = inventoryAdapter

        // FAB Add (if present): show create dialog
        try {
            fabAdd.setOnClickListener {
                showAddEditDialog(null) // null -> create new
            }
        } catch (_: Exception) { /* no fab present */ }

        // load items from Firestore
        loadInventory()
    }

    // Load all inventory docs into inventoryList
    private fun loadInventory() {
        db.collection(collectionName)
            .get()
            .addOnSuccessListener { result ->
                inventoryList.clear()
                for (doc in result) {
                    val item = doc.toObject(InventoryItem::class.java)
                    item.id = doc.id
                    inventoryList.add(item)
                }
                inventoryAdapter.notifyDataSetChanged()
                updateSummary()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load inventory: ${e.message}", Toast.LENGTH_LONG).show()
                // optionally populate sample data for testing
            }
    }

    // Shows a dialog to create (if item==null) or edit (if item != null)
    private fun showAddEditDialog(item: InventoryItem?) {
        val isEdit = item != null
        val title = if (isEdit) "Edit Item" else "Add Item"

        // Build a simple dialog with EditTexts (no extra layout file required)
        val builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setPositiveButton(if (isEdit) "Save" else "Add", null)
            .setNegativeButton("Cancel", null)

        // Use a custom view for nicer layout
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_inventory, null)
        val etName = view.findViewById<EditText>(R.id.etName)
        val etQuantity = view.findViewById<EditText>(R.id.etQuantity)
        val etPrice = view.findViewById<EditText>(R.id.etPrice)
        val etCategory = view.findViewById<EditText>(R.id.etCategory)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)

        if (isEdit) {
            etName.setText(item!!.name)
            etQuantity.setText(item.quantity.toString())
            etPrice.setText(item.price.toString())
            etCategory.setText(item.category ?: "")
            etDescription.setText(item.description ?: "")
        }

        builder.setView(view)
        val dialog = builder.create()
        dialog.setOnShowListener {
            val positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positive.setOnClickListener {
                val name = etName.text.toString().trim()
                val qtyText = etQuantity.text.toString().trim()
                val priceText = etPrice.text.toString().trim()
                val category = etCategory.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (name.isEmpty()) {
                    etName.error = "Enter name"
                    return@setOnClickListener
                }
                val qty = qtyText.toIntOrNull() ?: 0
                val price = priceText.toDoubleOrNull() ?: 0.0

                if (isEdit) {
                    // update existing
                    val updated = item!!.copy(
                        name = name,
                        quantity = qty,
                        price = price,
                        category = category,
                        description = description
                    )
                    updateFirestoreItem(updated)
                    dialog.dismiss()
                } else {
                    // create new
                    val newItem = InventoryItem(
                        id = "",
                        name = name,
                        quantity = qty,
                        price = price,
                        category = category,
                        description = description,
                        lowStock = qty <= 5
                    )
                    createFirestoreItem(newItem)
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    // Firestore create
    private fun createFirestoreItem(item: InventoryItem) {
        val map = hashMapOf(
            "name" to item.name,
            "quantity" to item.quantity,
            "price" to item.price,
            "category" to item.category,
            "description" to item.description,
            "lowStock" to (item.quantity <= 5)
        )
        db.collection(collectionName)
            .add(map)
            .addOnSuccessListener { docRef ->
                // set returned id locally and add to list
                item.id = docRef.id
                inventoryList.add(0, item) // add top
                inventoryAdapter.notifyItemInserted(0)
                inventoryRecyclerView.scrollToPosition(0)
                updateSummary()
                Toast.makeText(this, "Item added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Add failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Firestore update
    private fun updateFirestoreItem(item: InventoryItem) {
        val map = hashMapOf(
            "name" to item.name,
            "quantity" to item.quantity,
            "price" to item.price,
            "category" to item.category,
            "description" to item.description,
            "lowStock" to (item.quantity <= 5)
        )
        if (item.id.isEmpty()) {
            Toast.makeText(this, "Invalid item id", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection(collectionName).document(item.id)
            .set(map)
            .addOnSuccessListener {
                // update local list
                val index = inventoryList.indexOfFirst { it.id == item.id }
                if (index >= 0) {
                    inventoryList[index] = item
                    inventoryAdapter.notifyItemChanged(index)
                } else {
                    // if not found insert
                    inventoryList.add(item)
                    inventoryAdapter.notifyDataSetChanged()
                }
                updateSummary()
                Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // Confirm delete
    private fun confirmDelete(item: InventoryItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete item")
            .setMessage("Are you sure you want to delete \"${item.name}\"?")
            .setPositiveButton("Delete") { _, _ -> deleteItem(item) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Firestore delete
    private fun deleteItem(item: InventoryItem) {
        if (item.id.isEmpty()) {
            Toast.makeText(this, "Item has no id", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection(collectionName).document(item.id)
            .delete()
            .addOnSuccessListener {
                val index = inventoryList.indexOfFirst { it.id == item.id }
                if (index >= 0) {
                    inventoryList.removeAt(index)
                    inventoryAdapter.notifyItemRemoved(index)
                } else {
                    inventoryAdapter.notifyDataSetChanged()
                }
                updateSummary()
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateSummary() {
        val total = inventoryList.size
        val lowStock = inventoryList.count { it.quantity <= 5 }
        tvTotalItems.text = total.toString()
        tvLowStock.text = lowStock.toString()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }

}