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

import androidx.activity.result.contract.ActivityResultContracts

import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

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
    var detectedText by remember { mutableStateOf("") }
    var detectedPickup by remember { mutableStateOf("") }
    var detectedRide by remember { mutableStateOf("") }
    var detectedFare by remember { mutableStateOf("") }

    fun calculateRide() {

        if (fare.isBlank() || pickupDistance.isBlank() || rideDistance.isBlank()) {
            message = "Please enter fare, pickup distance, and ride distance."
            return
        }

        val pickup = pickupDistance.toDoubleOrNull()
        val ride = rideDistance.toDoubleOrNull()
        val fareAmount = fare.toDoubleOrNull()

        if (pickup == null || ride == null || fareAmount == null) {
            message = "Please enter valid numbers."
            return
        }

        if (pickup < 0 || ride < 0) {
            message = "Distance cannot be negative."
            return
        }

        val total = pickup + ride

        if (total <= 0) {
            message = "Total distance must be greater than 0 km."
            return
        }

        totalDistance = total.toString()

        val perKm = fareAmount / total

        rupeesPerKm = String.format("%.2f", perKm)

        message = ""
    }

    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {

            val image = InputImage.fromFilePath(context, uri)

            val recognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
            )

            recognizer.process(image)
                .addOnSuccessListener { result ->
                    detectedText = result.text

                    val distances = extractDistances(result.text)

                    if (distances.size >= 2) {
                        detectedPickup = distances[0]
                        detectedRide = distances[1]

                        pickupDistance = distances[0].replace(" km", "")
                        rideDistance = distances[1].replace(" km", "")
                    }

                    detectedFare = extractFare(result.text)
                    fare = detectedFare

                    calculateRide()
                }
                .addOnFailureListener {
                    detectedText = "OCR failed"
                }
        }
    }

    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Riding Assistant"
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                imagePicker.launch("image/*")
            }
        ) {
            Text("Choose Screenshot")
        }

        Text(
            text = "Detected Text:"
        )

        Text(
            text = detectedText
        )

        Text(
            text = "Pickup detected: $detectedPickup"
        )

        Text(
            text = "Ride detected: $detectedRide"
        )

        Text(
            text = "Fare detected: ₹$detectedFare"
        )

        Spacer(modifier = Modifier.height(24.dp))

        // YOUR EXISTING FARE FIELD STARTS HERE
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
                calculateRide()
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

fun extractDistances(text: String): List<String> {
    val regex = Regex("""\d+(?:\.\d+)?\s*km""", RegexOption.IGNORE_CASE)

    return regex.findAll(text)
        .map { it.value }
        .toList()
}

fun extractFare(text: String): String {

    val lines = text.lines().map { it.trim() }

    // 1. Handle fares written like: 75 + 19
    for (line in lines) {

        val plusMatch = Regex(
            """[₹₨]?\s*(\d+(?:\.\d+)?)\s*\+\s*(\d+(?:\.\d+)?)"""
        ).find(line)

        if (plusMatch != null) {
            val first = plusMatch.groupValues[1].toDouble()
            val second = plusMatch.groupValues[2].toDouble()

            val total = first + second

            return if (total % 1.0 == 0.0) {
                total.toInt().toString()
            } else {
                total.toString()
            }
        }
    }

    // 2. Normal fare with ₹ or ₨
    for (line in lines) {

        val cleaned = line
            .replace("₹", "")
            .replace("₨", "")
            .trim()

        if (cleaned.toDoubleOrNull() != null) {
            if (line.startsWith("₹") || line.startsWith("₨")) {
                return cleaned
            }
        }
    }

    // 3. OCR sometimes reads ₹62 as T62
    for (line in lines) {

        if (line.matches(Regex("""[Tt]\d{1,4}"""))) {

            val number = line.substring(1)

            if (number.toDoubleOrNull() != null) {
                return number
            }
        }
    }

    // 4. OCR may read ₹62 as 762 when the next line is "(Rapido)"
    for (i in lines.indices) {

        val line = lines[i]

        if (line.matches(Regex("""7\d{1,3}"""))) {

            val nextLine = if (i + 1 < lines.size) {
                lines[i + 1]
            } else {
                ""
            }

            if (nextLine.contains("Rapido", ignoreCase = true)) {

                val number = line.substring(1)

                if (number.toDoubleOrNull() != null) {
                    return number
                }
            }
        }
    }

    return ""
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RidingAssistantTheme {
        Greeting("Android")
    }
}