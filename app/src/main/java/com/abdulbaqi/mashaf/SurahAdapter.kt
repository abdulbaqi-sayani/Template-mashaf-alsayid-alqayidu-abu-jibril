package com.abdulbaqi.mashaf

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemSurahBinding

// تعريف كلاس البيانات لضمان عدم حدوث خطأ Unresolved reference
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
        holder.bind(surahList[position])
    }

    override fun getItemCount(): Int = surahList.size

    inner class SurahVH(private val binding: ItemSurahBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(surah: Surah) {
            // المعرف الصحيح حسب ملف الـ XML الذي أرسلته هو tvName
            binding.tvName.text = surah.name
            
            // جعل لون اسم السورة أزرق في الفهرس
            binding.tvName.setTextColor(Color.parseColor("#1565C0"))

            binding.root.setOnClickListener {
                onClick(adapterPosition)
            }
        }
    }
}
