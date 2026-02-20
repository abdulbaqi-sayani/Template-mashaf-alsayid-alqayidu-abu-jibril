package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding
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

        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.setHasFixedSize(true)
        b.rvIndex.setItemViewCacheSize(30)

        // زر متابعة القراءة
        b.btnContinue.setOnClickListener {
            if (!BookmarkStore.hasBookmark(this)) return@setOnClickListener
            val s = BookmarkStore.getSurahIndex(this)
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("surahIndex", s)
            )
        }

        // تحميل أسماء السور بالخلفية
        Thread {
            try {
                val names = loadSurahNames()

                val bookmarkedSurah = if (BookmarkStore.hasBookmark(this)) {
                    BookmarkStore.getSurahIndex(this)
                } else -1

                runOnUiThread {
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
                }

            } catch (e: JSONException) {
                runOnUiThread {
                    showError("خطأ في ملف quran.json.\nتفاصيل: ${e.message}")
                }
            } catch (e: Exception) {
                runOnUiThread {
                    showError("حدث خطأ أثناء تحميل الفهرس.\nتفاصيل: ${e.message}")
                }
            }
        }.start()
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
