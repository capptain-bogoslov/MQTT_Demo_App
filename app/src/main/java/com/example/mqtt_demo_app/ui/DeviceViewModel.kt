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
 * ----------------- developed by Theo Batsioulas 20/01/2022 for MQTT Demo App
 */

@HiltViewModel
class DeviceViewModel @Inject constructor(private val repository: DeviceRepository): ViewModel() {

    private val allDevices: LiveData<List<Device>> = repository.allDevices.asLiveData()
    private val deviceId: MutableLiveData<Int> = MutableLiveData()
    val connected: MutableLiveData<Boolean> = MutableLiveData()
    private val specificDevice: LiveData<Device> = Transformations.switchMap(deviceId) { device_id ->
        repository.getDevice(device_id).asLiveData()
    }
    val isSubscribed: LiveData<Boolean> = Transformations.switchMap(deviceId) { device_id ->
        repository.isSubscribed(device_id).asLiveData()
    }
    val time: LiveData<String> = Transformations.switchMap(deviceId) { device_id ->
        repository.getTime(device_id).asLiveData()
    }
    val status: LiveData<String> = Transformations.switchMap(deviceId) { device_id ->
        repository.getStatus(device_id).asLiveData()
    }
    val message: LiveData<String> = Transformations.switchMap(deviceId) { device_id ->
        repository.getMessage(device_id).asLiveData()
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
          repository.resetValuesWhenUnsubscribed(specificDevice.value!!.id)
      }
    }

    //Subscribe to a Device with the specific topic
    fun subscribeToDevice(topic: String) {

        viewModelScope.launch(Dispatchers.IO) {
            //repository.markAsSubscribed(specificDevice.value!!.id, true)
            val result = repository.subscribeToTopic(topic)
            repository.changeSubscribed(specificDevice.value!!.id, result)
        }
    }

    //Unsubscribe to Device
    @ExperimentalCoroutinesApi
    fun unsubscribeToDevice(topic: String) {
        viewModelScope.launch(Dispatchers.IO) {

            val result = repository.unsubscribeToTopic(topic)
            if (result) {
                disconnectFromMqttBroker()
            } else {
                repository.resetValuesWhenUnsubscribed(specificDevice.value!!.id)
            }
        }
    }

    //Set Callback to Listen to Published Messages and save them to DB
    @ExperimentalCoroutinesApi
    fun setCallbackToClient() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveMessagesToDB(specificDevice.value!!.id)
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
            subscribed = false,
            time = "0",
            status = "Offline",
            temperature = 0.0,
            message = "Offline"
        )
    }

    //function that creates an MqttAndroidClient
    fun setMqttAndroidClient(client: MqttClientClass) {
        mqttClient = client

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
