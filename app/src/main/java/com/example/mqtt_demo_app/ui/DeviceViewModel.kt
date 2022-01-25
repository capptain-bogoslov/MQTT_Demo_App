package com.example.mqtt_demo_app.ui

import androidx.lifecycle.*
import com.example.mqtt_demo_app.data.DeviceRepository
import com.example.mqtt_demo_app.database.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import javax.inject.Inject

/**
 * DeviceViewModel.kt-------- ViewModel class that contains the data that are exposed to UI
 * ----------------- developed by Theologos Batsioulas 20/01/2022 for MQTT Demo App
 */

@HiltViewModel
class DeviceViewModel @Inject constructor(private val repository: DeviceRepository): ViewModel() {


    //Observe Data with LiveData
    private val allDevices: LiveData<List<Device>> = repository.allDevices.asLiveData()
    private val deviceId: MutableLiveData<Int> = MutableLiveData()
    private val clientSubscribed: MutableLiveData<Boolean> = MutableLiveData(false)
    private val connectionToBroker: MutableLiveData<String> = MutableLiveData("Start")
    private val messageReceived: MutableLiveData<String> = MutableLiveData()
    private val payload: MutableLiveData<String> = MutableLiveData()
    private val specificDevice: LiveData<Device> = Transformations.switchMap(deviceId) { device_id ->
        repository.getDevice(device_id).asLiveData()
    }
    //Holds an Instance of Mqtt client class
    private lateinit var client: MqttAndroidClient

    //Coroutines to handle data in a non-blocking way
    fun insert(device: Device) = viewModelScope.launch { repository.insert(device) }
    fun update(device: Device) = viewModelScope.launch { repository.update(device) }
    fun delete(device: Device) = viewModelScope.launch { repository.delete(device) }


    //get All Devices
    fun getAllDevices(): LiveData<List<Device>> {
        return allDevices
    }

    //get a Specific Device
    fun getSpecificDevice(): LiveData<Device> {
        return specificDevice
    }

    //Get payload of Message
    fun getPayload(): LiveData<String> {
        return payload
    }

    //set the value of a specific device
    fun setSpecificDevice(id: Int) {
        deviceId.value = id
    }

    //get if Message Received from MQTT Client
    fun getMessageReceived(): LiveData<String> {
        return messageReceived
    }

    //Updating values of existing Devices
    fun updateDeviceValues(device: Device, name: String, brand: String, type:String, topic: String): Device {
        device.deviceName = name
        device.deviceBrand = brand
        device.deviceType = type
        device.topicId = topic
        return device
    }

    //Create a Device to insert in DB
    fun createDevice(name: String, brand: String, type: String, topic: String): Device {
        return Device(
            deviceName = name,
            deviceBrand = brand,
            deviceType = type,
            topicId = topic,
            temperature = 0.0,
            time = "0"
        )
    }

    //function that creates an MqttAndroidClient
    fun setMqttAndroidClient(mqttClient: MqttAndroidClient) {
        client = mqttClient

    }

    //Function that will return the MqttAndroidClient
    fun getMqttAndroidClient(): MqttAndroidClient {
        return client
    }

    //function that returns if there is an active connection to Broker
    fun isUserConnectedToBroker(): LiveData<String> {
        return connectionToBroker
    }

    //Mark an Android Client as Subscribed
    fun setSubscribed() {
        clientSubscribed.postValue(true)
    }

    //Mark an Android Client as UnSubscribed
    fun setUnsubscribed() {
        clientSubscribed.postValue(false)
    }

    //Returns if a Client is Subscribed to a device
    fun isClientSubscribed(): LiveData<Boolean> {
        if (!client.isConnected) clientSubscribed.postValue(false)
        return clientSubscribed
    }

    //Connects Client to Broker
    fun connectToBroker() {
            try {
                val token = client.connect()
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        connectionToBroker.postValue("SUCCESS")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        connectionToBroker.postValue("FAILURE")
                    }
                }
            } catch (e: MqttException) {
                e.printStackTrace()
            }

    }

    //SetCallback Method to receive Messages and save them to DB
    fun setCallBackForPushMessages() {
        //Set a Callback method to handle the received messages
        client.setCallback(object: MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                //Toast.makeText(context, "ConnectionLost Message: ${cause.toString()}", Toast.LENGTH_LONG).show()
                messageReceived.postValue("FAILURE")

            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                messageReceived.postValue("SUCCESS")
                val device = specificDevice.value
                payload.postValue(message.toString())
                device!!.time = message.toString()
                update(device)

            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                TODO("Not yet implemented")
            }


        })
    }

    //Disconnect from client if the VW is destroyed
    override fun onCleared() {
        super.onCleared()
        if (client.isConnected) {
            client.disconnect()
        }
    }

}
