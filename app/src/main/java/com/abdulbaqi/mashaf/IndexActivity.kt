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

        binding = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val surahNamesArray = resources.getStringArray(R.array.surah_names)
        val surahList = surahNamesArray.map { Surah(it) }

        binding.rvSurah.layoutManager = LinearLayoutManager(this)

        binding.rvSurah.adapter = SurahAdapter(surahList) { position ->
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("surah_index", position)
            }
            startActivity(intent)
        }
    }
}
