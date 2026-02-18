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
    private lateinit var lm: LinearLayoutManager
    private lateinit var items: ArrayList<QItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)

        items = ArrayList(12000)

        for (s in 0 until surahs.length()) {
            val surahObj = surahs.getJSONObject(s)
            val name = surahObj.getString("name")
            items.add(QItem.SurahTitle(name = name, surahIndex = s))

            val ayahsArray = surahObj.getJSONArray("ayahs")
            for (a in 0 until ayahsArray.length()) {
                val raw = ayahsArray.getString(a)
                val text = raw
                    .replace("\r", " ")
                    .replace("\n", " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()

                // ✅ لا basmalaOffset هنا إطلاقًا
                items.add(QItem.Ayah(text = text, surahIndex = s, ayahIndex = a))
            }
        }

        val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

        lm = LinearLayoutManager(this)
        b.rvAyah.layoutManager = lm
        b.rvAyah.adapter = QuranAdapter(items, amiri)

        val divider = DividerItemDecoration(this, lm.orientation)
        ContextCompat.getDrawable(this, R.drawable.divider_ayah)?.let { divider.setDrawable(it) }
        b.rvAyah.addItemDecoration(divider)

        // فتح من الفهرس
        val fromIndex = intent.getBooleanExtra("fromIndex", false)
        val wantedName = intent.getStringExtra("surahName")

        if (fromIndex && !wantedName.isNullOrBlank()) {
            val pos = items.indexOfFirst { it is QItem.SurahTitle && it.name == wantedName }
            if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
            return
        }

        // متابعة القراءة
        if (BookmarkStore.hasBookmark(this)) {
            val s = BookmarkStore.getSurahIndex(this)
            val a = BookmarkStore.getAyahIndex(this)
            val pos = items.indexOfFirst { it is QItem.Ayah && it.surahIndex == s && it.ayahIndex == a }
            if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
        }
    }

    override fun onPause() {
        super.onPause()
        val adapter = b.rvAyah.adapter as? QuranAdapter ?: return
        val pos = lm.findFirstVisibleItemPosition()
        if (pos < 0) return

        val item = adapter.getItemAt(pos)
        if (item is QItem.Ayah) {
            BookmarkStore.save(this, item.surahIndex, item.ayahIndex)
        }
    }
}
