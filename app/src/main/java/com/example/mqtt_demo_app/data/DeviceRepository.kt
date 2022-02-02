package com.example.mqtt_demo_app.data

import android.util.Log
import com.example.mqtt_demo_app.database.Device
import com.example.mqtt_demo_app.database.DeviceDao
import com.example.mqtt_demo_app.mqtt.MqttClientApi
import com.example.mqtt_demo_app.mqtt.MqttClientClass
import com.example.mqtt_demo_app.mqtt.MqttPayload
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import org.eclipse.paho.client.mqttv3.*
import org.json.JSONObject
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * DeviceRepository.kt-------- Repository class that provides data access for the rest of the app
 * ----------------- developed by Theo Batsioulas 20/01/2022 for MQTT Demo App
 */

class DeviceRepository @Inject constructor(private val deviceDao: DeviceDao) {

    val allDevices: Flow<List<Device>> = deviceDao.getAllDevices()
    //Holds an Instance of Mqtt client class
    //private val mqttClient : MqttClientClass = MqttClientApi.getMqttClient()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val adapter: JsonAdapter<MqttPayload> = moshi.adapter(MqttPayload::class.java)

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

    //Get Time from db
    fun getStatus(id: Int): Flow<String> {
        return deviceDao.getStatus(id)
    }

    //Get Time from db
    fun getMessage(id: Int): Flow<String> {
        return deviceDao.getMessage(id)
    }

    //Get Connection Status
    fun getConnection(): Flow<Boolean> {
        return deviceDao.getConnection()
    }


    //Get if a User is SUBSCRIBED to a Device
    fun isSubscribed(id: Int): Flow<Boolean> {
        return deviceDao.getIfSubscribed(id)
    }

    //Mark a Device as "Subscribed
    suspend fun changeSubscribed(deviceId: Int, subscribed: Boolean) {
        deviceDao.changeSubscribed(deviceId, subscribed)
    }

    //Create suspendCancellableCoroutine that returns the Boolean value of the connection result
    suspend fun connectToMqttBroker(username: String?, password: String?): Boolean = suspendCancellableCoroutine { continuation ->
        //Set the Listener
        val mqttActionListener = object: IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                continuation.resume(true)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                continuation.resume(false)
            }
        }

        //Connect and attach the Listener
        MqttClientApi.getMqttClient().connect(username?:"", password?:"", mqttActionListener)

        //awaitClose{ channel.close() }
    }



    //Create suspendCancellableCoroutine that returns the Boolean value of the disconnection result
    suspend fun disconnectFromMqttBroker(): Boolean = suspendCancellableCoroutine { continuation ->
        //Set the Listener
        val mqttActionListener = object: IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                continuation.resume(false)
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                continuation.resume(false)
            }
        }

        MqttClientApi.getMqttClient().disconnect(mqttActionListener)

        //awaitClose { channel.close() }
    }

    //Set a Callback for mqttClient IOT receive Messages FLOW EDITION
    @ExperimentalCoroutinesApi
    suspend fun setCallbackToClient(): Flow<JSONObject> = callbackFlow {
        val mqttClientCallback = object : MqttCallback {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                //Create a json OBJ from the MqttMessage payload
                val json = JSONObject(String(message!!.payload))
                json.put("topicId", topic)
                trySend(json)
            }

            //Notify when connection Lost
            override fun connectionLost(cause: Throwable?) {
                //Create a json OBJ to return in case of connection Loss
                val json = JSONObject()
                json.put("time", "-1")
                json.put("status", "Offline")
                json.put("message", "Connection Lost")
                trySend(json)
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                //trySend("DeliveryComplete")
                Log.d(this.javaClass.name, "Delivery complete")
            }
        }
        MqttClientApi.getMqttClient().setCallBack(mqttClientCallback)

        awaitClose { channel.close() }

    }

    //Save Message to DB
    @ExperimentalCoroutinesApi
    suspend fun saveMessagesToDB(){
        coroutineScope {
            setCallbackToClient().collect { value ->
                //Handle the JSON OBJ with Moshi and retrieve the values to save to DB
                val payload = adapter.fromJson(value.toString())
                if (payload!!.message == "Connection Lost") {
                    deviceDao.updateWhenConnectionLost(payload.status, payload.message)
                    changeConnectionStatus(false)
                    resetValuesWhenDisconnected()
                } else {
                    deviceDao.updatePayload(payload!!.time, payload.status, payload.temperature, payload.message, payload.topicId)
                }
            }
        }
    }

    //Function that returns a Boolean value to Repository for when the connection is Lost
    suspend fun changeConnectionStatus(connected: Boolean){

        deviceDao.changeConnectionStatus(connected)

    }

    //Function that subscribes a User to a topic IOT receive Publish Messages from another MQTT Client
    suspend fun subscribeToTopic(topic: String): Boolean = suspendCancellableCoroutine { continuation ->

        //Subscribe to topic
        MqttClientApi.getMqttClient().subscribe(topic,
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
        MqttClientApi.getMqttClient().unsubscribe( topic,
            object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    continuation.resume(false)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    continuation.resume(true)
                }
            })
    }

    //Reset values in DB when there is no connection
    suspend fun resetValuesWhenDisconnected() {
        //Set Devices in DB as unsubscribed
        deviceDao.unsubscribeAll(false)
        deviceDao.setStatusToAll("Offline")
    }

    //Reset values in DB when User is UNSUBSCRIBED from a Device
    suspend fun resetValuesWhenUnsubscribed(deviceId: Int) {
        deviceDao.changeSubscribed(deviceId, false)
        deviceDao.updateWhenConnectionLost("Offline", "Unsubscribed")
    }

}