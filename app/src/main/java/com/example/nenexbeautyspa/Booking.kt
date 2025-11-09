package com.example.nenexbeautyspa

data class Booking(
    var id: String = "",
    var customerName: String = "",
    var serviceName: String = "",
    var dateTime: String = "",
    var status: String = "Pending"
)
