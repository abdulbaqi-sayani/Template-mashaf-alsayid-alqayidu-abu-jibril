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

        // قراءة quran.json واستخراج أسماء السور
        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)

        val names = ArrayList<String>(surahs.length())
        for (i in 0 until surahs.length()) {
            names.add(surahs.getJSONObject(i).getString("name"))
        }

        // السورة المحفوظة (للنجمة)
        val bookmarkedSurahIndex = if (BookmarkStore.hasBookmark(this)) {
            BookmarkStore.getSurahIndex(this)
        } else -1

        // عرض الفهرس
        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.adapter = SurahAdapter(
            names = names,
            bookmarkedIndex = bookmarkedSurahIndex
        ) { index ->
            // ✅ فتح السورة بالاسم (أكثر أمانًا لو تغيرت الفهارس داخل JSON)
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("fromIndex", true)
                    .putExtra("surahName", names[index])
            )
        }

        // متابعة القراءة من الإشارة المرجعية
        b.btnContinue.setOnClickListener {
            if (!BookmarkStore.hasBookmark(this)) return@setOnClickListener

            // نفتح MainActivity بدون fromIndex حتى يذهب للإشارة المرجعية
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("fromIndex", false)
            )
        }
    }
}
