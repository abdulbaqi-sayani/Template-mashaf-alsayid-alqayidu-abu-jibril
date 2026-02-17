package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding
import com.abdulbaqi.mashaf.databinding.ItemSurahTitleBinding

class QuranAdapter(
    private val items: List<QItem>,
    private val amiri: Typeface?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SURAH = 0
        private const val TYPE_AYAH = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is QItem.SurahTitle -> TYPE_SURAH
            is QItem.Ayah -> TYPE_AYAH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == TYPE_SURAH) {
            val binding = ItemSurahTitleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            SurahViewHolder(binding)
        } else {
            val binding = ItemAyahBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AyahViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (val item = items[position]) {

            is QItem.SurahTitle -> {
                val h = holder as SurahViewHolder
                h.b.tvSurahName.text = item.name
            }

            is QItem.Ayah -> {
                val h = holder as AyahViewHolder

                h.b.tvAyah.typeface = amiri
                h.b.tvAyah.text = item.text

                val ayahNumber = item.ayahIndex + 1
                h.b.tvNumber.visibility = View.VISIBLE
                h.b.tvNumber.text = formatOrnateAyahNumber(ayahNumber)
            }
        }
    }

    // ✅ تنسيق الرقم بهذا الشكل ﴿٥﴾
    private fun formatOrnateAyahNumber(n: Int): String {

        val arabic = n.toString()
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

        return "﴿$arabic﴾"
    }

    fun getItemAt(position: Int): QItem {
        return items[position]
    }

    class SurahViewHolder(val b: ItemSurahTitleBinding) :
        RecyclerView.ViewHolder(b.root)

    class AyahViewHolder(val b: ItemAyahBinding) :
        RecyclerView.ViewHolder(b.root)
}
