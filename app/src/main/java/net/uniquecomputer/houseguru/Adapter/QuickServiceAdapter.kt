package net.uniquecomputer.houseguru.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.uniquecomputer.houseguru.R
import net.uniquecomputer.houseguru.Model.QuickService
class QuickServiceAdapter(
    private val items: List<QuickService>,
    private val onClick: (QuickService) -> Unit
) : RecyclerView.Adapter<QuickServiceAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val iv: ImageView = v.findViewById(R.id.ivIcon)
        val tv: TextView = v.findViewById(R.id.tvLabel)
        init { v.setOnClickListener { onClick(items[bindingAdapterPosition]) } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quick_service, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.iv.setImageResource(item.iconRes)
        holder.tv.text = item.label
    }

    override fun getItemCount() = items.size
}
