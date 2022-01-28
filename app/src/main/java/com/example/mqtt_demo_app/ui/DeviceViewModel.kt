package com.example.mqtt_demo_app.ui

import androidx.lifecycle.*
import com.example.mqtt_demo_app.data.DeviceRepository
import com.example.mqtt_demo_app.database.Device
import com.example.mqtt_demo_app.mqtt.MqttClientClass
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.eclipse.paho.android.service.MqttAndroidClient
import javax.inject.Inject

/**
 * DeviceViewModel.kt-------- ViewModel class that contains the data that are exposed to UI
 * ----------------- developed by Theologos Batsioulas 20/01/2022 for MQTT Demo App
 */

@HiltViewModel
class DeviceViewModel @Inject constructor(private val repository: DeviceRepository): ViewModel() {



    private val allDevices: LiveData<List<Device>> = repository.allDevices.asLiveData()
    private val deviceId: MutableLiveData<Int> = MutableLiveData()
    val connected: MutableLiveData<Boolean> = MutableLiveData()
    val disconnected: MutableLiveData<Boolean> = MutableLiveData()
    private val clientSubscribed: MutableLiveData<Boolean> = MutableLiveData(false)
    private val specificDevice: LiveData<Device> = Transformations.switchMap(deviceId) { device_id ->
        repository.getDevice(device_id).asLiveData()
    }

    //Holds an Instance of Mqtt client class
    private lateinit var client: MqttAndroidClient
    private lateinit var mqttClient: MqttClientClass

    //Coroutines to handle data in a non-blocking way
    fun insert(device: Device) = viewModelScope.launch { repository.insert(device) }
    fun update(device: Device) = viewModelScope.launch { repository.update(device) }
    fun delete(device: Device) = viewModelScope.launch { repository.delete(device) }

    //Connect to MQTT Broker and update "connected" value
    @ExperimentalCoroutinesApi
    fun connectToMqttBroker(user_name: String, pass_word: String) {
        viewModelScope.launch {
            repository.connectToMqttBroker(user_name, pass_word).collect { value ->
                connected.postValue(value)
            }
        }

    }
    //Connect to MQTT Broker and update "connected" value
    @ExperimentalCoroutinesApi
    fun disconnectFromMqttBroker() {
      viewModelScope.launch {
          repository.disconnectFromMqttBroker().collect { value ->
              connected.postValue(value)
          }
      }
    }


    //get All Devices
    fun getAllDevices(): LiveData<List<Device>> {

        return allDevices
    }

    //get a Specific Device
    fun getSpecificDevice(): LiveData<Device> {
        return specificDevice
    }


    //set the value of a specific device
    fun setSpecificDevice(id: Int) {
        deviceId.value = id
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
    fun setMqttAndroidClient(client: MqttClientClass) {
        mqttClient = client

    }

    //Function that will return the MqttAndroidClient
    fun getMqttAndroidClient(): MqttClientClass {
        return mqttClient
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
        //if (!client.isConnected) clientSubscribed.postValue(false)
        return clientSubscribed
    }

    //Disconnect from client if the VW is destroyed
    override fun onCleared() {
        super.onCleared()
        try {
            if (client.isConnected) {
                client.disconnect()
            }
        } catch (e1: UninitializedPropertyAccessException) {
        }
    }

}
