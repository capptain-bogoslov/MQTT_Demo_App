package com.example.mqtt_demo_app.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Device.kt-------- Entity class that represent Room database table
 * ----------------- developed by Theo Batsioulas 20/01/2022 for MQTT Demo App
 */

//Create table in Room DB
@Entity(tableName = "devices")
data class Device(
    @PrimaryKey(autoGenerate = true) val id: Int=0,
    @ColumnInfo(name = "deviceName") var deviceName: String,
    @ColumnInfo(name = "deviceBrand") var deviceBrand: String,
    @ColumnInfo(name = "deviceType") var deviceType: String,
    @ColumnInfo(name = "topicId") var topicId: String,
    @ColumnInfo(name = "subscribed") var subscribed: Boolean,
    @ColumnInfo(name = "time") var time: String,
    @ColumnInfo(name = "temperature") var temperature: Double,
)