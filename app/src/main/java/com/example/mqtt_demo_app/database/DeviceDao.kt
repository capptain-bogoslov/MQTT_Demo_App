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

    //get Connection Status of Devices
    @Query("SELECT connected FROM devices GROUP BY connected")
    fun getConnection(): Flow<Boolean>

    //Get if a User is SUBSCRIBED to a DEVICE
    @Query("SELECT subscribed FROM devices WHERE id= :deviceId")
    fun getIfSubscribed(deviceId: Int): Flow<Boolean>

    //Mark a Device as SUBSCRIBED || UNSUBSCRIBED
    @Query("UPDATE devices SET subscribed= :value WHERE id= :deviceId")
    suspend fun changeSubscribed(deviceId: Int, value: Boolean)

    //Unsubscribe from All Devices
    @Query("UPDATE devices SET subscribed= :value")
    suspend fun unsubscribeAll(value: Boolean)

    //Set all Devices as Offline
    //Unsubscribe from All Devices
    @Query("UPDATE devices SET status= :value")
    suspend fun setStatusToAll(value: String)

    //Save Message to DB
    @Query("UPDATE devices SET time= :time, status= :status, temperature= :temperature, message= :message WHERE topicId= :topicId")
    suspend fun updatePayload(time: String, status: String, temperature: String, message: String, topicId: String)

    //Update DB in Connectivity Loss
    @Query("UPDATE devices SET status= :status, message= :message")
    suspend fun updateWhenConnectionLost(status: String, message: String)

    //Update Connection Status in DB
    @Query("UPDATE devices SET connected= :value")
    suspend fun changeConnectionStatus(value: Boolean)

    //Get Time from DB
    @Query("SELECT time FROM devices WHERE id= :deviceId")
    fun getTime(deviceId: Int): Flow<String>

    //Get Time from DB
    @Query("SELECT status FROM devices WHERE id= :deviceId")
    fun getStatus(deviceId: Int): Flow<String>

    //Get Time from DB
    @Query("SELECT message FROM devices WHERE id= :deviceId")
    fun getMessage(deviceId: Int): Flow<String>


}