package net.uniquecomputer.houseguru

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.uniquecomputer.houseguru.databinding.ActivityCustomerServiceBinding

class CustomerServiceActivity : AppCompatActivity() {

    companion object {
        private const val CUSTOMER_SERVICE_NUMBER = "+60 12-345 6789"
    }

    private lateinit var binding: ActivityCustomerServiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)

        val title = SpannableString("Customer Service").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, 0)
        }
        supportActionBar?.title = title

        binding.topAppBar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.topAppBar.navigationIcon?.setTint(Color.WHITE)
        binding.topAppBar.setNavigationOnClickListener { finish() }

        binding.buttonStartOnlineChat.setOnClickListener {
            val cleanedNumber = CUSTOMER_SERVICE_NUMBER
                .replace(" ", "")
                .replace("-", "")

            val smsUri = Uri.parse("smsto:$cleanedNumber")
            val intent = Intent(Intent.ACTION_SENDTO, smsUri)

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonCallSupport.setOnClickListener {
            dialCustomerService()
        }
    }

    private fun dialCustomerService() {
        val cleanedNumber = CUSTOMER_SERVICE_NUMBER
            .replace(" ", "")
            .replace("-", "")

        val uri = Uri.parse("tel:$cleanedNumber")
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = uri
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open dialer app", Toast.LENGTH_SHORT).show()
        }
    }
}
