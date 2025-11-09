package com.example.nenexbeautyspa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InventoryAdapter(
    private val inventoryList: MutableList<InventoryItem>,
    private val onItemAction: (InventoryItem, String) -> Unit
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        val tvItemQuantity: TextView = itemView.findViewById(R.id.tvItemQuantity)
        val tvItemPrice: TextView = itemView.findViewById(R.id.tvItemPrice)
        val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        val btnDelete: Button = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = inventoryList[position]
        holder.tvItemName.text = item.name
        holder.tvItemQuantity.text = "Qty: ${item.quantity}"
        holder.tvItemPrice.text = "R${item.price}"

        holder.btnEdit.setOnClickListener {
            onItemAction(item, "edit")
        }

        holder.btnDelete.setOnClickListener {
            onItemAction(item, "delete")
        }
    }

    override fun getItemCount(): Int = inventoryList.size
}
