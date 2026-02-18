package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding
import com.abdulbaqi.mashaf.databinding.ItemSurahTitleBinding

class QuranAdapter(
    private val items: List<QItem>,
    private val amiri: Typeface?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_AYAH = 1
    }

    // ✅ هل نحن داخل “قسم دعاء”؟ (مثل: دعاء ختم القرآن)
    private val noNumberForAyah = BooleanArray(items.size)

    init {
        var inDuaSection = false
        for (i in items.indices) {
            when (val it = items[i]) {
                is QItem.SurahTitle -> {
                    // أي عنوان فيه “دعاء” اعتبره قسم دعاء
                    inDuaSection = it.name.contains("دعاء", ignoreCase = true)
                }
                is QItem.Ayah -> {
                    // داخل قسم الدعاء => لا رقم
                    if (inDuaSection) noNumberForAyah[i] = true

                    // لو كان هذا السطر نفسه “دعاء ختم...” => لا رقم
                    if (isDuaHeader(it.text)) noNumberForAyah[i] = true
                }
            }
        }
    }

    inner class TitleVH(val b: ItemSurahTitleBinding) : RecyclerView.ViewHolder(b.root)
    inner class AyahVH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is QItem.SurahTitle -> TYPE_TITLE
            is QItem.Ayah -> TYPE_AYAH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TITLE) {
            val b = ItemSurahTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TitleVH(b)
        } else {
            val b = ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AyahVH(b)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {

            is QItem.SurahTitle -> {
                val h = holder as TitleVH
                h.b.tvSurahName.typeface = amiri
                h.b.tvSurahName.text = item.name
            }

            is QItem.Ayah -> {
                val h = holder as AyahVH
                val ctx = h.itemView.context

                h.b.tvAyah.typeface = amiri
                h.b.tvAyah.text = item.text

                // ✅ قرّر هل نعرض رقم أم لا
                val showNumber = shouldShowNumber(
                    position = position,
                    surahIndex = item.surahIndex,
                    text = item.text
                )

                if (!showNumber) {
                    h.b.tvNumber.visibility = View.GONE
                } else {
                    h.b.tvNumber.visibility = View.VISIBLE

                    // رقم الآية داخل ﴿ ﴾ وبالأرقام العربية + باللون الأحمر
                    val red = ctx.getColor(android.R.color.holo_red_dark)
                    val numberText = buildSpannedString {
                        color(red) { append("﴿${toArabicDigits(item.ayahIndex + 1)}﴾") }
                    }
                    h.b.tvNumber.text = numberText
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(position: Int): QItem = items[position]

    // ---------------- Helpers ----------------

    private fun shouldShowNumber(position: Int, surahIndex: Int, text: String): Boolean {
        // 1) داخل قسم دعاء => لا رقم
        if (noNumberForAyah[position]) return false

        // 2) اللهم صل على محمد وآل محمد => لا رقم
        if (isSalawat(text)) return false

        // 3) بسم الله => لا رقم في كل السور إلا الفاتحة (surahIndex = 0)
        if (isBasmalah(text) && surahIndex != 0) return false

        // غير ذلك => رقم طبيعي
        return true
    }

    private fun isBasmalah(t: String): Boolean {
        val s = t.replace("ٮ", "ب") // أحيانًا عندك (ٮسم) بدل (بسم)
        return s.contains("بسم الله", ignoreCase = true) ||
                s.contains("بِسۡمِ ٱللَّهِ", ignoreCase = true)
    }

    private fun isSalawat(t: String): Boolean {
        val s = t.replace("ٮ", "ب")
        return s.contains("اللهم صل", ignoreCase = true) ||
                s.contains("اللَّهُمَّ صَلِّ", ignoreCase = true)
    }

    private fun isDuaHeader(t: String): Boolean {
        val s = t.replace("حتم", "ختم")
        return s.contains("دعاء ختم", ignoreCase = true) ||
                s.contains("دعاء", ignoreCase = true) && s.contains("القرآن", ignoreCase = true)
    }

    private fun toArabicDigits(input: Int): String {
        return input.toString()
            .replace('0', '٠')
            .replace('1', '١')
            .replace('2', '٢')
            .replace('3', '٣')
            .replace('4', '٤')
            .replace('5', '٥')
            .replace('6', '٦')
            .replace('7', '٧')
            .replace('8', '٨')
            .replace('9', '٩')
    }
}
