package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
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

        // لا تجعل الشاشة فاضية بدون سبب
        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.setHasFixedSize(true)

        try {
            val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
            val surahs = JSONArray(jsonText)

            val names = ArrayList<String>(surahs.length())
            for (i in 0 until surahs.length()) {
                val obj = surahs.getJSONObject(i)
                val name = obj.optString("name", "").trim()
                if (name.isNotEmpty()) names.add(name) else names.add("سورة بدون اسم")
            }

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
                        .putExtra("fromIndex", true)
                )
            }

            b.btnContinue.visibility = View.VISIBLE
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

        } catch (e: JSONException) {
            // غالبًا مشكلة JSON (أقواس/فواصل/ترتيب)
            showFatalError("خطأ في quran.json: ${e.message}")
        } catch (e: Exception) {
            // أي سبب آخر (ملف غير موجود، خطأ قراءة..)
            showFatalError("تعذر تشغيل التطبيق: ${e.message}")
        }
    }

    private fun showFatalError(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        // لا ننهار — نخلي الشاشة موجودة
        b.btnContinue.visibility = View.VISIBLE
    }
}
