package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.LruCache
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
    }

    private val surahNameByIndex: HashMap<Int, String> = HashMap()
    private val spanCache = object : LruCache<Long, SpannableString>(900) {}

    init {
        // بناء خريطة أسماء السور مرة واحدة
        for (i in items.indices) {
            val qi = items[i]
            if (qi is QItem.SurahTitle) {
                surahNameByIndex[qi.surahIndex] = qi.name
            }
        }
        setHasStableIds(true)
    }

    fun getItemAt(pos: Int): QItem = items[pos]

    override fun getItemId(position: Int): Long {
        val item = items[position]
        return when (item) {
            is QItem.SurahTitle -> makeKey(item.surahIndex, -1, item.name.hashCode())
            is QItem.Ayah -> makeKey(item.surahIndex, item.ayahIndex, item.text.hashCode())
        }
    }

    private fun makeKey(s: Int, a: Int, h: Int): Long {
        val sPart = (s and 0xFFFF).toLong() shl 48
        val aPart = (a and 0xFFFF).toLong() shl 32
        val hPart = (h.toLong() and 0xFFFFFFFFL)
        return sPart or aPart or hPart
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
        val item = items[position]
        if (item is QItem.SurahTitle) {
            (holder as TitleVH).bind(item)
        } else if (item is QItem.Ayah) {
            (holder as AyahVH).bind(item)
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

            val surahName = surahNameByIndex[item.surahIndex].orEmpty()
            val cleanText = removeTrailingParenthesesNumber(item.text)

            val key = makeKey(item.surahIndex, item.ayahIndex, cleanText.hashCode())
            val cached = spanCache.get(key)
            if (cached != null) {
                b.tvAyah.text = cached
                return
            }

            val result = buildTextSpannable(item, surahName, cleanText)
            spanCache.put(key, result)
            b.tvAyah.text = result
        }

        private fun buildTextSpannable(item: QItem.Ayah, surahName: String, cleanText: String): SpannableString {
            // أسطر بلا أرقام
            if (isNoNumberLine(item.surahIndex, surahName, cleanText)) {
                return applyAllColors(cleanText, ornateNumberPart = null)
            }

            // حساب رقم العرض
            val basmalaFirst = hasBasmalaAsFirstAyah(item.surahIndex)
            val displayNumber = if (item.surahIndex != 0 && basmalaFirst) item.ayahIndex else item.ayahIndex + 1

            val ornate = formatOrnateAyahNumber(displayNumber)
            val finalText = "$cleanText  $ornate"
            return applyAllColors(finalText, ornateNumberPart = ornate)
        }

        // -------------------- منع الترقيم حسب شروطك --------------------

        private fun isNoNumberLine(surahIndex: Int, surahName: String, text: String): Boolean {
            if (isDuaKhatmQuranSurah(surahName)) return true
            if (isKhatimaSurah(surahName)) return true
            if (isSalawatLine(text)) return true
            if (isBasmalaLine(surahIndex, text)) return true
            return false
        }

        private fun isDuaKhatmQuranSurah(name: String): Boolean {
            val n = normalizeArabicForMatch(name)
            return n.contains("دعاء ختم") || n.contains("ختم القران") || n.contains("ختم القرآن")
        }

        private fun isKhatimaSurah(name: String): Boolean {
            val n = normalizeArabicForMatch(name)
            return n.contains("كلمة ختامية") || n.contains("كلمة ختاميه") || n.contains("خاتمة") || n.contains("ختامية")
        }

        private fun isSalawatLine(text: String): Boolean {
            val n = normalizeArabicForMatch(text)
            return n.contains("اللهم") && n.contains("صل") && n.contains("محمد")
        }

        private fun isBasmalaLine(surahIndex: Int, text: String): Boolean {
            if (surahIndex == 0) return false
            return containsBasmala(text)
        }

        private fun hasBasmalaAsFirstAyah(surahIndex: Int): Boolean {
            var first: QItem.Ayah? = null
            for (i in items.indices) {
                val qi = items[i]
                if (qi is QItem.Ayah && qi.surahIndex == surahIndex && qi.ayahIndex == 0) {
                    first = qi
                    break
                }
            }
            if (first == null) return false
            val txt = removeTrailingParenthesesNumber(first.text)
            return containsBasmala(txt)
        }

        private fun containsBasmala(text: String): Boolean {
            val n = normalizeArabicForMatch(text)
            return n.contains("بسم الله الرحمن الرحيم")
        }

        // -------------------- التلوين --------------------

        private fun applyAllColors(text: String, ornateNumberPart: String?): SpannableString {
            val ss = SpannableString(text)

            val green = ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark)
            val red = ContextCompat.getColor(b.root.context, android.R.color.holo_red_dark)

            // زخارف بالأخضر
            val deco = setOf('❁', '✿', '❀')
            for (i in text.indices) {
                if (deco.contains(text[i])) {
                    ss.setSpan(ForegroundColorSpan(green), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            // الله فقط
            val matches = ALLAH_PATTERN.findAll(text)
            for (m in matches) {
                ss.setSpan(ForegroundColorSpan(green), m.range.first, m.range.last + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            // إسرائيل + اليهود بالأحمر
            colorAllOccurrences(ss, text, "إسرائيل", red)
            colorAllOccurrences(ss, text, "اسرائيل", red)
            colorAllOccurrences(ss, text, "اليهود", red)

            // الرقم المزخرف بالأحمر
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

        // -------------------- أدوات نصية --------------------

        private fun normalizeArabicForMatch(text: String): String {
            return text
                .replace("ٰ", "")
                .replace("ٱ", "ا")
                .replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("ى", "ي")
                .replace("ـ", "")
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
