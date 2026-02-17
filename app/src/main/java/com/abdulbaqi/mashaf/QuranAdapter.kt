package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding

data class AyahRow(
    val text: String,
    val number: Int? // null => بدون رقم (عنوان السورة/اللهم صل...)
)

class AyahAdapter(
    private val items: List<AyahRow>,
    private val amiri: Typeface?
) : RecyclerView.Adapter<AyahAdapter.VH>() {

    class VH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val b = holder.b

        // خط المصحف
        if (amiri != null) b.tvAyah.typeface = amiri
        b.tvAyah.text = item.text

        // رقم الآية (مزخرف)
        if (item.number == null) {
            b.tvNumber.visibility = View.GONE
            b.tvNumber.text = ""
        } else {
            b.tvNumber.visibility = View.VISIBLE
            b.tvNumber.text = formatOrnateAyahNumber(item.number)
        }
    }

    override fun getItemCount(): Int = items.size

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

        // الشكل النهائي: ١۝
        return "$arabic۝"
    }
}
