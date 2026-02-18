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
            // مهم: لازم يكون في layout عنوان السورة TextView اسمه tvSurahName
            b.tvSurahName.text = item.name
        }
    }

    private inner class AyahVH(private val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(item: QItem.Ayah) {
            b.tvAyah.typeface = amiri

            val surahName = findSurahName(item.surahIndex)
            val isDuaKhatm = isDuaKhatmQuranSurah(surahName)

            val rawText = item.text
            val cleanText = removeTrailingParenthesesNumber(rawText)

            // (1) لا رقم لدعاء ختم القرآن
            if (isDuaKhatm) {
                b.tvAyah.text = colorDecorations(cleanText)
                return
            }

            // هل السورة فيها بسملة كأول عنصر؟ (لغير الفاتحة فقط)
            val basmalaFirst = hasBasmalaAsFirstAyah(item.surahIndex)

            // (2) البسملة بلا رقم في كل السور ما عدا الفاتحة
            val isBasmalaLine =
                item.ayahIndex == 0 &&
                item.surahIndex != 0 &&
                basmalaFirst &&
                containsBasmala(cleanText)

            if (isBasmalaLine) {
                b.tvAyah.text = colorDecorations(cleanText)
                return
            }

            // رقم العرض: لو البسملة موجودة كأول آية (لغير الفاتحة) نبدأ العد من الآية التالية
            val displayNumber = if (item.surahIndex != 0 && basmalaFirst) {
                item.ayahIndex // لأن index=1 -> رقم 1 ، index=2 -> رقم 2 ...
            } else {
                item.ayahIndex + 1 // الفاتحة أو السور بدون بسملة في البداية
            }

            // (3) الرقم المزخرف يكون نهاية الآية وليس بدايتها
            val ornate = formatOrnateAyahNumber(displayNumber)
            val finalText = "$cleanText  $ornate"

            // تلوين: الزخارف الخضراء + الرقم الأحمر
            b.tvAyah.text = colorDecorationsAndRedNumber(finalText, ornate)
        }

        private fun findSurahName(surahIndex: Int): String {
            val title = items.firstOrNull { it is QItem.SurahTitle && it.surahIndex == surahIndex } as? QItem.SurahTitle
            return title?.name ?: ""
        }

        private fun isDuaKhatmQuranSurah(name: String): Boolean {
            // أي اسم يحتوي "دعاء ختم" يكفي
            return name.contains("دعاء ختم")
                || name.contains("ختم القران")
                || name.contains("ختم القرآن")
        }

        private fun hasBasmalaAsFirstAyah(surahIndex: Int): Boolean {
            // نبحث عن أول آية لنفس السورة
            val firstAyah = items.firstOrNull { it is QItem.Ayah && it.surahIndex == surahIndex && it.ayahIndex == 0 } as? QItem.Ayah
            if (firstAyah == null) return false
            val txt = removeTrailingParenthesesNumber(firstAyah.text)
            return containsBasmala(txt)
        }

        private fun containsBasmala(text: String): Boolean {
            val t = text.replace("ٰ", "").replace("ٱ", "ا")
            return t.contains("بسم الله الرحمن الرحيم")
        }

        private fun removeTrailingParenthesesNumber(text: String): String {
            // يحذف أي (1) أو ( ١ ) في آخر السطر فقط (حتى لا تظهر الأقواس العادية نهائيًا)
            val trimmed = text.trim()
            val regex = Regex("""\s*\(\s*[\d٠١٢٣٤٥٦٧٨٩]+\s*\)\s*$""")
            return trimmed.replace(regex, "").trim()
        }

        private fun formatOrnateAyahNumber(n: Int): String {
            // ⟵ نفس أسلوب رقمك المزخرف: (رمز يمين) رقم عربي (رمز يسار)
            val arabic = n.toString()
                .replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣")
                .replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧")
                .replace("8", "٨").replace("9", "٩")

            // الزخرفة: ﴿٦﴾ كانت عندك… أنت طلبت المزخرف الحالي الأحمر، نتركه كما هو عندك
            // إذا تريد بالضبط ﴿٦﴾ أخبرني ونعملها بدل الرموز الحالية.
            return "﴿$arabic﴾"
        }

        private fun colorDecorations(text: String): SpannableString {
            val ss = SpannableString(text)
            val green = ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark)

            // الزخارف المطلوبة: ❁ ✿ ❀  (فقط هذه)
            val symbols = listOf('❁', '✿', '❀')
            for (i in text.indices) {
                if (symbols.contains(text[i])) {
                    ss.setSpan(ForegroundColorSpan(green), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
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
