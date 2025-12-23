package net.uniquecomputer.houseguru

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import net.uniquecomputer.houseguru.Adapter.AllServicesAdapter
import net.uniquecomputer.houseguru.Model.AllServicesModel
import net.uniquecomputer.houseguru.databinding.FragmentServiceBinding

class Service : Fragment() {

    private lateinit var binding: FragmentServiceBinding
    lateinit var allServicesAdapter: AllServicesAdapter
    private lateinit var allServicesArrayList: ArrayList<AllServicesModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentServiceBinding.inflate(layoutInflater, container, false)
        allServicesArrayList = ArrayList()

        allServicesArrayList.add(
            AllServicesModel(
                R.drawable.max_discont_cleaning,
                "Discount Cleaning",
                desc = "Basic exterior wash for a quick glossy shine.",
                duration = "30 min",
                price = "RM19"
            )
        )
        allServicesArrayList.add(
            AllServicesModel(
                R.drawable.max_premium_cleaning,
                "Premium Cleaning",
                desc = "Inside & outside detailing with protective finish.",
                duration = "90 min",
                price = "RM89"
            )
        )
        allServicesArrayList.add(
            AllServicesModel(
                R.drawable.max_paint_repair,
                "Paint Repair",
                desc = "Fix paint chips and light scratches on body panels.",
                duration = "45 min",
                price = "RM39"
            )
        )
        allServicesArrayList.add(
            AllServicesModel(
                R.drawable.max_car_waxing,
                "Car Waxing",
                desc = "Glossy finish with UV protection to seal your paint.",
                duration = "60 min",
                price = "RM49"
            )
        )
        allServicesArrayList.add(
            AllServicesModel(
                R.drawable.max_car_disinfection,
                "Car Disinfection",
                desc = "A/C vent sanitization and interior disinfection.",
                duration = "40 min",
                price = "RM29"
            )
        )
        allServicesArrayList.add(
            AllServicesModel(
                R.drawable.max_brand_maintainance,
                "Brand Maintenance",
                desc = "Scheduled check & care for your vehicle brand.",
                duration = "75 min",
                price = "RM69"
            )
        )

        allServicesAdapter = AllServicesAdapter(requireContext(), allServicesArrayList)
        binding.allservices.layoutManager = LinearLayoutManager(requireContext())
        binding.allservices.adapter = allServicesAdapter

        return binding.root
    }
}
