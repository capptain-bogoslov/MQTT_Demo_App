package com.example.mqtt_demo_app.data

import com.example.mqtt_demo_app.database.Device
import com.example.mqtt_demo_app.database.DeviceDao
import com.example.mqtt_demo_app.mqtt.MqttClientApi
import com.example.mqtt_demo_app.mqtt.MqttClientClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.eclipse.paho.client.mqttv3.*
import javax.inject.Inject

/**
 * DeviceRepository.kt-------- Repository class that provides data access for the rest of the app
 * ----------------- developed by Theologos Batsioulas 20/01/2022 for MQTT Demo App
 */

class DeviceRepository @Inject constructor(private val deviceDao: DeviceDao) {

    val allDevices: Flow<List<Device>> = deviceDao.getAllDevices()
    //Holds an Instance of Mqtt client class
    private val mqttClient : MqttClientClass = MqttClientApi.getMqttClient()


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


    //Return a Flow with the result of the connection with MQTT Broker
    @ExperimentalCoroutinesApi
    fun connectToMqttBroker(username: String?, password: String?): Flow<Boolean> = callbackFlow {
        //Set the Listener
        val mqttActionListener = object: IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                trySend(true)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                trySend(false)
            }
        }

        //Connect and attach the Listener
        mqttClient.connect(username?:"", password?:"", mqttActionListener)

        awaitClose{ channel.close() }
    }



    //Return a Flow with the result of disconnect from MQTT Broker
    @ExperimentalCoroutinesApi
    fun disconnectFromMqttBroker(): Flow<Boolean> = callbackFlow {
        //Set the Listener
        val mqttActionListener = object: IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                trySend(false)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                trySend(false)
            }
        }

        mqttClient.disconnect(mqttActionListener)

        awaitClose { channel.close() }
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