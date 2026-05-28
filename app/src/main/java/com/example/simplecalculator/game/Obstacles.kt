package com.example.simplecalculator.game

import kotlin.math.abs
import kotlin.random.Random

/**
 * Simple obstacle system for the endless-runner MVP.
 *
 * Design goals:
 * - Keep state minimal and deterministic enough for unit tests.
 * - No external assets; RoadView renders obstacles as rectangles.
 * - Obstacles move "toward" the player based on car speed (endless runner illusion).
 */

/**
 * Represents an obstacle located in a specific lane with a distance ahead of the car.
 *
 * Coordinate convention:
 * - distanceAheadM: meters in front of the car along the road direction.
 *   0 means at the car; positive values are ahead; negative values are behind/passed.
 */
data class Obstacle(
    val laneIndex: Int,
    var distanceAheadM: Float
)

/**
 * Manages obstacle spawning, motion, and collision detection.
 *
 * Collision model (simple):
 * - When any obstacle in the same lane crosses distanceAheadM <= 0 (within a small
 *   hit window), it's considered a hit and is removed.
 * - A hit returns a speed multiplier (< 1) that the caller can apply to CarPhysics.
 */
class ObstacleSystem(
    private val params: Params = Params(),
    private val rng: Random = Random.Default
) {

    data class Params(
        /** Number of lanes in the road rendering. Must match RoadView lane count. */
        val laneCount: Int = 2,
        /** Minimum time between spawns (seconds). */
        val spawnIntervalMinS: Float = 0.9f,
        /** Maximum time between spawns (seconds). */
        val spawnIntervalMaxS: Float = 1.7f,
        /** Spawn distance ahead of car (meters). */
        val spawnDistanceMinM: Float = 35f,
        /** Spawn distance max (meters). */
        val spawnDistanceMaxM: Float = 70f,
        /**
         * Hit window in meters around the car position.
         * If abs(distanceAheadM) <= hitWindowM at check time, we count as collision.
         */
        val hitWindowM: Float = 1.6f,
        /**
         * Speed multiplier applied on hit. 0.7 means immediate -30% speed.
         * Caller can additionally add other effects if desired.
         */
        val hitSpeedMultiplier: Float = 0.7f,
        /** Cap number of obstacles kept at once to avoid runaway lists. */
        val maxObstacles: Int = 8
    )

    private var timeUntilNextSpawnS: Float = sampleSpawnInterval()
    private val _obstacles: MutableList<Obstacle> = mutableListOf()

    // PUBLIC_INTERFACE
    fun reset() {
        /** Clears all obstacles and resets spawn timers. */
        _obstacles.clear()
        timeUntilNextSpawnS = sampleSpawnInterval()
    }

    // PUBLIC_INTERFACE
    fun obstacles(): List<Obstacle> {
        /** Immutable view for rendering. */
        return _obstacles
    }

    // PUBLIC_INTERFACE
    fun update(dtSeconds: Float, carSpeedMps: Float) {
        /**
         * Advances obstacle positions and spawns new ones based on time.
         *
         * @param dtSeconds frame time step (seconds)
         * @param carSpeedMps current car speed used to move obstacles toward the player
         */
        if (dtSeconds <= 0f) return

        // Move obstacles toward the player.
        val deltaM = carSpeedMps * dtSeconds
        for (o in _obstacles) {
            o.distanceAheadM -= deltaM
        }

        // Remove those that are far behind (passed).
        _obstacles.removeAll { it.distanceAheadM < -10f }

        // Spawn timer.
        timeUntilNextSpawnS -= dtSeconds
        if (timeUntilNextSpawnS <= 0f) {
            spawnOneIfPossible()
            timeUntilNextSpawnS = sampleSpawnInterval()
        }
    }

    // PUBLIC_INTERFACE
    fun checkCollisionAndConsume(laneIndex: Int): CollisionResult {
        /**
         * Checks whether the player's current lane collides with an obstacle.
         * If a collision is detected, consumes (removes) the obstacle and returns slowdown info.
         *
         * @param laneIndex player's lane index [0..laneCount-1]
         */
        val i = _obstacles.indexOfFirst { it.laneIndex == laneIndex && abs(it.distanceAheadM) <= params.hitWindowM }
        return if (i >= 0) {
            _obstacles.removeAt(i)
            CollisionResult.Hit(params.hitSpeedMultiplier)
        } else {
            CollisionResult.None
        }
    }

    sealed class CollisionResult {
        data object None : CollisionResult()
        data class Hit(val speedMultiplier: Float) : CollisionResult()
    }

    private fun spawnOneIfPossible() {
        if (_obstacles.size >= params.maxObstacles) return

        // Try to avoid spawning an obstacle too close to an existing one in the same lane.
        val lane = rng.nextInt(params.laneCount)
        val distance = lerp(params.spawnDistanceMinM, params.spawnDistanceMaxM, rng.nextFloat())

        val tooCloseSameLane = _obstacles.any { it.laneIndex == lane && abs(it.distanceAheadM - distance) < 12f }
        if (tooCloseSameLane) return

        _obstacles.add(Obstacle(laneIndex = lane, distanceAheadM = distance))
    }

    private fun sampleSpawnInterval(): Float {
        val t = rng.nextFloat()
        return lerp(params.spawnIntervalMinS, params.spawnIntervalMaxS, t)
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t.coerceIn(0f, 1f)
}
