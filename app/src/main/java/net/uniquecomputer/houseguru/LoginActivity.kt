package net.uniquecomputer.houseguru

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.uniquecomputer.houseguru.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val prefillPhone = intent.getStringExtra("phone")
        if (!prefillPhone.isNullOrEmpty()) {
            binding.editPhone.setText(prefillPhone)
        }

        binding.buttonLogin.setOnClickListener {
            val phone = binding.editPhone.text?.toString()?.trim().orEmpty()
            val password = binding.editPassword.text?.toString()?.trim().orEmpty()

            if (phone.isEmpty()) {
                binding.editPhone.error = "Phone is required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.editPassword.error = "Password is required"
                return@setOnClickListener
            }

            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            val hasUser = prefs.getBoolean("has_user", false)

            if (!hasUser) {
                Toast.makeText(this, "Please sign up first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedPhone = prefs.getString("phone", null)
            val savedPassword = prefs.getString("password", null)

            if (phone == savedPhone && password == savedPassword) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Phone or password is incorrect", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
