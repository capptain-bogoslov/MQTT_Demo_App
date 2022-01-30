package com.example.mqtt_demo_app.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DeviceDao.kt-------- Data Access Object class that that include functions to read/manipulate Data in Room DB
 * ----------------- developed by Theologos Batsioulas 20/01/2022 for MQTT Demo App
 */

@Dao
interface DeviceDao {

    //Insert Device in DB
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(device: Device)

    //Insert a List of Devices in DB
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(devices: List<Device>)

    //Update Device in DB
    @Update
    suspend fun update(device: Device)

    //Delete Device from DB
    @Delete
    suspend fun delete(device: Device)

    //Get all Devices from DB
    @Query("SELECT * FROM devices ORDER BY id ASC")
    fun getAllDevices(): Flow<List<Device>>

    //Get specific Device
    @Query("SELECT * FROM devices WHERE id= :deviceId")
    fun getDevice(deviceId: Int): Flow<Device>

    //Get if a User is SUBSCRIBED to a DEVICE
    @Query("SELECT subscribed FROM devices WHERE id= :deviceId")
    fun getIfSubscribed(deviceId: Int): Flow<Boolean>

    //Mark a Device as SUBSCRIBED || UNSUBSCRIBED
    @Query("UPDATE devices SET subscribed= :value WHERE id= :deviceId")
    suspend fun changeSubscribed(deviceId: Int, value: Boolean)

    //Unsubscribe from All Devices
    @Query("UPDATE devices SET subscribed= :value")
    fun unsubscribeAll(value: Boolean)

    //Save Message to DB
    @Query("UPDATE devices SET time= :time WHERE id= :deviceId")
    suspend fun updateTime(time: String, deviceId: Int)

    //Get Time from DB
    @Query("SELECT time FROM devices WHERE id= :deviceId")
    fun getTime(deviceId: Int): Flow<String>

  /*  //Get time for a specific device in DB
    @Query("SELECT time FROM devices WHERE id = :deviceId")
    fun getTime(deviceId: Int): Flow<Device>

    //Get Temperature for a specific device
    @Query("SELECT temperature FROM devices WHERE id= :deviceId")
    fun getTemperature(deviceId: Int): Flow<Device>*/

}