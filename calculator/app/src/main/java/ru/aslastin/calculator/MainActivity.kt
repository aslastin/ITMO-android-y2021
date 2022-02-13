package ru.aslastin.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.coroutineScope
import kotlinx.coroutines.*
import ru.aslastin.calculator.databinding.ActivityMainBinding

const val BUNDLE_INPUT = "BUNDLE_INPUT"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var input: TextView

    var evalJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        input = binding.input

        binding.buttonClear.setOnClickListener {
            input.text = ""
        }

        binding.buttonCopy.setOnClickListener {
            val manager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Copied evaluation", input.text)
            manager.setPrimaryClip(clipData)
        }

        binding.buttonBackspace.setOnClickListener { onBackspaceButtonClick() }
        binding.buttonBackspace.setOnLongClickListener {
            val button = it as ImageButton
            lifecycle.coroutineScope.launch {
                while (button.isPressed) {
                    onBackspaceButtonClick()
                    delay(100)
                }
            }
            true
        }

        initButtons()

        binding.buttonEquals.setOnClickListener {
            if (input.text.isEmpty()) {
                return@setOnClickListener
            }
            evalJob?.cancel()
            evalJob = lifecycle.coroutineScope.launch {
                showToast("Calculating...")
                var msg = ""
                try {
                    val toParse = input.text.toString()
                    input.text = withContext(Dispatchers.Default) {
                        Parser(toParse).parse().stripTrailingZeros().toPlainString()
                    }
                    msg = "Got it!"
                } catch (e: ArithmeticException) {
                    msg = e.message!!
                } catch (e: ParserException) {
                    msg = e.toString()
                }
                showToast(msg, Toast.LENGTH_LONG)
            }
        }
    }

    private fun initButtons() {
        listOf(
            binding.button0,
            binding.button1,
            binding.button2,
            binding.button3,
            binding.button4,
            binding.button5,
            binding.button6,
            binding.button7,
            binding.button8,
            binding.button9,
            binding.button0,
            binding.buttonDot,
            binding.buttonMul,
            binding.buttonDiv,
            binding.buttonPlus,
            binding.buttonMinus
        ).forEach {
            val curButton = it
            curButton.setOnClickListener {
                input.text = input.text.toString() + curButton.text
            }
        }
    }

    private fun onBackspaceButtonClick() {
        if (input.text.isEmpty()) {
            return
        }
        input.text = input.text.substring(0, input.text.lastIndex)
    }

    private fun showToast(text: String, length: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, length).show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putCharSequence(BUNDLE_INPUT, input.text)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        input.text = savedInstanceState.getCharSequence(BUNDLE_INPUT)
    }

}
