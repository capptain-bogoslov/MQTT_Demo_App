package com.example.mqtt_demo_app.di

import android.app.Application
import com.example.mqtt_demo_app.database.DeviceDao
import com.example.mqtt_demo_app.database.DeviceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * AppModule.kt-------- Object class that creates an instance of DeviceDatabase
 * ----------------- developed by Theo Batsioulas 20/01/2022 for MQTT Demo App
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun getAppDB(context: Application): DeviceDatabase {
        return DeviceDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun getDeviceDao(deviceDB: DeviceDatabase): DeviceDao {
        return deviceDB.deviceDao()
    }
}