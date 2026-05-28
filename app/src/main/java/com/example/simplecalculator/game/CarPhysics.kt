package com.example.simplecalculator.game

import kotlin.math.max
import kotlin.math.min

/**
 * Simple, realistic-enough 1D longitudinal vehicle model for an MVP endless runner.
 *
 * We model speed only (no steering/position) because the request focuses on:
 * - speed
 * - acceleration
 * - braking with realistic limits
 *
 * Model choices (why):
 * - Using m/s internally to keep units consistent and easier to reason about physics.
 * - Engine acceleration is capped (like limited traction/power).
 * - Braking deceleration is stronger but capped (like tire friction limit).
 * - Adds rolling resistance + aerodynamic drag to prevent infinite coasting and to create a realistic top speed.
 */
class CarPhysics(
    private val params: Params = Params()
) {

    data class Params(
        /** Maximum engine acceleration when throttle is fully pressed (m/s^2). */
        val maxEngineAccelMps2: Float = 3.2f, // ~0-100 km/h in ~9s (MVP-friendly)
        /** Maximum braking deceleration when brake is fully pressed (m/s^2). */
        val maxBrakeDecelMps2: Float = 7.5f, // strong but plausible for a road car
        /** Linear rolling resistance coefficient (m/s^2 per m/s). */
        val rollingResistanceK: Float = 0.12f,
        /** Quadratic aero drag coefficient (m/s^2 per (m/s)^2). */
        val aeroDragK: Float = 0.015f,
        /** Safety clamp for max speed (m/s). Also acts like a gearbox/engine speed limiter. */
        val maxSpeedMps: Float = 55.0f // ~198 km/h
    )

    /** Current speed in meters/second. */
    var speedMps: Float = 0f
        private set

    /** Throttle input [0..1]. */
    var throttle: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    /** Brake input [0..1]. */
    var brake: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    // PUBLIC_INTERFACE
    fun applySpeedMultiplier(multiplier: Float) {
        /**
         * Applies an instantaneous speed change, typically used for collisions/impacts.
         *
         * @param multiplier multiplicative factor applied to current speed (e.g., 0.7 reduces speed by 30%)
         */
        speedMps = (speedMps * multiplier.coerceIn(0f, 1f)).coerceIn(0f, params.maxSpeedMps)
    }

    // PUBLIC_INTERFACE
    fun reset() {
        /** Reset is public for the Activity's "Reset" button (user-facing restart). */
        speedMps = 0f
        throttle = 0f
        brake = 0f
    }

    // PUBLIC_INTERFACE
    fun update(dtSeconds: Float) {
        /**
         * Advances the physics simulation by dtSeconds.
         *
         * @param dtSeconds time step in seconds (should be small, e.g., ~16ms)
         * @return none; updates internal state (speedMps)
         *
         * Why semi-implicit Euler:
         * - Stable enough for small dt
         * - Simple and appropriate for an MVP
         */
        if (dtSeconds <= 0f) return

        // Convert inputs to desired acceleration/deceleration.
        val engineAccel = params.maxEngineAccelMps2 * throttle
        val brakeDecel = params.maxBrakeDecelMps2 * brake

        // Resistive forces increase with speed; these prevent unrealistic perpetual motion.
        val rolling = params.rollingResistanceK * speedMps
        val aero = params.aeroDragK * speedMps * speedMps

        // Net acceleration: engine pushes forward; brake + resistances oppose motion.
        val netAccel = engineAccel - brakeDecel - rolling - aero

        // Integrate velocity.
        speedMps = speedMps + netAccel * dtSeconds

        // Clamp to physical bounds: speed cannot go negative and is capped at max speed.
        speedMps = min(params.maxSpeedMps, max(0f, speedMps))

        // If both throttle and brake are pressed, braking should "win" (common safety behavior).
        // This is already true numerically (brakeDecel likely > engineAccel), but clamping ensures no negative.
    }
}
