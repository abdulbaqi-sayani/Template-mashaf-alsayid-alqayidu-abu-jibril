// IndexActivity.kt
package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding
import org.json.JSONArray

class IndexActivity : AppCompatActivity() {

    private lateinit var b: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(b.root)

        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)

        val names = ArrayList<String>(surahs.length())
        for (i in 0 until surahs.length()) {
            names.add(surahs.getJSONObject(i).getString("name"))
        }

        val bookmarkedSurah = if (BookmarkStore.hasBookmark(this)) {
            BookmarkStore.getSurahIndex(this)
        } else -1

        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.adapter = SurahAdapter(
            names = names,
            bookmarkedIndex = bookmarkedSurah
        ) { index ->
            // ✅ أرسل الاسم + الرقم معًا (الاسم هو الأهم)
            val surahName = names.getOrNull(index) ?: return@SurahAdapter

            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("fromIndex", true)
                    .putExtra("surahIndex", index)
                    .putExtra("surahName", surahName)
            )
        }

        // ✅ متابعة القراءة من الإشارة المرجعية
        b.btnContinue.setOnClickListener {
            if (!BookmarkStore.hasBookmark(this)) return@setOnClickListener

            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("fromIndex", false)
            )
        }
    }
}
