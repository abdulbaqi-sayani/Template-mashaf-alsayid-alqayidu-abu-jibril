override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    b = com.abdulbaqi.mashaf.databinding.ActivityMainBinding.inflate(layoutInflater)
    setContentView(b.root)

    // 1) قراءة quran.json
    val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
    val surahs = org.json.JSONArray(jsonText)

    // 2) تجهيز قائمة واحدة (عنوان سورة + آياتها)
    val items = ArrayList<QItem>(10000)

    for (s in 0 until surahs.length()) {
        val surahObj = surahs.getJSONObject(s)
        val name = surahObj.getString("name")

        // عنوان السورة (يظهر مرة واحدة فقط)
        items.add(QItem.SurahTitle(name = name, surahIndex = s))

        val ayahsArray = surahObj.getJSONArray("ayahs")
        for (a in 0 until ayahsArray.length()) {
            val raw = ayahsArray.getString(a)

            // لا نكسر الآية إلى عدة عناصر (يبقى نص الآية كما هو داخل JSON)
            val text = raw
                .replace("\r", " ")
                .replace("\n", " ")
                .replace(Regex("\\s+"), " ")
                .trim()

            items.add(QItem.Ayah(text = text, surahIndex = s, ayahIndex = a))
        }
    }

    // 3) إعداد الخط + RecyclerView
    val amiri = androidx.core.content.res.ResourcesCompat.getFont(this, R.font.amiri_quran)
    lm = androidx.recyclerview.widget.LinearLayoutManager(this)
    b.rvAyah.layoutManager = lm
    b.rvAyah.adapter = QuranAdapter(items, amiri)

    // 4) Divider (الخط الفاصل بين كل عنصر)
    val divider = androidx.recyclerview.widget.DividerItemDecoration(this, lm.orientation)
    androidx.core.content.ContextCompat.getDrawable(this, R.drawable.divider_ayah)
        ?.let { divider.setDrawable(it) }
    b.rvAyah.addItemDecoration(divider)

    // 5) فتح من الفهرس: بالاسم حتى لا يتعطل لو تغيّر ترتيب السور
    val fromIndex = intent.getBooleanExtra("fromIndex", false)
    val wantedName = intent.getStringExtra("surahName")

    if (fromIndex && !wantedName.isNullOrBlank()) {
        val pos = items.indexOfFirst { it is QItem.SurahTitle && it.name == wantedName }
        if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
        return
    }

    // 6) متابعة القراءة (Bookmark)
    if (BookmarkStore.hasBookmark(this)) {
        val s = BookmarkStore.getSurahIndex(this)
        val a = BookmarkStore.getAyahIndex(this)

        val pos = items.indexOfFirst { it is QItem.Ayah && it.surahIndex == s && it.ayahIndex == a }
        if (pos >= 0) b.rvAyah.post { lm.scrollToPositionWithOffset(pos, 0) }
    }
}
