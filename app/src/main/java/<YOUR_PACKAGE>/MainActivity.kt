package YOUR.PACKAGE

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import YOUR.PACKAGE.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

  private lateinit var b: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    b = ActivityMainBinding.inflate(layoutInflater)
    setContentView(b.root)

    val lines = assets.open("ayah/001.txt").bufferedReader().readLines()
      .map { it.trim() }
      .filter { it.isNotEmpty() }

    val amiri = try {
      Typeface.createFromAsset(assets, "fonts/amiri_quran.ttf")
    } catch (e: Exception) {
      null
    }

    b.rvAyah.layoutManager = LinearLayoutManager(this)
    b.rvAyah.adapter = AyahAdapter(lines, amiri)
  }
}
