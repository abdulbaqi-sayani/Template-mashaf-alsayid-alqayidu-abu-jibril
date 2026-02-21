package com.abdulbaqi.mashaf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.abdulbaqi.mashaf.databinding.ItemSurahBinding

class SurahAdapter(
    private val names: List<String>,
    private val bookmarkedIndex: Int,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SurahAdapter.VH>() {

    // ViewHolder: يقوم بربط عناصر واجهة المستخدم
    class VH(val b: ItemSurahBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // استخدام LayoutInflater لإنشاء واجهة العنصر الواحد
        val b = ItemSurahBinding.inflate(
            LayoutInflater.from(parent.context), 
            parent, 
            false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val name = names.getOrNull(position).orEmpty()
        
        // عرض اسم السورة فقط
        holder.b.tvName.text = name

        // الاستماع لضغطة المستخدم ونقلها للشاشة الرئيسية
        holder.b.root.setOnClickListener {
            val p = holder.bindingAdapterPosition
            if (p != RecyclerView.NO_POSITION) {
                onClick(p)
            }
        }
    }

    override fun getItemCount(): Int = names.size

    // تحسين الأداء لمنع إعادة الرسم غير الضروري
    override fun getItemId(position: Int): Long = position.toLong()
}
