package com.example.nenexbeautyspa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingAdapter(
    private val bookings: List<Booking>,
    private val listener: (Booking, String) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bind(booking)
    }

    override fun getItemCount(): Int = bookings.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCustomer: TextView = itemView.findViewById(R.id.tvCustomerName)
        private val tvService: TextView = itemView.findViewById(R.id.tvServiceName)
        private val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        private val btnDecline: Button = itemView.findViewById(R.id.btnDecline)

        fun bind(booking: Booking) {
            tvCustomer.text = booking.customerName
            tvService.text = booking.serviceName
            tvDateTime.text = booking.dateTime
            tvStatus.text = booking.status

            if (booking.status != "Pending") {
                btnApprove.visibility = View.GONE
                btnDecline.visibility = View.GONE
            } else {
                btnApprove.visibility = View.VISIBLE
                btnDecline.visibility = View.VISIBLE
            }

            btnApprove.setOnClickListener { listener(booking, "approve") }
            btnDecline.setOnClickListener { listener(booking, "decline") }
        }
    }
}
