package net.uniquecomputer.houseguru
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.uniquecomputer.houseguru.databinding.ActivityBookingSuccessfullyBinding

class BookingSuccessfully : AppCompatActivity() {

  private lateinit var binding: ActivityBookingSuccessfullyBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingSuccessfullyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.show()
        val title = intent.getStringExtra("title")
        supportActionBar?.title = title

        val data = intent.getStringExtra("title")
        binding.servicename.text = data

        val orderid = intent.getStringExtra("orderid")
        binding.orderiddetails.text = orderid

        val date = intent.getStringExtra("date")
        val time = intent.getStringExtra("time")
        val image = intent.getIntExtra("image", 0)

        binding.datedetails.text = date ?: "-"
        binding.timedetails.text = formatTimeToAmPm(time)


        binding.done.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

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