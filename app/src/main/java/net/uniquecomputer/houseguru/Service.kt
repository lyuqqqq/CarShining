package net.uniquecomputer.houseguru

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import net.uniquecomputer.houseguru.Adapter.HomeMaintenanceAdapter
import net.uniquecomputer.houseguru.Model.HomeMaintenceModel
import net.uniquecomputer.houseguru.databinding.FragmentServiceBinding

class Service : Fragment() {

    private lateinit var  binding : FragmentServiceBinding

    lateinit var homeMaintenanceAdapter: HomeMaintenanceAdapter
    private lateinit var homeMaintenanceArrayList : ArrayList<HomeMaintenceModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServiceBinding.inflate(layoutInflater,container,false)
        homeMaintenanceArrayList = ArrayList()

// 1. Discount Cleaning
        homeMaintenanceArrayList.add(
            HomeMaintenceModel(
                R.drawable.max_discont_cleaning,
                "Discount Cleaning",
                desc = "Basic exterior wash for a quick glossy shine.",
                duration = "30 min",
                price = "RM19"
            )
        )

// 2. Premium Cleaning
        homeMaintenanceArrayList.add(
            HomeMaintenceModel(
                R.drawable.max_premium_cleaning,
                "Premium Cleaning",
                desc = "Inside & outside detailing with protective finish.",
                duration = "90 min",
                price = "RM89"
            )
        )

// 3. Paint Repair
        homeMaintenanceArrayList.add(
            HomeMaintenceModel(
                R.drawable.max_paint_repair,
                "Paint Repair",
                desc = "Fix paint chips and light scratches on body panels.",
                duration = "45 min",
                price = "RM39"
            )
        )

// 4. Car Waxing
        homeMaintenanceArrayList.add(
            HomeMaintenceModel(
                R.drawable.max_car_waxing,
                "Car Waxing",
                desc = "Glossy finish with UV protection to seal your paint.",
                duration = "60 min",
                price = "RM49"
            )
        )

// 5. Car Disinfection
        homeMaintenanceArrayList.add(
            HomeMaintenceModel(
                R.drawable.max_car_disinfection,
                "Car Disinfection",
                desc = "A/C vent sanitization and interior disinfection.",
                duration = "40 min",
                price = "RM29"
            )
        )

// 6. Brand Maintenance
        homeMaintenanceArrayList.add(
            HomeMaintenceModel(
                R.drawable.max_brand_maintainance,
                "Brand Maintenance",
                desc = "Scheduled check & care for your vehicle brand.",
                duration = "75 min",
                price = "RM69"
            )
        )


        homeMaintenanceAdapter = HomeMaintenanceAdapter(requireContext(),homeMaintenanceArrayList)
        binding.homemaintenance.layoutManager = LinearLayoutManager(requireContext())
        binding.homemaintenance.adapter = homeMaintenanceAdapter
        return binding.root
    }



}