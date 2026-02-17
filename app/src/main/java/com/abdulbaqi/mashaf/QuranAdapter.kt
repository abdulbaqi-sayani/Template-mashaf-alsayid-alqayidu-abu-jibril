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
        private const val VT_TITLE = 1
        private const val VT_AYAH = 2
    }

    // ✅ خريطة رقم العرض لكل آية (قد تكون null إذا لا نريد رقم)
    private val displayNumber: MutableMap<Pair<Int, Int>, Int?> = mutableMapOf()

    init {
        buildDisplayNumbers()
    }

    private fun buildDisplayNumbers() {
        var currentSurah = -1
        var counter = 0

        for (it in items) {
            when (it) {
                is QItem.SurahTitle -> {
                    currentSurah = it.surahIndex
                    counter = 0
                }

                is QItem.Ayah -> {
                    if (it.surahIndex != currentSurah) {
                        currentSurah = it.surahIndex
                        counter = 0
                    }

                    val key = it.surahIndex to it.ayahIndex

                    if (shouldHideNumber(it.text, it.surahIndex)) {
                        displayNumber[key] = null
                    } else {
                        counter += 1
                        displayNumber[key] = counter
                    }
                }
            }
        }
    }

    private fun shouldHideNumber(text: String, surahIndex: Int): Boolean {
        val t = text.trim()

        // ✅ لا رقم لسطر الصلاة على محمد وآل محمد (إن كان موجودًا بالنص)
        if (t.contains("اللَّهُمَّ صَلِّ عَلَى مُحَمَّد") || t.contains("اللهم صل على محمد")) {
            return true
        }

        // ✅ البسملة: لا رقم لها في كل السور إلا الفاتحة (surahIndex == 0)
        val isBasmala = t.contains("بِسْمِ") && t.contains("الرَّحْمٰن") && t.contains("الرَّحِيم")
        if (isBasmala && surahIndex != 0) return true

        return false
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is QItem.SurahTitle -> VT_TITLE
            is QItem.Ayah -> VT_AYAH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            VT_TITLE -> {
                val b = ItemSurahTitleBinding.inflate(inflater, parent, false)
                TitleVH(b)
            }
            else -> {
                val b = ItemAyahBinding.inflate(inflater, parent, false)
                AyahVH(b)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is QItem.SurahTitle -> (holder as TitleVH).bind(item)
            is QItem.Ayah -> (holder as AyahVH).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(position: Int): QItem = items[position]

    // ---------------- ViewHolders ----------------

    inner class TitleVH(private val b: ItemSurahTitleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: QItem.SurahTitle) {
            // حسب تصميمك: غالباً لديك TextView للعنوان (مثلاً tvTitle أو tvName)
            // عدّل السطر التالي إذا اسم الـ TextView مختلف عندك:
            b.tvTitle.text = item.name
        }
    }

    inner class AyahVH(private val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: QItem.Ayah) {
            b.tvAyah.typeface = amiri

            val key = item.surahIndex to item.ayahIndex
            val n = displayNumber[key]

            // ✅ رقم الآية في آخر النص (إن وجد)
            b.tvAyah.text = if (n == null) {
                item.text
            } else {
                // مسافة + رقم مزخرف آخر الآية
                "${item.text}  ${formatOrnateAyahNumber(n)}"
            }
        }
    }

    private fun formatOrnateAyahNumber(n: Int): String {
        // مثال: ٦۝
        val arabic = n.toString()
            .replace("0", "٠").replace("1", "١").replace("2", "٢").replace("3", "٣")
            .replace("4", "٤").replace("5", "٥").replace("6", "٦").replace("7", "٧")
            .replace("8", "٨").replace("9", "٩")

        return "${arabic}۝"
    }
}
