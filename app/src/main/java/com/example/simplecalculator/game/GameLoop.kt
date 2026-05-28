package com.example.simplecalculator.game

import android.os.SystemClock
import android.view.Choreographer

/**
 * Small main-thread game loop using Choreographer callbacks.
 *
 * Why Choreographer:
 * - Tied to display vsync for smooth updates
 * - No extra threads for an MVP
 * - Avoids timers that drift under load
 */
class GameLoop(
    private val onFrame: (dtSeconds: Float) -> Unit
) {

    private var running = false
    private var lastFrameTimeMs: Long = 0L

    // Explicit type + object expression avoids a Kotlin type-checking recursion issue that can
    // happen when a lambda captures and re-posts itself (self-referential inference).
    private val frameCallback: Choreographer.FrameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!running) return

            val now = SystemClock.uptimeMillis()
            val dtMs = if (lastFrameTimeMs == 0L) 16L else (now - lastFrameTimeMs)
            lastFrameTimeMs = now

            // Clamp dt to avoid huge physics jumps if app was backgrounded or paused.
            val clampedDtSeconds = (dtMs.coerceIn(1L, 50L)).toFloat() / 1000f
            onFrame(clampedDtSeconds)

            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    // PUBLIC_INTERFACE
    fun start() {
        /** Starts the loop; safe to call multiple times. */
        if (running) return
        running = true
        lastFrameTimeMs = 0L
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    // PUBLIC_INTERFACE
    fun stop() {
        /** Stops the loop; called from Activity lifecycle (onPause). */
        if (!running) return
        running = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        lastFrameTimeMs = 0L
    }
}
