package net.uniquecomputer.houseguru

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import net.uniquecomputer.houseguru.databinding.FragmentBookingBinding
import java.text.SimpleDateFormat
import java.util.Locale

class Booking : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: AppDatabaseHelper
    private var currentUserId: Int = -1

    private val editLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (_binding == null) return@registerForActivityResult
            if (result.resultCode != android.app.Activity.RESULT_OK) return@registerForActivityResult
            binding.bookingScrollView.post {
                if (_binding == null) return@post
                setupIncompleteSection()
                setupCompletedSection()
                val selectedPos = binding.tabLayoutBooking.selectedTabPosition
                if (selectedPos == 0) showCompleted() else showIncomplete()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)

        dbHelper = AppDatabaseHelper(requireContext())

        val sessionPrefs = requireActivity()
            .getSharedPreferences("user_session", Context.MODE_PRIVATE)
        currentUserId = sessionPrefs.getInt("current_user_id", -1)

        if (currentUserId != -1) {
            dbHelper.insertSampleCompletedHistoryOnce(currentUserId)
        }

        setupTabs()
        setupIncompleteSection()
        setupCompletedSection()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.bookingScrollView.post {
            if (_binding == null) return@post
            setupIncompleteSection()
            setupCompletedSection()
            val selectedPos = binding.tabLayoutBooking.selectedTabPosition
            if (selectedPos == 0) showCompleted() else showIncomplete()
        }
    }

    private fun setupTabs() {
        val tabLayout = binding.tabLayoutBooking
        showCompleted()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> showCompleted()
                    1 -> showIncomplete()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun showCompleted() {
        binding.layoutCompletedSection.apply {
            visibility = View.VISIBLE
            alpha = 0f
            animate().alpha(1f).setDuration(150).start()
        }
        binding.layoutIncompleteSection.visibility = View.GONE
    }

    private fun showIncomplete() {
        binding.layoutIncompleteSection.apply {
            visibility = View.VISIBLE
            alpha = 0f
            animate().alpha(1f).setDuration(150).start()
        }
        binding.layoutCompletedSection.visibility = View.GONE
    }

    private fun setupIncompleteSection() {
        binding.layoutIncompleteSection.removeAllViews()
        if (currentUserId == -1) return

        val allServices = dbHelper.getServicesForUser(currentUserId)

        val incompleteServices = allServices
            .filter { it.status == "incomplete" }
            .sortedWith(compareBy<ServiceItem> { parseDateForSort(it.date) }
                .thenBy { parseTimeForSort(it.time) })

        if (incompleteServices.isEmpty()) return

        val inflater = LayoutInflater.from(requireContext())

        for (service in incompleteServices) {
            val itemView = inflater.inflate(
                R.layout.item_incomplete_booking,
                binding.layoutIncompleteSection,
                false
            )

            val imageView = itemView.findViewById<ImageView>(R.id.imageIncompleteService)
            val nameView = itemView.findViewById<TextView>(R.id.textIncompleteServiceName)
            val dateTimeView = itemView.findViewById<TextView>(R.id.textIncompleteDateTime)
            val priceView = itemView.findViewById<TextView>(R.id.textIncompletePrice)
            val btnUpdate = itemView.findViewById<MaterialButton>(R.id.btnUpdateBooking)
            val btnDelete = itemView.findViewById<MaterialButton>(R.id.btnDeleteBooking)

            nameView.text = service.title
            dateTimeView.text = "${formatDateToDisplay(service.date)} · ${formatTimeToDisplay(service.time)}"
            priceView.text = service.price

            val imageRes = if (service.imageRes != 0) service.imageRes else R.drawable.max_premium_cleaning
            imageView.setImageResource(imageRes)

            btnUpdate.setOnClickListener {
                val intent = Intent(requireContext(), Details::class.java).apply {
                    putExtra("mode", "edit")
                    putExtra("service_id", service.id)
                }
                editLauncher.launch(intent)
            }

            btnDelete.setOnClickListener { showDeleteConfirmDialog(service) }

            binding.layoutIncompleteSection.addView(itemView)
        }
    }

    private fun setupCompletedSection() {
        binding.layoutCompletedSection.removeAllViews()
        if (currentUserId == -1) return

        val allServices = dbHelper.getServicesForUser(currentUserId)

        val completedServices = allServices
            .filter { it.status.equals("completed", ignoreCase = true) }
            .sortedByDescending { parseDateForSort(it.date) }

        if (completedServices.isEmpty()) {
            val tv = TextView(requireContext()).apply {
                text = "No completed bookings yet."
                textSize = 14f
                setTextColor(android.graphics.Color.parseColor("#666666"))
            }
            binding.layoutCompletedSection.addView(tv)
            return
        }

        val inflater = LayoutInflater.from(requireContext())

        for (service in completedServices) {
            val itemView = inflater.inflate(
                R.layout.item_completed_booking,
                binding.layoutCompletedSection,
                false
            )

            val imageView = itemView.findViewById<ImageView>(R.id.imageServiceCompleted)
            val nameView = itemView.findViewById<TextView>(R.id.textServiceNameCompleted)
            val dateTimeView = itemView.findViewById<TextView>(R.id.textDateTimeCompleted)
            val priceView = itemView.findViewById<TextView>(R.id.textPriceCompleted)

            nameView.text = service.title
            dateTimeView.text = "${formatDateToDisplay(service.date)} · ${formatTimeToDisplay(service.time)}"
            priceView.text = service.price

            val imageRes = if (service.imageRes != 0) service.imageRes else R.drawable.max_premium_cleaning
            imageView.setImageResource(imageRes)

            binding.layoutCompletedSection.addView(itemView)
        }
    }

    private fun showDeleteConfirmDialog(service: ServiceItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete booking")
            .setMessage("Are you sure you want to delete this booking?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteService(service.id)
                Toast.makeText(requireContext(), "Booking deleted", Toast.LENGTH_SHORT).show()
                setupIncompleteSection()
                setupCompletedSection()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun parseDateForSort(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return Long.MAX_VALUE
        val patterns = listOf("EEE, dd MMM", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd")
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                val parsed = sdf.parse(dateStr)
                if (parsed != null) return parsed.time
            } catch (_: Exception) {}
        }
        return Long.MAX_VALUE
    }

    private fun parseTimeForSort(timeStr: String?): Int {
        if (timeStr.isNullOrBlank()) return Int.MAX_VALUE
        val patterns = listOf("HH:mm", "hh:mm a")
        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.getDefault())
                val d = sdf.parse(timeStr)
                if (d != null) {
                    val cal = java.util.Calendar.getInstance().apply { time = d }
                    return cal.get(java.util.Calendar.HOUR_OF_DAY) * 60 + cal.get(java.util.Calendar.MINUTE)
                }
            } catch (_: Exception) {}
        }
        return Int.MAX_VALUE
    }

    private fun formatDateToDisplay(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "-"
        val patterns = listOf("EEE, dd MMM", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd")
        for (pattern in patterns) {
            try {
                val inputFormat = SimpleDateFormat(pattern, Locale.getDefault())
                val parsed = inputFormat.parse(dateStr)
                if (parsed != null) {
                    val outputFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
                    return outputFormat.format(parsed)
                }
            } catch (_: Exception) {}
        }
        return dateStr
    }

    private fun formatTimeToDisplay(timeStr: String?): String {
        if (timeStr.isNullOrBlank()) return "-"
        val patterns = listOf("hh:mm a", "HH:mm")
        for (pattern in patterns) {
            try {
                val inputFormat = SimpleDateFormat(pattern, Locale.getDefault())
                val parsed = inputFormat.parse(timeStr)
                if (parsed != null) {
                    val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    return outputFormat.format(parsed)
                }
            } catch (_: Exception) {}
        }
        return timeStr
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
