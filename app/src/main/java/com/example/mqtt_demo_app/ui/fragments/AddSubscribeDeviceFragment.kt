package com.example.mqtt_demo_app.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.SpinnerAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mqtt_demo_app.R
import com.example.mqtt_demo_app.ui.DeviceViewModel
import com.example.mqtt_demo_app.databinding.FragmentAddDeviceBinding
import com.example.mqtt_demo_app.mqtt.MqttClientApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


/**
 * AddSubscribeDeviceFragment.kt-------- Fragment where to Add, Update & Delete a Device from DB & Subscribe || Unsubscribe to it to receive Push Notifications
 * ----------------- developed by Theo Batsioulas 22/01/2022 for MQTT Demo App
 */


@AndroidEntryPoint
class AddDeviceFragment : Fragment() {

    //Get nullable reference to FragmentUserBindingClass
    private var _binding: FragmentAddDeviceBinding? = null

    //Get the value but once assigned you can't assign it to something else
    private val binding get() = _binding!!

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
    ): View {
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


        //Set the values for the layouts
        topicIdEditText.setText(topicId)
        deviceNameEditText.setText(deviceName)
        deviceBrandEditText.setText(deviceBrand)
        typeSpinner.setSelection(resources.getStringArray(R.array.deviceTypes).indexOf(type))

        //If User Add a Device for the first time in DB hide Layout for Subscribe Button
        if (type == "ADD") subscribeButton.isVisible = false
        //If Device is Saved in DB observe the value for SUBSCRIBE
        if (type == "EDIT") {
            //Observe if the User is subscribed to device
            viewModel.isSubscribed.observe(viewLifecycleOwner, { value ->
                if (value) {
                    subscribeButton.text = getString(R.string.monitor)
                    subscribeButton.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.orange
                        )
                    )
                    unsubscribeButton.visibility = View.VISIBLE
                } else {
                    subscribeButton.text = getString(R.string.subscribe)
                    subscribeButton.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorPrimary
                        )
                    )
                    unsubscribeButton.visibility = View.GONE
                }
            })
        }


        //Go to Connect to Broker Fragment when Connection Lost
        viewModel.connected.observe(viewLifecycleOwner, { value ->
            if (!value) {
                Toast.makeText(context, "Connection Lost! Please connect again", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_addDeviceFragment_to_connectToBrokerFragment)
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

                    "EDIT" -> viewModel.getSpecificDevice().value?.let { it ->
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


            view.findNavController().navigate(AddDeviceFragmentDirections.actionAddDeviceFragmentToConnectToDeviceFragment(
                message = "Your device is successfully added to you List "
            ))

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

            if (MqttClientApi.getMqttClient().isConnected()) {
                viewModel.subscribeToDevice(topicIdEditText.text.toString())

                //Navigate to next Fragment
                view.findNavController()
                    .navigate(
                        AddDeviceFragmentDirections.actionAddDeviceFragmentToMonitorMqttClientFragment(
                            deviceId = deviceId,
                            deviceName = deviceName,
                            deviceType = deviceType,
                            deviceBrand = deviceBrand
                        )
                    )
            } else {
                //There is NO CONNECTION to Broker
                Toast.makeText(context, "Connection Lost! Please connect again", Toast.LENGTH_LONG).show()
                //Navigate to start Destination to connect again
                findNavController().navigate(R.id.action_addDeviceFragment_to_connectToBrokerFragment)

            }

        }

        //Listener for UNSUBSCRIBE Button that unsubscribes Android Client from the Topic in MQTT Broker
        unsubscribeButton.setOnClickListener {

            viewModel.unsubscribeToDevice(topicIdEditText.text.toString())

        }//end Unsubscribe
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}