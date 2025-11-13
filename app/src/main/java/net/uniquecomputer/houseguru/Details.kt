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

class Details : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    private val pickDateTime = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val date = data?.getStringExtra("date")
            val time = data?.getStringExtra("time")
            binding.setdata.text = "Date: $date \nTime: $time"
            binding.setdata.visibility = android.view.View.VISIBLE
            binding.button.text = "Change Date and Time"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.topAppBar.navigationIcon?.setTint(android.graphics.Color.WHITE)
        binding.topAppBar.setNavigationOnClickListener { finish() }

        supportActionBar?.show()
        val title = intent.getStringExtra("title")
        val bold = SpannableString(title ?: "")
        bold.setSpan(StyleSpan(Typeface.BOLD), 0, bold.length, 0)
        supportActionBar?.title = bold

        val date = intent.getStringExtra("date")
        val time = intent.getStringExtra("time")
        binding.setdata.text = "Date: $date \nTime: $time"

        if (date == null && time == null) {
            binding.setdata.visibility = android.view.View.GONE
        } else {
            binding.setdata.visibility = android.view.View.VISIBLE
            binding.button.text = "Change Date and Time"
        }

        binding.button.setOnClickListener {
            val intent = Intent(this, DateandTime::class.java)
            intent.putExtra("title", title)
            pickDateTime.launch(intent)
        }

        binding.continueDetails.setOnClickListener {
            if (binding.address.text.toString().length < 50) {
                binding.address.error = "Address is required"
                Toast.makeText(this, "Address Must Be 50 Character", Toast.LENGTH_SHORT).show()
                binding.address.requestFocus()
            } else if (binding.name.text.toString().isEmpty()) {
                binding.name.error = "Name is required"
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                binding.name.requestFocus()
            } else if (binding.number.text.toString().length < 10) {
                binding.number.error = "Number is required"
                Toast.makeText(this, "Number Must Be 10 Character", Toast.LENGTH_SHORT).show()
                binding.number.requestFocus()
            } else {
                val orderid = UUID.randomUUID().toString().substring(0, 18)
                val image = intent.getIntExtra("image", 0)
                val intent = Intent(this, BookingSuccessfully::class.java)
                intent.putExtra("title", title)
                intent.putExtra("date", date)
                intent.putExtra("image", image)
                intent.putExtra("orderid", orderid)
                startActivity(intent)
                finish()
            }
        }
    }
}
