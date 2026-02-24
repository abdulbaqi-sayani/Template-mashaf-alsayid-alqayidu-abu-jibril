package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding

class IndexActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. إعداد الـ View Binding لربط واجهة المستخدم 
        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. جلب مصفوفة الأسماء من ملف strings.xml
        val surahNamesArray = resources.getStringArray(R.array.surah_names)
        
        // 3. تحويل مصفوفة النصوص إلى قائمة من كائنات Surah لحل مشكلة Type mismatch 
        val surahList = surahNamesArray.map { Surah(it) }

        // 4. إعداد الـ RecyclerView
        binding.rvSurah.layoutManager = LinearLayoutManager(this)

        // 5. إنشاء الـ Adapter مع تمرير القائمة ودالة الضغط (Lambda) 
        val adapter = SurahAdapter(surahList) { position ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("surah_index", position) // تمرير رقم السورة المحددة 
            startActivity(intent)
        }

        // 6. ربط المحول بالـ RecyclerView لعرض القائمة
        binding.rvSurah.adapter = adapter
    }
}
