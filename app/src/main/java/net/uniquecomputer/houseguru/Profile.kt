package net.uniquecomputer.houseguru

import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import net.uniquecomputer.houseguru.databinding.FragmentProfileBinding

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: AppDatabaseHelper

    companion object {
        private const val CUSTOMER_SERVICE_NUMBER = "+60 12-345 6789"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        dbHelper = AppDatabaseHelper(requireContext())

        refreshProfileHeader()

        binding.layoutAllOrder.setOnClickListener {
            (activity as? MainActivity)?.openBookingScreen()
        }

        binding.layoutVoucher.setOnClickListener {
            (activity as? MainActivity)?.openWalletScreen()
        }

        binding.layoutAddress.setOnClickListener {
            val intent = Intent(requireContext(), AddressActivity::class.java)
            startActivity(intent)
        }

        binding.rowMyProfile.setOnClickListener {
            val intent = android.content.Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.rowNotification.setOnClickListener {
            val intent = Intent(requireContext(), CarManagementActivity::class.java)
            startActivity(intent)
        }

        binding.rowSetting.setOnClickListener {
            val intent = Intent(requireContext(), CustomerServiceActivity::class.java)
            startActivity(intent)
        }

        binding.rowPayment.setOnClickListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        refreshProfileHeader()
    }

    private fun refreshProfileHeader() {
        val sessionPrefs = requireContext()
            .getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val userId = sessionPrefs.getInt("current_user_id", -1)

        if (userId == -1) {
            binding.textUserName.text = "Car Shining User"
            binding.textUserEmail.text = "user@example.com"
            return
        }

        val user = dbHelper.getUserById(userId)

        if (user != null) {
            binding.textUserName.text =
                if (user.name.isNotBlank()) user.name else "Car Shining User"

            binding.textUserEmail.text =
                if (user.email.isNotBlank()) user.email else "user@example.com"
        } else {
            binding.textUserName.text = "Car Shining User"
            binding.textUserEmail.text = "user@example.com"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
