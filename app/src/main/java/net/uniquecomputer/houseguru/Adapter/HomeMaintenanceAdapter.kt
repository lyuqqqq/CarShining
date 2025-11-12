package net.uniquecomputer.houseguru.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import net.uniquecomputer.houseguru.Details
import net.uniquecomputer.houseguru.Model.HomeMaintenceModel
import net.uniquecomputer.houseguru.databinding.ItemServiceRowBinding

class HomeMaintenanceAdapter(
    private val context: Context,
    private val homeMaintenanceArrayList: ArrayList<HomeMaintenceModel>
) : RecyclerView.Adapter<HomeMaintenanceAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemServiceRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemServiceRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = homeMaintenanceArrayList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = homeMaintenanceArrayList[position]

        holder.binding.imgRight.setImageResource(item.image)

        holder.binding.tvTitle.text = item.title
        holder.binding.tvDesc.text =
            if (item.desc.isNotBlank()) item.desc else "Professional mobile car care at your doorstep"
        holder.binding.tvMeta.text =
            "${item.duration.ifBlank { "45 min" }} · ${item.price.ifBlank { "$39" }}"

        holder.itemView.setOnClickListener {
            Toast.makeText(context, "Click", Toast.LENGTH_LONG).show()
            val intent = Intent(context, Details::class.java)
            intent.putExtra("title", item.title)
            intent.putExtra("image", item.image)
            context.startActivity(intent)
        }

        holder.binding.btnBook.setOnClickListener {
            Toast.makeText(context, "Booking: ${item.title}", Toast.LENGTH_SHORT).show()
        }
    }
}
