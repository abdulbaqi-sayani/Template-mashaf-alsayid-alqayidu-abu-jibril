package com.abdulbaqi.mashaf

import android.content.pm.ActivityInfo
import android.graphics.Typeface
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

    // تعريف المتغيرات الأساسية للتطبيق
    private lateinit var b: ActivityMainBinding // لربط واجهة المستخدم (View Binding)
    private lateinit var lm: LinearLayoutManager // لإدارة طريقة عرض القائمة
    private lateinit var items: ArrayList<QItem> // القائمة التي ستحمل بيانات السور والآيات

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // 1. تثبيت اتجاه الشاشة ليكون عمودياً فقط
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            // 2. إعداد واجهة المستخدم
            b = ActivityMainBinding.inflate(layoutInflater)
            setContentView(b.root)

            // 3. قراءة ملف JSON الذي يحتوي على القرآن الكريم من مجلد assets
            val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
            val surahs = JSONArray(jsonText)

            // تهيئة القائمة بحجم مبدئي لاستيعاب الآيات
            items = ArrayList(10000)

            // 4. المرور على السور واستخراج البيانات
            for (s in 0 until surahs.length()) {
                val surahObj = surahs.getJSONObject(s)
                val name = surahObj.getString("name")

                // إضافة عنوان السورة إلى القائمة
                items.add(QItem.SurahTitle(name = name, surahIndex = s))

                val ayahsArray = surahObj.getJSONArray("ayahs")
                for (a in 0 until ayahsArray.length()) {
                    val raw = ayahsArray.optString(a, "")
                    // تنظيف النص من المسافات والأسطر الزائدة
                    val text = raw
                        .replace("\r", " ")
                        .replace("\n", " ")
                        .replace(Regex("\\s+"), " ")
                        .trim()

                    // إضافة الآية إذا لم تكن فارغة
                    if (text.isNotBlank()) {
                        items.add(QItem.Ayah(text = text, surahIndex = s, ayahIndex = a))
                    }
                }
            }

            // 5. تحميل الخط (هنا يكمن التعديل لتجنب الأخطاء)
            // إذا قمت بوضع الخط في مسار app/src/main/res/font/amiri_quran.ttf استخدم السطر التالي:
            val amiri = ResourcesCompat.getFont(this, R.font.amiri_quran) 
            
            // ملاحظة: إذا أردت استخدام مجلد assets بدلاً من res/font، قم بتفعيل السطر التالي وإلغاء الذي قبله:
            // val amiri = Typeface.createFromAsset(assets, "amiri_quran.ttf")

            // 6. إعداد القائمة (RecyclerView)
            lm = LinearLayoutManager(this)
            b.rvAyah.layoutManager = lm
            val adapter = QuranAdapter(items, amiri)
            b.rvAyah.adapter = adapter

            // 7. إضافة فاصل (Divider) بين الآيات لتجميل العرض
            val divider = DividerItemDecoration(this, lm.orientation)
            ContextCompat.getDrawable(this, R.drawable.divider_ayah)?.let { divider.setDrawable(it) }
            b.rvAyah.addItemDecoration(divider)

            // 8. التحقق مما إذا كان المستخدم قادماً من شاشة الفهرس
            val fromIndex = intent.getBooleanExtra("fromIndex", false)
            val wantedName = intent.getStringExtra("surahName")
            val wantedIndex = intent.getIntExtra("surahIndex", -1)

            if (fromIndex) {
                // البحث عن موضع السورة المطلوبة
                var pos = if (!wantedName.isNullOrBlank()) {
                    items.indexOfFirst { it is QItem.SurahTitle && it.name == wantedName }
                } else -1

                if (pos < 0 && wantedIndex >= 0) {
                    pos = items.indexOfFirst { it is QItem.SurahTitle && it.surahIndex == wantedIndex }
                }

                // التمرير (Scroll) إلى السورة المطلوبة
                if (pos >= 0) {
                    b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
                } else {
                    Toast.makeText(this, "لم يتم العثور على السورة", Toast.LENGTH_SHORT).show()
                }
                return
            }

            // 9. متابعة القراءة من الإشارة المرجعية (آخر مكان توقف عنده المستخدم)
            if (BookmarkStore.hasBookmark(this)) {
                val s = BookmarkStore.getSurahIndex(this)
                val a = BookmarkStore.getAyahIndex(this)

                val pos = items.indexOfFirst { it is QItem.Ayah && it.surahIndex == s && it.ayahIndex == a }
                if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
            }

        } catch (e: Exception) {
            // معالجة الأخطاء حتى لا يتوقف التطبيق فجأة
            Toast.makeText(this, "حدث خطأ: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()

        // 10. حفظ آخر مكان للآية المرئية عندما يخرج المستخدم من التطبيق
        val adapter = b.rvAyah.adapter as? QuranAdapter ?: return
        val pos = lm.findFirstVisibleItemPosition()
        if (pos < 0) return

        val item = adapter.getItemAt(pos)
        if (item is QItem.Ayah) {
            BookmarkStore.save(this, item.surahIndex, item.ayahIndex)
        }
    }
}
