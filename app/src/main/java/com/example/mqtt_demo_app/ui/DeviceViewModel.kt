package com.example.mqtt_demo_app.ui

import androidx.lifecycle.*
import com.example.mqtt_demo_app.data.DeviceRepository
import com.example.mqtt_demo_app.database.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * DeviceViewModel.kt-------- ViewModel class that contains the data that are exposed to UI
 * ----------------- developed by Theo Batsioulas 20/01/2022 for MQTT Demo App
 */

@HiltViewModel
class DeviceViewModel @Inject constructor(private val repository: DeviceRepository): ViewModel() {

    //Holds an Instance of Mqtt client class
    private val allDevices: LiveData<List<Device>> = repository.allDevices.asLiveData()
    private val deviceId: MutableLiveData<Int> = MutableLiveData()
    val connected: LiveData<Boolean> = repository.getConnection().asLiveData()
    val brokerActive: MutableLiveData<Boolean> = MutableLiveData()
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

    //Coroutines to handle data in a non-blocking way
    fun insert(device: Device) = viewModelScope.launch { repository.insert(device) }
    fun update(device: Device) = viewModelScope.launch { repository.update(device) }
    fun delete(device: Device) = viewModelScope.launch { repository.delete(device) }

    //Connect to MQTT Broker and update "connected" value
    fun connectToMqttBroker(user_name: String, pass_word: String) {
        viewModelScope.launch {
            val result = repository.connectToMqttBroker(user_name, pass_word)
            brokerActive.postValue(result)
            repository.changeConnectionStatus(result)
            //Reset old values because it is a New Connection with Broker
            repository.resetValuesWhenDisconnected()
        }
    }

    //Connect to MQTT Broker and update "connected" value
    fun disconnectFromMqttBroker() {
      viewModelScope.launch {
          val result = repository.disconnectFromMqttBroker()
          brokerActive.postValue(result)
          repository.changeConnectionStatus(result)
          repository.resetValuesWhenDisconnected()
      }
    }

    //Subscribe to a Device with the specific topic
    fun subscribeToDevice(topic: String) {
        viewModelScope.launch(Dispatchers.IO) {
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
            repository.saveMessagesToDB()
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

    //Change the value of BrokerActive in case something changed
    fun changeBrokerStatus(value: Boolean) {
        brokerActive.postValue(value)
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
            connected = false,
            time = "0",
            status = "Offline",
            temperature = 0.0,
            message = "Offline"
        )
    }
}
