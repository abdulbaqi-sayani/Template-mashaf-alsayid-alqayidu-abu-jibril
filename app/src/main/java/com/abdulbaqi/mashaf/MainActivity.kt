package com.abdulbaqi.mashaf

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityMainBinding
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var lm: LinearLayoutManager
    private lateinit var items: ArrayList<QItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            b = ActivityMainBinding.inflate(layoutInflater)
            setContentView(b.root)

            val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
            val surahs = JSONArray(jsonText)

            items = ArrayList()

            for (s in 0 until surahs.length()) {
                val surahObj = surahs.getJSONObject(s)
                val name = surahObj.getString("name")

                items.add(QItem.SurahTitle(name = name, surahIndex = s))

                val ayahsArray = surahObj.getJSONArray("ayahs")
                for (a in 0 until ayahsArray.length()) {
                    val raw = ayahsArray.optString(a, "")
                    val text = raw
                        .replace("\r", " ")
                        .replace("\n", " ")
                        .replace(Regex("\\s+"), " ")
                        .trim()

                    if (text.isNotBlank()) {
                        items.add(QItem.Ayah(text = text, surahIndex = s, ayahIndex = a))
                    }
                }
            }

            val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

            lm = LinearLayoutManager(this)
            b.rvAyah.layoutManager = lm
            b.rvAyah.setHasFixedSize(true)

            val adapter = QuranAdapter(items, amiri)
            b.rvAyah.adapter = adapter

            val divider = DividerItemDecoration(this, lm.orientation)
            ContextCompat.getDrawable(this, R.drawable.divider_ayah)
                ?.let { divider.setDrawable(it) }
            b.rvAyah.addItemDecoration(divider)

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
