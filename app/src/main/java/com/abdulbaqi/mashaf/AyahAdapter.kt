package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding

class AyahAdapter(
    private val items: List<String>,
    private val typeface: Typeface?
) : RecyclerView.Adapter<AyahAdapter.VH>() {

    class VH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    private fun toArabicDigits(n: Int): String {
        val d = arrayOf("٠","١","٢","٣","٤","٥","٦","٧","٨","٩")
        return n.toString().map { d[it - '0'] }.joinToString("")
    }

    // ✅ حذف أرقام الأقواس من النص: (1) (٢) ... إلخ
    private fun removeBracketNumbers(text: String): String {
        // يحذف (123) أو (١٢٣)
        return text.replace(Regex("\\(([0-9٠-٩]+)\\)"), "").trim()
    }

    // ✅ هل هذا السطر عنوان سورة؟ (كما في ملفاتك: "سوره ...")
    private fun isSurahTitle(text: String): Boolean {
        val t = text.trim()
        return t.contains("سوره") || t.contains("سورة")
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

        val raw = items[position].trim()

        // ✅ عنوان السورة: لا رقم مزخرف ولا أقواس
        if (position == 0 && isSurahTitle(raw)) {
            holder.b.tvAyah.text = raw
            if (typeface != null) holder.b.tvAyah.typeface = typeface
            return
        }

        // ✅ آية عادية: احذف (1)(2) من النص ثم أضف رقم مزخرف واحد فقط
        val cleanText = removeBracketNumbers(raw)

        val number = toArabicDigits(position)  // لأن position=1 ستكون أول آية بعد العنوان
        val marker = "﴿" + number + "﴾"

        // ضع الرقم قبل النص ليظهر يميناً (بداية السطر في RTL)
        holder.b.tvAyah.text = marker + "  " + cleanText

        if (typeface != null) holder.b.tvAyah.typeface = typeface
    }

    override fun getItemCount(): Int = items.size
}
