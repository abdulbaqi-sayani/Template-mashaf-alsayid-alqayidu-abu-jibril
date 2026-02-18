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

    // كاش بسيط لتحسين الأداء (حتى لا نعيد التلوين مع كل bind)
    private val cache = HashMap<Int, CharSequence>(4096)

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
            is QItem.Ayah -> (holder as AyahVH).bind(item, position)
        }
    }

    private class TitleVH(private val b: ItemSurahTitleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: QItem.SurahTitle) {
            b.tvSurahName.text = item.name
        }
    }

    private inner class AyahVH(private val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root) {

        fun bind(item: QItem.Ayah, adapterPos: Int) {
            // لو موجود في الكاش نستخدمه مباشرة
            cache[adapterPos]?.let {
                b.tvAyah.typeface = amiri
                b.tvAyah.text = it
                return
            }

            b.tvAyah.typeface = amiri

            val surahName = findSurahName(item.surahIndex)
            val isDuaKhatm = isDuaKhatmQuranSection(surahName)
            val rawText = item.text

            // نحذف (1) الإنجليزية/العادية في نهاية السطر فقط
            val cleanText = removeTrailingParenthesesNumber(rawText)

            // قواعد منع الترقيم
            val isSalawatLine = isSalawat(cleanText)

            // هل السورة (غير الفاتحة) أول آية فيها بسملة؟
            val basmalaFirst = hasBasmalaAsFirstAyah(item.surahIndex)
            val isBasmalaLine =
                item.ayahIndex == 0 &&
                item.surahIndex != 0 &&
                basmalaFirst &&
                containsBasmala(cleanText)

            // (1) لا ترقيم لدعاء ختم القرآن
            // (2) لا ترقيم للبسملة في كل السور إلا الفاتحة
            // (3) لا ترقيم للصلاة على محمد وآل محمد
            val shouldShowNumber = !isDuaKhatm && !isBasmalaLine && !isSalawatLine

            val finalText: String
            val ornateNumber: String?

            if (!shouldShowNumber) {
                finalText = cleanText
                ornateNumber = null
            } else {
                val displayNumber = computeDisplayAyahNumber(
                    surahIndex = item.surahIndex,
                    ayahIndex = item.ayahIndex,
                    basmalaFirst = basmalaFirst
                )
                ornateNumber = formatOrnateAyahNumber(displayNumber)
                finalText = "$cleanText  $ornateNumber"
            }

            // تلوين: الزخارف + لفظ الجلالة + رقم الآية (إن وجد)
            val colored = applyColors(finalText, ornateNumber)

            cache[adapterPos] = colored
            b.tvAyah.text = colored
        }

        private fun computeDisplayAyahNumber(surahIndex: Int, ayahIndex: Int, basmalaFirst: Boolean): Int {
            // الفاتحة: البسملة لها رقم (آية 1) طبيعي
            if (surahIndex == 0) return ayahIndex + 1

            // غير الفاتحة:
            // إذا كانت البسملة موجودة كأول عنصر في JSON، فهي تُعرض بلا رقم
            // ثم يبدأ العد من الآية التالية: index=1 -> رقم 1
            return if (basmalaFirst) ayahIndex else ayahIndex + 1
        }

        private fun findSurahName(surahIndex: Int): String {
            val title = items.firstOrNull { it is QItem.SurahTitle && it.surahIndex == surahIndex } as? QItem.SurahTitle
            return title?.name ?: ""
        }

        private fun isDuaKhatmQuranSection(name: String): Boolean {
            // أي اسم يحتوي هذا
            return name.contains("دعاء ختم", ignoreCase = true) ||
                name.contains("ختم القران", ignoreCase = true) ||
                name.contains("ختم القرآن", ignoreCase = true)
        }

        private fun isSalawat(text: String): Boolean {
            val t = normalizeArabic(text)
            // تغطية أغلب الصيغ الشائعة
            return t.contains("اللهم صل على محمد", ignoreCase = true) ||
                t.contains("اللهم صل على سيدنا محمد", ignoreCase = true) ||
                (t.contains("صل على محمد", ignoreCase = true) && t.contains("وآل محمد", ignoreCase = true)) ||
                t.contains("واله محمد", ignoreCase = true) ||
                t.contains("وآل محمد", ignoreCase = true)
        }

        private fun hasBasmalaAsFirstAyah(surahIndex: Int): Boolean {
            val firstAyah = items.firstOrNull { it is QItem.Ayah && it.surahIndex == surahIndex && it.ayahIndex == 0 } as? QItem.Ayah
                ?: return false
            val txt = removeTrailingParenthesesNumber(firstAyah.text)
            return containsBasmala(txt)
        }

        private fun containsBasmala(text: String): Boolean {
            val t = normalizeArabic(text)
            return t.contains("بسم الله الرحمن الرحيم")
        }

        private fun normalizeArabic(s: String): String {
            // توحيد بعض الأشكال لتسهيل المطابقة
            return s
                .replace("\u0640", "") // ـ
                .replace("ٰ", "")
                .replace("ٱ", "ا")
                .replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("\r", " ")
                .replace("\n", " ")
                .replace(Regex("\\s+"), " ")
                .trim()
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

        private fun applyColors(fullText: String, numberPart: String?): SpannableString {
            val ss = SpannableString(fullText)

            val green = ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark)
            val red = ContextCompat.getColor(b.root.context, android.R.color.holo_red_dark)

            // (A) تلوين الزخارف ❁ ✿ ❀ بالأخضر
            val decorations = setOf('❁', '✿', '❀')
            for (i in fullText.indices) {
                if (decorations.contains(fullText[i])) {
                    ss.setSpan(ForegroundColorSpan(green), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            // (B) تلوين لفظ الجلالة بالأخضر (الله / ٱللَّه / لله / باللّه .. الخ)
            // نلوّن أي مقطع يحتوي: الله مع احتمال وجود حركات/تشكيل.
            // نبحث يدويًا على النص الأصلي (للاحتفاظ بالمؤشرات الصحيحة)
            colorAllahMentions(fullText, ss, green)

            // (C) تلوين رقم الآية بالأحمر (فقط الجزء المزخرف)
            if (!numberPart.isNullOrBlank()) {
                val start = fullText.lastIndexOf(numberPart)
                if (start >= 0) {
                    ss.setSpan(
                        ForegroundColorSpan(red),
                        start,
                        start + numberPart.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }

            return ss
        }

        private fun colorAllahMentions(text: String, ss: SpannableString, green: Int) {
            // نعتبر أن لفظ الجلالة هو "الله" وقد يسبقه لام الجر "لله"
            // نبحث عن تسلسل أحرف: ا ل ل ه (مع احتمال وجود حركات بينهما)
            // ونلوّن من بداية "ا" إلى نهاية "ه" مع ما بينهما.
            val chars = text.toCharArray()

            fun isTashkeel(c: Char): Boolean {
                val code = c.code
                return (code in 0x064B..0x065F) || c == 'ٰ' || c == 'ٖ' || c == 'ٗ' || c == 'ٚ'
            }

            fun matchesFrom(i: Int, target: Char): Boolean {
                return i in chars.indices && (chars[i] == target || (target == 'ا' && chars[i] == 'ٱ'))
            }

            var i = 0
            while (i < chars.size) {
                // ابحث عن بداية "ا" أو "ٱ"
                if (chars[i] == 'ا' || chars[i] == 'ٱ') {
                    var j = i + 1
                    // تجاوز الحركات
                    while (j < chars.size && isTashkeel(chars[j])) j++

                    // "ل"
                    if (j < chars.size && chars[j] == 'ل') {
                        j++
                        while (j < chars.size && isTashkeel(chars[j])) j++

                        // "ل" ثانية
                        if (j < chars.size && chars[j] == 'ل') {
                            j++
                            while (j < chars.size && isTashkeel(chars[j])) j++

                            // "ه"
                            if (j < chars.size && chars[j] == 'ه') {
                                val end = j + 1
                                ss.setSpan(
                                    ForegroundColorSpan(green),
                                    i,
                                    end,
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                                i = end
                                continue
                            }
                        }
                    }
                }
                i++
            }

            // كذلك نلوّن "لله" عندما تأتي بدون همزة/ٱ (مثلاً لله)
            // في الغالب تلتقطها القاعدة أعلاه داخل "الله" نفسها.
        }
    }
}        } else {
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
