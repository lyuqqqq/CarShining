package net.uniquecomputer.houseguru

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.uniquecomputer.houseguru.databinding.ActivityDateandTimeBinding
import android.text.SpannableString
import android.text.style.StyleSpan

class DateandTime : AppCompatActivity() {

    private lateinit var binding: ActivityDateandTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDateandTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.topAppBar)
        val t = SpannableString("Selecte Date and Time")
        t.setSpan(StyleSpan(Typeface.BOLD), 0, t.length, 0)
        supportActionBar?.title = t
        binding.topAppBar.setNavigationIcon(R.drawable.ic_arrow_back)
        binding.topAppBar.setNavigationOnClickListener { finish() }

        val serviceTitle = intent.getStringExtra("title")

        binding.continueBtn.setOnClickListener {
            val y = binding.datePicker.year
            val m = binding.datePicker.month + 1
            val d = binding.datePicker.dayOfMonth

            val hour = if (android.os.Build.VERSION.SDK_INT >= 23)
                binding.timePicker.hour
            else
                @Suppress("DEPRECATION") binding.timePicker.currentHour

            val minute = if (android.os.Build.VERSION.SDK_INT >= 23)
                binding.timePicker.minute
            else
                @Suppress("DEPRECATION") binding.timePicker.currentMinute

            val date = "$d/$m/$y"
            val time = String.format("%02d:%02d", hour, minute)

            val data = Intent().apply {
                putExtra("date", date)
                putExtra("time", time)
                putExtra("title", serviceTitle)
            }
            setResult(RESULT_OK, data)
            finish()
        }
    }
}
