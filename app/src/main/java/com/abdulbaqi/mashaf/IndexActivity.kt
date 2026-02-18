package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
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

        // تجهيز RecyclerView مباشرة (حتى لا ينهار لو تأخر التحميل)
        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.adapter = SurahAdapter(
            names = emptyList(),
            bookmarkedIndex = -1
        ) { }

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

        // ✅ تحميل البيانات بالخلفية لتجنب تجميد الشاشة
        loadIndexInBackground()
    }

    private fun loadIndexInBackground() {
        // (اختياري) لو عندك ProgressBar اسمه progress:
        // b.progress.visibility = View.VISIBLE

        // اجعل الزر والضغط معطّل أثناء التحميل
        b.btnContinue.isEnabled = false
        b.rvIndex.visibility = View.INVISIBLE

        Thread {
            try {
                val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
                val surahs = JSONArray(jsonText)

                val names = ArrayList<String>(surahs.length())
                for (i in 0 until surahs.length()) {
                    val name = surahs.getJSONObject(i).optString("name", "").trim()
                    if (name.isNotEmpty()) names.add(name)
                }

                val bookmarkedSurah = if (BookmarkStore.hasBookmark(this)) {
                    BookmarkStore.getSurahIndex(this)
                } else -1

                runOnUiThread {
                    b.btnContinue.isEnabled = true
                    b.rvIndex.visibility = View.VISIBLE
                    // b.progress.visibility = View.GONE

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
                }
            } catch (e: Exception) {
                runOnUiThread {
                    b.btnContinue.isEnabled = true
                    // b.progress.visibility = View.GONE
                    Toast.makeText(this, "خطأ في قراءة quran.json: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}
