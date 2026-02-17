package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding
import org.json.JSONArray

class IndexActivity : AppCompatActivity() {

    private lateinit var b: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(b.root)

        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)

        val names = ArrayList<String>()
        for (i in 0 until surahs.length()) {
            names.add(surahs.getJSONObject(i).getString("name"))
        }

        b.rvSurah.layoutManager = LinearLayoutManager(this)
        b.rvSurah.adapter = SurahAdapter(names) { position ->

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("surahIndex", position)
            startActivity(intent)
        }
    }
}
