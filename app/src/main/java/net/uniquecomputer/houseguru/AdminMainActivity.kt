package net.uniquecomputer.houseguru

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.content.res.ColorStateList
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import net.uniquecomputer.houseguru.databinding.ActivityAdminMainBinding

class AdminMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminMainBinding

    private enum class AdminScreen {
        HOME, USERS, BOOKINGS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)

        val nav: BottomNavigationView = binding.adminBottomNavigation
        nav.itemActiveIndicatorColor = ColorStateList.valueOf(Color.parseColor("#90CAF9"))
        nav.itemRippleColor = ColorStateList.valueOf(Color.parseColor("#3390CAF9"))

        if (savedInstanceState == null) {
            replaceFragment(AdminHomeFragment())
            updateTopBar(AdminScreen.HOME)
        }

        binding.adminBottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.admin_home -> {
                    replaceFragment(AdminHomeFragment())
                    updateTopBar(AdminScreen.HOME)
                    true
                }
                R.id.admin_users -> {
                    replaceFragment(AdminUsersFragment())
                    updateTopBar(AdminScreen.USERS)
                    true
                }
                R.id.admin_bookings -> {
                    replaceFragment(AdminBookingsFragment())
                    updateTopBar(AdminScreen.BOOKINGS)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, fragment)
            .commit()
    }

    fun openAdminBookingsScreen() {
        binding.adminBottomNavigation.selectedItemId = R.id.admin_bookings
    }

    private fun updateTopBar(screen: AdminScreen) {
        val toolbar = binding.topAppBar

        when (screen) {
            AdminScreen.HOME -> {
                setBoldTitle("Admin Home")
                toolbar.navigationIcon =
                    AppCompatResources.getDrawable(this, R.drawable.ic_menu)
                toolbar.setNavigationOnClickListener {
                    showAdminMenu(toolbar)
                }
            }
            AdminScreen.USERS -> {
                setBoldTitle("Customers")
                toolbar.navigationIcon =
                    AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back)
                toolbar.setNavigationOnClickListener {
                    binding.adminBottomNavigation.selectedItemId = R.id.admin_home
                }
            }
            AdminScreen.BOOKINGS -> {
                setBoldTitle("Check Bookings")
                toolbar.navigationIcon =
                    AppCompatResources.getDrawable(this, R.drawable.ic_arrow_back)
                toolbar.setNavigationOnClickListener {
                    binding.adminBottomNavigation.selectedItemId = R.id.admin_home
                }
            }
        }

        toolbar.navigationIcon?.setTint(Color.WHITE)
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)
    }

    private fun showAdminMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add("Logout")

        popup.setOnMenuItemClickListener { item ->
            if (item.title == "Logout") {
                val intent = Intent(this, LoginActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                    )
                }
                startActivity(intent)
                true
            } else {
                false
            }
        }

        popup.show()
    }

    private fun setBoldTitle(text: String) {
        val s = SpannableString(text)
        s.setSpan(StyleSpan(Typeface.BOLD), 0, s.length, 0)
        binding.topAppBar.title = s
    }
}
