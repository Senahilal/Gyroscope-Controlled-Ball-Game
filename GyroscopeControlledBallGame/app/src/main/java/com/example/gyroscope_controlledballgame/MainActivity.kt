package com.example.gyroscope_controlledballgame

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.gyroscope_controlledballgame.ui.theme.GyroscopeControlledBallGameTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var gyroscope: Sensor? = null

    private var xVelocity by mutableStateOf(0f)
    private var yVelocity by mutableStateOf(0f)

    private var _accuracy by mutableStateOf("Unknown")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Sensor Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        setContent {
            GyroscopeControlledBallGameTheme {
                BallGameScreen(xVelocity, yVelocity)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {

                // changing sensitivity for testing
                val sensitivity = 10f

                //when we stop tilting and not getting new values from sensor
                //to slow down gradually, multiply the values with alpha < 1
                val alpha = 0.90f

                // velocity based on gyroscope input
                xVelocity = alpha * (xVelocity + it.values[0] * sensitivity)
                yVelocity = alpha * (yVelocity + it.values[1] * sensitivity)

            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        _accuracy = when (accuracy) {
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
            SensorManager.SENSOR_STATUS_UNRELIABLE -> "Unreliable"
            else -> "Unknown"
        }
    }
}

@Composable
fun BallGameScreen(xVelocity: Float, yVelocity: Float) {
    var ballPosition by remember { mutableStateOf(Offset(500f, 500f)) }

    // Define the obstacle (wall) - A simple rectangle
    val wallTopLeft = Offset(200f, 700f)  // Top-left corner
    val wallBottomRight = Offset(900f, 800f) // Bottom-right corner

    LaunchedEffect(xVelocity, yVelocity) { // ✅ Runs only when velocity changes
        while (true) {
            val newX = (ballPosition.x + xVelocity).coerceIn(50f, 950f)
            val newY = (ballPosition.y - yVelocity).coerceIn(50f, 1600f)

            // Check collision - adding radius(40f) into calculation to prevent ball's edges to go into red wall
            val isColliding = newX in (wallTopLeft.x+40)..(wallBottomRight.x-40) &&
                    newY in (wallTopLeft.y-40)..(wallBottomRight.y+40)

            // Update position only if no collision
            if (!isColliding) {
                ballPosition = Offset(newX, newY)
            }

            delay(15L)
        }
    }



    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            /// Red wall
            drawRect(
                color = Color.Red,
                topLeft = wallTopLeft,
                size = androidx.compose.ui.geometry.Size(
                    width = wallBottomRight.x - wallTopLeft.x,
                    height = wallBottomRight.y - wallTopLeft.y
                )
            )

            drawCircle(
                color = Color.Blue,
                radius = 40f,

                //using ballPosition as center
                //when ballPosition changes > center changes > ball moves
                center = ballPosition
            )
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GyroscopeControlledBallGameTheme {
    }
}