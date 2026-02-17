package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding

class AyahAdapter(
    rawItems: List<String>,
    private val typeface: Typeface?
) : RecyclerView.Adapter<AyahAdapter.VH>() {

    data class Row(val text: String, val number: Int?) // number=null => بدون رقم

    private val rows: List<Row>

    class VH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

    init {
        rows = buildRows(rawItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    private fun toArabicDigits(n: Int): String {
        val d = arrayOf("٠","١","٢","٣","٤","٥","٦","٧","٨","٩")
        return n.toString().map { d[it - '0'] }.joinToString("")
    }

    // حذف (1) (٢) ... إلخ
    private fun removeBracketNumbers(text: String): String {
        return text.replace(Regex("\\(([0-9٠-٩]+)\\)"), "").trim()
    }

    private fun isSurahTitle(text: String): Boolean {
        val t = text.trim()
        return t.contains("سوره") || t.contains("سورة") || t.contains("✿")
    }

    private fun isSalawatLine(text: String): Boolean {
        val t = text.trim()
        return t.contains("اللَّهُمَّ") || t.contains("اللهم") || t.contains("صَلِّ") || t.contains("صل")
    }

    private fun isBasmala(text: String): Boolean {
        val t = text.trim()
        // مرن حتى لو كانت كتابة مختلفة قليلًا
        return t.contains("بسم") && (t.contains("الله") || t.contains("ٱللَّه"))
    }

    private fun isFatihaTitle(text: String): Boolean {
        val t = text.trim()
        return t.contains("الفاتحة") || t.contains("ڡاتحه") || t.contains("فاتحة")
    }

    private fun buildRows(rawItems: List<String>): List<Row> {
        val cleaned = rawItems.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleaned.isEmpty()) return emptyList()

        val title = cleaned.first()
        val isFatiha = isFatihaTitle(title)

        val out = ArrayList<Row>(cleaned.size)

        var ayahCounter = 0

        for ((index, item) in cleaned.withIndex()) {
            val raw = item.trim()

            // 1) عنوان السورة بدون رقم
            if (index == 0 && isSurahTitle(raw)) {
                out.add(Row(raw, null))
                continue
            }

            // 2) سطر الصلاة على محمد وآل محمد بدون رقم
            if (isSalawatLine(raw)) {
                out.add(Row(raw, null))
                continue
            }

            // 3) البسملة: تُرقّم فقط في الفاتحة، وبقية السور بدون رقم
            if (isBasmala(raw) && !isFatiha) {
                out.add(Row(raw, null))
                continue
            }

            // 4) آية عادية: احذف أرقام الأقواس ثم رقّمها
            val text = removeBracketNumbers(raw)
            if (text.isEmpty()) {
                out.add(Row(raw, null))
                continue
            }

            ayahCounter += 1
            out.add(Row(text, ayahCounter))
        }

        return out
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = rows[position]

        if (row.number == null) {
            holder.b.tvAyah.text = row.text
        } else {
            val number = toArabicDigits(row.number)
            val marker = "﴿" + number + "﴾"

            // ✅ الرقم في آخر الآية (نهاية السطر)
            holder.b.tvAyah.text = row.text + "  " + marker
        }

        if (typeface != null) holder.b.tvAyah.typeface = typeface
    }

    override fun getItemCount(): Int = rows.size
}
