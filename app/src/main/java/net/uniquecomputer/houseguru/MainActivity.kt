package net.uniquecomputer.houseguru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import net.uniquecomputer.houseguru.databinding.ActivityMainBinding
import android.content.res.ColorStateList
import android.graphics.Color
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.topAppBar
        setSupportActionBar(toolbar)
        toolbar.title = "Home"

        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        val nav: BottomNavigationView = binding.bottomNavigation
        nav.itemActiveIndicatorColor = ColorStateList.valueOf(Color.parseColor("#A0D7FD"))
        nav.itemRippleColor = ColorStateList.valueOf(Color.parseColor("#33A0D7FD"))

        replaceFragment(Home())
        setHomeMode(true)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    replaceFragment(Home())
                    toolbar.title = "Car Shining"
                    setHomeMode(true)
                    true
                }
                R.id.Service -> {
                    replaceFragment(Service())
                    toolbar.title = "All Services"
                    setHomeMode(false)
                    true
                }
                R.id.Wallet -> {
                    replaceFragment(Wallet())
                    toolbar.title = "Wallet"
                    setHomeMode(false)
                    true
                }
                R.id.Booking -> {
                    replaceFragment(Booking())
                    toolbar.title = "My Bookings"
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
            binding.topAppBar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            binding.topAppBar.setNavigationOnClickListener {
                binding.bottomNavigation.selectedItemId = R.id.home
            }
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_about -> {}
            R.id.nav_contact -> {}
            R.id.nav_settings -> {}
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
}
