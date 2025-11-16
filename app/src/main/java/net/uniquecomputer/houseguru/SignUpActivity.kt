package net.uniquecomputer.houseguru

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.uniquecomputer.houseguru.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.buttonSignUp.setOnClickListener {
            val name = binding.editName.text?.toString()?.trim().orEmpty()
            val phone = binding.editPhone.text?.toString()?.trim().orEmpty()
            val email = binding.editEmail.text?.toString()?.trim().orEmpty()
            val password = binding.editPassword.text?.toString()?.trim().orEmpty()

            when {
                name.isEmpty() -> {
                    binding.editName.error = "Name is required"
                    return@setOnClickListener
                }
                phone.isEmpty() -> {
                    binding.editPhone.error = "Phone is required"
                    return@setOnClickListener
                }
                email.isEmpty() -> {
                    binding.editEmail.error = "Email is required"
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    binding.editPassword.error = "Password is required"
                    return@setOnClickListener
                }
            }

            val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
            prefs.edit().apply {
                putString("name", name)
                putString("phone", phone)
                putString("email", email)
                putString("password", password)
                putBoolean("has_user", true)
                apply()
            }

            val intent = Intent(this, LoginActivity::class.java).apply {
                putExtra("phone", phone)
            }
            startActivity(intent)
            finish()
        }

        binding.textLoginLink.setOnClickListener {
            goToLogin()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
