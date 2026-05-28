package com.example.simplecalculator.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

/**
 * A very lightweight "road" view for an endless runner.
 *
 * Why custom drawing:
 * - No external assets needed for MVP
 * - Visualizes speed using moving lane dashes (faster speed => faster dash movement)
 */
class RoadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val roadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(40, 40, 40) // dark asphalt
        style = Paint.Style.FILL
    }

    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }

    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(220, 220, 220)
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    private var dashOffsetPx: Float = 0f
    private var dashSpeedPxPerSecond: Float = 0f

    /**
     * Updates the dash speed. Activity calls this each frame to map physics speed to visuals.
     *
     * @param speedMps current vehicle speed in m/s
     */
    fun setSpeed(speedMps: Float) {
        // Map speed to pixels/sec. Keep a minimum so "movement" is visible even at low speed.
        // Why: player needs continuous feedback; pure 0 speed looks "stuck".
        dashSpeedPxPerSecond = max(0f, speedMps) * 55f
    }

    /**
     * Advances animation state.
     *
     * @param dtSeconds frame delta in seconds
     */
    fun update(dtSeconds: Float) {
        dashOffsetPx += dashSpeedPxPerSecond * dtSeconds
        // Keep the number from growing forever.
        if (dashOffsetPx > 10_000f) dashOffsetPx -= 10_000f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Draw road area with margins (a simple "perspective" feel without complex math).
        val roadLeft = w * 0.18f
        val roadRight = w * 0.82f

        canvas.drawRect(roadLeft, 0f, roadRight, h, roadPaint)

        // Draw road edges.
        canvas.drawLine(roadLeft, 0f, roadLeft, h, edgePaint)
        canvas.drawLine(roadRight, 0f, roadRight, h, edgePaint)

        // Draw center lane dashes.
        val centerX = (roadLeft + roadRight) / 2f
        val dashLength = 70f
        val gap = 55f
        val step = dashLength + gap

        // Offset makes dashes move downward (endless runner illusion).
        var y = -((dashOffsetPx % step))
        while (y < h) {
            canvas.drawLine(centerX, y, centerX, y + dashLength, dashPaint)
            y += step
        }
    }
}
