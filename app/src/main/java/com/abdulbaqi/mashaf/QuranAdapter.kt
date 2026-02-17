package com.abdulbaqi.mashaf

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding
import com.abdulbaqi.mashaf.databinding.ItemSurahTitleBinding

class QuranAdapter(
    private val items: List<QItem>,
    private val amiri: Typeface?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TITLE = 1
        private const val TYPE_AYAH = 2
    }

    // ===== ViewHolders =====
    class TitleVH(val b: ItemSurahTitleBinding) : RecyclerView.ViewHolder(b.root)
    class AyahVH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is QItem.SurahTitle -> TYPE_TITLE
            is QItem.Ayah -> TYPE_AYAH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TITLE -> TitleVH(ItemSurahTitleBinding.inflate(inflater, parent, false))
            else -> AyahVH(ItemAyahBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is QItem.SurahTitle -> bindTitle(holder as TitleVH, item)
            is QItem.Ayah -> bindAyah(holder as AyahVH, item)
        }
    }

    private fun bindTitle(h: TitleVH, item: QItem.SurahTitle) {
        h.b.tvTitle.typeface = amiri
        h.b.tvTitle.text = item.name
    }

    private fun bindAyah(h: AyahVH, item: QItem.Ayah) {
        h.b.tvAyah.typeface = amiri

        val clean = item.text
            .replace("\r", " ")
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val number = computeAyahNumber(item.surahIndex, item.ayahIndex, clean)

        h.b.tvAyah.text = if (number == null) {
            clean
        } else {
            // رقم الآية في آخرها
            "$clean  ${formatOrnateAyahNumber(number)}"
        }
    }

    /**
     * قواعد الترقيم:
     * - الفاتحة (surahIndex = 0): الترقيم يبدأ من البسملة (1).
     * - باقي السور:
     *   - لا رقم للبسملة
     *   - إذا كانت البسملة هي الآية الأولى (ayahIndex=0) => البسملة بدون رقم
     *   - الآية التالية (ayahIndex=1) رقمها 1 وهكذا
     * - سطر "سورة ..." و "اللهم صل..." بدون رقم
     */
    private fun computeAyahNumber(surahIndex: Int, ayahIndex: Int, text: String): Int? {
        val t = text.trim()

        // أسطر خاصة بدون أرقام
        if (t.startsWith("سورة")) return null
        if (t.contains("اللَّهُمَّ صَلِّ") || t.contains("اللهم صل")) return null

        val isBasmala = isBasmalaLine(t)

        // الفاتحة: البسملة مرقمة
        if (surahIndex == 0) {
            return ayahIndex + 1
        }

        // التوبة (سورة التوبة غالبًا بدون بسملة)
        // إذا كان نص الآية الأولى ليس بسملة، نرقّم طبيعي
        // وإذا وُجدت بسملة في البيانات (بالخطأ) لن نرقمها.
        if (isBasmala) return null

        // إذا كانت البيانات تحتوي على بسملة كأول عنصر (ayahIndex 0)،
        // فالأرقام تبدأ من ayahIndex=1 => رقم 1
        // يعني رقم الآية = ayahIndex
        return if (ayahIndex == 0) 1 else ayahIndex
    }

    private fun isBasmalaLine(text: String): Boolean {
        // تطبيع بسيط
        val t = text.replace("ٱ", "ا").replace("إ", "ا").replace("أ", "ا").replace("آ", "ا")
        return t.contains("بسم الله") && t.contains("الرحمن") && t.contains("الرحيم")
    }

    private fun formatOrnateAyahNumber(n: Int): String {
        val arabic = n.toString()
            .replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣")
            .replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧")
            .replace("8", "٨").replace("9", "٩")

        // نفس شكل “١۝”
        return "$arabic۝"
    }

    override fun getItemCount(): Int = items.size

    // تحتاجه MainActivity لحفظ الإشارة المرجعية
    fun getItemAt(position: Int): QItem = items[position]
}
