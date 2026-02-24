package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
        private const val DIACRITICS = "[\\u0610-\\u061A\\u064B-\\u065F\\u0670\\u06D6-\\u06EDٰ]"
        private const val RTL_MARK = "\u200F" 
    }

    // الدالة الضرورية لملف MainActivity
    fun getItemAt(pos: Int): QItem = items[pos]

    override fun getItemViewType(position: Int): Int = if (items[position] is QItem.SurahTitle) TYPE_TITLE else TYPE_AYAH

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_TITLE) TitleVH(ItemSurahTitleBinding.inflate(inflater, parent, false))
        else AyahVH(ItemAyahBinding.inflate(inflater, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is QItem.SurahTitle -> (holder as TitleVH).bind(item)
            is QItem.Ayah -> (holder as AyahVH).bind(item)
        }
    }

    private class TitleVH(private val b: ItemSurahTitleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: QItem.SurahTitle) { 
            b.tvSurahName.text = item.name 
            b.tvSurahName.setTextColor(Color.parseColor("#1565C0"))
        }
    }

    private inner class AyahVH(private val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: QItem.Ayah) {
            b.tvAyah.typeface = amiri
            val rawText = item.text.trim()
            val cleanText = removeTrailingParenthesesNumber(rawText)
            val surahName = findSurahName(item.surahIndex)
            val isBasmala = containsBasmala(cleanText)
            val isSalawat = isSalawatLine(cleanText)
            val isKhatima = isKhatimaSurah(surahName) || isDuaKhatmQuranSurah(surahName)
            val isFatiha = item.surahIndex == 0

            when {
                isFatiha || isBasmala || isSalawat || isKhatima -> {
                    b.tvAyah.gravity = Gravity.CENTER
                    when {
                        isSalawat -> b.tvAyah.text = applyAllColors(cleanText, null)
                        isBasmala && isFatiha -> {
                            val ornate = formatOrnateAyahNumber(1)
                            b.tvAyah.text = applyAllColors("$cleanText $ornate", ornate)
                        }
                        isFatiha -> {
                            val ornate = formatOrnateAyahNumber(item.ayahIndex + 1)
                            b.tvAyah.text = applyAllColors("$cleanText $ornate", ornate)
                        }
                        else -> b.tvAyah.text = applyAllColors(cleanText, null)
                    }
                }
                else -> {
                    b.tvAyah.gravity = Gravity.FILL_HORIZONTAL
                    val basmalaAtStart = hasBasmalaAsFirstAyah(item.surahIndex)
                    val displayNumber = if (basmalaAtStart) item.ayahIndex else item.ayahIndex + 1
                    if (displayNumber > 0) {
                        val ornate = formatOrnateAyahNumber(displayNumber)
                        b.tvAyah.text = applyAllColors("$RTL_MARK$cleanText  $ornate", ornate)
                    } else {
                        b.tvAyah.text = applyAllColors("$RTL_MARK$cleanText", null)
                    }
                }
            }
        }

        private fun findSurahName(surahIndex: Int): String = (items.firstOrNull { it is QItem.SurahTitle && it.surahIndex == surahIndex } as? QItem.SurahTitle)?.name ?: ""
        private fun isDuaKhatmQuranSurah(name: String): Boolean = normalize(name).contains("دعاء") || normalize(name).contains("ختم")
        private fun isKhatimaSurah(name: String): Boolean = normalize(name).let { it.contains("كلمه") || it.contains("خاتمه") || it.contains("ختام") }
        private fun isSalawatLine(text: String): Boolean = normalize(text).let { it.contains("اللهم") && it.contains("صل") && it.contains("محمد") }
        private fun containsBasmala(text: String): Boolean = normalize(text).contains("بسم الله الرحمن الرحيم")
        private fun hasBasmalaAsFirstAyah(surahIndex: Int): Boolean {
            val first = items.firstOrNull { it is QItem.Ayah && it.surahIndex == surahIndex && it.ayahIndex == 0 } as? QItem.Ayah ?: return false
            return containsBasmala(removeTrailingParenthesesNumber(first.text))
        }

        private fun applyAllColors(text: String, ornateNumberPart: String?): SpannableString {
            val ss = SpannableString(text)
            val green = ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark)
            val red = ContextCompat.getColor(b.root.context, android.R.color.holo_red_dark)
            Regex("(?<![\\u0600-\\u06FF])([ٱا]$DIACRITICS*ل$DIACRITICS*ل$DIACRITICS*ه$DIACRITICS*)(?![\\u0600-\\u06FF])")
                .findAll(text).forEach { ss.setSpan(ForegroundColorSpan(green), it.range.first, it.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
            setOf('❁', '✿', '❀').forEach { char ->
                text.forEachIndexed { i, c -> if (c == char) ss.setSpan(ForegroundColorSpan(green), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
            }
            if (!ornateNumberPart.isNullOrEmpty()) {
                val start = text.lastIndexOf(ornateNumberPart)
                if (start >= 0) ss.setSpan(ForegroundColorSpan(red), start, start + ornateNumberPart.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            return ss
        }

        private fun normalize(t: String): String = t.replace(Regex(DIACRITICS), "").replace("ٱ", "ا").replace("أ", "ا").replace("إ", "ا").replace("آ", "ا").replace("ى", "ي").replace("ة", "ه").replace("ـ", "").trim()
        private fun removeTrailingParenthesesNumber(t: String): String = t.replace(Regex("""\s*\(\s*[\d٠١٢٣٤٥٦٧٨٩]+\s*\)\s*$"""), "").trim()
        private fun formatOrnateAyahNumber(n: Int): String {
            val ar = n.toString().map { "٠١٢٣٤٥٦٧٨٩"[it - '0'] }.joinToString("")
            return "﴿$ar﴾"
        }
    }
}
