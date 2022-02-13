package ru.aslastin.animation

import android.animation.ObjectAnimator
import android.animation.ValueAnimator.INFINITE
import android.animation.ValueAnimator.REVERSE
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.coroutines.*
import ru.aslastin.animation.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var curValue = -1
    private var N = -1
    private var primes: ArrayList<Int> = ArrayList()
    private var job: Job? = null

    private var frameVisible = false

    companion object {
        const val BUNDLE_PRIMES = "BUNDLE_CUR_PRIMES"
        const val BUNDLE_CUR_VALUE = "BUNDLE_CUR_VALUE"
        const val BUNDLE_N = "BUNDLE_N"
        const val BUNDLE_EDIT_TEXT = "BUNDLE_EDIT_TEXT"
        const val BUNDLE_CALCULATING = "BUNDLE_CALCULATING"
        const val BUNDLE_FRAME_VISIBLE = "BUNDLE_FRAME_VISIBLE"

        const val PURPLE = "#FF6200EE"
        const val PINK = "#C11AAF"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ObjectAnimator.ofArgb(
            binding.buttonGo, "backgroundColor",
            Color.parseColor(PURPLE),
            Color.parseColor(PINK)
        ).apply {
            duration = 1000L
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = INFINITE
            repeatMode = REVERSE
            start()
        }

        binding.buttonGo.setOnClickListener {
            val input = binding.editTextNumber.text.toString()
            try {
                job?.cancel()
                N = input.toInt()

                binding.frameLayout.visibility = View.VISIBLE
                frameVisible = true

                job = lifecycle.coroutineScope.launch {
                    curValue = 2
                    binding.semiCircleProgressBar.restart()

                    binding.calculating.text = resources.getString(R.string.calculating)

                    binding.semiCircleProgressBar.minValue = 1
                    binding.semiCircleProgressBar.progress = 1
                    binding.semiCircleProgressBar.maxValue = N

                    primes.clear()

                    eval()
                }
            } catch (e: NumberFormatException) {
                Toast.makeText(this@MainActivity, "Please enter a number <= ${Int.MAX_VALUE}", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
        }

        if (savedInstanceState != null) {
            curValue = savedInstanceState.getInt(BUNDLE_CUR_VALUE)
            N = savedInstanceState.getInt(BUNDLE_N)
            binding.editTextNumber.setText(savedInstanceState.getString(BUNDLE_EDIT_TEXT))
            primes = savedInstanceState.getIntegerArrayList(BUNDLE_PRIMES) as ArrayList<Int>
            binding.calculating.text = savedInstanceState.getString(BUNDLE_CALCULATING)
            frameVisible = savedInstanceState.getBoolean(BUNDLE_FRAME_VISIBLE)

            if (frameVisible) {
                binding.frameLayout.visibility = View.VISIBLE
            }

            if (N != -1 && curValue != N) {
                eval()
            }
        }
    }

    // можно улучшить, если вынести primes в отдельный поток (внутри Dispatchers.Main) -
    // когда вью будет пересоздаваться, то primes будет продолжать вычислять
    private fun eval() = lifecycle.coroutineScope.launch {
        val evalJob = launch {
            for (i in curValue..N) {
                if (!primes.any { i % it == 0 }) {
                    primes.add(i);
                }
                curValue = i
                ++binding.semiCircleProgressBar.progress
                when {
                    i % 1000 == 0 -> binding.semiCircleProgressBar.speedUp()
                    i % 100 == 0 -> delay(100L)
                }
            }
        }
        evalJob.join()
        binding.semiCircleProgressBar.speedUp()
        while (binding.semiCircleProgressBar.curValue != N) {
            delay(500L)
        }

        binding.calculating.setText("${primes.size}")

        primes.clear()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(BUNDLE_CUR_VALUE, curValue)
        outState.putInt(BUNDLE_N, N)
        outState.putString(BUNDLE_EDIT_TEXT, binding.editTextNumber.text.toString())
        outState.putIntegerArrayList(BUNDLE_PRIMES, primes as java.util.ArrayList<Int>?)
        outState.putString(BUNDLE_CALCULATING, binding.calculating.text.toString())
        outState.putBoolean(BUNDLE_FRAME_VISIBLE, frameVisible)
        super.onSaveInstanceState(outState)
    }

}
