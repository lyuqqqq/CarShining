package net.uniquecomputer.houseguru

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.StyleSpan
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editPhone: EditText
    private lateinit var editEmail: EditText
    private lateinit var buttonSave: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        val title = SpannableString("Edit Profile").apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, length, 0)
        }
        supportActionBar?.title = title

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setNavigationOnClickListener { finish() }

        editName = findViewById(R.id.editName)
        editPhone = findViewById(R.id.editPhone)
        editEmail = findViewById(R.id.editEmail)
        buttonSave = findViewById(R.id.buttonSaveProfile)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val name = prefs.getString("user_name", prefs.getString("name", "")) ?: ""
        val email = prefs.getString("user_email", prefs.getString("email", "")) ?: ""
        val rawPhone = prefs.getString("user_phone", prefs.getString("phone", "")) ?: ""

        val displayPhone = rawPhone.removePrefix("+60").trim()

        editName.setText(name)
        editEmail.setText(email)
        editPhone.setText(displayPhone)

        buttonSave.setOnClickListener {
            val name = editName.text.toString().trim()
            val phone = editPhone.text.toString().trim()
            val email = editEmail.text.toString().trim()

            when {
                name.isEmpty() -> {
                    editName.error = "Name is required"
                    editName.requestFocus()
                }
                phone.isEmpty() -> {
                    editPhone.error = "Phone is required"
                    editPhone.requestFocus()
                }
                email.isEmpty() -> {
                    editEmail.error = "Email is required"
                    editEmail.requestFocus()
                }
                !email.contains("@") || !email.contains(".") -> {
                    editEmail.error = "Invalid email"
                    editEmail.requestFocus()
                }
                else -> {
                    prefs.edit()
                        .putString("user_name", name)
                        .putString("user_phone", phone)
                        .putString("user_email", email)
                        .apply()

                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
