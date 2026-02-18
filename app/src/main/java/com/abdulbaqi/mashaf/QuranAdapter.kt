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
            val rawText = item.text
            val cleanText = removeTrailingParenthesesNumber(rawText)

            // 1) دعاء ختم القرآن: بدون أرقام إطلاقًا
            if (isDuaKhatmQuranSurah(surahName) || containsDuaKhatmKeyword(cleanText)) {
                b.tvAyah.text = colorDecorations(cleanText)
                return
            }

            // 2) "اللهم صل على محمد وآل محمد": بدون رقم إطلاقًا
            if (isSalawatLine(cleanText)) {
                b.tvAyah.text = colorDecorations(cleanText)
                return
            }

            // 3) البسملة: بدون رقم في كل السور إلا الفاتحة
            //    (نكتشفها من النص نفسه حتى لو كانت ليست أول عنصر عندك)
            val isBasmala = isBasmalaLine(cleanText)
            if (isBasmala && item.surahIndex != 0) { // كل السور ما عدا الفاتحة
                b.tvAyah.text = colorDecorations(cleanText)
                return
            }

            // رقم العرض (الترقيم الداخلي عندك يعتمد على ترتيب الملف)
            val displayNumber = item.ayahIndex + 1

            // الرقم المزخرف آخر الآية
            val ornate = formatOrnateAyahNumber(displayNumber)
            val finalText = "$cleanText  $ornate"

            // تلوين: زخارف خضراء + لفظ الجلالة أخضر + الرقم أحمر
            b.tvAyah.text = colorDecorationsAndRedNumber(finalText, ornate)
        }

        private fun findSurahName(surahIndex: Int): String {
            val title = items.firstOrNull {
                it is QItem.SurahTitle && it.surahIndex == surahIndex
            } as? QItem.SurahTitle
            return title?.name ?: ""
        }

        private fun removeTrailingParenthesesNumber(text: String): String {
            // يحذف أي (1) أو ( ١ ) في آخر السطر فقط
            val trimmed = text.trim()
            val regex = Regex("""\s*\(\s*[\d٠١٢٣٤٥٦٧٨٩]+\s*\)\s*$""")
            return trimmed.replace(regex, "").trim()
        }

        private fun formatOrnateAyahNumber(n: Int): String {
            val arabic = n.toString()
                .replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣")
                .replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧")
                .replace("8", "٨").replace("9", "٩")

            // الشكل الذي طلبته
            return "﴿$arabic﴾"
        }

        // --------------------- قواعد التعرف ---------------------

        private fun normalizeForMatch(s: String): String {
            // تبسيط للمقارنة: إزالة تشكيل/رموز، وتوحيد الألف والهمزات، وإزالة الزخارف
            return s
                .replace(Regex("[\\u064B-\\u0652\\u0670]"), "") // حركات
                .replace("ٱ", "ا")
                .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
                .replace("ة", "ه")
                .replace("ى", "ي")
                .replace("ٰ", "")
                .replace("۞", "")
                .replace("❁", "").replace("✿", "").replace("❀", "")
                .replace(Regex("\\s+"), " ")
                .trim()
        }

        private fun isBasmalaLine(text: String): Boolean {
            val t = normalizeForMatch(text)
            val basmala = "بسم الله الرحمن الرحيم"
            return t.contains(basmala)
        }

        private fun isSalawatLine(text: String): Boolean {
            val t = normalizeForMatch(text)
            // نقبل: اللهم صل على محمد وآل محمد (مع اختلافات بسيطة)
            return t.contains("اللهم صل على محمد") && (t.contains("وال محمد") || t.contains("وآل محمد"))
        }

        private fun isDuaKhatmQuranSurah(name: String): Boolean {
            val n = normalizeForMatch(name)
            return n.contains("دعاء ختم") || n.contains("ختم القران") || n.contains("ختم القرآن")
        }

        private fun containsDuaKhatmKeyword(text: String): Boolean {
            val t = normalizeForMatch(text)
            return t.contains("دعاء ختم") || t.contains("ختم القران") || t.contains("ختم القرآن")
        }

        // --------------------- التلوين ---------------------

        private fun colorDecorations(text: String): SpannableString {
            val ss = SpannableString(text)
            val green = ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark)

            // الزخارف المطلوبة: ❁ ✿ ❀ فقط
            val symbols = setOf('❁', '✿', '❀')
            for (i in text.indices) {
                if (symbols.contains(text[i])) {
                    ss.setSpan(
                        ForegroundColorSpan(green),
                        i,
                        i + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            // تلوين لفظ الجلالة
            return applyAllahGreen(ss, green)
        }

        private fun applyAllahGreen(ss: SpannableString, green: Int): SpannableString {
            val text = ss.toString()
            val harakat = "\\u064B-\\u0652\\u0670"

            val pattern = Regex(
                """([ٱا][${harakat}]*ل[${harakat}]*ل[${harakat}]*ه[${harakat}]*)|(ل[${harakat}]*ل[${harakat}]*ه[${harakat}]*)"""
            )

            for (m in pattern.findAll(text)) {
                ss.setSpan(
                    ForegroundColorSpan(green),
                    m.range.first,
                    m.range.last + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return ss
        }

        private fun colorDecorationsAndRedNumber(full: String, numberPart: String): SpannableString {
            val ss = colorDecorations(full)
            val red = ContextCompat.getColor(b.root.context, android.R.color.holo_red_dark)

            val start = full.lastIndexOf(numberPart)
            if (start >= 0) {
                ss.setSpan(
                    ForegroundColorSpan(red),
                    start,
                    start + numberPart.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return ss
        }
    }
}
