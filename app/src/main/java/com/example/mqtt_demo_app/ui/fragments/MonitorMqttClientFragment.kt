package com.example.mqtt_demo_app.ui.fragments

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.mqtt_demo_app.R
import com.example.mqtt_demo_app.data.DeviceViewModel
import com.example.mqtt_demo_app.databinding.FragmentAddDeviceBinding
import com.example.mqtt_demo_app.databinding.FragmentMonitorMqttClientBinding
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.math.abs


/**
 * MonitorMqttClientFragment.kt-------- Fragment that will receive Push messages from another MQTT Client(Device) through MQTT Broker
 * ----------------- developed by Theologos Batsioulas 21/01/2022 for MQTT Demo App
 */

class   MonitorMqttClientFragment : Fragment() {

    //Get nullable reference to FragmentUserBindingClass
    private var _binding: FragmentMonitorMqttClientBinding? = null
    //Get the value but once assigned you can't assign it to something else
    private val binding get() = _binding!!
    private lateinit var deviceType: String
    private lateinit var deviceBrand: String
    //Get the VM
    private val viewModel: DeviceViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Get arguments from previous Fragment
        arguments?.let {
            deviceBrand = it.getString("deviceBrand").toString()
            deviceType = it.getString("deviceType").toString()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        /**
         * OBSERVE OF VARIABLES FOR TIME AND PROGRESS BAR
         */



        //Get the MqttAndroidClient IOT receive the Push Messages
        val client = viewModel.getMqttAndroidClient()



        //Set a Callback method to handle the received messages
        client.setCallback(object: MqttCallback{
            override fun connectionLost(cause: Throwable?) {
                Toast.makeText(context, "ConnectionLost Message: ${cause.toString()}", Toast.LENGTH_LONG).show()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Toast.makeText(context, "$topic Message: ${message.toString()}", Toast.LENGTH_LONG).show()
                val value = message.toString()
                time.text = value
                progressBar.progress = value.toInt()

                /**
                 * SAVE VALUES TO ROOM DB
                 */
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                TODO("Not yet implemented")
            }


        })

    }

}