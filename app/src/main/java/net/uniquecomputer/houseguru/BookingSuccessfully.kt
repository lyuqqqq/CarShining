package net.uniquecomputer.houseguru

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.uniquecomputer.houseguru.databinding.ActivityBookingSuccessfullyBinding
import java.text.SimpleDateFormat
import java.util.Locale

class BookingSuccessfully : AppCompatActivity() {

    private lateinit var binding: ActivityBookingSuccessfullyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingSuccessfullyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.show()

        val title = intent.getStringExtra("title")
        supportActionBar?.title = title

        val serviceName = title ?: ""
        binding.servicename.text = serviceName

        val orderid = intent.getStringExtra("orderid")
        binding.orderiddetails.text = orderid

        val rawDate = intent.getStringExtra("date")
        val rawTime = intent.getStringExtra("time")
        val image = intent.getIntExtra("image", 0)

        val rawPrice = intent.getStringExtra("price") ?: ""

        val price = when {
            rawPrice.isNotBlank() && rawPrice.startsWith("RM") -> rawPrice
            rawPrice.isNotBlank() -> "RM$rawPrice"
            serviceName.contains("Discount", ignoreCase = true) -> "RM19"
            serviceName.contains("Premium", ignoreCase = true) -> "RM89"
            serviceName.contains("Paint", ignoreCase = true) -> "RM39"
            serviceName.contains("Wax", ignoreCase = true) -> "RM49"
            serviceName.contains("Disinfection", ignoreCase = true) -> "RM29"
            serviceName.contains("Brand", ignoreCase = true) -> "RM69"
            else -> "RM89"
        }

        val displayDate = formatDateToDisplay(rawDate)
        val displayTime = formatTimeToAmPm(rawTime)

        binding.datedetails.text = if (displayDate.isBlank()) "-" else displayDate
        binding.timedetails.text = displayTime
        binding.pricedetails.text = price

        val sharedPref = getSharedPreferences("booking_prefs", MODE_PRIVATE)

        val count = sharedPref.getInt("incomplete_count", 0)
        val index = count

        sharedPref.edit().apply {
            putInt("incomplete_count", count + 1)

            putString("service_name_$index", serviceName)
            putString("date_$index", displayDate)
            putString("time_$index", displayTime)
            putString("price_$index", price)
            putInt("imageRes_$index", image)

            apply()
        }

        binding.done.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }

    private fun formatDateToDisplay(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return ""

        val possiblePatterns = listOf(
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "EEE, dd MMM"
        )

        for (pattern in possiblePatterns) {
            try {
                val inputFormat = SimpleDateFormat(pattern, Locale.getDefault())
                val parsed = inputFormat.parse(dateStr)
                if (parsed != null) {
                    val outputFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
                    return outputFormat.format(parsed)
                }
            } catch (_: Exception) {

            }
        }

        return dateStr
    }

    private fun formatTimeToAmPm(time: String?): String {
        if (time.isNullOrBlank()) return "-"

        return try {
            val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(time)
            if (date != null) outputFormat.format(date) else time
        } catch (e: Exception) {
            time
        }
    }
}
