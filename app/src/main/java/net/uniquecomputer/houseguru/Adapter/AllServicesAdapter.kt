package net.uniquecomputer.houseguru.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import net.uniquecomputer.houseguru.Details
import net.uniquecomputer.houseguru.Model.AllServicesModel
import net.uniquecomputer.houseguru.databinding.ItemServiceRowBinding

class AllServicesAdapter(
    private val context: Context,
    private val homeMaintenanceArrayList: ArrayList<AllServicesModel>
) : RecyclerView.Adapter<AllServicesAdapter.ViewHolder>() {

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

        val openDetails: () -> Unit = {
            val ctx = holder.itemView.context
            val intent = Intent(ctx, Details::class.java).apply {
                putExtra("title", item.title)
                putExtra("image", item.image)
            }
            if (ctx !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            ctx.startActivity(intent)
        }

        holder.itemView.setOnClickListener { openDetails() }
        holder.binding.btnBook.setOnClickListener { openDetails() }
    }
}
