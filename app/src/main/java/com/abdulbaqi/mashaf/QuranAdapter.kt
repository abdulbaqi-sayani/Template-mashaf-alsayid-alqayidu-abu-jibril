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

                val number = formatAyahNumber(item.ayahIndex + 1)
                val fullText = "${item.text} $number"

                val spannable = SpannableString(fullText)

                val start = fullText.indexOf(number)
                val end = start + number.length

                // ✅ لون أحمر للرقم فقط
                spannable.setSpan(
                    ForegroundColorSpan(Color.RED),
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                tv.text = spannable
                tv.gravity = Gravity.END
                tv.textSize = 24f
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(pos: Int): QItem = items[pos]

    // تحويل الرقم إلى عربي داخل ﴿ ﴾
    private fun formatAyahNumber(n: Int): String {

        val arabicNumber = n.toString()
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

        return "﴿$arabicNumber﴾"
    }
}
