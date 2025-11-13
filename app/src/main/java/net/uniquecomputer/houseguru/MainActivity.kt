package net.uniquecomputer.houseguru

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import android.content.res.ColorStateList
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import net.uniquecomputer.houseguru.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)

        setBoldTitle("Car Shining")
        toolbar.setTitleTextColor(Color.WHITE)
        toolbar.overflowIcon?.setTint(Color.WHITE)

        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        drawerToggle.drawerArrowDrawable.color = Color.WHITE
        binding.topAppBar.navigationIcon?.setTint(Color.WHITE)

        val header = binding.navView.getHeaderView(0)
        val headerToolbar =
            header.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.drawerToolbar)
        headerToolbar.setNavigationOnClickListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        binding.navView.setNavigationItemSelectedListener(this)

        val nav: BottomNavigationView = binding.bottomNavigation
        nav.itemActiveIndicatorColor = ColorStateList.valueOf(Color.parseColor("#90CAF9"))  // 选中项的“悬浮胶囊”背景
        nav.itemRippleColor = ColorStateList.valueOf(Color.parseColor("#3390CAF9"))          // 点击涟漪（可选，33=~20%透明）


        replaceFragment(Home())
        setHomeMode(true)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(Home())
                    setBoldTitle("Car Shining")
                    setHomeMode(true)
                    true
                }
                R.id.Service -> {
                    replaceFragment(Service())
                    setBoldTitle("All Services")
                    setHomeMode(false)
                    true
                }
                R.id.Wallet -> {
                    replaceFragment(Wallet())
                    setBoldTitle("Wallet")
                    setHomeMode(false)
                    true
                }
                R.id.Booking -> {
                    replaceFragment(Booking())
                    setBoldTitle("My Bookings")
                    setHomeMode(false)
                    true
                }
                else -> false
            }
        }
    }

    private fun setHomeMode(isHome: Boolean) {
        drawerToggle.isDrawerIndicatorEnabled = isHome
        drawerToggle.syncState()

        if (!isHome) {

            binding.topAppBar.setNavigationIcon(R.drawable.ic_arrow_back)
            binding.topAppBar.navigationIcon?.setTint(Color.WHITE)

            binding.topAppBar.setNavigationOnClickListener {
                binding.bottomNavigation.selectedItemId = R.id.home
            }

            binding.topAppBar.overflowIcon?.setTint(Color.WHITE)
        } else {

            drawerToggle.isDrawerIndicatorEnabled = true
            drawerToggle.syncState()
            binding.topAppBar.navigationIcon?.setTint(Color.WHITE)
            binding.topAppBar.setNavigationOnClickListener {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
            binding.topAppBar.overflowIcon?.setTint(Color.WHITE)
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_about -> {
                // TODO: implement
            }
            R.id.nav_contact -> {
                // TODO: implement
            }
            R.id.nav_settings -> {
                // TODO: implement
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun setBoldTitle(text: String) {
        val s = SpannableString(text)
        s.setSpan(StyleSpan(Typeface.BOLD), 0, s.length, 0)
        binding.topAppBar.title = s
    }
}
