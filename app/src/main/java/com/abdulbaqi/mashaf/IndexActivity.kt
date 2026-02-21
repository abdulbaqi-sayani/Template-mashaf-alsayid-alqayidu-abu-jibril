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

        b.tvError.visibility = View.GONE
        b.pbLoading.visibility = View.VISIBLE

        // استخدام Coroutines لتحميل البيانات في الخلفية دون تعطيل الواجهة
        lifecycleScope.launch {
            try {
                val names = loadSurahNames()
                
                // العودة لواجهة المستخدم لعرض البيانات
                b.rvIndex.layoutManager = LinearLayoutManager(this@IndexActivity)
                b.rvIndex.setHasFixedSize(true) // تحسين أداء القائمة
                
                b.rvIndex.adapter = SurahAdapter(names = names, bookmarkedIndex = -1) { index ->
                    startActivity(
                        Intent(this@IndexActivity, MainActivity::class.java)
                            .putExtra("surahIndex", index)
                            .putExtra("fromIndex", true)
                    )
                }
                
                b.pbLoading.visibility = View.GONE

            } catch (e: Exception) {
                b.pbLoading.visibility = View.GONE
                b.tvError.visibility = View.VISIBLE
                b.tvError.text = "حدث خطأ أثناء تحميل الفهرس"
            }
        }
    }

    // دالة مخصصة للقراءة في الخلفية
    private suspend fun loadSurahNames(): ArrayList<String> = withContext(Dispatchers.IO) {
        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)
        val namesList = ArrayList<String>(surahs.length())
        
        for (i in 0 until surahs.length()) {
            val obj = surahs.getJSONObject(i)
            namesList.add(obj.getString("name"))
        }
        namesList
    }
}
