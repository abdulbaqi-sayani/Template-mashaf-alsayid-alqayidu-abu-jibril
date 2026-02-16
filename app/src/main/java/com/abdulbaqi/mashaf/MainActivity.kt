package com.abdulbaqi.mashaf

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityMainBinding
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // قفل الوضع العمودي
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)
        val firstSurah = surahs.getJSONObject(0)
        val ayahsArray = firstSurah.getJSONArray("ayahs")

        val ayahs = ArrayList<String>(ayahsArray.length())
        for (i in 0 until ayahsArray.length()) {
            ayahs.add(ayahsArray.getString(i))
        }

        val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

        b.rvAyah.layoutManager = LinearLayoutManager(this)
        b.rvAyah.adapter = AyahAdapter(ayahs, amiri)
    }
}
