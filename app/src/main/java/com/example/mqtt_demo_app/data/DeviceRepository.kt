package com.example.mqtt_demo_app.data

import android.util.Log
import android.widget.Toast
import com.example.mqtt_demo_app.database.Device
import com.example.mqtt_demo_app.database.DeviceDao
import com.example.mqtt_demo_app.mqtt.MqttClientApi
import com.example.mqtt_demo_app.mqtt.MqttClientClass
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.eclipse.paho.client.mqttv3.*
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * DeviceRepository.kt-------- Repository class that provides data access for the rest of the app
 * ----------------- developed by Theo Batsioulas 20/01/2022 for MQTT Demo App
 */

class DeviceRepository @Inject constructor(private val deviceDao: DeviceDao) {

    val allDevices: Flow<List<Device>> = deviceDao.getAllDevices()
    //Holds an Instance of Mqtt client class
    private val mqttClient : MqttClientClass = MqttClientApi.getMqttClient()
    private val messageArrived: Boolean = false

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

    //Get Time from db
    fun getTime(id: Int): Flow<String> {
        return deviceDao.getTime(id)
    }

    //Get if a User is SUBSCRIBED to a Device
    fun isSubscribed(id: Int): Flow<Boolean> {
        return deviceDao.getIfSubscribed(id)
    }

    //Mark a Device as "Subscribed
    suspend fun changeSubscribed(deviceId: Int, subscribed: Boolean) {
        deviceDao.changeSubscribed(deviceId, subscribed)
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

    //Set a Callback for mqttClient IOT receive Messages FLOW EDITION
    @ExperimentalCoroutinesApi
    suspend fun setCallbackToClient(): Flow<String> = callbackFlow {
        val mqttClientCallback = object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                trySend(message.toString())
            }

            //Notify when connection Lost
            override fun connectionLost(cause: Throwable?) {
                trySend("-1")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                trySend("DeliveryComplete")
                Log.d(this.javaClass.name, "Delivery complete")
            }
        }

        mqttClient.setCallBack(mqttClientCallback)

        awaitClose { channel.close() }

    }

    /*//Set a Callback for mqttClient IOT receive Messages
    private suspend fun setCallbackToClient(): String = suspendCancellableCoroutine { continuation ->
        val mqttClientCallback = object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                val msg = "MESSAGE 108: ${message.toString()} from topic: $topic"
                deviceDao.updateTime(msg, 12)
                continuation.resume(message.toString())
                //deviceDao.updateTime(msg, 12)

            }

            //Notify when connection Lost
            override fun connectionLost(cause: Throwable?) {
                continuation.resume("ConnectionLost")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                continuation.resume("DeliveryComplete")
                Log.d(this.javaClass.name, "Delivery complete")
            }
        }

        mqttClient.setCallBack(mqttClientCallback)
    }*/

    //Save Message to DB
    suspend fun saveMessageToDB(message:String, deviceId: Int){


        deviceDao.updateTime(message, deviceId)

    }

    //Save Message to DB
    @ExperimentalCoroutinesApi
    suspend fun saveMessagesToDB(deviceId: Int){
        var message = ""
        coroutineScope {
            setCallbackToClient().collect { value ->
                message = value
                deviceDao.updateTime(message, deviceId)
            }
        }




    }

    //Function that subscribes a User to a topic IOT receive Publish Messages from another MQTT Client
    suspend fun subscribeToTopic(topic: String): Boolean = suspendCancellableCoroutine { continuation ->

        //Subscribe to topic
        mqttClient.subscribe(topic,
            1,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    continuation.resume(true)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    continuation.resume(false)
                }
            })

    }

    //Function that UNSUBSCRIBES  User from Device Topic
    suspend fun unsubscribeToTopic(topic: String): Boolean = suspendCancellableCoroutine { continuation ->

        //Unsubscribe from topic
        mqttClient.unsubscribe( topic,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    continuation.resume(false)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    continuation.resume(false)
                }
            })


    }
/*   suspend fun unsubscribeToTopic(topic: String, deviceId: Int) {

        var isSubscribed = false

        coroutineScope {

            async {

                mqttClient.unsubscribe( topic,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            isSubscribed = false
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            isSubscribed = false
                        }
                    })
                changeSubscribed(deviceId, isSubscribed)

            }.await()

        }

    }*/


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