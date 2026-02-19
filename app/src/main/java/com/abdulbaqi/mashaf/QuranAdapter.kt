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
        private const val AR_DIACRITICS = "\u064B-\u065F\u0670"
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
            val namePlain = stripDecorations(surahName)

            // ✅ تنظيف جذري: حذف أي (1) أو ( ١ ) من أي مكان في النص
            val cleanText = removeAllParenthesesNumbers(item.text)

            val suppressNumber = shouldSuppressAyahNumber(
                surahIndex = item.surahIndex,
                surahNamePlain = namePlain,
                ayahIndex = item.ayahIndex,
                ayahText = cleanText
            )

            if (suppressNumber) {
                b.tvAyah.text = colorDecorationsAndDivineNames(cleanText)
                return
            }

            // رقم العرض (نفس منطقك السابق: إن كانت السورة تبدأ ببسملة - غير الفاتحة - نبدأ العد من الآية التالية)
            val basmalaFirst = hasBasmalaAsFirstAyah(item.surahIndex)
            val displayNumber = if (item.surahIndex != 0 && basmalaFirst) {
                item.ayahIndex
            } else {
                item.ayahIndex + 1
            }

            val ornate = formatOrnateAyahNumber(displayNumber)
            val finalText = "$cleanText  $ornate"

            b.tvAyah.text = colorDecorationsDivineNamesAndRedNumber(finalText, ornate)
        }

        // ---------------------------
        // قواعد المنع (جذرية)
        // ---------------------------
        private fun shouldSuppressAyahNumber(
            surahIndex: Int,
            surahNamePlain: String,
            ayahIndex: Int,
            ayahText: String
        ): Boolean {

            // 1) دعاء ختم القرآن: كله بلا أرقام
            if (isDuaKhatmQuranSurah(surahNamePlain)) return true

            // 2) كلمة ختامية: كلها بلا أرقام
            if (isKhatimaSurah(surahNamePlain)) return true

            // 3) الصلاة على محمد وآل محمد: بلا أرقام
            if (containsSalawat(ayahText)) return true

            // 4) البسملة: بلا رقم في كل السور إلا الفاتحة (بسملة الفاتحة فقط مسموح لها رقم)
            if (containsBasmala(ayahText)) {
                val isFatihaBasmala = (surahIndex == 0 && ayahIndex == 0)
                if (!isFatihaBasmala) return true
            }

            return false
        }

        private fun isDuaKhatmQuranSurah(name: String): Boolean {
            val n = name.replace("ٯ", "ق").replace("حٮم", "ختم")
            return n.contains("دعاء") && (n.contains("ختم") || n.contains("القران") || n.contains("القرآن"))
        }

        private fun isKhatimaSurah(name: String): Boolean {
            return name.contains("كلمة ختامية") || name.contains("كلمة ختاميه")
        }

        private fun containsBasmala(text: String): Boolean {
            val t = normalize(text)
            return t.contains("بسم الله الرحمن الرحيم")
        }

        private fun containsSalawat(text: String): Boolean {
            val t = normalize(text)
            // يكفي وجود "اللهم" و "محمد" و "آل" (أو "وال") لضمان أنه سطر الصلاة
            return t.contains("اللهم") && t.contains("محمد") && (t.contains("آل") || t.contains("وال"))
        }

        private fun normalize(s: String): String {
            return s.replace("ٰ", "")
                .replace("ٱ", "ا")
                .replace("إ", "ا")
                .replace("أ", "ا")
                .replace("آ", "ا")
                .replace("ى", "ي")
        }

        // ---------------------------
        // أدوات النص
        // ---------------------------
        private fun stripDecorations(s: String): String {
            return s.replace("❁", "")
                .replace("✿", "")
                .replace("❀", "")
                .replace("❈", "")
                .replace("❉", "")
                .replace("❋", "")
                .replace("۞", "")
                .trim()
        }

        private fun removeAllParenthesesNumbers(text: String): String {
            val regex = Regex("""\(\s*[\d٠١٢٣٤٥٦٧٨٩]+\s*\)""")
            return text.replace(regex, "")
                .replace(Regex("""\s{2,}"""), " ")
                .trim()
        }

        private fun findSurahName(surahIndex: Int): String {
            val title = items.firstOrNull {
                it is QItem.SurahTitle && it.surahIndex == surahIndex
            } as? QItem.SurahTitle
            return title?.name ?: ""
        }

        private fun hasBasmalaAsFirstAyah(surahIndex: Int): Boolean {
            val firstAyah = items.firstOrNull {
                it is QItem.Ayah && it.surahIndex == surahIndex && it.ayahIndex == 0
            } as? QItem.Ayah ?: return false

            val txt = removeAllParenthesesNumbers(firstAyah.text)
            return containsBasmala(txt)
        }

        private fun formatOrnateAyahNumber(n: Int): String {
            val arabic = n.toString()
                .replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣")
                .replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧")
                .replace("8", "٨").replace("9", "٩")
            return "﴿$arabic﴾"
        }

        // ---------------------------
        // التلوين (الزخارف + لفظ الجلالة)
        // ---------------------------
        private fun colorDecorationsAndDivineNames(text: String): SpannableString {
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

            applyGreenSpansForDivineNames(ss, text, green)
            return ss
        }

        private fun colorDecorationsDivineNamesAndRedNumber(full: String, numberPart: String): SpannableString {
            val ss = colorDecorationsAndDivineNames(full)
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

        private fun applyGreenSpansForDivineNames(ss: SpannableString, text: String, green: Int) {
            val di = "[$AR_DIACRITICS]*"

            // الله / لله / إله / اله
            val patterns = listOf(
                Regex("ا${di}ل${di}ل${di}ه${di}"),
                Regex("ل${di}ل${di}ه${di}"),
                Regex("إ${di}ل${di}ه${di}"),
                Regex("ا${di}ل${di}ه${di}")
            )

            for (rx in patterns) {
                rx.findAll(text).forEach { m ->
                    val start = m.range.first
                    val end = m.range.last + 1
                    if (start >= 0 && end <= text.length) {
                        ss.setSpan(
                            ForegroundColorSpan(green),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        }
    }
}        val inflater = LayoutInflater.from(parent.context)
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

            val basmalaFirst = hasBasmalaAsFirstAyah(item.surahIndex)

            // قرار نهائي: هل هذا السطر ممنوع إضافة رقم له؟
            val suppressNumber = shouldSuppressAyahNumber(
                surahIndex = item.surahIndex,
                surahName = surahName,
                ayahText = cleanText,
                ayahIndex = item.ayahIndex,
                basmalaFirst = basmalaFirst
            )

            if (suppressNumber) {
                b.tvAyah.text = colorDecorationsAndDivineNames(cleanText)
                return
            }

            // حساب رقم العرض (للسور التي تبدأ ببسملة - غير الفاتحة - نبدأ العد من الآية التالية)
            val displayNumber = if (item.surahIndex != 0 && basmalaFirst) {
                item.ayahIndex // index=1 -> 1 ، index=2 -> 2 ...
            } else {
                item.ayahIndex + 1
            }

            val ornate = formatOrnateAyahNumber(displayNumber)
            val finalText = "$cleanText  $ornate"

            b.tvAyah.text = colorDecorationsDivineNamesAndRedNumber(finalText, ornate)
        }

        // ---------------------------
        // قواعد منع الترقيم (جذرية)
        // ---------------------------
        private fun shouldSuppressAyahNumber(
            surahIndex: Int,
            surahName: String,
            ayahText: String,
            ayahIndex: Int,
            basmalaFirst: Boolean
        ): Boolean {
            val namePlain = stripDecorations(surahName)

            // 1) دعاء ختم القرآن: كل أسطره بلا أرقام
            if (isDuaKhatmQuranSurah(namePlain)) return true

            // 2) كلمة ختامية: كل أسطرها بلا أرقام
            if (isKhatimaSurah(namePlain)) return true

            // 3) سطر الصلاة على محمد وآل محمد: بلا أرقام أينما كان
            if (containsSalawat(ayahText)) return true

            // 4) البسملة: بلا رقم في كل السور إلا الفاتحة
            if (surahIndex != 0 && containsBasmala(ayahText)) return true

            // احتياط إضافي
            if (surahIndex != 0 && basmalaFirst && ayahIndex == 0 && containsBasmala(ayahText)) return true

            return false
        }

        private fun stripDecorations(s: String): String {
            return s.replace("❁", "")
                .replace("✿", "")
                .replace("❀", "")
                .replace("❈", "")
                .replace("❉", "")
                .replace("❋", "")
                .replace("۞", "")
                .trim()
        }

        private fun isDuaKhatmQuranSurah(name: String): Boolean {
            return name.contains("دعاء") && (name.contains("ختم") || name.contains("حتم") || name.contains("القران") || name.contains("القرآن"))
        }

        private fun isKhatimaSurah(name: String): Boolean {
            return name.contains("كلمة ختامية") || name.contains("كلمة ختاميه")
        }

        private fun containsBasmala(text: String): Boolean {
            val t = text.replace("ٰ", "").replace("ٱ", "ا")
            return t.contains("بسم الله الرحمن الرحيم")
        }

        private fun containsSalawat(text: String): Boolean {
            val t = text.replace("ٰ", "").replace("ٱ", "ا")
            return t.contains("اللهم") && t.contains("محمد") && (t.contains("صل") || t.contains("صَل") || t.contains("صَلِ"))
        }

        private fun findSurahName(surahIndex: Int): String {
            val title = items.firstOrNull {
                it is QItem.SurahTitle && it.surahIndex == surahIndex
            } as? QItem.SurahTitle
            return title?.name ?: ""
        }

        private fun hasBasmalaAsFirstAyah(surahIndex: Int): Boolean {
            val firstAyah = items.firstOrNull {
                it is QItem.Ayah && it.surahIndex == surahIndex && it.ayahIndex == 0
            } as? QItem.Ayah ?: return false

            val txt = removeTrailingParenthesesNumber(firstAyah.text)
            return containsBasmala(txt)
        }

        private fun removeTrailingParenthesesNumber(text: String): String {
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

        // ---------------------------
        // التلوين (الزخارف + لفظ الجلالة)
        // ---------------------------
        private fun colorDecorationsAndDivineNames(text: String): SpannableString {
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

            applyGreenSpansForDivineNames(ss, text, green)
            return ss
        }

        private fun colorDecorationsDivineNamesAndRedNumber(full: String, numberPart: String): SpannableString {
            val ss = colorDecorationsAndDivineNames(full)
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

        private fun applyGreenSpansForDivineNames(ss: SpannableString, text: String, green: Int) {
            val di = "[$AR_DIACRITICS]*"

            // ✅ الإصلاح هنا: بناء النص بالـ String template بشكل صحيح
            val patterns = listOf(
                // الله
                Regex("ا${di}ل${di}ل${di}ه${di}"),
                // لله
                Regex("ل${di}ل${di}ه${di}"),
                // إله (همزة)
                Regex("إ${di}ل${di}ه${di}"),
                // اله (بدون همزة)
                Regex("ا${di}ل${di}ه${di}")
            )

            for (rx in patterns) {
                rx.findAll(text).forEach { m ->
                    val start = m.range.first
                    val end = m.range.last + 1
                    if (start >= 0 && end <= text.length) {
                        ss.setSpan(
                            ForegroundColorSpan(green),
                            start,
                            end,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        }
    }
}
