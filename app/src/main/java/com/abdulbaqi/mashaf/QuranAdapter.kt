package com.abdulbaqi.mashaf

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        private const val RED = "#C62828"     // أحمر للأرقام
        private const val GREEN = "#2E7D32"   // أخضر للزخارف
    }

    override fun getItemViewType(position: Int): Int =
        when (items[position]) {
            is QItem.SurahTitle -> TYPE_TITLE
            is QItem.Ayah -> TYPE_AYAH
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TITLE) {
            val b = ItemSurahTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            TitleVH(b)
        } else {
            val b = ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AyahVH(b)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {

            is QItem.SurahTitle -> {
                (holder as TitleVH).b.tvSurahName.text = item.name
            }

            is QItem.Ayah -> {
                val vh = holder as AyahVH
                vh.b.tvAyah.typeface = amiri

                val cleanedText = cleanText(item.text)

                val isFatiha = item.surahIndex == 0

                // 1) لا رقم للبسملة إلا الفاتحة
                val isBasmala = cleanedText.contains("بِسۡمِ") || cleanedText.contains("بسم الله")

                // 2) لا رقم للصلاة على محمد وآل محمد
                val isSalawat = cleanedText.contains("اللهم صل") || cleanedText.contains("صَلِّ عَلَى مُحَمَّد")

                // 3) لا رقم لدعاء ختم القرآن
                val isKhatmDuaTitle = cleanedText.contains("دعاء ختم") || cleanedText.contains("ختم القران")
                val isKhatmDuaBody = cleanedText.contains("اللهم ارحمني بالقرآن") || cleanedText.contains("واجعله لي إماما")

                // 4) لا رقم لـ "كلمة ختامية" (وأي نصوصها)
                val isClosingSection =
                    cleanedText.contains("صدقة جارية") ||
                    cleanedText.contains("راجيًا القبول") ||
                    cleanedText.contains("عبد الباقي محمد") ||
                    cleanedText.contains("غفر الله له ولوالديه")

                val shouldHideNumber =
                    (isBasmala && !isFatiha) ||
                    isSalawat ||
                    isKhatmDuaTitle ||
                    isKhatmDuaBody ||
                    isClosingSection

                if (shouldHideNumber) {
                    vh.b.tvAyah.text = colorDecorations(cleanedText)
                } else {
                    val n = item.ayahIndex + 1
                    val ornate = formatOrnateNumber(n)
                    val line = "$ornate  $cleanedText"

                    // أخضر للزخارف + أحمر للرقم المزخرف
                    val sp = colorDecorations(line)
                    colorFirstOccurrence(sp, ornate, RED)

                    vh.b.tvAyah.text = sp
                }
            }
        }
    }

    // ✅ إزالة (1) أو ( 1 ) أو (١) من أي مكان + تنظيف المسافات
    private fun cleanText(text: String): String {
        val removedParenNumbers = text.replace(Regex("""\(\s*[0-9٠-٩]+\s*\)"""), "")
        return removedParenNumbers
            .replace("\r", " ")
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    // ✅ الرقم المزخرف بالشكل الذي تريده: ﴿١﴾
    private fun formatOrnateNumber(n: Int): String {
        val arabic = n.toString()
            .replace("0", "٠")
            .replace("1", "١")
            .replace("2", "٢")
            .replace("3", "٣")
            .replace("4", "٤")
            .replace("5", "٥")
            .replace("6", "٦")
            .replace("7", "٧")
            .replace("8", "٨")
            .replace("9", "٩")

        return "﴿$arabic﴾"
    }

    // ✅ تلوين الزخارف ❀ ❁ ✿ بالأخضر
    private fun colorDecorations(text: String): SpannableString {
        val sp = SpannableString(text)
        val symbols = listOf("❀", "❁", "✿")

        for (sym in symbols) {
            var i = text.indexOf(sym)
            while (i >= 0) {
                sp.setSpan(
                    ForegroundColorSpan(Color.parseColor(GREEN)),
                    i,
                    i + sym.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                i = text.indexOf(sym, i + 1)
            }
        }
        return sp
    }

    // ✅ تلوين أول ظهور لنص معين (هنا الرقم المزخرف) بالأحمر
    private fun colorFirstOccurrence(sp: SpannableString, target: String, hex: String) {
        val full = sp.toString()
        val start = full.indexOf(target)
        if (start >= 0) {
            sp.setSpan(
                ForegroundColorSpan(Color.parseColor(hex)),
                start,
                start + target.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    class TitleVH(val b: ItemSurahTitleBinding) : RecyclerView.ViewHolder(b.root)
    class AyahVH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

    fun getItemAt(position: Int): QItem = items[position]
}
