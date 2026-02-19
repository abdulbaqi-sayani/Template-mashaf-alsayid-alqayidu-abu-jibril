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
    }

    fun getItemAt(pos: Int): QItem = items[pos]

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

            val surahName = findSurahName(item.surahIndex)
            val cleanText = removeTrailingParenthesesNumber(item.text)

            // 1) أسطر بلا أرقام إطلاقًا:
            // - دعاء ختم القرآن
            // - كلمة ختامية
            // - الصلاة على محمد وآل محمد
            // - البسملة في كل السور ما عدا الفاتحة
            if (isNoNumberLine(item.surahIndex, surahName, cleanText)) {
                b.tvAyah.text = applyAllColors(cleanText, ornateNumberPart = null)
                return
            }

            // 2) رقم العرض (العدّ يبدأ من أول آية غير البسملة إذا كانت البسملة موجودة كأول عنصر)
            val basmalaFirst = hasBasmalaAsFirstAyah(item.surahIndex)
            val displayNumber = if (item.surahIndex != 0 && basmalaFirst) {
                item.ayahIndex // index=1 -> رقم 1
            } else {
                item.ayahIndex + 1
            }

            // 3) الرقم المزخرف في نهاية الآية
            val ornate = formatOrnateAyahNumber(displayNumber)
            val finalText = "$cleanText  $ornate"

            b.tvAyah.text = applyAllColors(finalText, ornateNumberPart = ornate)
        }

        // -------------------- قواعد منع الترقيم --------------------

        private fun isNoNumberLine(surahIndex: Int, surahName: String, text: String): Boolean {
            // (A) دعاء ختم القرآن: كل الأسطر بلا أرقام
            if (isDuaKhatmQuranSurah(surahName)) return true

            // (B) كلمة ختامية: كل الأسطر بلا أرقام
            if (isKhatimaSurah(surahName)) return true

            // (C) الصلاة على محمد وآل محمد: بلا رقم
            if (isSalawatLine(text)) return true

            // (D) البسملة: بلا رقم في كل السور إلا الفاتحة
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
            // يكفي وجود "اللهم" + "صل" + "محمد" في نفس السطر
            return n.contains("اللهم") && n.contains("صل") && n.contains("محمد")
        }

        private fun isBasmalaLine(surahIndex: Int, text: String): Boolean {
            // الفاتحة (index=0) مسموح للبسملة رقم، فلا نمنعها
            if (surahIndex == 0) return false

            // إذا السطر يحتوي البسملة → ممنوع رقم
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

        // -------------------- ألوان وتنسيق --------------------

        private fun applyAllColors(text: String, ornateNumberPart: String?): SpannableString {
            // 1) تلوين الزخارف ❁ ✿ ❀ بالأخضر
            // 2) تلوين لفظ الجلالة بالأخضر
            // 3) تلوين الرقم المزخرف بالأحمر (إن وجد)
            val ss = SpannableString(text)

            val green = ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark)
            val red = ContextCompat.getColor(b.root.context, android.R.color.holo_red_dark)

            // (A) زخارف: ❁ ✿ ❀
            val deco = setOf('❁', '✿', '❀')
            for (i in text.indices) {
                if (deco.contains(text[i])) {
                    ss.setSpan(ForegroundColorSpan(green), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            // (B) تلوين لفظ الجلالة (الله / اللَّه / لله / إله)
            colorAllahWords(ss, text, green)

            // (C) الرقم المزخرف بالأحمر
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

        private fun colorAllahWords(ss: SpannableString, original: String, green: Int) {
            // نبحث في النص الأصلي عن "الله" بأشكالها، بدون ما نكسر الإعراب.
            // نلوّن أي مقطع يحتوي: الله
            // وكذلك "لله" و"إله" (لكن نركّز على وجود "الله" صراحة أو "لله")
            // الحل العملي: نبحث عن تسلسل "الل" ثم "ه" مع أي حركات بينهم.
            val simplified = removeDiacritics(original)

            // مواضع "الله"
            highlightAllOccurrences(ss, original, simplified, "الله", green)

            // مواضع "لله" (قد لا تُلتقط لو كانت بدون ألف قبلها في النص الأصلي)
            highlightAllOccurrences(ss, original, simplified, "لله", green)

            // مواضع "إله" (اختياري)
            highlightAllOccurrences(ss, original, simplified, "إله", green)
            highlightAllOccurrences(ss, original, simplified, "اله", green)
        }

        private fun highlightAllOccurrences(
            ss: SpannableString,
            original: String,
            simplifiedOriginal: String,
            needle: String,
            color: Int
        ) {
            var idx = simplifiedOriginal.indexOf(needle)
            while (idx >= 0) {
                // نحاول إسقاط هذا الموضع على الأصل:
                // بما أننا حذفنا الحركات فقط، طول الأصل قد يكون أكبر.
                // نحدد مدى تقريبي عبر التقدم في الأصل حتى نصل لنفس عدد الحروف بدون حركات.
                val (start, end) = mapSimplifiedRangeToOriginal(original, idx, idx + needle.length)
                if (start >= 0 && end > start && end <= original.length) {
                    ss.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                idx = simplifiedOriginal.indexOf(needle, idx + needle.length)
            }
        }

        private fun mapSimplifiedRangeToOriginal(original: String, sStart: Int, sEnd: Int): Pair<Int, Int> {
            var oIndex = 0
            var sIndex = 0
            var oStart = -1
            var oEnd = -1

            while (oIndex < original.length && sIndex < sEnd) {
                val ch = original[oIndex]
                val isDiacritic = isArabicDiacritic(ch)

                if (!isDiacritic) {
                    if (sIndex == sStart) oStart = oIndex
                    sIndex++
                    if (sIndex == sEnd) {
                        oEnd = oIndex + 1
                        break
                    }
                }
                oIndex++
            }

            if (oStart == -1 || oEnd == -1) return -1 to -1
            return oStart to oEnd
        }

        private fun removeDiacritics(text: String): String {
            val sb = StringBuilder(text.length)
            for (c in text) {
                if (!isArabicDiacritic(c)) sb.append(c)
            }
            return sb.toString()
        }

        private fun isArabicDiacritic(c: Char): Boolean {
            // نطاقات حركات عربية شائعة + علامات قرآنية (تقريب عملي)
            val code = c.code
            return (code in 0x064B..0x065F) || (code in 0x0610..0x061A) || (code in 0x06D6..0x06ED) || c == 'ٰ'
        }

        // -------------------- أدوات نصية --------------------

        private fun findSurahName(surahIndex: Int): String {
            val title = items.firstOrNull {
                it is QItem.SurahTitle && it.surahIndex == surahIndex
            } as? QItem.SurahTitle
            return title?.name ?: ""
        }

        private fun normalizeArabicForMatch(text: String): String {
            return text
                .replace("ٰ", "")
                .replace("ٱ", "ا")
                .replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("ى", "ي")
                .trim()
        }

        private fun removeTrailingParenthesesNumber(text: String): String {
            // يحذف (1) أو ( ١ ) من نهاية السطر فقط
            val trimmed = text.trim()
            val regex = Regex("""\s*\(\s*[\d٠١٢٣٤٥٦٧٨٩]+\s*\)\s*$""")
            return trimmed.replace(regex, "").trim()
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
