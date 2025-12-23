package net.uniquecomputer.houseguru

import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import net.uniquecomputer.houseguru.databinding.ActivityDetailsBinding
import java.text.SimpleDateFormat
import java.util.Locale
import android.text.SpannableString
import android.text.style.StyleSpan
import android.graphics.Typeface
import android.view.View

class Details : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    private var selectedDate: String? = null
    private var selectedTime: String? = null

    private var editingServiceId: Int = -1
    private var isEditMode: Boolean = false

    private lateinit var dbHelper: AppDatabaseHelper

    private var serviceTitle: String = ""
    private var servicePrice: String = ""
    private var serviceImageRes: Int = 0

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

    private val pickAddress = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult

            val title = data.getStringExtra("title") ?: ""
            val phone = data.getStringExtra("phone") ?: ""
            val address = data.getStringExtra("address") ?: ""

            binding.radioAddress2.isChecked = true
            binding.radioAddress1.isChecked = false
            updateAddressCardStyles()

            if (title.isNotBlank()) binding.textTitle2.text = title
            if (phone.isNotBlank()) binding.textPhone2.text = phone
            if (address.isNotBlank()) binding.textAddress2.text = address
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = AppDatabaseHelper(this)

        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.topAppBar.navigationIcon?.setTint(Color.WHITE)
        binding.topAppBar.setNavigationOnClickListener { finish() }
        supportActionBar?.show()

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

        binding.textAddAddress.setOnClickListener {
            val i = Intent(this, AddressActivity::class.java)
            pickAddress.launch(i)
        }

        binding.buttonAddMoneyWallet.setOnClickListener {
            startActivity(Intent(this, Wallet::class.java))
        }

        val sessionPrefs = getSharedPreferences("user_session", MODE_PRIVATE)
        currentUserId = sessionPrefs.getInt("current_user_id", -1)

        editingServiceId = intent.getIntExtra("service_id", -1)
        val mode = intent.getStringExtra("mode")
        isEditMode = (mode == "edit" && editingServiceId != -1)

        if (isEditMode) {
            val service = dbHelper.getServiceById(editingServiceId)
            if (service == null) {
                Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            serviceTitle = service.title
            servicePrice = service.price
            serviceImageRes = service.imageRes

            val bold = SpannableString(serviceTitle)
            bold.setSpan(StyleSpan(Typeface.BOLD), 0, bold.length, 0)
            supportActionBar?.title = bold

            selectedDate = service.date
            selectedTime = service.time
            updateDateTimeUi()

            binding.buttonAddMoneyWallet.visibility = View.GONE
            binding.continueDetails.text = "Save Changes"

            loadContactInfo()
        } else {
            val title = intent.getStringExtra("title") ?: ""
            serviceTitle = title
            servicePrice = intent.getStringExtra("price") ?: ""
            serviceImageRes = intent.getIntExtra("image", 0)

            val bold = SpannableString(serviceTitle)
            bold.setSpan(StyleSpan(Typeface.BOLD), 0, bold.length, 0)
            supportActionBar?.title = bold

            selectedDate = intent.getStringExtra("date")
            selectedTime = intent.getStringExtra("time")
            updateDateTimeUi()

            binding.name.setText("")
            binding.number.setText("")
        }

        if (servicePrice.isNotBlank()) {
            binding.textPriceLabel.visibility = View.VISIBLE
            binding.textPriceValue.visibility = View.VISIBLE
            binding.textPriceValue.text = servicePrice
        } else {
            binding.textPriceLabel.visibility = View.GONE
            binding.textPriceValue.visibility = View.GONE
        }

        binding.button.setOnClickListener {
            val dtIntent = Intent(this, DateandTime::class.java).apply {
                putExtra("title", serviceTitle)
            }
            pickDateTime.launch(dtIntent)
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
                    if (isEditMode && editingServiceId != -1) {
                        saveContactInfo()

                        val newDate = selectedDate ?: ""
                        val newTime = selectedTime ?: ""

                        val rows = dbHelper.updateServiceDateTime(editingServiceId, newDate, newTime)
                        if (rows <= 0) {
                            Toast.makeText(this, "Update failed (rows=$rows)", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        setResult(RESULT_OK)
                        finish()
                        return@setOnClickListener
                    }

                    if (currentUserId == -1) {
                        Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val finalPriceStr = normalizedPriceString(serviceTitle, servicePrice)
                    val needPay = priceToInt(finalPriceStr)

                    val balance = dbHelper.getWalletBalance(currentUserId)
                    if (needPay <= 0) {
                        Toast.makeText(this, "Invalid price", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (balance < needPay) {
                        Toast.makeText(
                            this,
                            "Insufficient wallet balance. Please top up your wallet.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    dbHelper.updateWalletBalance(currentUserId, balance - needPay)

                    val orderId = "HS" + System.currentTimeMillis()
                    val addressIndex = if (binding.radioAddress1.isChecked) 1 else 2

                    val intent = Intent(this, BookingSuccessfully::class.java).apply {
                        putExtra("title", serviceTitle)
                        putExtra("date", selectedDate ?: "")
                        putExtra("time", selectedTime ?: "")
                        putExtra("image", serviceImageRes)
                        putExtra("price", finalPriceStr)

                        putExtra("contact_name", binding.name.text.toString().trim())
                        putExtra("contact_number", binding.number.text.toString().trim())
                        putExtra("address_index", addressIndex)
                        putExtra("orderid", orderId)
                    }

                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun loadContactInfo() {
        if (!isEditMode || editingServiceId == -1) return

        val prefs = getSharedPreferences("details_prefs", MODE_PRIVATE)
        val nameKey = "contact_name_${editingServiceId}"
        val numberKey = "contact_number_${editingServiceId}"
        val addrKey = "address_index_${editingServiceId}"

        val savedName = prefs.getString(nameKey, "")
        val savedNumber = prefs.getString(numberKey, "")
        val savedAddressIndex = prefs.getInt(addrKey, 1)

        if (!savedName.isNullOrEmpty()) binding.name.setText(savedName)
        if (!savedNumber.isNullOrEmpty()) binding.number.setText(savedNumber)

        if (savedAddressIndex == 1) {
            binding.radioAddress1.isChecked = true
            binding.radioAddress2.isChecked = false
        } else {
            binding.radioAddress1.isChecked = false
            binding.radioAddress2.isChecked = true
        }
        updateAddressCardStyles()
    }

    private fun saveContactInfo() {
        if (!isEditMode || editingServiceId == -1) return

        val prefs = getSharedPreferences("details_prefs", MODE_PRIVATE)
        val nameKey = "contact_name_${editingServiceId}"
        val numberKey = "contact_number_${editingServiceId}"
        val addrKey = "address_index_${editingServiceId}"
        val selectedAddressIndex = if (binding.radioAddress1.isChecked) 1 else 2

        prefs.edit()
            .putString(nameKey, binding.name.text.toString())
            .putString(numberKey, binding.number.text.toString())
            .putInt(addrKey, selectedAddressIndex)
            .commit()
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

    private fun to12Hour(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val s = raw.trim()
        if (s.contains("AM", true) || s.contains("PM", true)) {
            return s.replace(" ", "").lowercase(Locale.getDefault())
        }
        val inFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val outFmt = SimpleDateFormat("h:mma", Locale.getDefault())
        return try {
            val d = inFmt.parse(s)
            outFmt.format(d!!).lowercase(Locale.getDefault())
        } catch (e: Exception) {
            s
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
            binding.setdata.text = "Date: $date\nTime: ${to12Hour(time)}"
            binding.button.text = "Change Date and Time"
        }
    }

    private var currentUserId: Int = -1

    private fun normalizedPriceString(title: String, rawPrice: String): String {
        val p = rawPrice.trim()
        if (p.isNotBlank()) return if (p.startsWith("RM", true)) p else "RM$p"

        return when {
            title.contains("Discount", true) -> "RM19"
            title.contains("Premium", true) -> "RM89"
            title.contains("Paint", true) -> "RM39"
            title.contains("Wax", true) -> "RM49"
            title.contains("Disinfection", true) -> "RM29"
            title.contains("Brand", true) -> "RM69"
            else -> "RM89"
        }
    }

    private fun priceToInt(priceStr: String): Int {
        val num = Regex("""\d+""").find(priceStr)?.value
        return num?.toIntOrNull() ?: 0
    }

}
