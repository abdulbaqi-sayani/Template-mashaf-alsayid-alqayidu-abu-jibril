package com.abdulbaqi.mashaf

sealed class QItem {
    data class SurahTitle(val title: String, val surahIndex: Int) : QItem()
    data class Ayah(
        val text: String,
        val surahIndex: Int,
        val ayahIndex: Int,
        val number: Int? // ✅ إذا null = بدون رقم
    ) : QItem()
}
