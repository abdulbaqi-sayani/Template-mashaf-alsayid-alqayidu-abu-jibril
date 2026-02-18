package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
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

        // تجهيز RecyclerView مباشرة
        b.rvIndex.layoutManager = LinearLayoutManager(this)

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

        // نضع Adapter مبدئي (فارغ) لكن القائمة تظل ظاهرة (لا نخفيها)
        b.rvIndex.adapter = SurahAdapter(
            names = emptyList(),
            bookmarkedIndex = if (BookmarkStore.hasBookmark(this)) BookmarkStore.getSurahIndex(this) else -1
        ) { /* لا شيء */ }

        // تحميل الفهرس بالخلفية
        loadIndexInBackground()
    }

    private fun loadIndexInBackground() {
        Thread {
            try {
                val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
                val surahs = JSONArray(jsonText)

                val names = ArrayList<String>(surahs.length())
                for (i in 0 until surahs.length()) {
                    val name = surahs.getJSONObject(i).optString("name", "").trim()
                    names.add(name)
                }

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
                                .putExtra("fromIndex", true)
                        )
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "تعذر تحميل الفهرس (quran.json): ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }
}
