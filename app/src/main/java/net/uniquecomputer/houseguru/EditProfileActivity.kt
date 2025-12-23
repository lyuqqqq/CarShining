package net.uniquecomputer.houseguru

import net.uniquecomputer.houseguru.R
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
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

    private lateinit var dbHelper: AppDatabaseHelper
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        dbHelper = AppDatabaseHelper(this)

        val sessionPrefs = getSharedPreferences("user_session", MODE_PRIVATE)
        currentUserId = sessionPrefs.getInt("current_user_id", -1)

        if (currentUserId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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

        val user = dbHelper.getUserById(currentUserId)
        if (user != null) {
            editName.setText(user.name)
            editEmail.setText(user.email)
            editPhone.setText(user.phone)
        }

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
                    val rows = dbHelper.updateUserProfile(
                        userId = currentUserId,
                        name = name,
                        phone = phone,
                        email = email
                    )

                    if (rows > 0) {
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }

                    finish()
                }
            }
        }
    }
}
