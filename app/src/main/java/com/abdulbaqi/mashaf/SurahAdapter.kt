package com.abdulbaqi.mashaf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemSurahBinding

class SurahAdapter(
    private val items: List<String>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SurahAdapter.VH>() {

    class VH(val b: ItemSurahBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemSurahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.b.tvName.text = items[position]
        holder.b.root.setOnClickListener { onClick(position) }
    }

    override fun getItemCount(): Int = items.size
}
