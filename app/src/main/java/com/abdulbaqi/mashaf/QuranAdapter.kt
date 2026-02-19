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

            // أسطر بلا أرقام إطلاقًا:
            if (isNoNumberLine(item.surahIndex, surahName, cleanText)) {
                b.tvAyah.text = applyAllColors(cleanText, ornateNumberPart = null)
                return
            }

            // رقم العرض (العدّ يبدأ من أول آية غير البسملة إذا كانت البسملة موجودة كأول عنصر)
            val basmalaFirst = hasBasmalaAsFirstAyah(item.surahIndex)
            val displayNumber = if (item.surahIndex != 0 && basmalaFirst) {
                item.ayahIndex // index=1 -> رقم 1
            } else {
                item.ayahIndex + 1
            }

            val ornate = formatOrnateAyahNumber(displayNumber)
            val finalText = "$cleanText  $ornate"

            b.tvAyah.text = applyAllColors(finalText, ornateNumberPart = ornate)
        }

        // -------------------- منع الترقيم --------------------

        private fun isNoNumberLine(surahIndex: Int, surahName: String, text: String): Boolean {
            // دعاء ختم القرآن: كل الأسطر بلا أرقام
            if (isDuaKhatmQuranSurah(surahName)) return true

            // كلمة ختامية: كل الأسطر بلا أرقام
            if (isKhatimaSurah(surahName)) return true

            // الصلاة على محمد وآل محمد: بلا رقم
            if (isSalawatLine(text)) return true

            // البسملة: بلا رقم في كل السور إلا الفاتحة
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
            if (surahIndex == 0) return false // الفاتحة مسموح لها رقم
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
            val ss = SpannableString(text)

            val green = ContextCompat.getColor(b.root.context, android.R.color.holo_green_dark)
            val red = ContextCompat.getColor(b.root.context, android.R.color.holo_red_dark)

            // (A) زخارف: ❁ ✿ ❀ بالأخضر
            val deco = setOf('❁', '✿', '❀')
            for (i in text.indices) {
                if (deco.contains(text[i])) {
                    ss.setSpan(ForegroundColorSpan(green), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            // (B) تلوين "الله" فقط بالأخضر (بدون إله/لله)
            colorExactWordBySimplified(ss, original = text, needle = "الله", color = green)

            // (C) تلوين "إسرائيل" و"اليهود" بالأحمر في كامل المصحف
            // نلوّن أيضاً "اسرائيل" بدون همزة احتياطاً
            colorExactWordBySimplified(ss, original = text, needle = "إسرائيل", color = red)
            colorExactWordBySimplified(ss, original = text, needle = "اسرائيل", color = red)
            colorExactWordBySimplified(ss, original = text, needle = "اليهود", color = red)

            // (D) الرقم المزخرف بالأحمر (إن وجد)
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

        /**
         * يلوّن كلمة معينة بالاعتماد على نسخة مبسطة (بدون حركات + تطبيع بعض الحروف)
         * ثم يحوّل النطاق إلى النص الأصلي حتى يلوّنها كما هي (بالحركات والزخارف).
         */
        private fun colorExactWordBySimplified(
            ss: SpannableString,
            original: String,
            needle: String,
            color: Int
        ) {
            val simplifiedOriginal = simplifyForSearch(original)
            val simplifiedNeedle = simplifyForSearch(needle)

            var idx = simplifiedOriginal.indexOf(simplifiedNeedle)
            while (idx >= 0) {
                val (start, end) = mapSimplifiedRangeToOriginal(original, idx, idx + simplifiedNeedle.length)
                if (start >= 0 && end > start && end <= original.length) {
                    ss.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                idx = simplifiedOriginal.indexOf(simplifiedNeedle, idx + simplifiedNeedle.length)
            }
        }

        private fun simplifyForSearch(text: String): String {
            val noDia = removeDiacritics(text)
            return noDia
                .replace("ٱ", "ا")
                .replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("ى", "ي")
                .replace("ـ", "")
                .trim()
        }

        private fun mapSimplifiedRangeToOriginal(original: String, sStart: Int, sEnd: Int): Pair<Int, Int> {
            var oIndex = 0
            var sIndex = 0
            var oStart = -
