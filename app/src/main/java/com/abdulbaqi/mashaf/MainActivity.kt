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
            b.rvAyah.setItemViewCacheSize(30)
            b.rvAyah.itemAnimator = null

            val divider = DividerItemDecoration(this, lm.orientation)
            ContextCompat.getDrawable(this, R.drawable.divider_ayah)?.let { divider.setDrawable(it) }
            b.rvAyah.addItemDecoration(divider)

            val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

            val wantedSurah = intent.getIntExtra("surahIndex", -1)

            // ✅ تأكد أن البيانات جاهزة (من الكاش)
            QuranRepository.ensureLoaded(this) { ok, err ->
                if (!ok) {
                    runOnUiThread {
                        Toast.makeText(this, "خطأ: $err", Toast.LENGTH_LONG).show()
                        finish()
                    }
                    return@ensureLoaded
                }

                Thread {
                    try {
                        val built = buildItemsForOneSurah(wantedSurah)
                        runOnUiThread {
                            items = built
                            b.rvAyah.adapter = QuranAdapter(items, amiri)
                            scrollToBookmarkIfSameSurah(wantedSurah)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }
                }.start()
            }

        } catch (e: Exception) {
            Toast.makeText(this, "خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun buildItemsForOneSurah(surahIndex: Int): ArrayList<QItem> {
        val count = QuranRepository.surahCount()
        val safeIndex = when {
            surahIndex in 0 until count -> surahIndex
            BookmarkStore.hasBookmark(this) -> BookmarkStore.getSurahIndex(this).coerceIn(0, count - 1)
            else -> 0
        }

        val surahObj = QuranRepository.getSurahObject(safeIndex)
            ?: throw IllegalStateException("تعذر تحميل السورة")

        val name = surahObj.getString("name")
        val ayahsArray = surahObj.getJSONArray("ayahs")

        val out = ArrayList<QItem>(ayahsArray.length() + 1)
        out.add(QItem.SurahTitle(name = name, surahIndex = safeIndex))

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

    private fun scrollToBookmarkIfSameSurah(openedSurah: Int) {
        if (!BookmarkStore.hasBookmark(this)) return

        val s = BookmarkStore.getSurahIndex(this)
        val a = BookmarkStore.getAyahIndex(this)

        if (openedSurah >= 0 && s != openedSurah) return

        val pos = items.indexOfFirst { it is QItem.Ayah && it.ayahIndex == a }
        if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
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
