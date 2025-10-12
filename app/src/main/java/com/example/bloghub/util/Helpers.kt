package com.example.bloghub.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Helpers {
    fun formatDate(date: Date): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
}


