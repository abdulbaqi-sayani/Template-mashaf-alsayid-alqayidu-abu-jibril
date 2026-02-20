package com.abdulbaqi.mashaf

import android.content.Context
import org.json.JSONArray

object QuranRepository {

    @Volatile private var isLoading = false
    @Volatile private var isReady = false

    private var surahs: JSONArray? = null
    private var names: ArrayList<String> = arrayListOf()

    fun ensureLoaded(context: Context, onDone: (ok: Boolean, err: String?) -> Unit) {
        if (isReady && surahs != null && names.isNotEmpty()) {
            onDone(true, null)
            return
        }

        if (isLoading) {
            // انتظر بسيط حتى يخلص التحميل
            Thread {
                var tries = 0
                while (!isReady && tries < 200) { // ~2 ثانية
                    Thread.sleep(10)
                    tries++
                }
                if (isReady && surahs != null) onDone(true, null)
                else onDone(false, "لم يكتمل تحميل البيانات")
            }.start()
            return
        }

        isLoading = true

        Thread {
            try {
                val jsonText = context.assets.open("quran.json").bufferedReader().use { it.readText() }
                val arr = JSONArray(jsonText)

                val tmpNames = ArrayList<String>(arr.length())
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    tmpNames.add(obj.getString("name"))
                }

                surahs = arr
                names = tmpNames
                isReady = true
                isLoading = false
                onDone(true, null)

            } catch (e: Exception) {
                isReady = false
                isLoading = false
                onDone(false, e.message ?: "خطأ غير معروف")
            }
        }.start()
    }

    fun getSurahNames(): List<String> = names

    fun surahCount(): Int = surahs?.length() ?: 0

    fun getSurahObject(index: Int) = surahs?.getJSONObject(index)
}
