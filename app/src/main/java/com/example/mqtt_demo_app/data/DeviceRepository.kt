package com.example.mqtt_demo_app.data

import com.example.mqtt_demo_app.database.Device
import com.example.mqtt_demo_app.database.DeviceDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * DeviceRepository.kt-------- Repository class that provides data access for the rest of the app
 * ----------------- developed by Theologos Batsioulas 20/01/2022 for MQTT Demo App
 */

class DeviceRepository @Inject constructor(private val deviceDao: DeviceDao) {

    val allDevices: Flow<List<Device>> = deviceDao.getAllDevices()


    //Insert Device to DB
    suspend fun insert(device: Device) {
        deviceDao.insert(device)
    }

    //Update Device in DB
    suspend fun update(device: Device) {
        deviceDao.update(device)
    }

    //Delete Device
    suspend fun delete(device: Device) {
        deviceDao.delete(device)
    }

    //Get specific device from DB
    fun getDevice(id: Int): Flow<Device> {
        return deviceDao.getDevice(id)
    }

    //Save values to DB

   /* //Get if a User is Subscribed to Device
    fun getSubscribed(id: Int) : Flow<Boolean> {
        return deviceDao.getSubscribed(id)
    }*/

 /*   //Get time for a specific device
    fun getTime(id: Int): Flow<Device> {
        return deviceDao.getTime(id)
    }

    //Get Temperature for a specific device
    fun getTemperature(id: Int): Flow<Device> {
        return deviceDao.getTemperature(id)
    }*/




}