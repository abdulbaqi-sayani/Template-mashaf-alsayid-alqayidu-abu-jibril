package com.abdulbaqi.mashaf

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.abdulbaqi.mashaf.databinding.ActivityIndexBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IndexActivity : AppCompatActivity() {

    private lateinit var b: ActivityIndexBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // إعداد الربط (Binding)
        b = ActivityIndexBinding.inflate(layoutInflater)
        setContentView(b.root)

        // إعداد القائمة بشكل سريع
        b.rvIndex.layoutManager = LinearLayoutManager(this)
        b.rvIndex.setHasFixedSize(true)

        // زر متابعة القراءة
        b.btnContinue.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        loadDataAsync()
    }

    private fun loadDataAsync() {
        b.pbLoading.visibility = View.VISIBLE
        
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                // إنشاء قائمة وهمية للاختبار لضمان السرعة القصوى
                val names = ArrayList<String>(114)
                for (i in 1..114) {
                    names.add("سورة تجريبية رقم $i")
                }

                withContext(Dispatchers.Main) {
                    b.pbLoading.visibility = View.GONE
                    b.rvIndex.adapter = SurahAdapter(names, -1) { index ->
                        val intent = Intent(this@IndexActivity, MainActivity::class.java)
                        intent.putExtra("surahIndex", index)
                        intent.putExtra("fromIndex", true)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    b.pbLoading.visibility = View.GONE
                    b.tvError.visibility = View.VISIBLE
                    b.tvError.text = "خطأ في التحميل"
                }
            }
        }
    }
}
