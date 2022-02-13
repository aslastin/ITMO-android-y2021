package ru.aslastin.calculator

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val CALC_PACKAGE = "ru.aslastin.hw1"
private const val TIMEOUT = 5000L

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
class CalculatorBehaviourTest {

    private lateinit var device: UiDevice

    @Before
    fun setUp() {
        device = UiDevice.getInstance(getInstrumentation())
        val context = getApplicationContext<Context>()
        val intent = context.packageManager.getLaunchIntentForPackage(CALC_PACKAGE)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        device.wait(Until.hasObject(By.pkg(CALC_PACKAGE).depth(0)), TIMEOUT)
    }

    private fun getResourceId(id: String) = "$CALC_PACKAGE:id/$id"

    private fun findObject(id: String) =
        device.findObject(UiSelector().packageName(CALC_PACKAGE).resourceId(getResourceId(id)))

    private fun findButtonObject(name: String) = findObject("button_$name")

    private fun clickButton(name: String) = findButtonObject(name).click()

    private fun clickButtons(names: List<String>) = names.forEach { clickButton(it) }

    private fun getInputText(): String {
        val res = findObject("input").text.toString()
        SystemClock.sleep(500)
        return res
    }

    private fun assertInputIsEmpty() = assertThat(getInputText()).isEmpty()

    private fun assertInputIsEqualTo(value: String) = assertThat(getInputText()).isEqualTo(value)

    private fun parse(input: String) = Parser(input).parse().stripTrailingZeros().toString()

    @Test
    fun savingInputAfterScreenRotation() {
        clickButtons(listOf("1", "2", "3", "plus", "3", "2", "equals"))
        val expected = "155"

        assertInputIsEqualTo(expected)

        device.setOrientationLeft()
        assertInputIsEqualTo(expected)

        device.setOrientationRight()
        assertInputIsEqualTo(expected)
    }

    @Test
    fun checkDeleteButtons() {
        assertInputIsEmpty()
        clickButton("backspace")
        assertInputIsEmpty()

        clickButtons(listOf("9", "0", "0", "div", "5", "equals"))
        assertInputIsEqualTo("180")

        clickButton("backspace")
        clickButton("backspace")
        assertInputIsEqualTo("1")

        clickButtons(listOf("plus", "5"))
        clickButton("clear")
        assertInputIsEmpty()
    }

    @Test
    fun produceBadInputs() {
        clickButtons(listOf("4", "1", "div", "0", "equals"))
        assertInputIsEqualTo("41÷0")

        clickButton("clear")

        clickButtons(listOf("0", "div", "0", "equals"))
        assertInputIsEqualTo("0÷0")

        clickButton("clear")

        clickButtons(listOf("0", "minus", "plus", "0", "equals"))
        assertInputIsEqualTo("0-+0")
    }

    @Test
    fun checkClipboardManager() {
        clickButtons(listOf("4", "1", "plus", "minus", "5", "dot", "5", "mul", "2", "0"))
        var expected = "41+-5.5×20"
        assertInputIsEqualTo(expected)

        device.setOrientationLeft()
        assertInputIsEqualTo(expected)

        clickButton("equals")
        expected = parse(expected)
        assertInputIsEqualTo(expected)

        clickButtons(listOf("minus", "mul", "2", "0", "equals"))
        expected = "$expected-×20"
        assertInputIsEqualTo(expected)

        clickButton("copy")

        val clipBoardManager = getApplicationContext<Context>().applicationContext
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        assertThat(clipBoardManager.primaryClip?.getItemAt(0)?.text ?: "none")
            .isEqualTo(expected)
    }
}
