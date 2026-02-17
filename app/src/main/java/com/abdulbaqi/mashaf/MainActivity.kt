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

        // ✅ اجمع كل المصحف في قائمة واحدة (للتمرير المستمر)
        val items = ArrayList<String>()
        val surahStartPos = IntArray(surahs.length()) // موضع بداية كل سورة داخل القائمة

        for (s in 0 until surahs.length()) {
            surahStartPos[s] = items.size
            val surahObj = surahs.getJSONObject(s)
            val ayahsArray = surahObj.getJSONArray("ayahs")

            for (i in 0 until ayahsArray.length()) {
                items.add(ayahsArray.getString(i))
            }
        }

        // ✅ خط Amiri Quran
        val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

        // ✅ RecyclerView
        val lm = LinearLayoutManager(this)
        b.rvAyah.layoutManager = lm

        // ✅ Divider مخصص
        val divider = DividerItemDecoration(this, lm.orientation)
        ContextCompat.getDrawable(this, R.drawable.divider_ayah)?.let { divider.setDrawable(it) }
        b.rvAyah.addItemDecoration(divider)

        // ✅ Adapter
        b.rvAyah.adapter = AyahAdapter(items, amiri)

        // ✅ الانتقال لموضع السورة من الفهرس أو استرجاع المرجعية
        val prefs = getSharedPreferences("mashaf", MODE_PRIVATE)
        val savedPos = prefs.getInt("last_pos", 0).coerceAtLeast(0)

        val indexFromIndex = intent.getIntExtra("surahIndex", -1)
        val targetPos =
            if (indexFromIndex in 0 until surahs.length()) surahStartPos[indexFromIndex]
            else savedPos

        b.rvAyah.post { b.rvAyah.scrollToPosition(targetPos) }
    }

    override fun onPause() {
        super.onPause()

        val lm = b.rvAyah.layoutManager as LinearLayoutManager
        val pos = lm.findFirstVisibleItemPosition().coerceAtLeast(0)

        getSharedPreferences("mashaf", MODE_PRIVATE)
            .edit()
            .putInt("last_pos", pos)
            .apply()
    }
}
