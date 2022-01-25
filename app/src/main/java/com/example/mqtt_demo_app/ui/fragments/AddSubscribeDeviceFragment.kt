package com.example.mqtt_demo_app.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import com.example.mqtt_demo_app.R
import com.example.mqtt_demo_app.ui.DeviceViewModel
import com.example.mqtt_demo_app.databinding.FragmentAddDeviceBinding
import com.example.mqtt_demo_app.mqtt.MqttClient2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*


/**
 * AddSubscribeDeviceFragment.kt-------- Fragment where to Add, Update & Delete a Device from DB & Subscribe || Unsubscribe to it to receive Push Notifications
 * ----------------- developed by Theologos Batsioulas 22/01/2022 for MQTT Demo App
 */



@AndroidEntryPoint
class AddDeviceFragment : Fragment() {

    //Get nullable reference to FragmentUserBindingClass
    private var _binding: FragmentAddDeviceBinding? = null
    //Get the value but once assigned you can't assign it to something else
    private val binding get() = _binding!!
    //Holds an Instance of Mqtt client class
    private lateinit var mqttClient2 : MqttClient2

    //Variables
    private var deviceId: Int = 0
    private lateinit var deviceName: String
    private lateinit var deviceType: String
    private lateinit var deviceBrand: String
    private lateinit var type: String
    private lateinit var topicId: String
    private val viewModel: DeviceViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Get arguments from previous Fragment
        arguments?.let {
            deviceId = it.getInt("deviceId")
            deviceBrand = it.getString("deviceBrand").toString()
            deviceName = it.getString("deviceName").toString()
            deviceType = it.getString("deviceType").toString()
            type = it.getString("type").toString()
            topicId = it.getString("topicId").toString()
            viewModel.setSpecificDevice(deviceId)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the view, set the value of binding and return the root view
        _binding = FragmentAddDeviceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Observe Specific Device so that appears that we can manipulate the data
        viewModel.getSpecificDevice().observe(viewLifecycleOwner, {})

        //Layouts
        val topicIdEditText = binding.topicId
        val deviceNameEditText = binding.addDeviceName
        val deviceBrandEditText = binding.addDeviceBrand
        val typeSpinner = binding.deviceTypeSpinner
        val saveUpdateButton = binding.saveEditButton
        val deleteButton = binding.deleteButton
        val subscribeButton = binding.subscribeButton
        val unsubscribeButton = binding.unsubscribeButton

        //Get the MqttAndroidClient to Connect to MQTT Broker
        val client = viewModel.getMqttAndroidClient()

        //Set the values for the layouts
        topicIdEditText.setText(topicId)
        deviceNameEditText.setText(deviceName)
        deviceBrandEditText.setText(deviceBrand)
        typeSpinner.setSelection(resources.getStringArray(R.array.deviceTypes).indexOf(type))

        //Hide Layout for Subscribe if the User Adds a new Device
        if (type == "ADD") subscribeButton.isVisible = false

        //Observe if the User is already Subscribed toa Device and the Connection is Active to display the Layouts
        viewModel.isClientSubscribed().observe(viewLifecycleOwner, { subscribed ->
            if (subscribed) {
                subscribeButton.text = getString(R.string.monitor)
                subscribeButton.setBackgroundColor(resources.getColor(R.color.orange))
                unsubscribeButton.visibility = View.VISIBLE
            } else {
                subscribeButton.text = getString(R.string.subscribe)
                subscribeButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                unsubscribeButton.visibility = View.GONE
            }

        })

        //Put Values to Spinner
        val adapter = activity?.applicationContext?.let {
            ArrayAdapter(
                it,
                android.R.layout.simple_spinner_item, resources.getStringArray(R.array.deviceTypes)
            )
        } as SpinnerAdapter
        typeSpinner.adapter = adapter

        //Button Listeners to handle Device
        saveUpdateButton.setOnClickListener {

            lifecycle.coroutineScope.launch {

                //Action depending if we Add a Device for the first time or Update an existing one
                when (type) {

                    "ADD" -> viewModel.insert(
                        viewModel.createDevice(
                            deviceNameEditText.text.toString(),
                            deviceBrandEditText.text.toString(),
                            typeSpinner.selectedItem.toString(),
                            topicIdEditText.text.toString()
                        )
                    )

                    "EDIT" -> viewModel.
                    getSpecificDevice().value?.let { it ->
                        //Update the value of Device with current value and then Update it in DB
                        viewModel.updateDeviceValues(
                            it,
                            deviceNameEditText.text.toString(),
                            deviceBrandEditText.text.toString(),
                            typeSpinner.selectedItem.toString(),
                            topicIdEditText.text.toString()
                        )
                        viewModel.update(it)
                    }
                }
            }

            val action =
                AddDeviceFragmentDirections.actionAddDeviceFragmentToConnectToDeviceFragment(
                    message = "Your device is successfully added to you List "
                )
            view.findNavController().navigate(action)

        }//end saveButtonListener

        //Listener for DELETE Button that deletes a Device from DB
        deleteButton.setOnClickListener {
            lifecycle.coroutineScope.launch {
                //Delete Task from DB & Navigate to Previous Fragment
                viewModel.getSpecificDevice().value?.let { it -> viewModel.delete(it) }
            }

            view.findNavController().navigate(
                AddDeviceFragmentDirections.actionAddDeviceFragmentToConnectToDeviceFragment(
                    message = "Your device has been deleted"
                )
            )

        }

        //Listener for SUBSCRIBE Button that subscribes the Android Client to the Topic in MQTT Broker
        subscribeButton.setOnClickListener {

            //Subscribe User to Device if not subscribed
                val topic = topicIdEditText.text.toString()
                val qos = 1
                try {
                    val subToken = client.subscribe(topic, qos)
                    subToken.actionCallback = object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {

                            viewModel.setSubscribed()
                            view.findNavController()
                                .navigate(
                                    AddDeviceFragmentDirections.actionAddDeviceFragmentToMonitorMqttClientFragment(
                                        deviceId = deviceId,
                                        deviceName = deviceName,
                                        deviceType = deviceType,
                                        deviceBrand = deviceBrand
                                    )
                                )
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken,
                            exception: Throwable
                        ) {
                            // The subscription could not be performed, maybe the user was not
                            // authorized to subscribe on the specified topic e.g. using wildcards
                        }
                    }
                } catch (e: MqttException) {
                    e.printStackTrace()
                }

        }

        //Listener for UNSUBSCRIBE Button that unsubscribes Android Client from the Topic in MQTT Broker
        unsubscribeButton.setOnClickListener {

                val topic = topicIdEditText.text.toString()
                try {
                    val unsubToken = client.unsubscribe(topic)
                    unsubToken.actionCallback = object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken) {
                            // The subscription could successfully be removed from the client
                            viewModel.setUnsubscribed()
                            Toast.makeText(context, "You are now unsubscribed to that topic!", Toast.LENGTH_LONG).show()
                        }

                        override fun onFailure(
                            asyncActionToken: IMqttToken,
                            exception: Throwable
                        ) {
                            // some error occurred, this is very unlikely as even if the client
                            // did not had a subscription to the topic the unsubscribe action
                            // will be successfully
                        }
                    }
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
        }//end Unsubscribe
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}