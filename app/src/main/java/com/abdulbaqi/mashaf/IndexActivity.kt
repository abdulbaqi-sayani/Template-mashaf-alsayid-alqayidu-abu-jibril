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

        // إعداد القائمة
        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.setHasFixedSize(true)

        // زر متابعة القراءة
        b.btnContinue.setOnClickListener {
            if (BookmarkStore.hasBookmark(this)) {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

        // استدعاء التحميل في الخلفية
        loadDataAsync()
    }

    private fun loadDataAsync() {
        b.pbLoading.visibility = View.VISIBLE
        
        // التحميل في خلفية التطبيق (Worker Thread)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
                val surahs = JSONArray(jsonText)
                val names = ArrayList<String>(114)

                for (i in 0 until surahs.length()) {
                    names.add(surahs.getJSONObject(i).getString("name"))
                }

                // العودة للواجهة الرئيسية لعرض الأسماء
                withContext(Dispatchers.Main) {
                    b.pbLoading.visibility = View.GONE
                    b.rvIndex.adapter = SurahAdapter(names, -1) { index ->
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
                    b.tvError.text = "خطأ في تحميل البيانات"
                }
            }
        }
    }
}
