package com.abdulbaqi.mashaf

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.textViewQuran)

        // تحميل الخط من res/font
        val typeface = ResourcesCompat.getFont(this, R.font.amiriquran)
        textView.typeface = typeface

        val text = "بسم الله الرحمن الرحيم ۝ الحمد لله رب العالمين"

        val spannable = SpannableString(text)

        // تلوين لفظ الجلالة بالأخضر
        val wordAllah = "الله"
        val startAllah = text.indexOf(wordAllah)
        if (startAllah >= 0) {
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#008000")),
                startAllah,
                startAllah + wordAllah.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        // تلوين علامة الآية بالأحمر
        val ayahSymbol = "۝"
        val startAyah = text.indexOf(ayahSymbol)
        if (startAyah >= 0) {
            spannable.setSpan(
                ForegroundColorSpan(Color.RED),
                startAyah,
                startAyah + ayahSymbol.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textView.text = spannable
    }
}
