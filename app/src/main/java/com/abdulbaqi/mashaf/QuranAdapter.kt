package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding
import com.abdulbaqi.mashaf.databinding.ItemSurahTitleBinding

class QuranAdapter(
    private val items: List<QItem>,
    private val amiri: Typeface?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SURAH = 1
        private const val TYPE_AYAH = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is QItem.SurahTitle -> TYPE_SURAH
            is QItem.Ayah -> TYPE_AYAH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SURAH -> SurahVH(ItemSurahTitleBinding.inflate(inflater, parent, false))
            else -> AyahVH(ItemAyahBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {

            is QItem.SurahTitle -> {
                val h = holder as SurahVH
                h.b.tvSurahName.typeface = amiri
                h.b.tvSurahName.text = item.name
            }

            is QItem.Ayah -> {
                val h = holder as AyahVH
                h.b.tvAyah.typeface = amiri

                // ✅ رقم الآية داخل الأقواس (١) بدون أي رموز ﴿﴾
                val ayahNumber = (item.ayahIndex + 1) // يبدأ من 1
                val numberText = "(${toArabicDigits(ayahNumber)})"

                // ✅ الرقم في آخر الآية
                h.b.tvAyah.text = "${item.text} $numberText"
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(position: Int): QItem = items[position]

    class SurahVH(val b: ItemSurahTitleBinding) : RecyclerView.ViewHolder(b.root)
    class AyahVH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

    private fun toArabicDigits(n: Int): String {
        return n.toString()
            .replace("0", "٠")
            .replace("1", "١")
            .replace("2", "٢")
            .replace("3", "٣")
            .replace("4", "٤")
            .replace("5", "٥")
            .replace("6", "٦")
            .replace("7", "٧")
            .replace("8", "٨")
            .replace("9", "٩")
    }
}
