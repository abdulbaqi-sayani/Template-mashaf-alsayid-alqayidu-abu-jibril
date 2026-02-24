package com.abdulbaqi.mashaf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemSurahBinding

// 1. تعريف كلاس Surah هنا مباشرة لتجنب خطأ "Unresolved reference: Surah"
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
            
            // 2. تعديل المعرفات لتطابق مشروعك (استخدمنا الأسماء الأكثر شيوعاً في هذا القالب)
            // إذا استمر الخطأ في أسماء العناصر، يرجى التأكد من الـ ID في ملف item_surah.xml
            
            binding.surahName.text = surah.name
            binding.surahName.setTextColor(Color.parseColor("#1565C0")) // اللون الأزرق
            
            binding.surahNumber.text = (position + 1).toString()
            binding.surahNumber.setTextColor(Color.parseColor("#1565C0")) // اللون الأزرق

            binding.root.setOnClickListener {
                onClick(position)
            }
        }
    }
}
