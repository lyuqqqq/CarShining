package net.uniquecomputer.houseguru

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class AddAddressActivity : AppCompatActivity() {

    private lateinit var editLabel: EditText
    private lateinit var editPhone: EditText
    private lateinit var editAddress: EditText
    private lateinit var buttonSave: MaterialButton

    private lateinit var dbHelper: AppDatabaseHelper
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_address)

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val title = SpannableString("Add New Address").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, 0)
        }
        supportActionBar?.title = title

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.navigationIcon?.setTint(Color.WHITE)
        }
        toolbar.setNavigationOnClickListener { finish() }

        dbHelper = AppDatabaseHelper(this)
        val sessionPrefs = getSharedPreferences("user_session", MODE_PRIVATE)
        currentUserId = sessionPrefs.getInt("current_user_id", -1)

        editLabel = findViewById(R.id.editLabel)
        editPhone = findViewById(R.id.editPhone)
        editAddress = findViewById(R.id.editAddress)
        buttonSave = findViewById(R.id.buttonSaveAddress)

        buttonSave.setOnClickListener {
            val label = editLabel.text.toString().trim()
            val phone = editPhone.text.toString().trim()
            val address = editAddress.text.toString().trim()

            when {
                label.isEmpty() -> {
                    editLabel.error = "Label is required"
                    editLabel.requestFocus()
                }
                phone.isEmpty() -> {
                    editPhone.error = "Phone is required"
                    editPhone.requestFocus()
                }
                address.isEmpty() -> {
                    editAddress.error = "Address is required"
                    editAddress.requestFocus()
                }
                currentUserId == -1 -> {
                    Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val fullPhone = if (phone.startsWith("+60")) phone else "+60 $phone"

                    dbHelper.upsertCustomAddressForUser(
                        userId = currentUserId,
                        label = label,
                        phone = fullPhone,
                        detail = address
                    )

                    Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
