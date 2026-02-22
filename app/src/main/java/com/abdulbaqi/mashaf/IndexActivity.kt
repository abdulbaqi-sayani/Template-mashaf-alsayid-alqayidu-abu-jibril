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

class IndexActivity : AppCompatActivity() {

    private lateinit var b: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.setHasFixedSize(true)

        b.btnContinue.setOnClickListener {
            if (BookmarkStore.hasBookmark(this)) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("fromIndex", false)
                startActivity(intent)
            }
        }

        loadDataAsync()
    }

    private fun loadDataAsync() {
        b.pbLoading.visibility = View.VISIBLE
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. قراءة البيانات
                val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
                val surahsArray = JSONArray(jsonText)
                val names = ArrayList<String>(surahsArray.length())
                for (i in 0 until surahsArray.length()) {
                    names.add(surahsArray.getJSONObject(i).getString("name"))
                }

                // 2. جلب رقم السورة المحفوظة من الذاكرة
                val bookmarkedSurah = if (BookmarkStore.hasBookmark(this@IndexActivity)) {
                    BookmarkStore.getSurahIndex(this@IndexActivity)
                } else -1

                // 3. تحديث الواجهة
                withContext(Dispatchers.Main) {
                    b.pbLoading.visibility = View.GONE
                    b.rvIndex.adapter = SurahAdapter(names, bookmarkedSurah) { index ->
                        val intent = Intent(this@IndexActivity, MainActivity::class.java)
                        intent.putExtra("surahIndex", index)
                        intent.putExtra("fromIndex", true)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    b.pbLoading.visibility = View.GONE
                    b.tvError.visibility = View.VISIBLE
                    b.tvError.text = "فشل تحميل البيانات"
                }
            }
        }
    }
}
