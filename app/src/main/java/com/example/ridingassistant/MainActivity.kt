package com.example.ridingassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ridingassistant.ui.theme.RidingAssistantTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.unit.dp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.material3.Button

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RidingAssistantTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    var fare by remember { mutableStateOf("") }
    var pickupDistance by remember { mutableStateOf("") }
    var rideDistance by remember { mutableStateOf("") }
    var totalDistance by remember { mutableStateOf("") }
    var rupeesPerKm by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = modifier.padding(24.dp)
    ) {
        Text(
            text = "Riding Assistant"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ride Fare (₹)"
        )

        OutlinedTextField(
            value = fare,
            onValueChange = { fare = it },
            label = { Text("Enter fare") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pickup Distance (Km)"
        )

        OutlinedTextField(
            value = pickupDistance,
            onValueChange = { pickupDistance = it },
            label = { Text("Enter Pickup Distance") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Drop Distance (Km)"
        )

        OutlinedTextField(
            value = rideDistance,
            onValueChange = { rideDistance = it },
            label = { Text("Enter Ride Distance") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (fare.isBlank() || pickupDistance.isBlank() || rideDistance.isBlank()) {

                    message = "Please enter fare, pickup distance, and ride distance."

                } else {

                    val pickup = pickupDistance.toDoubleOrNull()
                    val ride = rideDistance.toDoubleOrNull()
                    val fareAmount = fare.toDoubleOrNull()

                    if (pickup == null || ride == null || fareAmount == null) {
                        message = "Please enter valid numbers."
                    } else {
                        if (pickup < 0 || ride < 0) {
                            message = "Distance cannot be negative."
                        } else if (pickup + ride <= 0) {
                            message = "Total distance must be greater than 0 km."
                        } else {
                            val total = pickup + ride

                            totalDistance = total.toString()

                            val perKm = fareAmount / total

                            rupeesPerKm = String.format("%.2f", perKm)

                            message = ""
                        }
                    }
                }
            }
        ) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total Distance: $totalDistance km"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Rupees(₹) per Kilometer: ₹$rupeesPerKm"
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RidingAssistantTheme {
        Greeting("Android")
    }
}