package net.uniquecomputer.houseguru

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.os.Bundle
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class Wallet : AppCompatActivity() {

    private lateinit var balanceTextView: TextView
    private lateinit var amountEditText: EditText
    private lateinit var addMoneyButton: Button

    private lateinit var dbHelper: AppDatabaseHelper
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallet)

        // ---------- Top App Bar ----------
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val title = SpannableString("Wallet").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, 0)
        }
        supportActionBar?.title = title

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener { finish() }

        dbHelper = AppDatabaseHelper(this)

        val sessionPrefs = getSharedPreferences("user_session", MODE_PRIVATE)
        currentUserId = sessionPrefs.getInt("current_user_id", -1)

        val userPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val userName = userPref.getString("user_name", "Wallet")
        val titleView = findViewById<TextView>(R.id.textWalletTitle)
        titleView.text = userName

        balanceTextView = findViewById(R.id.textView10)
        amountEditText = findViewById(R.id.editTextNumber)
        addMoneyButton = findViewById(R.id.buttonAddMoney)

        val currentBalance = loadBalanceFromDb()
        balanceTextView.text = "RM $currentBalance"

        addMoneyButton.setOnClickListener {
            val input = amountEditText.text.toString().trim()

            if (input.isBlank()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val amount = input.toIntOrNull()
            if (amount == null || amount <= 0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (currentUserId == -1) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val current = loadBalanceFromDb()
            val newBalance = current + amount

            dbHelper.updateWalletBalance(currentUserId, newBalance)

            val intent = Intent(this, TopUpSuccessActivity::class.java).apply {
                putExtra("added_amount", amount)
                putExtra("new_balance", newBalance)
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        if (currentUserId != -1 && ::balanceTextView.isInitialized) {
            val currentBalance = loadBalanceFromDb()
            balanceTextView.text = "RM $currentBalance"
        }

        if (::amountEditText.isInitialized) {
            amountEditText.text?.clear()
        }
    }

    private fun loadBalanceFromDb(): Int {
        if (currentUserId == -1) return 0
        return dbHelper.getWalletBalance(currentUserId)
    }
}
