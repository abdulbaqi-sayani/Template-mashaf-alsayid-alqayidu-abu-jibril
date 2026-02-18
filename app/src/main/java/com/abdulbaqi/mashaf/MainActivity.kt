// MainActivity.kt
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

            items = ArrayList(10000)

            for (s in 0 until surahs.length()) {
                val surahObj = surahs.getJSONObject(s)
                val name = surahObj.getString("name")

                // ✅ عنوان السورة مرة واحدة
                items.add(QItem.SurahTitle(name = name, surahIndex = s))

                val ayahsArray = surahObj.getJSONArray("ayahs")
                for (a in 0 until ayahsArray.length()) {
                    val raw = ayahsArray.optString(a, "")
                    val text = raw
                        .replace("\r", " ")
                        .replace("\n", " ")
                        .replace(Regex("\\s+"), " ")
                        .trim()

                    // تجاهل الأسطر الفارغة فقط
                    if (text.isNotBlank()) {
                        items.add(QItem.Ayah(text = text, surahIndex = s, ayahIndex = a))
                    }
                }
            }

            val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

            lm = LinearLayoutManager(this)
            b.rvAyah.layoutManager = lm
            val adapter = QuranAdapter(items, amiri)
            b.rvAyah.adapter = adapter

            // Divider مخصص
            val divider = DividerItemDecoration(this, lm.orientation)
            ContextCompat.getDrawable(this, R.drawable.divider_ayah)?.let { divider.setDrawable(it) }
            b.rvAyah.addItemDecoration(divider)

            // ✅ فتح من الفهرس
            val fromIndex = intent.getBooleanExtra("fromIndex", false)
            val wantedName = intent.getStringExtra("surahName")
            val wantedIndex = intent.getIntExtra("surahIndex", -1)

            if (fromIndex) {
                // 1) الأفضل: ابحث بالاسم (آمن حتى لو تغيّر ترتيب السور أو حذفت التكرار)
                var pos = if (!wantedName.isNullOrBlank()) {
                    items.indexOfFirst { it is QItem.SurahTitle && it.name == wantedName }
                } else -1

                // 2) احتياط: لو الاسم غير موجود، استخدم الرقم إذا كان ضمن النطاق
                if (pos < 0 && wantedIndex >= 0) {
                    pos = items.indexOfFirst { it is QItem.SurahTitle && it.surahIndex == wantedIndex }
                }

                if (pos >= 0) {
                    b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
                } else {
                    Toast.makeText(this, "لم يتم العثور على السورة", Toast.LENGTH_SHORT).show()
                }
                return
            }

            // ✅ متابعة القراءة من الإشارة المرجعية
            if (BookmarkStore.hasBookmark(this)) {
                val s = BookmarkStore.getSurahIndex(this)
                val a = BookmarkStore.getAyahIndex(this)

                val pos = items.indexOfFirst { it is QItem.Ayah && it.surahIndex == s && it.ayahIndex == a }
                if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
            }

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
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
