package net.uniquecomputer.houseguru

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class AddressActivity : AppCompatActivity() {

    private lateinit var cardAddress1: CardView
    private lateinit var cardAddress2: CardView
    private lateinit var cardAddressCustom: CardView

    private lateinit var radioAddress1: RadioButton
    private lateinit var radioAddress2: RadioButton
    private lateinit var radioAddressCustom: RadioButton

    private lateinit var textTitleCustom: TextView
    private lateinit var textPhoneCustom: TextView
    private lateinit var textAddressCustom: TextView

    private lateinit var textTitle1: TextView
    private lateinit var textPhone1: TextView
    private lateinit var textAddress1: TextView

    private lateinit var textTitle2: TextView
    private lateinit var textPhone2: TextView
    private lateinit var textAddress2: TextView

    private lateinit var imageDelete1: ImageView
    private lateinit var imageDelete2: ImageView
    private lateinit var imageDeleteCustom: ImageView

    private lateinit var buttonAddNewAddress: MaterialButton

    private lateinit var dbHelper: AppDatabaseHelper
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val title = SpannableString("Address").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, 0)
        }
        supportActionBar?.title = title

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.navigationIcon?.setTint(Color.WHITE)
        }
        toolbar.setNavigationOnClickListener { finish() }

        cardAddress1 = findViewById(R.id.cardAddress1)
        cardAddress2 = findViewById(R.id.cardAddress2)
        cardAddressCustom = findViewById(R.id.cardAddressCustom)

        radioAddress1 = findViewById(R.id.radioAddress1)
        radioAddress2 = findViewById(R.id.radioAddress2)
        radioAddressCustom = findViewById(R.id.radioAddressCustom)

        textTitleCustom = findViewById(R.id.textTitleCustom)
        textPhoneCustom = findViewById(R.id.textPhoneCustom)
        textAddressCustom = findViewById(R.id.textAddressCustom)

        textTitle1 = findViewById(R.id.textTitle1)
        textPhone1 = findViewById(R.id.textPhone1)
        textAddress1 = findViewById(R.id.textAddress1)

        textTitle2 = findViewById(R.id.textTitle2)
        textPhone2 = findViewById(R.id.textPhone2)
        textAddress2 = findViewById(R.id.textAddress2)

        imageDelete1 = findViewById(R.id.imageDelete1)
        imageDelete2 = findViewById(R.id.imageDelete2)
        imageDeleteCustom = findViewById(R.id.imageDeleteCustom)

        buttonAddNewAddress = findViewById(R.id.buttonAddNewAddress)

        dbHelper = AppDatabaseHelper(this)
        val sessionPrefs = getSharedPreferences("user_session", MODE_PRIVATE)
        currentUserId = sessionPrefs.getInt("current_user_id", -1)

        if (currentUserId != -1) {
            dbHelper.ensureFixedAddressesOnce(currentUserId)
            loadFixedAddresses()
        }

        val selectedColor = Color.parseColor("#E5F5FC")
        val unselectedColor = Color.WHITE

        fun selectFirst() {
            cardAddress1.setCardBackgroundColor(selectedColor)
            cardAddress2.setCardBackgroundColor(unselectedColor)
            cardAddressCustom.setCardBackgroundColor(unselectedColor)

            radioAddress1.isChecked = true
            radioAddress2.isChecked = false
            radioAddressCustom.isChecked = false
        }

        fun selectSecond() {
            cardAddress1.setCardBackgroundColor(unselectedColor)
            cardAddress2.setCardBackgroundColor(selectedColor)
            cardAddressCustom.setCardBackgroundColor(unselectedColor)

            radioAddress1.isChecked = false
            radioAddress2.isChecked = true
            radioAddressCustom.isChecked = false
        }

        fun selectCustom() {
            cardAddress1.setCardBackgroundColor(unselectedColor)
            cardAddress2.setCardBackgroundColor(unselectedColor)
            cardAddressCustom.setCardBackgroundColor(selectedColor)

            radioAddress1.isChecked = false
            radioAddress2.isChecked = false
            radioAddressCustom.isChecked = true
        }

        selectFirst()

        cardAddress1.setOnClickListener { selectFirst() }
        radioAddress1.setOnClickListener { selectFirst() }

        cardAddress2.setOnClickListener { selectSecond() }
        radioAddress2.setOnClickListener { selectSecond() }

        cardAddressCustom.setOnClickListener { selectCustom() }
        radioAddressCustom.setOnClickListener { selectCustom() }

        buttonAddNewAddress.setOnClickListener {
            val intent = android.content.Intent(this, AddAddressActivity::class.java)
            startActivity(intent)
        }

        imageDelete1.setOnClickListener {
            showDefaultAddressCannotDeleteDialog()
        }

        imageDelete2.setOnClickListener {
            showDefaultAddressCannotDeleteDialog()
        }

        imageDeleteCustom.setOnClickListener {
            showDeleteCustomAddressDialog()
        }

        loadCustomAddress()
    }

    override fun onResume() {
        super.onResume()
        loadFixedAddresses()
        loadCustomAddress()
    }

    private fun loadFixedAddresses() {
        if (currentUserId == -1) return

        val defaults = dbHelper.getDefaultAddressesForUser(currentUserId, 2)

        if (defaults.size >= 1) {
            textTitle1.text = defaults[0].label
            textPhone1.text = defaults[0].phone
            textAddress1.text = defaults[0].detail
        }

        if (defaults.size >= 2) {
            textTitle2.text = defaults[1].label
            textPhone2.text = defaults[1].phone
            textAddress2.text = defaults[1].detail
        }
    }

    private fun loadCustomAddress() {
        if (currentUserId == -1) {
            cardAddressCustom.visibility = View.GONE
            return
        }

        val custom = dbHelper.getCustomAddressForUser(currentUserId)
        if (custom != null) {
            cardAddressCustom.visibility = View.VISIBLE
            textTitleCustom.text = custom.label
            textPhoneCustom.text = custom.phone
            textAddressCustom.text = custom.detail
        } else {
            cardAddressCustom.visibility = View.GONE
        }
    }

    private fun showDefaultAddressCannotDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cannot delete")
            .setMessage("This is a default address and cannot be deleted.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteCustomAddressDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete address")
            .setMessage("Are you sure you want to delete this address?")
            .setPositiveButton("Delete") { _, _ ->
                if (currentUserId != -1) {
                    dbHelper.deleteCustomAddressForUser(currentUserId)
                }

                cardAddressCustom.visibility = View.GONE

                if (radioAddressCustom.isChecked) {
                    radioAddressCustom.isChecked = false
                    cardAddressCustom.setCardBackgroundColor(Color.WHITE)
                    cardAddress1.performClick()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
