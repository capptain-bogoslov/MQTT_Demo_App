package com.example.mqtt_demo_app.data

import androidx.lifecycle.*
import com.example.mqtt_demo_app.database.Device
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import org.eclipse.paho.android.service.MqttAndroidClient
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
    val subscribed: MutableLiveData<Boolean> = MutableLiveData()
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

  /*  //get if a User is Subscribed to Device
    fun getSubscribed(): LiveData<Boolean> {
        return if(subscribed.value==null) {
            val s = MutableLiveData<Boolean>()
            s.postValue(false)
            s
        } else subscribed

    }*/

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

    //Mark a Device as Subscribed
    fun subscribeToDevice(device: Device): Device {
        //device.subscribed = true
        subscribed.postValue(true)
        return device
    }

    //Mark a Device as Unsubscribed
    fun unsubscribeToDevice(device: Device): Device {
        //device.subscribed = false
        subscribed.postValue(false)
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
            time = 0
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






}

/**
 * DeviceModelFactory.kt ----- ViewModel Factory Class that instantiate DeviceViewModel class
 * --------------developed by Theologos Batsioulas 21/01/22 for MQTT Demo App
 */

/*
class DeviceModelFactory(private val repository: DeviceRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeviceViewModel::class.java)) {
            return DeviceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }

}*/
