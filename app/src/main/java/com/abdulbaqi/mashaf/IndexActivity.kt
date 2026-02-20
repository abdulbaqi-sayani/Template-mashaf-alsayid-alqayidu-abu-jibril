package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding

class IndexActivity : AppCompatActivity() {

    private lateinit var b: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(b.root)

        // حالة البداية
        b.tvError.visibility = View.GONE
        b.rvIndex.visibility = View.GONE
        b.pbLoading.visibility = View.VISIBLE

        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.setHasFixedSize(true)
        b.rvIndex.setItemViewCacheSize(40)
        b.rvIndex.itemAnimator = null // يقلل الوميض/التقطيع

        // زر متابعة القراءة
        b.btnContinue.setOnClickListener {
            if (!BookmarkStore.hasBookmark(this)) return@setOnClickListener
            val s = BookmarkStore.getSurahIndex(this)
            startActivity(Intent(this, MainActivity::class.java).putExtra("surahIndex", s))
        }

        // ✅ تحميل مرة واحدة من الكاش
        QuranRepository.ensureLoaded(this) { ok, err ->
            runOnUiThread {
                if (!ok) {
                    showError("خطأ أثناء تحميل الفهرس.\nتفاصيل: $err")
                    return@runOnUiThread
                }

                val names = QuranRepository.getSurahNames()

                val bookmarkedSurah = if (BookmarkStore.hasBookmark(this)) {
                    BookmarkStore.getSurahIndex(this)
                } else -1

                b.rvIndex.adapter = SurahAdapter(
                    names = names,
                    bookmarkedIndex = bookmarkedSurah
                ) { index ->
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .putExtra("surahIndex", index)
                    )
                }

                b.pbLoading.visibility = View.GONE
                b.rvIndex.visibility = View.VISIBLE
            }
        }
    }

    private fun showError(msg: String) {
        b.pbLoading.visibility = View.GONE
        b.rvIndex.visibility = View.GONE
        b.tvError.visibility = View.VISIBLE
        b.tvError.text = msg
    }
}
