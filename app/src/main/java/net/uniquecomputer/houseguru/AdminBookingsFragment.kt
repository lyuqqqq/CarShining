package net.uniquecomputer.houseguru

import android.content.res.ColorStateList
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
import com.google.android.material.chip.Chip
import net.uniquecomputer.houseguru.databinding.FragmentAdminBookingsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AdminBookingsFragment : Fragment() {

    private var _binding: FragmentAdminBookingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: AppDatabaseHelper

    private val allBookings = mutableListOf<AdminBooking>()

    private var currentFilter: FilterType = FilterType.ALL

    private data class AdminBooking(
        val id: Int,
        val serviceTitle: String,
        val price: String,
        val userName: String,
        val dateStr: String,
        val timeStr: String,
        val status: String
    )

    private enum class FilterType {
        ALL, TODAY, PENDING, COMPLETED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = AppDatabaseHelper(requireContext())

        autoCompletePastBookings()

        loadAllBookingsFromDb()

        setupFilterChips()

        applyFilter(FilterType.ALL)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadAllBookingsFromDb() {
        allBookings.clear()

        val db = dbHelper.readableDatabase

        val query = """
            SELECT 
                s.${AppDatabaseHelper.COLUMN_SERVICE_ID}              AS service_id,
                s.${AppDatabaseHelper.COLUMN_SERVICE_TITLE}           AS title,
                s.${AppDatabaseHelper.COLUMN_SERVICE_DATE}            AS date,
                s.${AppDatabaseHelper.COLUMN_SERVICE_TIME}            AS time,
                s.${AppDatabaseHelper.COLUMN_SERVICE_PRICE}           AS price,
                s.${AppDatabaseHelper.COLUMN_SERVICE_STATUS}          AS status,
                u.${AppDatabaseHelper.COLUMN_NAME}                    AS user_name
            FROM ${AppDatabaseHelper.TABLE_SERVICES} s
            LEFT JOIN ${AppDatabaseHelper.TABLE_USERS} u
              ON s.${AppDatabaseHelper.COLUMN_SERVICE_USER_ID} = u.${AppDatabaseHelper.COLUMN_USER_ID}
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    val id = c.getInt(c.getColumnIndexOrThrow("service_id"))
                    val title = c.getString(c.getColumnIndexOrThrow("title")) ?: ""
                    val date = c.getString(c.getColumnIndexOrThrow("date")) ?: ""
                    val time = c.getString(c.getColumnIndexOrThrow("time")) ?: ""
                    val price = c.getString(c.getColumnIndexOrThrow("price")) ?: ""
                    val status = c.getString(c.getColumnIndexOrThrow("status")) ?: ""
                    val userName = c.getString(c.getColumnIndexOrThrow("user_name")) ?: "Unknown"

                    allBookings.add(
                        AdminBooking(
                            id = id,
                            serviceTitle = title,
                            price = price,
                            userName = userName,
                            dateStr = date,
                            timeStr = time,
                            status = status
                        )
                    )
                } while (c.moveToNext())
            }
        }
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

                    val d = parseDateAny(dateStr)

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

    private fun setupFilterChips() {
        val chipAll: Chip = binding.chipAll
        val chipToday: Chip = binding.chipToday
        val chipPending: Chip = binding.chipPending
        val chipCompleted: Chip = binding.chipCompleted

        fun applyChipStyles(filter: FilterType) {
            fun Chip.setActive(active: Boolean) {
                isChecked = active
                chipBackgroundColor = ColorStateList.valueOf(
                    Color.parseColor(
                        if (active) "#DFF1FB" else "#E0E0E0"   // 选中蓝色，未选中灰色
                    )
                )
            }

            chipAll.setActive(filter == FilterType.ALL)
            chipToday.setActive(filter == FilterType.TODAY)
            chipPending.setActive(filter == FilterType.PENDING)
            chipCompleted.setActive(filter == FilterType.COMPLETED)
        }

        applyChipStyles(currentFilter)

        chipAll.setOnClickListener {
            currentFilter = FilterType.ALL
            applyChipStyles(currentFilter)
            applyFilter(currentFilter)
        }

        chipToday.setOnClickListener {
            currentFilter = FilterType.TODAY
            applyChipStyles(currentFilter)
            applyFilter(currentFilter)
        }

        chipPending.setOnClickListener {
            currentFilter = FilterType.PENDING
            applyChipStyles(currentFilter)
            applyFilter(currentFilter)
        }

        chipCompleted.setOnClickListener {
            currentFilter = FilterType.COMPLETED
            applyChipStyles(currentFilter)
            applyFilter(currentFilter)
        }
    }

    private fun applyFilter(filter: FilterType) {
        val listToShow = when (filter) {
            FilterType.ALL -> allBookings

            FilterType.TODAY -> {
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)
                today.set(Calendar.MILLISECOND, 0)
                val todayDate = today.time

                allBookings.filter { booking ->
                    val d = parseDateAny(booking.dateStr)
                    d != null && isSameDay(d, todayDate)
                }
            }

            FilterType.PENDING -> {
                allBookings.filter {
                    val st = it.status.lowercase(Locale.getDefault())
                    st == "incomplete" || st == "pending"
                }
            }

            FilterType.COMPLETED -> {
                allBookings.filter {
                    it.status.equals("completed", ignoreCase = true)
                }
            }
        }

        val sorted = when (filter) {
            FilterType.ALL -> listToShow.sortedWith(
                compareBy<AdminBooking> {
                    if (it.status.equals("completed", ignoreCase = true)) 1 else 0
                }.thenBy { parseDateForSort(it.dateStr) }
                    .thenBy { parseTimeForSort(it.timeStr) }
            )

            else -> listToShow.sortedWith(
                compareBy<AdminBooking> { parseDateForSort(it.dateStr) }
                    .thenBy { parseTimeForSort(it.timeStr) }
            )
        }

        renderBookings(sorted)
    }

    private fun parseDateForSort(dateStr: String?): Long {
        if (dateStr.isNullOrBlank()) return Long.MAX_VALUE

        val patterns = listOf(
            "EEE, dd MMM",
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd"
        )

        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                val parsed = sdf.parse(dateStr)
                if (parsed != null) {
                    return parsed.time
                }
            } catch (_: Exception) { }
        }
        return Long.MAX_VALUE
    }

    private fun parseTimeForSort(timeStr: String?): Long {
        if (timeStr.isNullOrBlank()) return Long.MAX_VALUE

        val patterns = listOf(
            "hh:mm a",
            "HH:mm",
            "HH:mm:ss"
        )

        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                val parsed = sdf.parse(timeStr)
                if (parsed != null) {
                    return parsed.time
                }
            } catch (_: Exception) { }
        }
        return Long.MAX_VALUE
    }

    private fun parseDateAny(dateStr: String?): Date? {
        if (dateStr.isNullOrBlank()) return null

        val patterns = listOf(
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "EEE, dd MMM"
        )

        for (p in patterns) {
            try {
                val sdf = SimpleDateFormat(p, Locale.ENGLISH)
                sdf.isLenient = false
                val parsed = sdf.parse(dateStr)
                if (parsed != null) {
                    if (p == "EEE, dd MMM") {
                        val base = Calendar.getInstance()
                        val tmp = Calendar.getInstance().apply { time = parsed }
                        base.set(Calendar.MONTH, tmp.get(Calendar.MONTH))
                        base.set(Calendar.DAY_OF_MONTH, tmp.get(Calendar.DAY_OF_MONTH))
                        base.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR))
                        base.set(Calendar.HOUR_OF_DAY, 0)
                        base.set(Calendar.MINUTE, 0)
                        base.set(Calendar.SECOND, 0)
                        base.set(Calendar.MILLISECOND, 0)
                        return base.time
                    }
                    return parsed
                }
            } catch (_: Exception) {
            }
        }
        return null
    }

    private fun isSameDay(d1: Date, d2: Date): Boolean {
        val c1 = Calendar.getInstance().apply { time = d1 }
        val c2 = Calendar.getInstance().apply { time = d2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }

    private fun formatDateForDisplay(dateStr: String?): String {
        if (dateStr.isNullOrBlank()) return "-"

        val parsed = parseDateAny(dateStr)
        return if (parsed != null) {
            val sdf = SimpleDateFormat("EEE, dd MMM", Locale.ENGLISH)
            sdf.format(parsed)
        } else {
            dateStr
        }
    }

    private fun formatTimeForDisplay(timeStr: String?): String {
        if (timeStr.isNullOrBlank()) return "-"

        val patterns = listOf(
            "HH:mm",
            "HH:mm:ss",
            "hh:mm a"
        )

        for (p in patterns) {
            try {
                val inFmt = SimpleDateFormat(p, Locale.ENGLISH)
                inFmt.isLenient = false
                val d = inFmt.parse(timeStr)
                if (d != null) {
                    val outFmt = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                    return outFmt.format(d)
                }
            } catch (_: Exception) {
            }
        }

        return timeStr
    }

    private fun renderBookings(bookings: List<AdminBooking>) {
        val container: LinearLayout = binding.layoutAdminBookingsContainer
        container.removeAllViews()

        if (bookings.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "No bookings found for this filter."
                textSize = 14f
                setTextColor(Color.parseColor("#666666"))
                setPadding(8, 8, 8, 8)
            }
            container.addView(emptyView)
            return
        }

        val inflater = LayoutInflater.from(requireContext())

        for (booking in bookings) {
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
            textBookingId.visibility = View.GONE

            // 设置服务名和价格
            textService.text = booking.serviceTitle.ifBlank { "No Title" }
            textPrice.text = booking.price.ifBlank { "RM -" }

            textCustomer.text = "Customer: ${booking.userName.ifBlank { "Unknown" }}"

            val displayDate = formatDateForDisplay(booking.dateStr)
            val displayTime = formatTimeForDisplay(booking.timeStr)
            textDateTime.text = "$displayDate · $displayTime"

            val (statusText, statusColor) = when (booking.status.lowercase(Locale.getDefault())) {
                "incomplete", "pending" -> "Pending" to Color.parseColor("#FFA000")
                "completed" -> "Completed" to Color.parseColor("#2E7D32")
                else -> booking.status.ifBlank { "Unknown" } to Color.parseColor("#666666")
            }
            textStatus.text = statusText
            textStatus.setTextColor(statusColor)

            textBookingId.visibility = View.GONE


            card.setOnClickListener {
            }

            container.addView(itemView)
        }
    }
}
