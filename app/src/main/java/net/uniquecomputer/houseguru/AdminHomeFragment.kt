package net.uniquecomputer.houseguru

import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import net.uniquecomputer.houseguru.databinding.FragmentAdminHomeBinding
import java.text.SimpleDateFormat
import java.util.*

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: AppDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        dbHelper = AppDatabaseHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshSummaryCards()

        binding.buttonViewAllBookings.setOnClickListener {
            (activity as? AdminMainActivity)?.openAdminBookingsScreen()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSummaryCards()
    }

    private fun refreshSummaryCards() {
        autoCompletePastBookings()

        val allServices = dbHelper.getAllServices()

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayDateOnly = todayCal.time

        val todayCount = allServices.count { service ->
            val parsed = parseServiceDate(service.date)
            if (parsed != null) {
                val cal = Calendar.getInstance().apply {
                    time = parsed
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                cal.time == todayDateOnly
            } else {
                false
            }
        }

        val pendingCount = allServices.count {
            it.status.equals("incomplete", ignoreCase = true) ||
                    it.status.equals("pending", ignoreCase = true)
        }

        val completedCount = allServices.count {
            it.status.equals("completed", ignoreCase = true)
        }

        binding.textTodayBookingsValue.text = todayCount.toString()
        binding.textPendingValue.text = pendingCount.toString()
        binding.textCompletedValue.text = completedCount.toString()

        renderHomeIncompleteBookings(allServices)
    }

    private fun autoCompletePastBookings() {
        val db = dbHelper.writableDatabase

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayDate = todayCal.time

        val query = """
            SELECT 
                ${AppDatabaseHelper.COLUMN_SERVICE_ID}     AS service_id,
                ${AppDatabaseHelper.COLUMN_SERVICE_DATE}   AS date,
                ${AppDatabaseHelper.COLUMN_SERVICE_STATUS} AS status
            FROM ${AppDatabaseHelper.TABLE_SERVICES}
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    val id = c.getInt(c.getColumnIndexOrThrow("service_id"))
                    val dateStr = c.getString(c.getColumnIndexOrThrow("date")) ?: ""
                    val status = c.getString(c.getColumnIndexOrThrow("status")) ?: ""

                    val d = parseServiceDate(dateStr)
                    if (d != null && d.before(todayDate) && !status.equals("completed", true)) {
                        val values = ContentValues().apply {
                            put(AppDatabaseHelper.COLUMN_SERVICE_STATUS, "completed")
                        }
                        db.update(
                            AppDatabaseHelper.TABLE_SERVICES,
                            values,
                            "${AppDatabaseHelper.COLUMN_SERVICE_ID}=?",
                            arrayOf(id.toString())
                        )
                    }
                } while (c.moveToNext())
            }
        }
    }

    private fun renderHomeIncompleteBookings(allServices: List<ServiceItem>) {
        val container: LinearLayout = binding.layoutHomeIncompleteBookings
        container.removeAllViews()

        val pendingList = allServices.filter {
            val st = it.status.lowercase(Locale.getDefault())
            st == "incomplete" || st == "pending"
        }

        if (pendingList.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "No incomplete bookings."
                textSize = 14f
                setTextColor(Color.parseColor("#666666"))
                setPadding(8, 8, 8, 8)
            }
            container.addView(emptyView)
            return
        }

        val sorted = pendingList.sortedBy { parseDateTimeForSort(it.date, it.time) }

        val topTwo = sorted.take(2)

        val inflater = LayoutInflater.from(requireContext())
        val db = dbHelper.readableDatabase

        for (service in topTwo) {

            var userName = "Unknown"
            val userId = service.userId

            val cursor = db.rawQuery(
                """
                SELECT ${AppDatabaseHelper.COLUMN_NAME} AS name
                FROM ${AppDatabaseHelper.TABLE_USERS}
                WHERE ${AppDatabaseHelper.COLUMN_USER_ID} = ?
                """.trimIndent(),
                arrayOf(userId.toString())
            )
            cursor.use { c ->
                if (c.moveToFirst()) {
                    userName = c.getString(c.getColumnIndexOrThrow("name")) ?: "Unknown"
                }
            }

            val itemView = inflater.inflate(
                R.layout.item_admin_booking,
                container,
                false
            )

            val card = itemView.findViewById<CardView>(R.id.cardAdminBooking)
            val textService = itemView.findViewById<TextView>(R.id.textService)
            val textPrice = itemView.findViewById<TextView>(R.id.textPrice)
            val textCustomer = itemView.findViewById<TextView>(R.id.textCustomer)
            val textDateTime = itemView.findViewById<TextView>(R.id.textDateTime)
            val textStatus = itemView.findViewById<TextView>(R.id.textStatus)
            val textBookingId = itemView.findViewById<TextView>(R.id.textBookingId)

            textService.text = service.title.ifBlank { "No Title" }
            textPrice.text = service.price.ifBlank { "RM -" }
            textCustomer.text = "Customer: $userName"

            val displayDate = formatDateToDisplay(service.date)
            val displayTime = formatTimeToDisplay(service.time)
            textDateTime.text = "$displayDate · $displayTime"

            textStatus.text = "Pending"
            textStatus.setTextColor(Color.parseColor("#FFA000"))

            textBookingId.visibility = View.GONE

            card.setOnClickListener {

            }

            container.addView(itemView)
        }
    }

    private fun parseServiceDate(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null

        val patterns = listOf(
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "EEE, dd MMM"
        )

        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
                sdf.isLenient = false
                val parsed = sdf.parse(dateStr)
                if (parsed != null) return parsed
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun parseDateTimeForSort(dateStr: String?, timeStr: String?): Long {
        val baseDate = parseServiceDate(dateStr) ?: return Long.MAX_VALUE

        val cal = Calendar.getInstance().apply { time = baseDate }

        if (!timeStr.isNullOrBlank()) {
            val patterns = listOf("HH:mm", "hh:mm a")

            for (p in patterns) {
                try {
                    val sdf = SimpleDateFormat(p, Locale.ENGLISH)
                    sdf.isLenient = false
                    val parsed = sdf.parse(timeStr)
                    if (parsed != null) {
                        val tCal = Calendar.getInstance().apply { time = parsed }
                        cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY))
                        cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE))
                        cal.set(Calendar.SECOND, tCal.get(Calendar.SECOND))
                        cal.set(Calendar.MILLISECOND, 0)
                        break
                    }
                } catch (_: Exception) {
                }
            }
        }

        return cal.timeInMillis
    }

    private fun formatDateToDisplay(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "-"

        val patterns = listOf(
            "EEE, dd MMM",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd"
        )

        for (pattern in patterns) {
            try {
                val input = SimpleDateFormat(pattern, Locale.ENGLISH)
                val parsed = input.parse(dateStr)
                if (parsed != null) {
                    val out = SimpleDateFormat("EEE, dd MMM", Locale.ENGLISH)
                    return out.format(parsed)
                }
            } catch (_: Exception) {
            }
        }
        return dateStr
    }

    private fun formatTimeToDisplay(timeStr: String?): String {
        if (timeStr.isNullOrBlank()) return "-"

        val patterns = listOf("hh:mm a", "HH:mm")

        for (pattern in patterns) {
            try {
                val input = SimpleDateFormat(pattern, Locale.ENGLISH)
                val parsed = input.parse(timeStr)
                if (parsed != null) {
                    val out = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                    return out.format(parsed)
                }
            } catch (_: Exception) {
            }
        }
        return timeStr
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
