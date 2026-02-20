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
) : RecyclerView.Adapter<SurahAdapter.VH>() {

    init {
        setHasStableIds(true)
    }

    class VH(val b: ItemSurahBinding) : RecyclerView.ViewHolder(b.root)

    override fun getItemId(position: Int): Long {
        // ثابت لتحسين إعادة الاستخدام
        return names.getOrNull(position)?.hashCode()?.toLong() ?: position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemSurahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val name = names.getOrNull(position).orEmpty()
        holder.b.tvName.text = name

        // إظهار علامة المرجعية فقط عند السورة المحفوظة
        holder.b.ivBookmark.visibility = if (position == bookmarkedIndex) View.VISIBLE else View.GONE

        holder.b.root.setOnClickListener {
            val p = holder.bindingAdapterPosition
            if (p != RecyclerView.NO_POSITION) onClick(p)
        }
    }

    override fun getItemCount(): Int = names.size
}
