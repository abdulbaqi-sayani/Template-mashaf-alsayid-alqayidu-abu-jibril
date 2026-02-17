package com.abdulbaqi.mashaf

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityMainBinding
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var lm: LinearLayoutManager

    private var surahIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        // السورة القادمة من الفهرس
        surahIndex = intent.getIntExtra("surahIndex", 0)

        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)
        val surahObj = surahs.getJSONObject(surahIndex)
        val ayahsArray = surahObj.getJSONArray("ayahs")

        val ayahs = ArrayList<String>(ayahsArray.length())
        for (i in 0 until ayahsArray.length()) {
            ayahs.add(ayahsArray.getString(i))
        }

        val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

        lm = LinearLayoutManager(this)
        b.rvAyah.layoutManager = lm
        b.rvAyah.adapter = AyahAdapter(ayahs, amiri)

        // Divider (إذا كنت تستخدمه)
        val divider = DividerItemDecoration(this, lm.orientation)
        val d = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.divider_ayah)
        if (d != null) divider.setDrawable(d)
        b.rvAyah.addItemDecoration(divider)

        // ✅ الرجوع لآخر موضع محفوظ إذا كانت نفس السورة
        if (BookmarkStore.hasBookmark(this)) {
            val savedSurah = BookmarkStore.getSurahIndex(this)
            val savedAyah = BookmarkStore.getAyahIndex(this)
            if (savedSurah == surahIndex && savedAyah >= 0) {
                b.rvAyah.post { lm.scrollToPositionWithOffset(savedAyah, 0) }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // ✅ حفظ موضع القراءة تلقائيًا
        val firstVisible = lm.findFirstVisibleItemPosition()
        if (firstVisible >= 0) {
            BookmarkStore.save(this, surahIndex, firstVisible)
        }
    }
}
