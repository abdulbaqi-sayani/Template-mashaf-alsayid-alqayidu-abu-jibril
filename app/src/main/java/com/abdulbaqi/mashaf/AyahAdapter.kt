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
    private val rows: List<Row> = buildRows(rawItems)

    class VH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    private fun toArabicDigits(n: Int): String {
        val d = arrayOf("٠","١","٢","٣","٤","٥","٦","٧","٨","٩")
        return n.toString().map { d[it - '0'] }.joinToString("")
    }

    // ✅ حذف (1) (٢) ... إلخ
    private fun removeBracketNumbers(text: String): String {
        return text.replace(Regex("\\(([0-9٠-٩]+)\\)"), "").trim()
    }

    // ✅ تنظيف النص داخل الآية: إزالة أي كسر سطر حتى لا تنقسم بين الفواصل
    private fun normalizeWhitespace(text: String): String {
        return text
            .replace("\r", " ")
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun isSurahTitle(text: String): Boolean {
        val t = text.trim()
        return t.contains("سوره") || t.contains("سورة") || t.contains("✿")
    }

    private fun isFatihaTitle(text: String): Boolean {
        val t = text.trim()
        return t.contains("الفاتحة") || t.contains("ڡاتحه") || t.contains("فاتحة")
    }

    private fun isSalawatLine(text: String): Boolean {
        val t = text.trim()
        return t.contains("اللَّهُمَّ") || t.contains("اللهم") || t.contains("صَلِّ") || t.contains("صل")
    }

    private fun isBasmala(text: String): Boolean {
        val t = text.trim()
        return t.contains("بسم") && (t.contains("الله") || t.contains("ٱللَّه"))
    }

    private fun buildRows(rawItems: List<String>): List<Row> {
        val cleaned = rawItems.map { it.trim() }.filter { it.isNotEmpty() }
        if (cleaned.isEmpty()) return emptyList()

        val out = ArrayList<Row>(cleaned.size)

        var currentSurahIsFatiha = false
        var ayahCounter = 0

        for (item in cleaned) {
            val raw = item.trim()

            // ✅ عنوان السورة: بدون رقم + إعادة العد
            if (isSurahTitle(raw)) {
                currentSurahIsFatiha = isFatihaTitle(raw)
                ayahCounter = 0
                out.add(Row(normalizeWhitespace(raw), null))
                continue
            }

            // ✅ سطر الصلاة على محمد وآل محمد: بدون رقم
            if (isSalawatLine(raw)) {
                out.add(Row(normalizeWhitespace(raw), null))
                continue
            }

            // ✅ البسملة: تُرقّم فقط في الفاتحة، وبقية السور بدون رقم
            if (isBasmala(raw) && !currentSurahIsFatiha) {
                out.add(Row(normalizeWhitespace(raw), null))
                continue
            }

            // ✅ آية عادية: احذف أرقام الأقواس + نظّف كسر السطر + رقّم
            val cleanedText = normalizeWhitespace(removeBracketNumbers(raw))
            if (cleanedText.isEmpty()) {
                out.add(Row(normalizeWhitespace(raw), null))
                continue
            }

            ayahCounter += 1
            out.add(Row(cleanedText, ayahCounter))
        }

        return out
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val row = rows[position]

        if (row.number == null) {
            holder.b.tvAyah.text = row.text
        } else {
            val number = toArabicDigits(row.number)
            val marker = "﴿$number﴾"

            // ✅ الرقم في آخر الآية
            holder.b.tvAyah.text = row.text + "  " + marker
        }

        if (typeface != null) holder.b.tvAyah.typeface = typeface
    }

    override fun getItemCount(): Int = rows.size
}
