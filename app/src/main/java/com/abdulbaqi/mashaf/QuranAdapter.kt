package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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
        private val ALLAH_PATTERN = Regex(
            "(?<![\\u0600-\\u06FF])([ٱا]$DIACRITICS*ل$DIACRITICS*ل$DIACRITICS*ه$DIACRITICS*)(?![\\u0600-\\u06FF])"
        )
        private val TRAILING_PARENS_NUMBER = Regex("""\s*\(\s*[\d٠١٢٣٤٥٦٧٨٩]+\s*\)\s*$""")
        // رمز مخفي لفرض المحاذاة من اليمين لليسار
        private const val RTL_MARK = "\u200F" 
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is QItem.SurahTitle -> TYPE_TITLE
            is QItem.Ayah -> TYPE_AYAH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_TITLE) {
            TitleVH(ItemSurahTitleBinding.inflate(inflater, parent, false))
        } else {
            AyahVH(ItemAyahBinding.inflate(inflater, parent, false))
        }
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
        }
    }

    private inner class AyahVH(private val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(item: QItem.Ayah) {
            b.tvAyah.typeface = amiri

            val cleanText = removeTrailingParenthesesNumber(item.text)
            val surahName = findSurahName(item.surahIndex)

            // إذا كانت بسملة أو دعاء ختم، نعرضها بدون رقم مع فرض المحاذاة لليمين
            if (isNoNumberLine(item.surahIndex, surahName, cleanText)) {
                b.tvAyah.text = applyAllColors("$RTL_MARK$cleanText", ornateNumberPart = null)
                return
            }

            // حساب رقم العرض
            val basmalaFirst = hasBasmalaAsFirstAyah(item.surahIndex)
            val displayNumber = if (item.surahIndex != 0 && basmalaFirst) item.ayahIndex else item.ayahIndex + 1

            val ornate = formatOrnateAyahNumber(displayNumber)
            
            // إضافة رمز RTL لضمان أن الآية يمين والرقم في نهايتها
            val finalText = "$RTL_MARK$cleanText  $ornate"

            b.tvAyah.text = applyAllColors(finalText, ornateNumberPart = ornate)
        }

        private fun findSurahName(surahIndex: Int): String {
            val title = items.firstOrNull { it is QItem.SurahTitle && it.surahIndex == surahIndex } as? QItem.SurahTitle
            return title?.name ?: ""
        }

        private fun isNoNumberLine(surahIndex: Int, surahName: String, text: String): Boolean {
            if (isDuaKhatmQuranSurah(surahName)) return true
            if (isKhatimaSurah(surahName)) return true
            if (isSalawatLine(text)) return true
            if (isBasmalaLine(surahIndex, text)) return true
            return false
        }

        private fun isDuaKhatmQuranSurah(name: String): Boolean {
            val n = normalizeArabicForMatch(name)
            return n.contains("دعاء") || n.contains("ختم")
        }

        private fun isKhatimaSurah(name: String): Boolean {
            val n = normalizeArabicForMatch(name)
            return n.contains("كلمة") || n.contains("خاتمة") || n.contains("ختامية")
        }

        private fun isSalawatLine(text: String): Boolean {
            val n = normalizeArabicForMatch(text)
            return n.contains("اللهم") && n.contains("صل") && n.contains("محمد")
        }

        private fun isBasmalaLine(surahIndex: Int, text: String): Boolean {
            // سورة الفاتحة (index 0) نعرض البسملة مع رقمها
            if (surahIndex == 0) return false
            return containsBasmala(text)
        }

        private fun hasBasmalaAsFirstAyah(surahIndex: Int): Boolean {
            val firstAyah = items.firstOrNull {
                it is QItem.Ayah && it.surahIndex == surahIndex && it.ayahIndex == 0
            } as? QItem.Ayah ?: return false

            val txt = removeTrailingParenthesesNumber(firstAyah.text)
            return containsBasmala(txt)
        }

        private fun containsBasmala(text: String): Boolean {
            val n = normalizeArabicForMatch(text)
            return n.contains("بسم الله الرحمن الرحيم")
        }

        private fun applyAllColors(text: String, ornateNumberPart: String?): SpannableString {
            val ss = SpannableString(text)

            val green = ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark)
            val red = ContextCompat.getColor(b.root.context, android.R.color.holo_red_dark)

            val deco = setOf('❁', '✿', '❀')
            for (i in text.indices) {
                if (deco.contains(text[i])) {
                    ss.setSpan(ForegroundColorSpan(green), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            for (m in ALLAH_PATTERN.findAll(text)) {
                ss.setSpan(ForegroundColorSpan(green), m.range.first, m.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            colorAllOccurrences(ss, text, "إسرائيل", red)
            colorAllOccurrences(ss, text, "اسرائيل", red)
            colorAllOccurrences(ss, text, "اليهود", red)

            if (!ornateNumberPart.isNullOrEmpty()) {
                val start = text.lastIndexOf(ornateNumberPart)
                if (start >= 0) {
                    ss.setSpan(
                        ForegroundColorSpan(red),
                        start,
                        start + ornateNumberPart.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            return ss
        }

        private fun colorAllOccurrences(ss: SpannableString, text: String, needle: String, color: Int) {
            var idx = text.indexOf(needle)
            while (idx >= 0) {
                ss.setSpan(ForegroundColorSpan(color), idx, idx + needle.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                idx = text.indexOf(needle, idx + needle.length)
            }
        }

        private fun normalizeArabicForMatch(text: String): String {
            // حذف جميع علامات التشكيل بشكل كامل لضمان مطابقة الكلمات بنجاح
            val diacriticsRegex = Regex(DIACRITICS)
            return text.replace(diacriticsRegex, "")
                .replace("ٱ", "ا")
                .replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("ى", "ي")
                .replace("ـ", "")
                .replace("ة", "ه")
                .trim()
        }

        private fun removeTrailingParenthesesNumber(text: String): String {
            return text.trim().replace(TRAILING_PARENS_NUMBER, "").trim()
        }

        private fun formatOrnateAyahNumber(n: Int): String {
            val arabic = n.toString()
                .replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣")
                .replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧")
                .replace("8", "٨").replace("9", "٩")
            return "﴿$arabic﴾"
        }
    }
}
