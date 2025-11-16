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

    companion object {
        private const val CUSTOMER_SERVICE_NUMBER = "+60 12-345 6789"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

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
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val name = prefs.getString("user_name", "Car Shining User")
        val email = prefs.getString("user_email", "user@example.com")

        binding.textUserName.text = name
        binding.textUserEmail.text = email
    }

    private fun dialCustomerService() {
        val uri = Uri.parse("tel:$CUSTOMER_SERVICE_NUMBER")
        val intent = Intent(Intent.ACTION_DIAL, uri)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "No dialer app found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
