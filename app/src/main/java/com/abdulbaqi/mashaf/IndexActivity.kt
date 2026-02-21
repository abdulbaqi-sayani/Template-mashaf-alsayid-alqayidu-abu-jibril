package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding
import kotlinx.coroutines.*
import org.json.JSONArray

class IndexActivity : AppCompatActivity() {

    private lateinit var b: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(b.root)

        // إعداد القائمة فوراً وهي فارغة لتجهيز الواجهة
        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.setHasFixedSize(true)

        loadDataAsync()
    }

    private fun loadDataAsync() {
        b.pbLoading.visibility = View.VISIBLE
        
        // استخدام Default لعمليات المعالجة الحسابية (Parsing)
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                // 1. قراءة الملف
                val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
                val surahs = JSONArray(jsonText)
                val names = ArrayList<String>(114)

                // 2. استخراج الأسماء فقط (عملية سريعة)
                for (i in 0 until surahs.length()) {
                    names.add(surahs.getJSONObject(i).getString("name"))
                }

                // 3. التحديث على واجهة المستخدم
                withContext(Dispatchers.Main) {
                    b.rvIndex.adapter = SurahAdapter(names, -1) { index ->
                        val intent = Intent(this@IndexActivity, MainActivity::class.java)
                        intent.putExtra("surahIndex", index)
                        intent.putExtra("fromIndex", true)
                        startActivity(intent)
                    }
                    b.pbLoading.visibility = View.GONE
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    b.pbLoading.visibility = View.GONE
                    b.tvError.apply {
                        visibility = View.VISIBLE
                        text = "خطأ في تحميل البيانات"
                    }
                }
            }
        }
    }
}
