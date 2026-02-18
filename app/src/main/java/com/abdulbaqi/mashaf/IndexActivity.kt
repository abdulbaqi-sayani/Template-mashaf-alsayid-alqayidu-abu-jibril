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

        // افتراضيًا أخفِ رسالة الخطأ
        b.tvError.visibility = View.GONE
        b.pbLoading.visibility = View.VISIBLE

        // زر متابعة القراءة
        b.btnContinue.setOnClickListener {
            if (!BookmarkStore.hasBookmark(this)) return@setOnClickListener
            val s = BookmarkStore.getSurahIndex(this)
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("surahIndex", s)
                    .putExtra("fromIndex", false)
            )
        }

        // تجهيز الفهرس مع حماية من الانهيار
        try {
            val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
            val surahs = JSONArray(jsonText)

            val names = ArrayList<String>(surahs.length())
            for (i in 0 until surahs.length()) {
                val obj = surahs.getJSONObject(i)
                names.add(obj.getString("name"))
            }

            val bookmarkedSurah = if (BookmarkStore.hasBookmark(this)) {
                BookmarkStore.getSurahIndex(this)
            } else -1

            b.rvIndex.layoutManager = LinearLayoutManager(this)
            b.rvIndex.adapter = SurahAdapter(
                names = names,
                bookmarkedIndex = bookmarkedSurah
            ) { index ->
                startActivity(
                    Intent(this, MainActivity::class.java)
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
            showError(
                "حدث خطأ أثناء تحميل الفهرس.\n" +
                        "تفاصيل: ${e.message}"
            )
        }
    }

    private fun showError(msg: String) {
        b.pbLoading.visibility = View.GONE
        b.tvError.visibility = View.VISIBLE
        b.tvError.text = msg
    }
}
