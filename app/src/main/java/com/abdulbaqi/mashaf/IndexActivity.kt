package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray

class IndexActivity : AppCompatActivity() {

    private lateinit var b: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(b.root)

        // تجهيز RecyclerView من البداية
        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.setHasFixedSize(true)

        // زر متابعة القراءة
        b.btnContinue.setOnClickListener {
            if (!BookmarkStore.hasBookmark(this)) {
                Toast.makeText(this, "لا توجد إشارة مرجعية", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val s = BookmarkStore.getSurahIndex(this)
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("surahIndex", s)
                    .putExtra("fromIndex", false)
            )
        }

        // تحميل الفهرس في الخلفية لتجنب تعليق الشاشة
        loadIndex()
    }

    private fun loadIndex() {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val names = withContext(Dispatchers.IO) {
                    val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
                    val surahs = JSONArray(jsonText)

                    val list = ArrayList<String>(surahs.length())
                    for (i in 0 until surahs.length()) {
                        // name لازم تكون String دائماً
                        val name = surahs.getJSONObject(i).optString("name", "")
                        list.add(name)
                    }
                    list
                }

                val bookmarkedSurah = if (BookmarkStore.hasBookmark(this@IndexActivity)) {
                    BookmarkStore.getSurahIndex(this@IndexActivity)
                } else -1

                b.rvIndex.adapter = SurahAdapter(
                    names = names,
                    bookmarkedIndex = bookmarkedSurah
                ) { index ->
                    // فتح السورة عند الضغط من الفهرس
                    startActivity(
                        Intent(this@IndexActivity, MainActivity::class.java)
                            .putExtra("surahIndex", index)
                            .putExtra("fromIndex", true)
                    )
                }

                showLoading(false)

            } catch (e: Exception) {
                showLoading(false)
                // رسالة واضحة بدل الصمت
                Toast.makeText(
                    this@IndexActivity,
                    "خطأ في quran.json: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        b.progress.visibility = if (isLoading) View.VISIBLE else View.GONE
        b.rvIndex.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}
