package com.example.mqtt_demo_app.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.activityViewModels
import com.example.mqtt_demo_app.R
import com.example.mqtt_demo_app.ui.DeviceViewModel
import com.example.mqtt_demo_app.databinding.FragmentMonitorMqttClientBinding
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import java.lang.NumberFormatException
import kotlin.math.abs


/**
 * MonitorMqttClientFragment.kt-------- Fragment that will receive Push messages from another MQTT Client(Device) through MQTT Broker
 * ----------------- developed by Theo Batsioulas 21/01/2022 for MQTT Demo App
 */

class   MonitorMqttClientFragment : Fragment() {

    //Get nullable reference to FragmentUserBindingClass
    private var _binding: FragmentMonitorMqttClientBinding? = null
    //Get the value but once assigned you can't assign it to something else
    private val binding get() = _binding!!
    private lateinit var deviceType: String
    private lateinit var deviceBrand: String
    private lateinit var deviceName: String
    private var deviceId: Int =0
    //Get the VM
    private val viewModel: DeviceViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Get arguments from previous Fragment
        arguments?.let {
            deviceBrand = it.getString("deviceBrand").toString()
            deviceType = it.getString("deviceType").toString()
            deviceName = it.getString("deviceName").toString()
            deviceId = it.getInt("deviceId")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the view, set the value of binding and return the root view
        _binding = FragmentMonitorMqttClientBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Set the Layouts
        binding.deviceBrandLabel.text = deviceBrand
        binding.deviceTypeLabel.text = deviceType
        val time = binding.timeTextView
        val progressBar = binding.progressBar

        //time.text = viewModel.getSpecificDevice().value!!.time

        //Create the Callback to receive the Published messages from Device
        viewModel.setCallbackToClient()
        //viewModel.setSpecificDevice(deviceId)

        //Observe the time value that is saved in DB
        viewModel.time.observe(viewLifecycleOwner, {value->
            if (value == "-1") {

            } else {
                time.text = value
            }
        })

        //Get the MqttAndroidClient to Connect to MQTT Broker
        //val mqttClient = viewModel.getMqttAndroidClient()

/*

        //Set a Callback method to handle the received messages
        mqttClient.setCallBack(object: MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Toast.makeText(context, "Connection Lost", Toast.LENGTH_LONG).show()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {

                if(message.toString().isDigitsOnly()) time.text = message.toString() else time.text=getString(R.string.loading)

                try {
                    progressBar.progress = (abs(60-message.toString().toInt()))
                } catch (e1: NumberFormatException) {
                    Toast.makeText(context, "Not Valid Input from Client", Toast.LENGTH_LONG).show()

                }

                viewModel.
                getSpecificDevice().value?.let { it ->
                    //Update the value of Device with current value and then Update it in DB
                    it.time = message.toString()
                    viewModel.update(it)
                }

                Log.d("MESSAGE 104", "MESSAGE RECEIVED")

            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                TODO("Not yet implemented")
            }
        })
        */
    }

}