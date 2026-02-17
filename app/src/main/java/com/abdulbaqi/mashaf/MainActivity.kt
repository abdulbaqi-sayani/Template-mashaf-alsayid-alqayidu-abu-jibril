package com.abdulbaqi.mashaf

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityMainBinding
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ قفل الوضع العمودي
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // ✅ قراءة quran.json
        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)

        // ✅ استلام رقم السورة من الفهرس (وإلا افتح الأولى)
        val index = intent.getIntExtra("surahIndex", 0).coerceIn(0, surahs.length() - 1)

        val surahObj = surahs.getJSONObject(index)
        val ayahsArray = surahObj.getJSONArray("ayahs")

        val items = ArrayList<String>(ayahsArray.length())
        for (i in 0 until ayahsArray.length()) {
            items.add(ayahsArray.getString(i))
        }

        // ✅ خط Amiri Quran
        val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

        // ✅ RecyclerView
        val lm = LinearLayoutManager(this)
        b.rvAyah.layoutManager = lm

        // ✅ Divider مخصص (خط فاصل جميل)
        val divider = DividerItemDecoration(this, lm.orientation)
        ContextCompat.getDrawable(this, R.drawable.divider_ayah)?.let { divider.setDrawable(it) }
        b.rvAyah.addItemDecoration(divider)

        // ✅ Adapter
        b.rvAyah.adapter = AyahAdapter(items, amiri)
    }
}
