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

    fun getItemAt(position: Int): QItem = items[position]

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is QItem.SurahTitle -> TYPE_TITLE
            is QItem.Ayah -> TYPE_AYAH
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_TITLE) {
            val b = ItemSurahTitleBinding.inflate(inflater, parent, false)
            TitleVH(b)
        } else {
            val b = ItemAyahBinding.inflate(inflater, parent, false)
            AyahVH(b)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is QItem.SurahTitle -> (holder as TitleVH).bind(item)
            is QItem.Ayah -> (holder as AyahVH).bind(item, amiri)
        }
    }

    override fun getItemCount(): Int = items.size

    class TitleVH(private val b: ItemSurahTitleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: QItem.SurahTitle) {
            b.tvTitle.text = item.title
        }
    }

    class AyahVH(private val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: QItem.Ayah, amiri: Typeface?) {
            b.tvAyah.typeface = amiri
            b.tvAyah.text = item.text
        }
    }
}
