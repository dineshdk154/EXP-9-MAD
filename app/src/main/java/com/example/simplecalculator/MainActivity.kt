package com.example.simplecalculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {

    private lateinit var etNum1: EditText
    private lateinit var etNum2: EditText
    private lateinit var tvResult: TextView

    private lateinit var btnAdd: Button
    private lateinit var btnSub: Button
    private lateinit var btnMul: Button
    private lateinit var btnDiv: Button

    private lateinit var darkModeSwitch: Switch

    private lateinit var tvHistory: TextView
    private lateinit var btnClearHistory: Button

    /**
     * In-memory history (kept simple). Newest entries are inserted at index 0.
     * Note: not persisted across process death; this matches "UI simplicity".
     */
    private val history: MutableList<String> = mutableListOf()

    private var internalThemeChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etNum1 = findViewById(R.id.etNum1)
        etNum2 = findViewById(R.id.etNum2)
        tvResult = findViewById(R.id.tvResult)

        btnAdd = findViewById(R.id.btnAdd)
        btnSub = findViewById(R.id.btnSub)
        btnMul = findViewById(R.id.btnMul)
        btnDiv = findViewById(R.id.btnDiv)

        darkModeSwitch = findViewById(R.id.darkModeSwitch)

        tvHistory = findViewById(R.id.tvHistory)
        btnClearHistory = findViewById(R.id.btnClearHistory)

        // Initialize switch to reflect current mode.
        darkModeSwitch.isChecked = isCurrentlyDarkMode()
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Avoid re-trigger loops if we update UI state programmatically.
            if (internalThemeChange) return@setOnCheckedChangeListener
            setDarkModeEnabled(isChecked)
        }

        btnAdd.setOnClickListener { performBinaryOp("+") { a, b -> a + b } }
        btnSub.setOnClickListener { performBinaryOp("-") { a, b -> a - b } }
        btnMul.setOnClickListener { performBinaryOp("×") { a, b -> a * b } }
        btnDiv.setOnClickListener { performDivision() }

        btnClearHistory.setOnClickListener {
            history.clear()
            renderHistory()
        }

        renderHistory()
    }

    private fun isCurrentlyDarkMode(): Boolean {
        // MODE_NIGHT_YES/NO are explicit; MODE_NIGHT_FOLLOW_SYSTEM is also possible.
        return AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
    }

    private fun setDarkModeEnabled(enabled: Boolean) {
        internalThemeChange = true
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        internalThemeChange = false
        // Activity will recreate automatically to apply theme.
    }

    private fun performDivision() {
        val parsed = parseInputsOrShowToast() ?: return
        val a = parsed.first
        val b = parsed.second

        if (b == 0.0) {
            Toast.makeText(this, getString(R.string.divide_by_zero), Toast.LENGTH_SHORT).show()
            return
        }

        val result = a / b
        applyResultAndRecord(a, "÷", b, result)
    }

    private fun performBinaryOp(symbol: String, op: (Double, Double) -> Double) {
        val parsed = parseInputsOrShowToast() ?: return
        val a = parsed.first
        val b = parsed.second

        val result = op(a, b)
        applyResultAndRecord(a, symbol, b, result)
    }

    private fun applyResultAndRecord(a: Double, symbol: String, b: Double, result: Double) {
        val pretty = buildHistoryLine(a, symbol, b, result)
        tvResult.text = getString(R.string.result_format, result.toCleanString())

        history.add(0, pretty)
        // Keep history short and readable.
        if (history.size > 20) {
            history.removeAt(history.lastIndex)
        }
        renderHistory()
    }

    private fun renderHistory() {
        tvHistory.text = if (history.isEmpty()) {
            getString(R.string.history_empty)
        } else {
            history.joinToString(separator = "\n")
        }
    }

    private fun parseInputsOrShowToast(): Pair<Double, Double>? {
        val s1 = etNum1.text?.toString()?.trim().orEmpty()
        val s2 = etNum2.text?.toString()?.trim().orEmpty()

        if (s1.isEmpty() || s2.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_both_values), Toast.LENGTH_SHORT).show()
            return null
        }

        val a = s1.toDoubleOrNull()
        val b = s2.toDoubleOrNull()
        if (a == null || b == null) {
            Toast.makeText(this, getString(R.string.invalid_number), Toast.LENGTH_SHORT).show()
            return null
        }

        return a to b
    }

    private fun buildHistoryLine(a: Double, symbol: String, b: Double, result: Double): String {
        return "${a.toCleanString()} $symbol ${b.toCleanString()} = ${result.toCleanString()}"
    }
}

/**
 * Formats a Double for simple calculator display:
 * - removes trailing ".0" for whole numbers
 * - keeps a reasonable length for typical inputs
 */
private fun Double.toCleanString(): String {
    val asLong = this.toLong()
    if (this == asLong.toDouble()) return asLong.toString()

    // Keep it simple: up to 8 decimal places, trim trailing zeros.
    val s = String.format(java.util.Locale.US, "%.8f", this)
    return s.trimEnd('0').trimEnd('.')
}
