package net.uniquecomputer.houseguru

import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import net.uniquecomputer.houseguru.databinding.ActivityDetailsBinding
import java.util.UUID
import android.text.SpannableString
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.View
import android.graphics.Color

class Details : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    private var selectedDate: String? = null
    private var selectedTime: String? = null

    private val pickDateTime = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            selectedDate = data?.getStringExtra("date")
            selectedTime = data?.getStringExtra("time")
            updateDateTimeUi()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.topAppBar.navigationIcon?.setTint(Color.WHITE)
        binding.topAppBar.setNavigationOnClickListener { finish() }
        supportActionBar?.show()

        val title = intent.getStringExtra("title")
        val bold = SpannableString(title ?: "")
        bold.setSpan(StyleSpan(Typeface.BOLD), 0, bold.length, 0)
        supportActionBar?.title = bold

        selectedDate = intent.getStringExtra("date")
        selectedTime = intent.getStringExtra("time")
        updateDateTimeUi()

        binding.button.setOnClickListener {
            val intent = Intent(this, DateandTime::class.java)
            intent.putExtra("title", title)
            pickDateTime.launch(intent)
        }

        binding.continueDetails.setOnClickListener {
            when {
                binding.address.text.toString().isBlank() -> {
                    binding.address.error = "Address is required"
                    Toast.makeText(this, "Address is required", Toast.LENGTH_SHORT).show()
                    binding.address.requestFocus()
                }
                binding.name.text.toString().isEmpty() -> {
                    binding.name.error = "Name is required"
                    Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                    binding.name.requestFocus()
                }
                binding.number.text.toString().length != 9 -> {
                    binding.number.error = "Number is required"
                    Toast.makeText(this, "Number Must Be 9 Character", Toast.LENGTH_SHORT).show()
                    binding.number.requestFocus()
                }
                else -> {
                    val orderid = UUID.randomUUID().toString().substring(0, 18)
                    val image = intent.getIntExtra("image", 0)
                    val successIntent = Intent(this, BookingSuccessfully::class.java)
                    successIntent.putExtra("title", title)
                    successIntent.putExtra("date", selectedDate)
                    successIntent.putExtra("time", selectedTime)
                    successIntent.putExtra("image", image)
                    successIntent.putExtra("orderid", orderid)
                    startActivity(successIntent)
                    finish()
                }
            }
        }
    }

    private fun updateDateTimeUi() {
        val date = selectedDate
        val time = selectedTime

        if (date.isNullOrEmpty() && time.isNullOrEmpty()) {
            binding.setdata.visibility = View.GONE
            binding.button.text = "Select Date and Time"
        } else {
            binding.setdata.visibility = View.VISIBLE
            binding.setdata.text = "Date: $date\nTime: $time"
            binding.button.text = "Change Date and Time"
        }
    }
}
