package com.example.simplecalculator

import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.simplecalculator.game.CarPhysics
import com.example.simplecalculator.game.GameLoop
import com.example.simplecalculator.game.ObstacleSystem
import com.example.simplecalculator.game.RoadView
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var roadView: RoadView
    private lateinit var speedText: TextView
    private lateinit var stateText: TextView

    private lateinit var btnThrottle: Button
    private lateinit var btnBrake: Button
    private lateinit var btnReset: Button

    private val carPhysics = CarPhysics()
    private val obstacles = ObstacleSystem()
    private var playerLaneIndex: Int = 0

    private val loop = GameLoop { dt ->
        // Update physics with real dt for consistent behavior across devices.
        carPhysics.update(dt)

        // Update obstacle positions relative to player speed and spawn new ones.
        obstacles.update(dtSeconds = dt, carSpeedMps = carPhysics.speedMps)

        // Collision: if we hit an obstacle in our lane, apply an immediate slowdown.
        when (val res = obstacles.checkCollisionAndConsume(playerLaneIndex)) {
            is ObstacleSystem.CollisionResult.Hit -> carPhysics.applySpeedMultiplier(res.speedMultiplier)
            ObstacleSystem.CollisionResult.None -> Unit
        }

        // Push speed into visuals and animate the road dashes.
        roadView.setSpeed(carPhysics.speedMps)
        roadView.setPlayerLaneIndex(playerLaneIndex)
        roadView.setObstacles(obstacles.obstacles())
        roadView.update(dt)

        // Update HUD (simple and readable).
        updateHud()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        roadView = findViewById(R.id.roadView)
        speedText = findViewById(R.id.speedText)
        stateText = findViewById(R.id.stateText)

        btnThrottle = findViewById(R.id.btnThrottle)
        btnBrake = findViewById(R.id.btnBrake)
        btnReset = findViewById(R.id.btnReset)

        // Press & hold inputs: closer to a driving control than a click toggle.
        // Why: continuous throttle/brake control is required for realistic acceleration/braking behavior.
        wireHoldButton(
            button = btnThrottle,
            onDown = { carPhysics.throttle = 1f },
            onUp = { carPhysics.throttle = 0f }
        )

        wireHoldButton(
            button = btnBrake,
            onDown = { carPhysics.brake = 1f },
            onUp = { carPhysics.brake = 0f }
        )

        btnReset.setOnClickListener {
            // Reset also clears inputs so user restarts in a predictable state.
            carPhysics.reset()
            obstacles.reset()
            updateHud()
        }

        updateHud()
    }

    override fun onResume() {
        super.onResume()
        loop.start()
    }

    override fun onPause() {
        super.onPause()
        loop.stop()
    }

    /**
     * Wires a "press and hold" button using touch events.
     *
     * Why not click listeners:
     * - Click is discrete; we need continuous input while the user holds the control.
     */
    private fun wireHoldButton(
        button: Button,
        onDown: () -> Unit,
        onUp: () -> Unit
    ) {
        button.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    onDown()
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    onUp()
                    true
                }

                else -> false
            }
        }
    }

    private fun updateHud() {
        // Convert m/s to km/h for user-friendly display.
        val speedKmh = (carPhysics.speedMps * 3.6f).roundToInt()
        speedText.text = "Speed: $speedKmh km/h"

        // Show current control state.
        val state = when {
            carPhysics.brake > 0f -> "braking"
            carPhysics.throttle > 0f -> "accelerating"
            carPhysics.speedMps > 0.2f -> "coasting"
            else -> "idle"
        }
        stateText.text = "State: $state"
    }
}
