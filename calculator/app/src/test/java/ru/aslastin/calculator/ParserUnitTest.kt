package ru.aslastin.calculator

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test


class ParserUnitTest {

    private fun assertParse(input: String, expected: String) {
        assertThat(
            Parser(input).parse().stripTrailingZeros()
        ).isEqualTo(expected.toBigDecimal())
    }

    private fun assertParseFailsWith(input: String, clazz: Class<out Exception>, msg: String = "") {
        val e = assertThrows(clazz) { Parser(input).parse() }
        assertThat(e.toString()).contains(msg)
    }

    private fun assertParseFailsWith(
        inputs: List<String>,
        clazz: Class<out Exception>,
        msg: String = ""
    ) {
        inputs.forEach { input -> assertParseFailsWith(input, clazz, msg) }
    }

    private fun assertParse(inputs: List<Pair<String, String>>) {
        inputs.forEach { (input, expected) -> assertParse(input, expected) }
    }

    @Test
    fun numbersOk() {
        listOf(
            "31", "9", "38408480754622039848349527081084",
            "0034348341", "-4342342", "-004193",
            "-3.", "-345.4", "9341.425"
        ).forEach { assertParse(it, it) }
    }

    @Test
    fun numbersFails() {
        listOf(
            Pair("--7345", "-"),
            Pair("042..34234", "."),
            Pair("..47294", "."),
            Pair("4295..", "."),
            Pair("abc", "")
        ).forEach { (input, msg) -> assertParseFailsWith(input, ParserException::class.java, msg) }
    }

    @Test
    fun additionOk() {
        assertParse(
            listOf(
                Pair("4324+ 341", "4665"),
                Pair("-34034 +51", "-33983"),
                Pair("  3496 +  5941 +-294", "9143")
            )
        )
    }

    @Test
    fun additionFails() {
        assertParseFailsWith(
            listOf("4324++ 341", "-+34034 +51", " -3496 +  -5941 +-294+"),
            ParserException::class.java,
            "+"
        )
    }

    @Test
    fun subtractionOk() {
        assertParse(
            listOf(
                Pair("696- 34902", "-34206"),
                Pair("-34034134 --51031  ", "-33983103"),
                Pair(" -3496 --  5941 -294", "2151")
            )
        )
    }

    @Test
    fun subtractionFails() {
        assertParseFailsWith(
            listOf("4324--- 341", "--34034 -51", " -034396 -594441 +-294--"),
            ParserException::class.java,
            "-"
        )
    }

    @Test
    fun multiplicationOk() {
        assertParse(
            listOf(
                Pair("9584 × 3593", "34435312"),
                Pair("-3592 × 05451", "-19579992"),
                Pair("  3496 ×  5941 × 29 ", "602322344")
            )
        )
    }

    @Test
    fun multiplicationFails() {
        assertParseFailsWith(
            listOf("4324×× 341", "-×34034 +××51", " -3496 +  -5941 +××-294+"),
            ParserException::class.java,
            "×"
        )
    }

    @Test
    fun divisionOk() {
        assertParse(
            listOf(
                Pair("25 ÷ 625", "0.04"),
                Pair("-1 ÷ -4 ", "0.25"),
                Pair(" 144 ÷  12 ÷ 12", "1")
            )
        )
    }

    @Test
    fun divisionFails() {
        assertParseFailsWith(
            listOf("4324 ÷÷ 34÷1", "--34÷034 -÷51", " -0343÷96 -594÷441 +-29÷4--"),
            ParserException::class.java,
            "÷"
        )
    }

    @Test
    fun randomParseOk() {
        assertParse(
            listOf(
                Pair("2 + 2×2", "6"),
                Pair("4 - 20÷2×2 + 4", "-12"),
                Pair("-4 - -5 × -2 - 4 + 2 × 0.25", "-17.5")
            )
        )
    }

    @Test
    fun randomParseFails() {
        assertParseFailsWith(
            listOf(
                "4324 ÷ 20÷2×2--÷ 34÷1",
                "-2--÷÷03451",
                " -2--÷96 -2-451-÷441 +-34129÷4--"
            ), ParserException::class.java
        )
    }

    @Test
    fun divisionByZero() {
        assertParseFailsWith(
            listOf("4 + 2 ÷ 0", "124 + 0 ÷ 0 + 256", "5 × 0 ÷ 0"),
            ArithmeticException::class.java,
            "by zero"
        )
    }
}
