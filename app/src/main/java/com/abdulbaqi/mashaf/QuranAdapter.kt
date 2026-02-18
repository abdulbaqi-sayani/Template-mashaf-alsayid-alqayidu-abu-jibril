package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
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
        private const val VT_SURAH = 0
        private const val VT_AYAH = 1

        // الزخارف المطلوب تلوينها أخضر
        private val ORNAMENTS = setOf('❁', '✿', '❀')
    }

    // رقم العرض لكل عنصر (<=0 يعني لا تعرض رقم)
    private val displayNumberByPos: IntArray = IntArray(items.size) { -1 }

    init {
        buildDisplayNumbers()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is QItem.SurahTitle -> VT_SURAH
            is QItem.Ayah -> VT_AYAH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VT_SURAH) {
            val b = ItemSurahTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            SurahVH(b)
        } else {
            val b = ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            AyahVH(b)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {

            is QItem.SurahTitle -> {
                val h = holder as SurahVH
                // ✅ تلوين الزخارف داخل اسم السورة أخضر
                h.b.tvSurahName.text = colorOrnamentsGreen(item.name, h.b.root)
            }

            is QItem.Ayah -> {
                val h = holder as AyahVH
                h.b.tvAyah.typeface = amiri

                // ✅ تلوين الزخارف داخل الآية أخضر
                h.b.tvAyah.text = colorOrnamentsGreen(item.text, h.b.root)

                // ✅ رقم الآية (أحمر) بصيغة ﴿٦﴾
                val n = displayNumberByPos[position]
                if (n <= 0) {
                    h.b.tvNumber.visibility = View.GONE
                } else {
                    h.b.tvNumber.visibility = View.VISIBLE
                    h.b.tvNumber.text = formatAyahNumberBrackets(n)
                    h.b.tvNumber.setTextColor(
                        ContextCompat.getColor(h.b.root.context, android.R.color.holo_red_dark)
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(pos: Int): QItem = items[pos]

    private class SurahVH(val b: ItemSurahTitleBinding) : RecyclerView.ViewHolder(b.root)
    private class AyahVH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

    /** يبني الترقيم الصحيح:
     * - لا رقم للبسملة إلا سورة الفاتحة (surahIndex=0)
     * - لا رقم لعبارة (اللهم صل على محمد وآل محمد)
     * - يبدأ ترقيم السورة من أول آية (بعد البسملة في غير الفاتحة)
     */
    private fun buildDisplayNumbers() {
        val counterBySurah = HashMap<Int, Int>() // surahIndex -> lastNumber

        for (i in items.indices) {
            val it = items[i]
            if (it is QItem.Ayah) {
                val s = it.surahIndex

                if (isSalawat(it.text)) {
                    displayNumberByPos[i] = -1
                    continue
                }

                if (isBasmala(it.text) && s != 0) {
                    displayNumberByPos[i] = -1
                    continue
                }

                val next = (counterBySurah[s] ?: 0) + 1
                counterBySurah[s] = next
                displayNumberByPos[i] = next
            }
        }
    }

    // ✅ يلوّن ❁ ✿ ❀ باللون الأخضر داخل أي نص
    private fun colorOrnamentsGreen(text: String, root: View): CharSequence {
        val green = ContextCompat.getColor(root.context, android.R.color.holo_green_dark)
        val sp = SpannableString(text)

        for (i in text.indices) {
            if (ORNAMENTS.contains(text[i])) {
                sp.setSpan(
                    ForegroundColorSpan(green),
                    i, i + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return sp
    }

    private fun isBasmala(t: String): Boolean {
        val x = normalize(t)
        return x.contains("بسم الله") && x.contains("الرحمن") && x.contains("الرحيم")
    }

    private fun isSalawat(t: String): Boolean {
        val x = normalize(t)
        return x.contains("اللهم") && x.contains("صل") && x.contains("محمد") && x.contains("آل محمد")
    }

    private fun normalize(t: String): String {
        return t
            .replace("❀", "")
            .replace("❁", "")
            .replace("✿", "")
            .replace("❃", "")
            .replace("❂", "")
            .replace("❊", "")
            .replace("﴿", "")
            .replace("﴾", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun formatAyahNumberBrackets(n: Int): String {
        return "﴿${toArabicDigits(n)}﴾"
    }

    private fun toArabicDigits(n: Int): String {
        return n.toString()
            .replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣")
            .replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧")
            .replace("8", "٨").replace("9", "٩")
    }
}
