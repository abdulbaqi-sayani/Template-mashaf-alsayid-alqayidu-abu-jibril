package com.abdulbaqi.mashaf

import com.abdulbaqi.mashaf.databinding.ActivityMainBinding
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding
import com.abdulbaqi.mashaf.databinding.ActivityMainBinding
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding
import com.abdulbaqi.mashaf.databinding.ActivityMainBinding
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding
import com.abdulbaqi.mashaf.databinding.ActivityMainBinding
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding
import com.abdulbaqi.mashaf.databinding.ActivityMainBinding
import com.abdulbaqi.mashaf.databinding.ItemAyahBinding

class AyahAdapter(
  private val items: List<String>,
  private val typeface: Typeface?
) : RecyclerView.Adapter<AyahAdapter.VH>() {

  class VH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val b = ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return VH(b)
  }

  private fun toArabicDigits(n: Int): String {
    val d = arrayOf("٠","١","٢","٣","٤","٥","٦","٧","٨","٩")
    return n.toString().map { d[it - '0'] }.joinToString("")
  }

  override fun onBindViewHolder(holder: VH, position: Int) {
    val marker = "${toArabicDigits(position + 1)}۝"   // ✅ الشكل الذي اخترته
    holder.b.tvAyah.text = "${items[position]}  $marker"
    if (typeface != null) holder.b.tvAyah.typeface = typeface
  }

  override fun getItemCount(): Int = items.size
}
