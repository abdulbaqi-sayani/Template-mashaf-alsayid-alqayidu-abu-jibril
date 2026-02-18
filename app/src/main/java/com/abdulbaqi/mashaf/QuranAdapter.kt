package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
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
        private const val TYPE_TITLE = 0
        private const val TYPE_AYAH = 1
    }

    // ✅ علشان “قسم الدعاء” مثل دعاء ختم القرآن
    private val noNumberForAyah = BooleanArray(items.size)

    init {
        var inDuaSection = false
        for (i in items.indices) {
            when (val it = items[i]) {
                is QItem.SurahTitle -> {
                    inDuaSection = it.name.contains("دعاء", ignoreCase = true)
                }
                is QItem.Ayah -> {
                    if (inDuaSection) noNumberForAyah[i] = true
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
            TitleVH(ItemSurahTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            AyahVH(ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

                val showNumber = shouldShowNumber(
                    position = position,
                    surahIndex = item.surahIndex,
                    text = item.text
                )

                val red = ctx.getColor(android.R.color.holo_red_dark)
                val green = ctx.getColor(android.R.color.holo_green_dark)

                val sb = SpannableStringBuilder()

                if (showNumber) {
                    val num = "﴿${toArabicDigits(item.ayahIndex + 1)}﴾"
                    val start = sb.length
                    sb.append(num).append("  ")
                    sb.setSpan(
                        ForegroundColorSpan(red),
                        start,
                        start + num.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                val textStart = sb.length
                sb.append(item.text)

                // ✅ لوّن الزخارف ❁ ❀ ✿ باللون الأخضر داخل النص
                colorDecorations(sb, textStart, sb.length, green)

                // ✅ (اختياري) لو عندك رموز ﴿﴾ داخل النص نفسه وتريدها خضراء/لا شيء، اتركها كما هي.
                h.b.tvAyah.text = sb
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(position: Int): QItem = items[position]

    // ---------------- Rules ----------------

    private fun shouldShowNumber(position: Int, surahIndex: Int, text: String): Boolean {
        if (noNumberForAyah[position]) return false
        if (isSalawat(text)) return false

        // ✅ بسم الله: بدون رقم في كل السور إلا الفاتحة (surahIndex = 0)
        if (isBasmalah(text) && surahIndex != 0) return false

        return true
    }

    private fun isBasmalah(t: String): Boolean {
        val s = t.replace("ٮ", "ب")
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
            (s.contains("دعاء", ignoreCase = true) && s.contains("القرآن", ignoreCase = true))
    }

    // ---------------- Styling helpers ----------------

    private fun colorDecorations(sb: SpannableStringBuilder, from: Int, to: Int, color: Int) {
        val targets = charArrayOf('❁', '❀', '✿')
        for (i in from until to) {
            val c = sb[i]
            if (targets.contains(c)) {
                sb.setSpan(
                    ForegroundColorSpan(color),
                    i,
                    i + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
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
