package com.abdulbaqi.mashaf

import android.content.Context

object BookmarkStore {
    private const val PREF = "mashaf_prefs"
    private const val KEY_SURAH_INDEX = "bookmark_surah_index"
    private const val KEY_AYAH_INDEX = "bookmark_ayah_index"

    fun save(context: Context, surahIndex: Int, ayahIndex: Int) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_SURAH_INDEX, surahIndex)
            .putInt(KEY_AYAH_INDEX, ayahIndex)
            .apply()
    }

    fun getSurahIndex(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_SURAH_INDEX, 0)
    }

    fun getAyahIndex(context: Context): Int {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_AYAH_INDEX, 0)
    }

    fun hasBookmark(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return sp.contains(KEY_SURAH_INDEX) && sp.contains(KEY_AYAH_INDEX)
    }
}
