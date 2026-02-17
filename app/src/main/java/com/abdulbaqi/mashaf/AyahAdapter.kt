package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.view.LayoutInflater
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

        if (amiri != null) b.tvAyah.typeface = amiri

        val cleanText = removeBracketNumbers(item.text)

        b.tvAyah.text = if (item.number == null) {
            cleanText
        } else {
            // ✅ رقم الآية في آخر الآية
            "$cleanText  ${formatOrnateAyahNumber(item.number)}"
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

        return "$arabic۝"
    }

    private fun removeBracketNumbers(s: String): String {
        // يحذف: (1) ( 2 ) (٣) ... سواء أرقام عربية أو إنجليزية
        return s.replace(Regex("\\(\\s*[0-9٠-٩]+\\s*\\)"), "").trim()
    }
}
