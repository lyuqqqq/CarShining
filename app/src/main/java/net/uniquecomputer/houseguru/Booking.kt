package net.uniquecomputer.houseguru

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import net.uniquecomputer.houseguru.databinding.FragmentBookingBinding

class Booking : Fragment() {

    private var _binding: FragmentBookingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingBinding.inflate(inflater, container, false)

        setupIncompleteSection()

        setupTabs()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setupIncompleteSection()

        val selectedPos = binding.tabLayoutBooking.selectedTabPosition
        if (selectedPos == 0) {
            showCompleted()
        } else {
            showIncomplete()
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
        val sharedPref = requireActivity()
            .getSharedPreferences("booking_prefs", Context.MODE_PRIVATE)

        val count = sharedPref.getInt("incomplete_count", 0)

        binding.layoutIncompleteSection.removeAllViews()

        if (count == 0) {
            return
        }

        val inflater = LayoutInflater.from(requireContext())

        for (i in 0 until count) {
            val itemView = inflater.inflate(
                R.layout.item_incomplete_booking,
                binding.layoutIncompleteSection,
                false
            )

            val serviceName = sharedPref.getString("service_name_$i", "") ?: ""
            val date = sharedPref.getString("date_$i", "") ?: ""
            val time = sharedPref.getString("time_$i", "") ?: ""
            val price = sharedPref.getString("price_$i", "") ?: ""
            val rawImageRes = sharedPref.getInt("imageRes_$i", 0)
            val imageRes = if (rawImageRes != 0) rawImageRes else R.drawable.max_premium_cleaning

            val imageView = itemView.findViewById<ImageView>(R.id.imageIncompleteService)
            val nameView = itemView.findViewById<TextView>(R.id.textIncompleteServiceName)
            val dateTimeView = itemView.findViewById<TextView>(R.id.textIncompleteDateTime)
            val priceView = itemView.findViewById<TextView>(R.id.textIncompletePrice)

            nameView.text = serviceName
            dateTimeView.text = "$date · $time"
            priceView.text = price
            imageView.setImageResource(imageRes)

            binding.layoutIncompleteSection.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
