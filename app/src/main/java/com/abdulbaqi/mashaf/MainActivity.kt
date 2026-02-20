package com.abdulbaqi.mashaf

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityMainBinding
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private lateinit var lm: LinearLayoutManager
    private var items: ArrayList<QItem> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            b = ActivityMainBinding.inflate(layoutInflater)
            setContentView(b.root)

            lm = LinearLayoutManager(this)
            b.rvAyah.layoutManager = lm
            b.rvAyah.setHasFixedSize(true)
            b.rvAyah.setItemViewCacheSize(24)

            val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

            // رقم السورة المطلوب فتحها
            val surahIndex = intent.getIntExtra("surahIndex", -1)

            // تحميل السورة في الخلفية (خفيف جدًا الآن)
            Thread {
                try {
                    val built = buildItemsForOneSurah(surahIndex)
                    runOnUiThread {
                        items = built
                        b.rvAyah.adapter = QuranAdapter(items, amiri)

                        // فتح على الإشارة المرجعية إن كانت لنفس السورة
                        if (BookmarkStore.hasBookmark(this)) {
                            val s = BookmarkStore.getSurahIndex(this)
                            val a = BookmarkStore.getAyahIndex(this)

                            if (s == surahIndex) {
                                val pos = items.indexOfFirst { it is QItem.Ayah && it.ayahIndex == a }
                                if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
                            }
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }.start()

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun buildItemsForOneSurah(surahIndex: Int): ArrayList<QItem> {
        val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)

        // إذا لم يُرسل رقم سورة، افتح سورة الإشارة المرجعية إن وجدت، وإلا افتح الأولى
        val safeIndex = when {
            surahIndex in 0 until surahs.length() -> surahIndex
            BookmarkStore.hasBookmark(this) -> BookmarkStore.getSurahIndex(this).coerceIn(0, surahs.length() - 1)
            else -> 0
        }

        val surahObj = surahs.getJSONObject(safeIndex)
        val name = surahObj.getString("name")
        val ayahsArray = surahObj.getJSONArray("ayahs")

        val out = ArrayList<QItem>(ayahsArray.length() + 1)

        // عنوان السورة
        out.add(QItem.SurahTitle(name = name, surahIndex = safeIndex))

        // الآيات
        for (a in 0 until ayahsArray.length()) {
            val raw = ayahsArray.optString(a, "")
            val text = raw
                .replace("\r", " ")
                .replace("\n", " ")
                .replace(Regex("\\s+"), " ")
                .trim()

            if (text.isNotBlank()) {
                out.add(QItem.Ayah(text = text, surahIndex = safeIndex, ayahIndex = a))
            }
        }

        return out
    }

    override fun onPause() {
        super.onPause()

        val adapter = b.rvAyah.adapter as? QuranAdapter ?: return
        val pos = lm.findFirstVisibleItemPosition()
        if (pos < 0) return

        val item = adapter.getItemAt(pos)
        if (item is QItem.Ayah) {
            // حفظ السورة والآية
            BookmarkStore.save(this, item.surahIndex, item.ayahIndex)
        }
    }
}
