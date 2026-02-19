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

            // حالات يجب عدم وضع رقم لها إطلاقًا:
            // 1) دعاء ختم القرآن (كل الأسطر)
            // 2) كلمة ختامية (كل الأسطر)
            // 3) الصلاة على محمد وآل محمد (أينما ظهرت)
            // 4) البسملة: لكل السور ما عدا الفاتحة
            val isDuaKhatm = isDuaKhatmQuranSurah(surahName)
            val isKhatima = isKhatimaSurah(surahName)
            val isSalawatLine = containsSalawat(cleanText)

            val basmalaFirst = hasBasmalaAsFirstAyah(item.surahIndex)
            val isBasmalaLine =
                containsBasmala(cleanText) &&
                    // الفاتحة فقط مسموح لها رقم على البسملة
                    (item.surahIndex != 0) &&
                    // إذا كانت البسملة أول عنصر في السورة
                    (basmalaFirst && item.ayahIndex == 0)

            if (isDuaKhatm || isKhatima || isSalawatLine || isBasmalaLine) {
                b.tvAyah.text = colorDecorationsAndAllah(cleanText)
                return
            }

            // حساب رقم العرض:
            // - إذا كانت السورة (غير الفاتحة) تحتوي بسملة كأول آية: نبدأ العد من الآية التالية (ayahIndex 1 => رقم 1)
            // - غير ذلك: ayahIndex + 1
            val displayNumber = if (item.surahIndex != 0 && basmalaFirst) {
                item.ayahIndex
            } else {
                item.ayahIndex + 1
            }

            // الرقم المزخرف في نهاية الآية
            val ornate = formatOrnateAyahNumber(displayNumber)
            val finalText = "$cleanText  $ornate"

            // تلوين: الزخارف خضراء + لفظ الجلالة أخضر + الرقم أحمر
            b.tvAyah.text = colorDecorationsAllahAndRedNumber(finalText, ornate)
        }

        private fun findSurahName(surahIndex: Int): String {
            val title = items.firstOrNull {
                it is QItem.SurahTitle && it.surahIndex == surahIndex
            } as? QItem.SurahTitle
            return title?.name ?: ""
        }

        private fun isDuaKhatmQuranSurah(name: String): Boolean {
            return name.contains("دعاء ختم")
                || name.contains("ختم القران")
                || name.contains("ختم القرآن")
        }

        private fun isKhatimaSurah(name: String): Boolean {
            return name.contains("كلمة ختامية") || name.contains("كلمة ختاميه")
        }

        private fun hasBasmalaAsFirstAyah(surahIndex: Int): Boolean {
            val firstAyah = items.firstOrNull {
                it is QItem.Ayah && it.surahIndex == surahIndex && it.ayahIndex == 0
            } as? QItem.Ayah ?: return false

            val txt = removeTrailingParenthesesNumber(firstAyah.text)
            return containsBasmala(txt)
        }

        private fun containsBasmala(text: String): Boolean {
            val t = text.replace("ٰ", "").replace("ٱ", "ا")
            return t.contains("بسم الله الرحمن الرحيم")
        }

        private fun containsSalawat(text: String): Boolean {
            // يكفي وجود "اللهم صل" و "محمد" لضمان أنها سطر الصلاة
            val t = text.replace("ٰ", "").replace("ٱ", "ا")
            return t.contains("اللهم") && t.contains("صل") && t.contains("محمد")
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

            return "﴿$arabic﴾"
        }

        // تلوين الزخارف فقط (❁ ✿ ❀) بالأخضر + تلوين لفظ الجلالة بالأخضر
        private fun colorDecorationsAndAllah(text: String): SpannableString {
            val ss = SpannableString(text)
            val green = ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark)

            // الزخارف المطلوبة
            val symbols = setOf('❁', '✿', '❀')
            for (i in text.indices) {
                if (symbols.contains(text[i])) {
                    ss.setSpan(
                        ForegroundColorSpan(green),
                        i, i + 1,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            // تلوين كل "الله" بالأخضر (وأي ظهور لها داخل النص)
            val target = "الله"
            var start = text.indexOf(target)
            while (start >= 0) {
                ss.setSpan(
                    ForegroundColorSpan(green),
                    start,
                    start + target.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                start = text.indexOf(target, start + target.length)
            }

            return ss
        }

        // تلوين الزخارف + لفظ الجلالة + رقم الآية المزخرف بالأحمر
        private fun colorDecorationsAllahAndRedNumber(full: String, numberPart: String): SpannableString {
            val ss = colorDecorationsAndAllah(full)
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
