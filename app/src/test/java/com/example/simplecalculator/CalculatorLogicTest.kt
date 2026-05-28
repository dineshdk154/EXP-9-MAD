package com.example.simplecalculator

import com.example.simplecalculator.game.CarPhysics
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the MVP car physics.
 *
 * Why replace calculator tests:
 * - This container is repurposed into a racing MVP for the requested feature set.
 * - Tests validate the most important invariants: accelerate, brake, and no negative speed.
 */
class CalculatorLogicTest {

    @Test
    fun speed_increases_with_throttle() {
        val physics = CarPhysics()
        physics.throttle = 1f

        val before = physics.speedMps
        repeat(60) { physics.update(1f / 60f) } // ~1 second
        val after = physics.speedMps

        assertTrue("Speed should increase under throttle", after > before)
    }

    @Test
    fun speed_decreases_with_brake() {
        val physics = CarPhysics()
        physics.throttle = 1f
        repeat(120) { physics.update(1f / 60f) } // build some speed

        val cruising = physics.speedMps
        physics.throttle = 0f
        physics.brake = 1f
        repeat(30) { physics.update(1f / 60f) } // half second braking
        val afterBrake = physics.speedMps

        assertTrue("Speed should decrease under braking", afterBrake < cruising)
    }

    @Test
    fun speed_never_goes_negative() {
        val physics = CarPhysics()
        physics.brake = 1f
        repeat(120) { physics.update(1f / 60f) } // braking at rest should not go negative
        assertTrue("Speed must be non-negative", physics.speedMps >= 0f)
    }
}
