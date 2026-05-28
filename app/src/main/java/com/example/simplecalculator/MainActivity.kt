package com.example.simplecalculator

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var txt1: EditText
    private lateinit var txt2: EditText
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txt1 = findViewById(R.id.txt1)
        txt2 = findViewById(R.id.txt2)
        result = findViewById(R.id.result)

        val btnAdd: Button = findViewById(R.id.btnadd)
        val btnSub: Button = findViewById(R.id.btnsubs)
        val btnMul: Button = findViewById(R.id.btnmult)
        val btnDiv: Button = findViewById(R.id.btndiv)

        // Keep click handlers small and delegate to shared validation parsing:
        // This avoids duplicating "empty input" toast logic across operations.
        btnAdd.setOnClickListener {
            val (a, b) = readInputsOrToast() ?: return@setOnClickListener
            val c = a + b
            setResultText("The Addition Result Is $c")
        }

        btnSub.setOnClickListener {
            val (a, b) = readInputsOrToast() ?: return@setOnClickListener
            val c = a - b
            setResultText("The Subtraction Result Is $c")
        }

        btnMul.setOnClickListener {
            val (a, b) = readInputsOrToast() ?: return@setOnClickListener
            val c = a * b
            setResultText("The Multiplication Result Is $c")
        }

        btnDiv.setOnClickListener {
            val (a, b) = readInputsOrToast() ?: return@setOnClickListener

            // Guard against divide-by-zero to avoid showing Infinity/NaN to users.
            if (b == 0.0f) {
                Toast.makeText(this, getString(R.string.toast_divide_by_zero), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val c = a / b
            setResultText("The Division Result Is $c")
        }
    }

    /**
     * Reads the two input fields as floats.
     *
     * Why: The requirements specify toast validation when either input is empty.
     * This helper centralizes that behavior so we cannot accidentally skip it for any operation.
     *
     * @return Pair(a, b) if both inputs are non-empty and parseable; otherwise null after a toast.
     */
    private fun readInputsOrToast(): Pair<Float, Float>? {
        val s1 = txt1.text?.toString()?.trim().orEmpty()
        val s2 = txt2.text?.toString()?.trim().orEmpty()

        if (s1.isEmpty() || s2.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_enter_number), Toast.LENGTH_SHORT).show()
            return null
        }

        // Use toFloatOrNull() to avoid crashing on unexpected characters.
        val a = s1.toFloatOrNull()
        val b = s2.toFloatOrNull()
        if (a == null || b == null) {
            Toast.makeText(this, getString(R.string.toast_enter_number), Toast.LENGTH_SHORT).show()
            return null
        }

        return a to b
    }

    /**
     * Updates the result view.
     *
     * Why: Encapsulates UI formatting in one place; easy to enhance later (e.g., rounding).
     */
    private fun setResultText(text: String) {
        result.text = text
    }
}
