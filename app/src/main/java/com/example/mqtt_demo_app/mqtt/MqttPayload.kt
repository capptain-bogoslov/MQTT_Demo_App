package com.example.mqtt_demo_app.mqtt

import com.squareup.moshi.JsonClass

/**
 * MqttPayload.kt-------- Class to be used from Moshi Library to parse JSON OBJ into Kotlin OBJ
 * ----------------- developed by Theo Batsioulas 30/01/2022 for MQTT Demo App
 */

@JsonClass(generateAdapter = true)
data class MqttPayload(
    val time: String = "-",
    val status: String = "Offline",
    val temperature: String = "17",
    val message: String = "Offline"
)
