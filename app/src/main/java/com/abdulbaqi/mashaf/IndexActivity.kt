package com.abdulbaqi.mashaf

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding

class IndexActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val surahNames = try {
            resources.getStringArray(R.array.surah_names).toList()
        } catch (e: Resources.NotFoundException) {
            // في حال لم يتم إنشاء R.array.surah_names بعد
            listOf("الفاتحة", "البقرة", "آل عمران")
        }

        val surahList = surahNames.map { name -> Surah(name) }

        binding.rvSurah.layoutManager = LinearLayoutManager(this)

        val adapter = SurahAdapter(surahList) { position ->
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("surah_index", position)
            }
            startActivity(intent)
        }

        binding.rvSurah.adapter = adapter
    }
}
