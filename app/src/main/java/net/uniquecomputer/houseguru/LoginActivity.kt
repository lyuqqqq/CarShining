package net.uniquecomputer.houseguru

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.uniquecomputer.houseguru.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: AppDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dbHelper = AppDatabaseHelper(this)

        binding.buttonLogin.setOnClickListener {
            val phone = binding.editPhone.text?.toString()?.trim() ?: ""
            val password = binding.editPassword.text?.toString()?.trim() ?: ""

            val isAdmin = binding.rbAdmin.isChecked

            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isAdmin) {
                handleAdminLogin(phone, password)
            } else {
                handleCustomerLogin(phone, password)
            }
        }

        binding.textSignUp.setOnClickListener {
            if (binding.rbAdmin.isChecked) {
                Toast.makeText(
                    this,
                    "Admin account cannot sign up here. Please switch to Customer.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent = Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun handleAdminLogin(phone: String, password: String) {
        val user = dbHelper.getUserByPhoneAndPassword(phone, password)

        if (user == null || user.role != "admin") {
            Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("current_user_id", user.id)
            putString("current_user_name", user.name)
            putString("current_user_role", user.role)
            apply()
        }

        val intent = Intent(this, AdminMainActivity::class.java).apply {
            putExtra("user_role", user.role)
            putExtra("user_id", user.id)
        }
        startActivity(intent)
        finish()
    }


    private fun handleCustomerLogin(phone: String, password: String) {
        val user = dbHelper.getUserByPhoneAndPassword(phone, password)

        if (user == null) {
            Toast.makeText(
                this,
                "Phone or password is incorrect, or you haven't signed up yet.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (user.role != "user") {
            Toast.makeText(this, "This account is not a customer account.", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("user_session", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("current_user_id", user.id)
            putString("current_user_name", user.name)
            putString("current_user_phone", user.phone)
            apply()
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("user_role", user.role)
            putExtra("user_id", user.id)
        }
        startActivity(intent)
        finish()
    }

}