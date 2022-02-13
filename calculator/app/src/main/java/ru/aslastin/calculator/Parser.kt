package ru.aslastin.calculator

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min


/*
КС-грамматика, описывающая парсер:

E -> TE'
E' -> -TE'
E' -> +TE'
E' -> eps

T -> FT'
T' -> *FT'
T' -> /FT'
T' -> eps

F -> -N
F -> N

N -> n
*/

class ParserException(
    override val message: String,
    private val index: Int,
    val input: String
) : RuntimeException(message) {
    override fun toString(): String {
        val errorPlace = StringBuilder()
        val shiftLeft = max(0, index - 5)
        if (shiftLeft != 0) {
            errorPlace.append("...")
        }
        val shiftRight = min(input.length, index + 5)
        errorPlace.append(input.substring(shiftLeft, shiftRight))
        if (shiftRight != input.length) {
            errorPlace.append("...")
        }
        if (errorPlace.isNotEmpty()) {
            errorPlace.insert(0, "  :  ")
        }
        return "$message$errorPlace"
    }
}

enum class Token {
    NUMBER,
    SUB,
    MUL,
    ADD,
    DIV,
    END,
}

val OPS = listOf('+', '-', '×', '÷')

class ParsedToken(val token: Token, val value: String, val index: Int)

fun parsedTokenSequence(input: String) = sequence<ParsedToken> {
    val number = StringBuilder()
    var wasDot = false
    for ((index, char) in input.withIndex()) {
        if (char.isWhitespace()) continue

        if (char in OPS && number.isNotEmpty()) {
            yield(ParsedToken(Token.NUMBER, number.toString(), index))
            wasDot = false
            number.clear()
        }

        when {
            char == '+' -> yield(ParsedToken(Token.ADD, "+", index))
            char == '-' -> yield(ParsedToken(Token.SUB, "-", index))
            char == '×' -> yield(ParsedToken(Token.MUL, "×", index))
            char == '÷' -> yield(ParsedToken(Token.DIV, "÷", index))
            char.isDigit() -> number.append(char)
            char == '.' -> {
                if (number.isEmpty()) {
                    throw ParserException("Expected digits before .", index, input)
                }
                if (wasDot) {
                    throw ParserException("Only one . can be in number", index, input)
                }
                number.append('.')
                wasDot = true
            }
            else -> throw ParserException("Unexpected symbol", index, input)
        }
    }
    if (number.isNotEmpty()) {
        yield(ParsedToken(Token.NUMBER, number.toString(), input.length))
    }
    yield(ParsedToken(Token.END, "end of input", input.length))
}

class Parser(val input: String, val scale: Int = 10) {
    private val iterator: Iterator<ParsedToken> = parsedTokenSequence(input).iterator()
    private lateinit var cur: ParsedToken

    init {
        next()
    }

    private fun next() {
        cur = iterator.next()
    }

    fun parse(): BigDecimal {
        val res = E()
        if (cur.token == Token.END) {
            return res
        }
        throw ParserException(
            "Expected end of expression, but found ${cur.value}", cur.index, input
        )
    }

    private fun E(): BigDecimal {
        var res = T()
        while (true) {
            when (cur.token) {
                Token.ADD -> {
                    next(); res += T()
                }
                Token.SUB -> {
                    next(); res -= T()
                }
                else -> break
            }
        }
        return res
    }

    private fun T(): BigDecimal {
        var res = F()
        while (true) {
            when (cur.token) {
                Token.MUL -> {
                    next(); res *= F()
                }
                Token.DIV -> {
                    next(); res /= F()
                }
                else -> break
            }
        }
        return res
    }

    private fun F(): BigDecimal = when (cur.token) {
        Token.SUB -> {
            next(); -N()
        }
        else -> N()
    }

    private fun N(): BigDecimal {
        if (cur.token == Token.NUMBER) {
            val number = cur.value
            next()
            return number.toBigDecimal().setScale(scale, RoundingMode.HALF_UP)
        } else {
            throw ParserException("Expected number, buf found ${cur.value}", cur.index, input)
        }
    }
}
