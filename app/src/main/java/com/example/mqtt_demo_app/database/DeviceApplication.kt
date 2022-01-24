package com.example.mqtt_demo_app.database

import android.app.Application
import com.example.mqtt_demo_app.data.DeviceRepository


/**
 * DeviceApplication.kt-------- Application class that subclass Application and gets the DB
 * ----------------- developed by Theologos Batsioulas 20/01/2022 for MQTT Demo App
 */

class DeviceApplication: Application() {

    //DB and Repo Class are created when they're needed and not when tha APP starts
    val database: DeviceDatabase by lazy { DeviceDatabase.getDatabase(this) }

    val repository: DeviceRepository by lazy { DeviceRepository(database.deviceDao()) }
}