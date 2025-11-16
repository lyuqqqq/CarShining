package net.uniquecomputer.houseguru

import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import net.uniquecomputer.houseguru.databinding.ActivityDetailsBinding
import java.util.UUID
import android.text.SpannableString
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.View

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
        // 默认选中第一个地址
        binding.radioAddress1.isChecked = true
        binding.radioAddress2.isChecked = false
        updateAddressCardStyles()
        binding.cardAddress1.setOnClickListener {
            binding.radioAddress1.isChecked = true
            binding.radioAddress2.isChecked = false
            updateAddressCardStyles()
        }

        binding.radioAddress1.setOnClickListener {
            binding.radioAddress1.isChecked = true
            binding.radioAddress2.isChecked = false
            updateAddressCardStyles()
        }

        binding.cardAddress2.setOnClickListener {
            binding.radioAddress2.isChecked = true
            binding.radioAddress1.isChecked = false
            updateAddressCardStyles()
        }

        binding.radioAddress2.setOnClickListener {
            binding.radioAddress2.isChecked = true
            binding.radioAddress1.isChecked = false
            updateAddressCardStyles()
        }

        setContentView(binding.root)

        binding.buttonAddMoneyWallet.setOnClickListener {
            val intent = Intent(this, Wallet::class.java)
            startActivity(intent)
        }

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.topAppBar.navigationIcon?.setTint(Color.WHITE)
        binding.topAppBar.setNavigationOnClickListener { finish() }
        supportActionBar?.show()

        val title = intent.getStringExtra("title")
        val bold = SpannableString(title ?: "")
        bold.setSpan(StyleSpan(Typeface.BOLD), 0, bold.length, 0)
        supportActionBar?.title = bold

        val explicitPrice = intent.getStringExtra("price")

        val price = if (!explicitPrice.isNullOrBlank()) {
            explicitPrice
        } else {
            when {
                title?.contains("Discount", ignoreCase = true) == true -> "RM19"
                title?.contains("Premium", ignoreCase = true) == true -> "RM89"
                title?.contains("Paint", ignoreCase = true) == true -> "RM39"
                title?.contains("Wax", ignoreCase = true) == true -> "RM49"
                title?.contains("Disinfection", ignoreCase = true) == true -> "RM29"
                title?.contains("Brand", ignoreCase = true) == true -> "RM69"
                else -> ""
            }
        }

        if (price.isNotBlank()) {
            binding.textPriceLabel.visibility = View.VISIBLE
            binding.textPriceValue.visibility = View.VISIBLE
            binding.textPriceValue.text = price
        } else {
            binding.textPriceLabel.visibility = View.GONE
            binding.textPriceValue.visibility = View.GONE
        }

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
                selectedDate.isNullOrEmpty() || selectedTime.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show()
                }

                binding.name.text.toString().isEmpty() -> {
                    binding.name.error = "Name is required"
                    Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
                    binding.name.requestFocus()
                }

                binding.number.text.toString().length != 9 -> {
                    binding.number.error = "Number Must Be 9 Character"
                    Toast.makeText(this, "Number Must Be 9 Character", Toast.LENGTH_SHORT).show()
                    binding.number.requestFocus()
                }

                else -> {
                    val serviceName = title ?: ""
                    val servicePrice = when {
                        serviceName.contains("Discount", ignoreCase = true) -> 19
                        serviceName.contains("Premium", ignoreCase = true) -> 89
                        serviceName.contains("Paint", ignoreCase = true) -> 39
                        serviceName.contains("Wax", ignoreCase = true) -> 49
                        serviceName.contains("Disinfection", ignoreCase = true) -> 29
                        serviceName.contains("Brand", ignoreCase = true) -> 69
                        else -> 89
                    }

                    val walletPrefs = getSharedPreferences("wallet_prefs", MODE_PRIVATE)
                    val currentBalance = walletPrefs.getInt("wallet_balance", 0)

                    if (currentBalance < servicePrice) {
                        Toast.makeText(
                            this,
                            "Insufficient wallet balance. Please top up your wallet.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        walletPrefs.edit()
                            .putInt("wallet_balance", currentBalance - servicePrice)
                            .apply()

                        val orderid = UUID.randomUUID().toString().substring(0, 18)
                        val image = intent.getIntExtra("image", 0)
                        val title = intent.getStringExtra("title")

                        val successIntent = Intent(this, BookingSuccessfully::class.java).apply {
                            putExtra("title", title)
                            putExtra("date", selectedDate)
                            putExtra("time", selectedTime)
                            putExtra("image", image)
                            putExtra("orderid", orderid)
                            putExtra("price", "RM$servicePrice")
                        }
                        startActivity(successIntent)
                        finish()
                    }
                }
            }
        }
    }

    private fun updateAddressCardStyles() {
        val selectedColor = Color.parseColor("#E5F5FC")
        val normalColor = Color.WHITE

        binding.cardAddress1.setCardBackgroundColor(
            if (binding.radioAddress1.isChecked) selectedColor else normalColor
        )
        binding.cardAddress2.setCardBackgroundColor(
            if (binding.radioAddress2.isChecked) selectedColor else normalColor
        )
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
