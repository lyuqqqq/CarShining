package net.uniquecomputer.houseguru

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.appbar.MaterialToolbar

class AddressActivity : AppCompatActivity() {

    private lateinit var cardAddress1: CardView
    private lateinit var cardAddress2: CardView
    private lateinit var radioAddress1: RadioButton
    private lateinit var radioAddress2: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val title = SpannableString("Address").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, 0)
        }
        supportActionBar?.title = title

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener { finish() }

        cardAddress1 = findViewById(R.id.cardAddress1)
        cardAddress2 = findViewById(R.id.cardAddress2)
        radioAddress1 = findViewById(R.id.radioAddress1)
        radioAddress2 = findViewById(R.id.radioAddress2)

        val selectedColor = Color.parseColor("#E5F5FC")
        val unselectedColor = Color.WHITE

        fun selectFirst() {
            cardAddress1.setCardBackgroundColor(selectedColor)
            cardAddress2.setCardBackgroundColor(unselectedColor)
            radioAddress1.isChecked = true
            radioAddress2.isChecked = false
        }

        fun selectSecond() {
            cardAddress1.setCardBackgroundColor(unselectedColor)
            cardAddress2.setCardBackgroundColor(selectedColor)
            radioAddress1.isChecked = false
            radioAddress2.isChecked = true
        }

        selectFirst()

        cardAddress1.setOnClickListener { selectFirst() }
        radioAddress1.setOnClickListener { selectFirst() }

        cardAddress2.setOnClickListener { selectSecond() }
        radioAddress2.setOnClickListener { selectSecond() }
    }
}
