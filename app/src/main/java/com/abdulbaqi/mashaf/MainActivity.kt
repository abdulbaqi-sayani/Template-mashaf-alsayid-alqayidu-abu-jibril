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
    private lateinit var adapter: QuranAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)

        // ✅ قائمة واحدة لكل المصحف (عنوان سورة + آياتها)
        val items = ArrayList<QItem>(12000)

        for (s in 0 until surahs.length()) {
            val surahObj = surahs.getJSONObject(s)
            val name = surahObj.getString("name")
            items.add(QItem.SurahTitle(name, s))

            val ayahsArray = surahObj.getJSONArray("ayahs")

            var counter = 0 // ✅ عداد الآيات المرقّمة داخل السورة

            for (a in 0 until ayahsArray.length()) {
                val raw = ayahsArray.getString(a)
                val text = raw
                    .replace("\r", " ")
                    .replace("\n", " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()

                val isFatiha = (s == 0)

                val isBismillah =
                    text.contains("بسم الله") && text.contains("الرحمن") && text.contains("الرحيم")

                val isSalawatOrDecor =
                    text.contains("اللهم صل") || text.startsWith("✽") || text.endsWith("✽")

                // ✅ القاعدة:
                // - الفاتحة: البسملة مرقمة
                // - غير الفاتحة: البسملة بدون رقم
                // - سطر الصلاة/الزخرفة بدون رقم
                val number: Int? = when {
                    isSalawatOrDecor -> null
                    (!isFatiha && isBismillah) -> null
                    else -> {
                        counter += 1
                        counter
                    }
                }

                items.add(QItem.Ayah(text, s, a, number))
            }
        }

        val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

        lm = LinearLayoutManager(this)
        b.rvAyah.layoutManager = lm

        adapter = QuranAdapter(items, amiri)
        b.rvAyah.adapter = adapter

        // ✅ Divider (خط فاصل)
        val divider = DividerItemDecoration(this, lm.orientation)
        ContextCompat.getDrawable(this, R.drawable.divider_ayah)?.let { divider.setDrawable(it) }
        b.rvAyah.addItemDecoration(divider)

        // ✅ إذا جاء طلب من الفهرس: افتح السورة المطلوبة في أعلى الشاشة
        val fromIndex = intent.getBooleanExtra("fromIndex", false)
        val wantedSurah = intent.getIntExtra("surahIndex", 0)

        if (fromIndex) {
            val pos = items.indexOfFirst { it is QItem.SurahTitle && it.surahIndex == wantedSurah }
            if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
        } else {
            // ✅ متابعة القراءة (إشارة مرجعية)
            if (BookmarkStore.hasBookmark(this)) {
                val s = BookmarkStore.getSurahIndex(this)
                val a = BookmarkStore.getAyahIndex(this)

                val pos = items.indexOfFirst { it is QItem.Ayah && it.surahIndex == s && it.ayahIndex == a }
                if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        // ✅ حفظ الإشارة المرجعية: أول "آية" ظاهرة (نتجاوز عنوان السورة)
        val pos = lm.findFirstVisibleItemPosition()
        if (pos < 0) return

        val item = adapter.getItemAt(pos)
        if (item is QItem.Ayah) {
            BookmarkStore.save(this, item.surahIndex, item.ayahIndex)
        }
    }
}
