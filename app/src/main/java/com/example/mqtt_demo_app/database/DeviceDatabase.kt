package com.example.mqtt_demo_app.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * DeviceDatabase.kt-------- App Database class that is the main point for connection with DB and provide access to a single instance of DB
 * ----------------- developed by Theologos Batsioulas 20/01/2022 for MQTT Demo App
 */

@Database(entities = [Device::class], version = 1, exportSchema = false)
abstract class DeviceDatabase: RoomDatabase() {

    abstract fun deviceDao(): DeviceDao

    //Singleton that prevents multiple instances of database opening at the same time
    companion object {
        @Volatile
        private var INSTANCE: DeviceDatabase? = null

        fun getDatabase(context: Context): DeviceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    DeviceDatabase::class.java,
                    "device_database"
                )
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }

}