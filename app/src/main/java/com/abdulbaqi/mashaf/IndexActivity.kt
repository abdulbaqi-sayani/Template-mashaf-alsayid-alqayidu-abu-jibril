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
        
        // 1. إعداد واجهة المستخدم وربطها بالكود
        b = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(b.root)

        // 2. إعداد القائمة (RecyclerView) لتحقيق أفضل أداء
        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.setHasFixedSize(true)

        // 3. برمجة زر "متابعة القراءة" للانتقال لآخر مكان توقف عنده المستخدم
        b.btnContinue.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // نرسل fromIndex = false ليعلم التطبيق أنه يجب أن يفتح آخر علامة مرجعية
            intent.putExtra("fromIndex", false)
            startActivity(intent)
        }

        // 4. البدء في تحميل بيانات السور الحقيقية
        loadDataAsync()
    }

    private fun loadDataAsync() {
        // إظهار مؤشر التحميل
        b.pbLoading.visibility = View.VISIBLE
        b.tvError.visibility = View.GONE
        
        // استخدام Coroutines للتحميل في الخلفية (Dispatchers.IO)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // قراءة ملف quran.json من مجلد assets
                val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
                val surahsArray = JSONArray(jsonText)
                
                val names = ArrayList<String>(surahsArray.length())
                
                // استخراج أسماء السور
                for (i in 0 until surahsArray.length()) {
                    val surahObj = surahsArray.getJSONObject(i)
                    names.add(surahObj.getString("name"))
                }

                // العودة للواجهة الرئيسية (Main Thread) لتحديث القائمة
                withContext(Dispatchers.Main) {
                    b.pbLoading.visibility = View.GONE
                    
                    // إعداد الأداپتر وربطه بالقائمة
                    b.rvIndex.adapter = SurahAdapter(names, -1) { index ->
                        // عند الضغط على سورة، ننتقل لـ MainActivity ونرسل رقم السورة
                        val intent = Intent(this@IndexActivity, MainActivity::class.java)
                        intent.putExtra("surahIndex", index)
                        intent.putExtra("fromIndex", true)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                // في حال حدوث خطأ (مثل فقدان الملف أو خطأ في التنسيق)
                withContext(Dispatchers.Main) {
                    b.pbLoading.visibility = View.GONE
                    b.tvError.visibility = View.VISIBLE
                    b.tvError.text = "فشل تحميل الفهرس: ${e.message}"
                }
            }
        }
    }
}
