package com.abdulbaqi.mashaf

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding

class QuranAdapter(
    private val items: List<QItem>,
    private val amiri: Typeface?
) : RecyclerView.Adapter<QuranAdapter.VH>() {

    class VH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

    // هل هذه السورة تبدأ بآية بسملة داخل json؟ (نحتاجها لحساب الترقيم الصحيح)
    private val surahHasBasmalaAtStart: Map<Int, Boolean> = buildBasmalaMap(items)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        val tv = holder.b.tvAyah
        tv.typeface = amiri

        when (item) {

            is QItem.SurahTitle -> {
                tv.text = item.name
                tv.gravity = Gravity.CENTER
                tv.textSize = 26f
            }

            is QItem.Ayah -> {
                tv.gravity = Gravity.END
                tv.textSize = 24f

                // 1) نظّف النص: احذف رقم الأقواس العادية (1) من آخر النص إن وجد
                val cleanText = stripPlainParenNumber(item.text)

                // 2) قواعد "لا ترقيم" لبعض السطور
                val isFatiha = (item.surahIndex == 0)
                val isBasmala = isBasmalaLine(cleanText)
                val isSalawat = isSalawatLine(cleanText)

                // ✅ لا رقم للصلاة على محمد وآل محمد
                if (isSalawat) {
                    tv.text = cleanText
                    return
                }

                // ✅ لا رقم للبسملة في كل السور عدا الفاتحة
                if (isBasmala && !isFatiha) {
                    tv.text = cleanText
                    return
                }

                // 3) حساب رقم الآية المعروض
                val displayNumber: Int? = when {
                    // الفاتحة: البسملة لها رقم (1) وباقي الآيات تسلسل طبيعي
                    isFatiha -> item.ayahIndex + 1

                    // باقي السور: لو يوجد بسملة كبداية في json، نبدأ الترقيم من الآية التي بعدها
                    surahHasBasmalaAtStart[item.surahIndex] == true -> {
                        // إذا كانت هذه الآية بعد البسملة سيكون ayahIndex = 1 => رقمها 1
                        item.ayahIndex
                    }

                    // سور بدون بسملة في البداية (مثل التوبة): الترقيم عادي
                    else -> item.ayahIndex + 1
                }

                // 4) اكتب النص + الرقم الأحمر بين ﴿ ﴾
                val numberText = displayNumber?.let { formatAyahNumber(it) }
                if (numberText == null) {
                    tv.text = cleanText
                    return
                }

                val full = "$cleanText $numberText"
                val sp = SpannableString(full)

                val start = full.lastIndexOf(numberText)
                if (start >= 0) {
                    sp.setSpan(
                        ForegroundColorSpan(Color.RED),
                        start,
                        start + numberText.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                tv.text = sp
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(pos: Int): QItem = items[pos]

    // ---------------- Helpers ----------------

    private fun buildBasmalaMap(items: List<QItem>): Map<Int, Boolean> {
        val firstAyahBySurah = HashMap<Int, String>()
        for (it in items) {
            if (it is QItem.Ayah) {
                if (!firstAyahBySurah.containsKey(it.surahIndex)) {
                    firstAyahBySurah[it.surahIndex] = it.text
                }
            }
        }
        val result = HashMap<Int, Boolean>()
        for ((s, t) in firstAyahBySurah) {
            result[s] = isBasmalaLine(stripPlainParenNumber(t))
        }
        return result
    }

    // يحذف رقم الأقواس العادية من آخر النص مثل: ".... (17)" أو "( 17 )"
    private fun stripPlainParenNumber(text: String): String {
        return text
            .replace(Regex("\\s*\\(\\s*\\d+\\s*\\)\\s*$"), "")
            .trim()
    }

    private fun isBasmalaLine(text: String): Boolean {
        val t = text.replace("ٮ", "ب") // احتياط لتشكيلك الخاص
        return t.contains("بسم") && t.contains("الله") && t.contains("الرحمن") && t.contains("الرحيم")
    }

    private fun isSalawatLine(text: String): Boolean {
        val t = text.replace("ٮ", "ب")
        return t.contains("اللهم") && t.contains("صل") && t.contains("محمد")
    }

    private fun formatAyahNumber(n: Int): String {
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
}
