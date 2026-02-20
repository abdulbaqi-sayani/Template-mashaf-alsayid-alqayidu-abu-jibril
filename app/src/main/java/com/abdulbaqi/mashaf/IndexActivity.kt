package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import android.view.View
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

        b.tvError.visibility = View.GONE
        b.pbLoading.visibility = View.VISIBLE

        try {
            val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
            val surahs = JSONArray(jsonText)

            val names = ArrayList<String>(surahs.length())
            for (i in 0 until surahs.length()) {
                val obj = surahs.getJSONObject(i)
                names.add(obj.getString("name"))
            }

            b.rvIndex.layoutManager = LinearLayoutManager(this)
            b.rvIndex.setHasFixedSize(true)

            b.rvIndex.adapter = SurahAdapter(
                names = names,
                bookmarkedIndex = -1
            ) { index ->
                startActivity(
                    Intent(this, MainActivity::class.java)
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
