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
 * - Renders simple obstacles to demonstrate gameplay interaction (collision slows the car)
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

    private val obstaclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(255, 140, 0) // orange so it pops on dark road
        style = Paint.Style.FILL
    }

    private val playerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(80, 170, 255) // light blue player marker
        style = Paint.Style.FILL
    }

    private var dashOffsetPx: Float = 0f
    private var dashSpeedPxPerSecond: Float = 0f

    private var obstacles: List<Obstacle> = emptyList()

    /**
     * Simple lane model (2 lanes). This matches ObstacleSystem default params.
     * You can extend this later, but keeping it fixed for MVP.
     */
    private val laneCount: Int = 2

    /** Current player lane used for collision + rendering. */
    private var playerLaneIndex: Int = 0

    // PUBLIC_INTERFACE
    fun getLaneCount(): Int {
        /** Exposed so the Activity / obstacle system can stay consistent with the view. */
        return laneCount
    }

    // PUBLIC_INTERFACE
    fun setPlayerLaneIndex(laneIndex: Int) {
        /** Sets the player's lane marker (render-only for now). */
        playerLaneIndex = laneIndex.coerceIn(0, laneCount - 1)
    }

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

    // PUBLIC_INTERFACE
    fun setObstacles(obstacles: List<Obstacle>) {
        /** Provide current obstacles for rendering; called each frame. */
        this.obstacles = obstacles
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
        val roadWidth = roadRight - roadLeft

        canvas.drawRect(roadLeft, 0f, roadRight, h, roadPaint)

        // Draw road edges.
        canvas.drawLine(roadLeft, 0f, roadLeft, h, edgePaint)
        canvas.drawLine(roadRight, 0f, roadRight, h, edgePaint)

        // Draw center lane dashes (for 2 lanes).
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

        // Draw obstacles (simple rectangles in lanes).
        // Map obstacle distanceAheadM to y such that:
        // - far ahead (e.g., 70m) => near top
        // - near (0m) => near bottom (player position)
        val playerY = h * 0.84f
        val visibleDistanceM = 80f
        val laneWidth = roadWidth / laneCount
        val obstacleW = laneWidth * 0.55f
        val obstacleH = 34f

        for (o in obstacles) {
            val t = (o.distanceAheadM / visibleDistanceM).coerceIn(0f, 1f)
            val obstacleY = playerY - (1f - t) * (playerY - h * 0.10f)

            // Skip obstacles outside view.
            if (obstacleY < -50f || obstacleY > h + 50f) continue

            val laneLeft = roadLeft + o.laneIndex * laneWidth
            val cx = laneLeft + laneWidth / 2f
            canvas.drawRoundRect(
                cx - obstacleW / 2f,
                obstacleY - obstacleH / 2f,
                cx + obstacleW / 2f,
                obstacleY + obstacleH / 2f,
                10f,
                10f,
                obstaclePaint
            )
        }

        // Draw a minimal player marker (so you know where collisions happen).
        val playerLaneLeft = roadLeft + playerLaneIndex * laneWidth
        val playerCx = playerLaneLeft + laneWidth / 2f
        val playerW = laneWidth * 0.65f
        val playerH = 40f
        canvas.drawRoundRect(
            playerCx - playerW / 2f,
            playerY - playerH / 2f,
            playerCx + playerW / 2f,
            playerY + playerH / 2f,
            12f,
            12f,
            playerPaint
        )
    }
}
