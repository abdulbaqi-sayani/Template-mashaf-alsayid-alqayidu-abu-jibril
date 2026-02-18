package com.abdulbaqi.mashaf

import android.graphics.Typeface
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
                tv.text = item.text
                tv.gravity = Gravity.END
                tv.textSize = 24f
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItemAt(pos: Int): QItem = items[pos]
}
