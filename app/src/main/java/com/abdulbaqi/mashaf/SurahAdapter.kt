package com.abdulbaqi.mashaf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemSurahBinding

class SurahAdapter(
    private val surahList: List<Surah>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SurahAdapter.SurahVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahVH {
        val binding = ItemSurahBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SurahVH(binding)
    }

    override fun onBindViewHolder(holder: SurahVH, position: Int) {
        holder.bind(surahList[position])
    }

    override fun getItemCount(): Int = surahList.size

    inner class SurahVH(
        private val binding: ItemSurahBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(surah: Surah) {
            binding.tvName.text = surah.name
            binding.tvName.setTextColor(Color.parseColor("#1565C0"))

            binding.root.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClick(pos)
                }
            }
        }
    }
}
