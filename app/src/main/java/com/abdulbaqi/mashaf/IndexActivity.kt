package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException

class IndexActivity : AppCompatActivity() {

    private lateinit var b: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.tvError.visibility = View.GONE
        b.pbLoading.visibility = View.VISIBLE

        b.btnContinue.setOnClickListener {
            if (!BookmarkStore.hasBookmark(this)) return@setOnClickListener
            val s = BookmarkStore.getSurahIndex(this)
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("surahIndex", s)
                    .putExtra("fromIndex", false)
            )
        }

        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.setHasFixedSize(true)
        b.rvIndex.itemViewCacheSize = 24

        // ✅ تحميل أسماء السور في الخلفية (حل بطء فتح الفهرس)
        lifecycleScope.launch {
            try {
                val names = withContext(Dispatchers.IO) {
                    loadSurahNames()
                }

                val bookmarkedSurah = if (BookmarkStore.hasBookmark(this@IndexActivity)) {
                    BookmarkStore.getSurahIndex(this@IndexActivity)
                } else -1

                b.rvIndex.adapter = SurahAdapter(
                    names = names,
                    bookmarkedIndex = bookmarkedSurah
                ) { index ->
                    startActivity(
                        Intent(this@IndexActivity, MainActivity::class.java)
                            .putExtra("surahIndex", index)
                            .putExtra("fromIndex", true)
                    )
                }

                b.pbLoading.visibility = View.GONE

            } catch (e: JSONException) {
                showError(
                    "خطأ في ملف quran.json (تنسيق JSON غير صحيح).\n" +
                            "غالبًا يوجد فاصلة أو قوس زائد/ناقص.\n" +
                            "تفاصيل: ${e.message}"
                )
            } catch (e: Exception) {
                showError("حدث خطأ أثناء تحميل الفهرس.\nتفاصيل: ${e.message}")
            }
        }
    }

    private fun loadSurahNames(): ArrayList<String> {
        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)

        val names = ArrayList<String>(surahs.length())
        for (i in 0 until surahs.length()) {
            val obj = surahs.getJSONObject(i)
            names.add(obj.getString("name"))
        }
        return names
    }

    private fun showError(msg: String) {
        b.pbLoading.visibility = View.GONE
        b.tvError.visibility = View.VISIBLE
        b.tvError.text = msg
    }
}
