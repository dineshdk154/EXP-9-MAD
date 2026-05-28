package com.example.simplecalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for calculator parsing and arithmetic behavior.
 *
 * Note: The production app currently performs logic inside MainActivity with Toasts. For minimal,
 * stable unit tests, we replicate the parsing/formatting rules here in a test-only helper that
 * matches the Activity behavior (trim, empty -> invalid, non-numeric -> invalid).
 */
class CalculatorLogicTest {

    @Test
    fun parseInputsOrNull_returnsNull_whenEitherInputEmpty() {
        assertNull(TestCalculatorLogic.parseInputsOrNull("", "2"))
        assertNull(TestCalculatorLogic.parseInputsOrNull("1", ""))
        assertNull(TestCalculatorLogic.parseInputsOrNull("   ", "2"))
        assertNull(TestCalculatorLogic.parseInputsOrNull("1", "   "))
    }

    @Test
    fun parseInputsOrNull_returnsNull_whenNonNumeric() {
        assertNull(TestCalculatorLogic.parseInputsOrNull("abc", "2"))
        assertNull(TestCalculatorLogic.parseInputsOrNull("1", "2x"))
        assertNull(TestCalculatorLogic.parseInputsOrNull(".", "2")) // toFloatOrNull() => null
    }

    @Test
    fun parseInputsOrNull_parsesTrimmedFloats() {
        val (a, b) = TestCalculatorLogic.parseInputsOrNull(" 1.5 ", " 2 ")!!
        assertEquals(1.5f, a, 0.0001f)
        assertEquals(2.0f, b, 0.0001f)
    }

    @Test
    fun operations_matchExpectedResultStrings() {
        val (a, b) = TestCalculatorLogic.parseInputsOrNull("10", "4")!!

        assertEquals("The Addition Result Is 14.0", TestCalculatorLogic.addText(a, b))
        assertEquals("The Subtraction Result Is 6.0", TestCalculatorLogic.subText(a, b))
        assertEquals("The Multiplication Result Is 40.0", TestCalculatorLogic.mulText(a, b))
        assertEquals("The Division Result Is 2.5", TestCalculatorLogic.divText(a, b)!!)
    }

    @Test
    fun division_returnsNull_whenDivideByZero() {
        val (a, b) = TestCalculatorLogic.parseInputsOrNull("10", "0")!!
        assertNull(TestCalculatorLogic.divText(a, b))
    }
}

/**
 * Test-only logic that mirrors MainActivity behavior closely enough for unit testing.
 * Keeping it in test sources avoids changing production code as required by this step.
 */
private object TestCalculatorLogic {

    fun parseInputsOrNull(s1: String?, s2: String?): Pair<Float, Float>? {
        val t1 = s1?.trim().orEmpty()
        val t2 = s2?.trim().orEmpty()
        if (t1.isEmpty() || t2.isEmpty()) return null

        val a = t1.toFloatOrNull() ?: return null
        val b = t2.toFloatOrNull() ?: return null
        return a to b
    }

    fun addText(a: Float, b: Float): String = "The Addition Result Is ${a + b}"
    fun subText(a: Float, b: Float): String = "The Subtraction Result Is ${a - b}"
    fun mulText(a: Float, b: Float): String = "The Multiplication Result Is ${a * b}"

    /**
     * @return formatted division text, or null when division is invalid (divide-by-zero)
     */
    fun divText(a: Float, b: Float): String? {
        if (b == 0.0f) return null
        return "The Division Result Is ${a / b}"
    }
}
