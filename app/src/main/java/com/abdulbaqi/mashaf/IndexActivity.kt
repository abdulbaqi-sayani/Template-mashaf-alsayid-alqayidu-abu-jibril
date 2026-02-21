private fun loadDataAsync() {
    b.pbLoading.visibility = View.VISIBLE
    
    lifecycleScope.launch(Dispatchers.Default) {
        try {
            // 1. تعطيل قراءة الـ JSON مؤقتاً
            /* val jsonText = assets.open("quran.json").bufferedReader().use { it.readText() }
            val surahs = JSONArray(jsonText)
            */

            // 2. إنشاء قائمة وهمية سريعة جداً
            val names = ArrayList<String>(114)
            for (i in 1..114) {
                names.add("سورة تجريبية رقم $i")
            }

            // 3. التحديث على الواجهة
            withContext(Dispatchers.Main) {
                b.pbLoading.visibility = View.GONE
                b.rvIndex.adapter = SurahAdapter(names, -1) { index ->
                    // لا تفعل شيئاً عند الضغط الآن، نحن نختبر السرعة فقط
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                b.pbLoading.visibility = View.GONE
                b.tvError.text = "خطأ في الاختبار"
            }
        }
    }
}
