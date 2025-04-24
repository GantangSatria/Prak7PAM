package com.example.prak7pam

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import org.json.JSONArray
import java.net.URL
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var slotImage1: ImageView
    private lateinit var slotImage2: ImageView
    private lateinit var slotImage3: ImageView
    private lateinit var triggerButton: Button
    private lateinit var resultText: TextView

    private var running = false

    private val executor = Executors.newFixedThreadPool(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        triggerButton = findViewById(R.id.btn_random)
        slotImage1 = findViewById(R.id.slot1)
        slotImage2 = findViewById(R.id.slot2)
        slotImage3 = findViewById(R.id.slot3)
        resultText = findViewById(R.id.tv_hasil)

        slotImage1.setImageResource(R.drawable.bar)
        slotImage2.setImageResource(R.drawable.bar)
        slotImage3.setImageResource(R.drawable.bar)

        val runner1 = SlotRunner(slotImage1)
        val runner2 = SlotRunner(slotImage2)
        val runner3 = SlotRunner(slotImage3)

        triggerButton.setOnClickListener {
            if (!running) {
                runner1.startRolling()
                runner2.startRolling()
                runner3.startRolling()

                executor.execute(runner1)
                executor.execute(runner2)
                executor.execute(runner3)

                triggerButton.text = "Berhenti"
                resultText.visibility = View.GONE
                running = true
            } else {
                runner1.stopRolling()
                runner2.stopRolling()
                runner3.stopRolling()

                val isJackpot = runner1.imageIndex == runner2.imageIndex &&
                        runner2.imageIndex == runner3.imageIndex

                resultText.text = if (isJackpot) "MAX WIN!" else "COBA LAGI!"
                resultText.visibility = View.VISIBLE
                triggerButton.text = "Putar Lagi"
                running = false
            }
        }
    }

    internal class SlotRunner(private val imageView: ImageView) : Runnable {
        private val imageUrls = mutableListOf<String>()
        private val randomizer = Random()
        private var isSpinning = true

        var imageIndex: Int = -1
            private set

        override fun run() {
            while (isSpinning) {
                val (url, index) = fetchRandomImage()
                imageIndex = index

                Handler(Looper.getMainLooper()).post {
                    Glide.with(imageView.context)
                        .load(url)
                        .into(imageView)
                }

                Thread.sleep(randomizer.nextInt(400).toLong() + 100)
            }
        }

        fun startRolling() {
            isSpinning = true
        }

        fun stopRolling() {
            isSpinning = false
        }

        private fun fetchRandomImage(): Pair<String, Int> {
            val endpoint = "https://662e87fba7dda1fa378d337e.mockapi.io/api/v1/fruits"
            return try {
                if (imageUrls.isEmpty()) {
                    val jsonData = URL(endpoint).readText()
                    val jsonArray = JSONArray(jsonData)
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        imageUrls.add(item.getString("url"))
                    }
                }
                val idx = randomizer.nextInt(imageUrls.size)
                Pair(imageUrls[idx], idx)
            } catch (e: Exception) {
                e.printStackTrace()
                Pair("", -1)
            }
        }
    }
}
