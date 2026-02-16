package YOUR.PACKAGE

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import YOUR.PACKAGE.databinding.ItemAyahBinding

class AyahAdapter(
  private val items: List<String>,
  private val typeface: Typeface?
) : RecyclerView.Adapter<AyahAdapter.VH>() {

  class VH(val b: ItemAyahBinding) : RecyclerView.ViewHolder(b.root)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    val b = ItemAyahBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return VH(b)
  }

  override fun onBindViewHolder(holder: VH, position: Int) {
    holder.b.tvAyah.text = items[position]
    if (typeface != null) holder.b.tvAyah.typeface = typeface
  }

  override fun getItemCount(): Int = items.size
}
