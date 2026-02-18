package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding

class IndexActivity : AppCompatActivity() {

    private lateinit var b: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(b.root)

        val names = QuranRepo.getNames(this)

        val bookmarkedIndex = if (BookmarkStore.hasBookmark(this)) {
            BookmarkStore.getSurahIndex(this)
        } else -1

        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.adapter = SurahAdapter(
            names = names,
            bookmarkedIndex = bookmarkedIndex
        ) { index ->
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("fromIndex", true)
                    .putExtra("surahName", names[index])
            )
        }

        b.btnContinue.setOnClickListener {
            if (!BookmarkStore.hasBookmark(this)) return@setOnClickListener
            startActivity(
                Intent(this, MainActivity::class.java)
                    .putExtra("fromIndex", false)
            )
        }
    }
}
