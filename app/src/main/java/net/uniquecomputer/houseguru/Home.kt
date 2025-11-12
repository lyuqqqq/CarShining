package net.uniquecomputer.houseguru
import net.uniquecomputer.houseguru.Adapter.QuickServiceAdapter
import net.uniquecomputer.houseguru.Model.QuickService

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import net.uniquecomputer.houseguru.Adapter.MostUseAdapter
import net.uniquecomputer.houseguru.Model.MostUseModel
import net.uniquecomputer.houseguru.databinding.FragmentHomeBinding
import org.imaginativeworld.whynotimagecarousel.ImageCarousel
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem

class Home : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var mostusedarraylist : ArrayList<MostUseModel>
    lateinit var mostUseAdapter: MostUseAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater,container,false)
        mostusedarraylist = ArrayList()
        mostusedarraylist.add(MostUseModel(R.drawable.max_discont_cleaning,"Discount Cleaning"))
        mostusedarraylist.add(MostUseModel(R.drawable.max_car_disinfection,"Car Disinfection"))
        mostusedarraylist.add(MostUseModel(R.drawable.max_car_waxing,"Car Waxing"))
        mostusedarraylist.add(MostUseModel(R.drawable.max_paint_repair,"Paint Repair"))
        mostusedarraylist.add(MostUseModel(R.drawable.max_brand_maintainance,"Brand Maintenance"))
        mostusedarraylist.add(MostUseModel(R.drawable.max_premium_cleaning,"Premium Cleaning"))

        mostUseAdapter = MostUseAdapter(requireContext(),mostusedarraylist)
        binding.mostusehomerv.layoutManager = GridLayoutManager(requireContext(),2)
//        binding.mostusehomerv.layoutManager = LinearLayoutManager(requireContext())
        binding.mostusehomerv.adapter = mostUseAdapter

        val quickList = listOf(
            QuickService("Discount Wash", R.drawable.discount_wash),
            QuickService("Premium Wash",  R.drawable.premium_wash),
            QuickService("Paint Repair",  R.drawable.paint_repair),
            QuickService("All Services",  R.drawable.all_services)
        )

        binding.rvQuickServices.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                requireContext(), androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false
            )
            adapter = QuickServiceAdapter(quickList) { item ->
                when (item.label) {
                    "Discount Wash" -> { /* TODO: navigate to discount list */ }
                    "Premium Wash"  -> { /* TODO: navigate to premium list */ }
                    "Paint Repair"  -> { /* TODO: navigate to paint repair */ }
                    "All Services"  -> {

                    }
                }
            }
        }
        showsliderimage()
        return binding.root

    }

    private fun showsliderimage() {
        val carousel: ImageCarousel = binding.carousel
        carousel.addData(CarouselItem(R.drawable.slideone))
        carousel.addData(CarouselItem(R.drawable.slidetwo))
        carousel.addData(CarouselItem(R.drawable.slidethree))
        carousel.addData(CarouselItem(R.drawable.slidefour))

    }


}