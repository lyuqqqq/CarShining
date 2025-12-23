package net.uniquecomputer.houseguru

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import net.uniquecomputer.houseguru.databinding.FragmentAdminUsersBinding
import java.util.Locale

class AdminUsersFragment : Fragment() {

    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: AppDatabaseHelper

    private data class AdminUser(
        val id: Int,
        val name: String,
        val email: String,
        val phone: String
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = AppDatabaseHelper(requireContext())

        val users = loadNonAdminUsers()

        binding.textTotalUsers.text = "Total Registered Users: ${users.size}"

        renderUsers(users)
    }

    private fun loadNonAdminUsers(): List<AdminUser> {
        val result = mutableListOf<AdminUser>()

        val db = dbHelper.readableDatabase
        val query = """
            SELECT 
                ${AppDatabaseHelper.COLUMN_USER_ID} AS user_id,
                ${AppDatabaseHelper.COLUMN_NAME}    AS name,
                ${AppDatabaseHelper.COLUMN_EMAIL}   AS email,
                ${AppDatabaseHelper.COLUMN_PHONE}   AS phone
            FROM ${AppDatabaseHelper.TABLE_USERS}
            ORDER BY ${AppDatabaseHelper.COLUMN_USER_ID} ASC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        cursor.use { c ->
            if (c.moveToFirst()) {
                do {
                    val id = c.getInt(c.getColumnIndexOrThrow("user_id"))
                    val name = c.getString(c.getColumnIndexOrThrow("name")) ?: ""
                    val email = c.getString(c.getColumnIndexOrThrow("email")) ?: ""
                    val phone = c.getString(c.getColumnIndexOrThrow("phone")) ?: ""

                    if (email.lowercase(Locale.getDefault()).contains("admin")) {
                        continue
                    }

                    result.add(
                        AdminUser(
                            id = id,
                            name = name,
                            email = email,
                            phone = phone
                        )
                    )
                } while (c.moveToNext())
            }
        }

        return result
    }

    private fun renderUsers(users: List<AdminUser>) {
        val container: LinearLayout = binding.layoutAdminUsersContainer
        container.removeAllViews()

        if (users.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "No registered users found."
                textSize = 14f
                setTextColor(Color.parseColor("#666666"))
                setPadding(8, 8, 8, 8)
            }
            container.addView(emptyView)
            return
        }

        val inflater = LayoutInflater.from(requireContext())

        users.forEachIndexed { index, user ->
            val itemView = inflater.inflate(
                R.layout.item_admin_user,
                container,
                false
            )

            val card = itemView.findViewById<CardView>(R.id.cardAdminUser)
            val textAvatar = itemView.findViewById<TextView>(R.id.textUserAvatar)
            val textName = itemView.findViewById<TextView>(R.id.textUserName)
            val textUserId = itemView.findViewById<TextView>(R.id.textUserId)
            val textEmail = itemView.findViewById<TextView>(R.id.textUserEmail)
            val textPhone = itemView.findViewById<TextView>(R.id.textUserPhone)

            val firstLetter = user.name.trim().firstOrNull()?.uppercaseChar() ?: 'U'
            textAvatar.text = firstLetter.toString()

            textName.text = user.name.ifBlank { "Unknown User" }

            val displayId = index + 1
            textUserId.text = "ID: $displayId"

            val emailDisplay = user.email.ifBlank { "-" }
            textEmail.text = "Email: $emailDisplay"

            val rawPhone = user.phone.trim()
            val displayPhone = if (rawPhone.isBlank()) {
                "-"
            } else {
                if (rawPhone.startsWith("+60")) {
                    rawPhone
                } else {
                    "+60 $rawPhone"
                }
            }
            textPhone.text = "Phone: $displayPhone"

            card.setOnClickListener {
            }

            container.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
