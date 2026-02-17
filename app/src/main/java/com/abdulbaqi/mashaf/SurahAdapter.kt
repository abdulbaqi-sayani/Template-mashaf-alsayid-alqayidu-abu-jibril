package com.abdulbaqi.mashaf

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemSurahBinding

class SurahAdapter(
    private val names: List<String>,
    private val bookmarkedIndex: Int,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SurahAdapter.SurahVH>() {

    class SurahVH(val b: ItemSurahBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahVH {
        val binding = ItemSurahBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SurahVH(binding)
    }

    override fun onBindViewHolder(holder: SurahVH, position: Int) {
        val name = names[position]
        holder.b.tvName.text = name

        holder.b.ivBookmark.visibility =
            if (position == bookmarkedIndex) View.VISIBLE else View.GONE

        holder.b.root.setOnClickListener {
            onClick(position) // ✅ يرسل رقم السورة الصحيح
        }
    }

    override fun getItemCount(): Int = names.size
}
