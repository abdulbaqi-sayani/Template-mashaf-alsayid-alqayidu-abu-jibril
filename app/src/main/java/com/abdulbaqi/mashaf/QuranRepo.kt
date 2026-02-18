package com.abdulbaqi.mashaf

import android.content.Context
import org.json.JSONArray

object QuranRepo {

    @Volatile
    private var cachedItems: ArrayList<QItem>? = null

    @Volatile
    private var cachedNames: ArrayList<String>? = null

    fun getNames(context: Context): ArrayList<String> {
        cachedNames?.let { return it }

        val jsonText = context.assets.open("quran.json").bufferedReader().use { it.readText() }
        val surahs = JSONArray(jsonText)

        val names = ArrayList<String>(surahs.length())
        for (i in 0 until surahs.length()) {
            names.add(surahs.getJSONObject(i).getString("name"))
        }

        cachedNames = names
        return names
    }

    fun loadItemsAsync(
        context: Context,
        onSuccess: (ArrayList<QItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        // ✅ إذا محمّل مسبقًا لا تعيد التحميل
        cachedItems?.let {
            onSuccess(it)
            return
        }

        Thread {
            try {
                val jsonText = context.assets.open("quran.json").bufferedReader().use { it.readText() }
                val surahs = JSONArray(jsonText)

                val items = ArrayList<QItem>(12000)

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

                        items.add(QItem.Ayah(text = text, surahIndex = s, ayahIndex = a))
                    }
                }

                cachedItems = items

                onSuccess(items)
            } catch (e: Exception) {
                onError(e.message ?: e.toString())
            }
        }.start()
    }
}
