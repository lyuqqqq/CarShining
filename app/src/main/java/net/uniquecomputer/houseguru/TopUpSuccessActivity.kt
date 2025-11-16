package net.uniquecomputer.houseguru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.uniquecomputer.houseguru.databinding.ActivityTopUpSuccessBinding

class TopUpSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTopUpSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTopUpSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val addedAmount = intent.getIntExtra("added_amount", 0)
        val newBalance = intent.getIntExtra("new_balance", 0)

        binding.textAddedAmount.text = "Added: RM $addedAmount"
        binding.textNewBalance.text = "New balance: RM $newBalance"

        binding.buttonDone.setOnClickListener {
            finish()
        }
    }
}
