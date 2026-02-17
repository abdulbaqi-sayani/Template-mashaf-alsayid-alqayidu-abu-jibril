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

        // ✅ اجلب اسم السورة المحفوظة (بدل index) حتى لا يتأثر بالحذف/الترتيب
        val bookmarkedName: String? = if (BookmarkStore.hasBookmark(this)) {
            val bookmarkedIndex = BookmarkStore.getSurahIndex(this)
            if (bookmarkedIndex in 0 until names.size) names[bookmarkedIndex] else null
        } else null

        val bookmarkedPos = if (bookmarkedName != null) names.indexOf(bookmarkedName) else -1

        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.adapter = SurahAdapter(
            names = names,
            bookmarkedIndex = bookmarkedPos
        ) { position ->
            val surahName = names[position]
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("fromIndex", true)
                    .putExtra("surahName", surahName) // ✅ بالاسم بدل الرقم
            )
        }

        // ✅ متابعة القراءة من الإشارة المرجعية (نتركها كما هي: ستفتح على الإشارة)
        b.btnContinue.setOnClickListener {
            if (!BookmarkStore.hasBookmark(this)) return@setOnClickListener
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("fromIndex", false) // افتح على الإشارة
            )
        }
    }
}
