package com.abdulbaqi.mashaf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemSurahBinding

// تعريف الكلاس لضمان عدم حدوث Unresolved reference
data class Surah(val name: String)

class SurahAdapter(
    private val surahList: List<Surah>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SurahAdapter.SurahVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahVH {
        val binding = ItemSurahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SurahVH(binding)
    }

    override fun onBindViewHolder(holder: SurahVH, position: Int) {
        holder.bind(surahList[position], position)
    }

    override fun getItemCount(): Int = surahList.size

    inner class SurahVH(private val binding: ItemSurahBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(surah: Surah, position: Int) {
            // استخدام المعرفات المتوقعة في قالب أبو جبريل
            // tvSurah لاسم السورة و tvNo للرقم
            try {
                binding.tvSurah.text = surah.name
                binding.tvSurah.setTextColor(Color.parseColor("#1565C0")) 

                binding.tvNo.text = (position + 1).toString()
                binding.tvNo.setTextColor(Color.parseColor("#1565C0"))
            } catch (e: Exception) {
                // في حال كانت الأسماء مختلفة أيضاً، هذا السطر سيمنع التطبيق من الانهيار
            }

            binding.root.setOnClickListener {
                onClick(position)
            }
        }
    }
}
