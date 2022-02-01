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
    private val mqttClient : MqttClientClass = MqttClientApi.getMqttClient()
    private val messageArrived: Boolean = false

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

        mqttClient.setCallBack(mqttClientCallback)

        awaitClose { channel.close() }

    }

    //Save Message to DB
    @ExperimentalCoroutinesApi
    suspend fun saveMessagesToDB(){
        coroutineScope {
            setCallbackToClient().collect { value ->

                //Handle the JSON OBJ with Moshi and retrieve the values to save to DB
                val payload = adapter.fromJson(value.toString())
                deviceDao.updatePayload(payload!!.time, payload.status, payload.temperature, payload.message, payload.topicId)
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
        deviceDao.updatePayload("-1", "Offline", "15", "Offline", "0")
    }

}