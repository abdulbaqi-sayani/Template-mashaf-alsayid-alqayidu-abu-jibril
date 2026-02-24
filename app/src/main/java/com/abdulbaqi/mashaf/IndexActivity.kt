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

        // 2. جلب الأسماء من المصفوفة وتحويلها إلى كائنات Surah
        val surahNamesArray = resources.getStringArray(R.array.surah_names)
        val surahList = surahNamesArray.map { Surah(it) }

        // 3. إعداد الـ RecyclerView
        // ملاحظة: تأكد أن ID الـ RecyclerView في ملف XML هو rv_surah أو rvSurah
        binding.rvSurah.layoutManager = LinearLayoutManager(this)

        // 4. إنشاء الـ Adapter مع دالة الضغط
        val adapter = SurahAdapter(surahList) { position ->
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("surah_index", position)
            startActivity(intent)
        }

        // 5. ربط المحول
        binding.rvSurah.adapter = adapter
    }
}
