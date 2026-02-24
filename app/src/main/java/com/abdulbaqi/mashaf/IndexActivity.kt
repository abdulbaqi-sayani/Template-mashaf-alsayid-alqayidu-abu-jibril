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
        
        // 1. إعداد الـ View Binding
        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. تجهيز قائمة السور (جلب الأسماء من المصفوفة النصية في strings.xml)
        val surahNamesArray = resources.getStringArray(R.array.surah_names)
        
        // تحويل المصفوفة النصية إلى قائمة من كائنات Surah لكي يقبلها الـ Adapter الجديد
        val surahList = surahNamesArray.map { name -> Surah(name) }

        // 3. إعداد الـ RecyclerView
        binding.rvSurah.layoutManager = LinearLayoutManager(this)

        // 4. إنشاء الـ Adapter مع دالة الضغط (Lambda)
        val adapter = SurahAdapter(surahList) { position ->
            // الكود الذي ينفذ عند الضغط على سورة معينة
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("surah_index", position) // إرسال رقم السورة للمصحف
            startActivity(intent)
        }

        // 5. ربط المحول بالـ RecyclerView
        binding.rvSurah.adapter = adapter
    }
}
