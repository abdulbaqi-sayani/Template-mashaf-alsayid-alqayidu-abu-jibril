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

    // كاش لتقليل العجن 😄
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
            // كاش
            cache[adapterPos]?.let {
                b.tvAyah.typeface = amiri
                b.tvAyah.text = it
                return
            }

            b.tvAyah.typeface = amiri

            val surahName = findSurahName(item.surahIndex)
            val isDuaKhatm = isDuaKhatmQuranSection(surahName)

            // حذف (17) من آخر السطر فقط
            val cleanText = removeTrailingParenthesesNumber(item.text)

            val basmalaFirst = hasBasmalaAsFirstAyah(item.surahIndex)
            val isBasmalaLine =
                item.ayahIndex == 0 &&
                        item.surahIndex != 0 &&
                        basmalaFirst &&
                        containsBasmala(cleanText)

            val isSalawatLine = isSalawat(cleanText)

            // لا أرقام لهذه الأقسام:
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
                // الرقم في آخر الآية
                finalText = "$cleanText  $ornateNumber"
            }

            val colored = applyColors(finalText, ornateNumber)
            cache[adapterPos] = colored
            b.tvAyah.text = colored
        }

        private fun computeDisplayAyahNumber(surahIndex: Int, ayahIndex: Int, basmalaFirst: Boolean): Int {
            // الفاتحة: البسملة لها رقم (آية 1)
            if (surahIndex == 0) return ayahIndex + 1

            // غير الفاتحة: إذا كانت البسملة أول عنصر فهي بلا رقم
            // ثم يبدأ العد من الآية التالية: index=1 -> رقم 1
            return if (basmalaFirst) ayahIndex else ayahIndex + 1
        }

        private fun findSurahName(surahIndex: Int): String {
            val title = items.firstOrNull {
                it is QItem.SurahTitle && it.surahIndex == surahIndex
            } as? QItem.SurahTitle
            return title?.name ?: ""
        }

        private fun isDuaKhatmQuranSection(name: String): Boolean {
            return name.contains("دعاء ختم", ignoreCase = true) ||
                    name.contains("ختم القران", ignoreCase = true) ||
                    name.contains("ختم القرآن", ignoreCase = true)
        }

        private fun isSalawat(text: String): Boolean {
            val t = normalizeArabic(text)
            return t.contains("اللهم صل على محمد") ||
                    t.contains("وآل محمد") ||
                    t.contains("واله محمد")
        }

        private fun hasBasmalaAsFirstAyah(surahIndex: Int): Boolean {
            val firstAyah = items.firstOrNull {
                it is QItem.Ayah && it.surahIndex == surahIndex && it.ayahIndex == 0
            } as? QItem.Ayah ?: return false

            val txt = removeTrailingParenthesesNumber(firstAyah.text)
            return containsBasmala(txt)
        }

        private fun containsBasmala(text: String): Boolean {
            val t = normalizeArabic(text)
            return t.contains("بسم الله الرحمن الرحيم")
        }

        private fun normalizeArabic(s: String): String {
            return s
                .replace("\u0640", "")
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

            // الزخارف ❁ ✿ ❀ بالأخضر
            val decorations = setOf('❁', '✿', '❀')
            for (i in fullText.indices) {
                if (decorations.contains(fullText[i])) {
                    ss.setSpan(ForegroundColorSpan(green), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            // تلوين لفظ الجلالة "الله" بالأخضر
            colorAllahMentions(fullText, ss, green)

            // رقم الآية بالأحمر (إن وجد)
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
            fun isTashkeel(c: Char): Boolean {
                val code = c.code
                return (code in 0x064B..0x065F) || c == 'ٰ'
            }

            val chars = text.toCharArray()
            var i = 0
            while (i < chars.size) {
                // ا أو ٱ
                if (chars[i] == 'ا' || chars[i] == 'ٱ') {
                    var j = i + 1
                    while (j < chars.size && isTashkeel(chars[j])) j++
                    if (j < chars.size && chars[j] == 'ل') {
                        j++
                        while (j < chars.size && isTashkeel(chars[j])) j++
                        if (j < chars.size && chars[j] == 'ل') {
                            j++
                            while (j < chars.size && isTashkeel(chars[j])) j++
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
        }
    }
}
