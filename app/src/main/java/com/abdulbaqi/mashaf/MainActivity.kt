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
    private var adapter: QuranAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran)

        lm = LinearLayoutManager(this)
        b.rvAyah.layoutManager = lm

        // Divider
        val divider = DividerItemDecoration(this, lm.orientation)
        ContextCompat.getDrawable(this, R.drawable.divider_ayah)?.let { divider.setDrawable(it) }
        b.rvAyah.addItemDecoration(divider)

        // ✅ تحميل المصحف في Thread + حماية من الكراش
        QuranRepo.loadItemsAsync(
            context = this,
            onSuccess = { loaded ->
                runOnUiThread {
                    items = loaded
                    adapter = QuranAdapter(items, amiri)
                    b.rvAyah.adapter = adapter
                    handleIntentScroll()
                }
            },
            onError = { msg ->
                runOnUiThread {
                    Toast.makeText(this, "خطأ في quran.json: $msg", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun handleIntentScroll() {
        val fromIndex = intent.getBooleanExtra("fromIndex", false)
        val wantedName = intent.getStringExtra("surahName")

        if (fromIndex && !wantedName.isNullOrBlank()) {
            val pos = items.indexOfFirst { it is QItem.SurahTitle && it.name == wantedName }
            if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
            return
        }

        // ✅ متابعة القراءة من الإشارة المرجعية
        if (BookmarkStore.hasBookmark(this)) {
            val s = BookmarkStore.getSurahIndex(this)
            val a = BookmarkStore.getAyahIndex(this)

            val pos = items.indexOfFirst { it is QItem.Ayah && it.surahIndex == s && it.ayahIndex == a }
            if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
        }
    }

    override fun onPause() {
        super.onPause()
        val ad = adapter ?: return
        val pos = lm.findFirstVisibleItemPosition()
        if (pos < 0) return

        val item = ad.getItemAt(pos)
        if (item is QItem.Ayah) {
            BookmarkStore.save(this, item.surahIndex, item.ayahIndex)
        }
    }
}
