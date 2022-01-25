package com.example.mqtt_demo_app.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.mqtt_demo_app.ui.DeviceViewModel
import com.example.mqtt_demo_app.databinding.FragmentConnectToBrokerBinding
import com.example.mqtt_demo_app.mqtt.*
import com.example.mqtt_demo_app.mqtt.MqttClient2
import dagger.hilt.android.AndroidEntryPoint
import org.eclipse.paho.client.mqttv3.*

import org.eclipse.paho.android.service.MqttAndroidClient


/**
 * ConnectToBrokerFragment.kt-------- Fragment that connects to MQTT Broker with username and password
 * ----------------- developed by Theologos Batsioulas 21/01/2022 for MQTT Demo App
 */

const val MQTT_SERVER_URI       = "tcp://broker.hivemq.com:1883"

@AndroidEntryPoint
class ConnectToBrokerFragment : Fragment() {

    //Get nullable reference to FragmentUserBindingClass
    private var _binding: FragmentConnectToBrokerBinding? = null
    //Get the value but once assigned you can't assign it to something else
    private val binding get() = _binding!!
    private var connectionStatus = "Start"
    private val viewModel: DeviceViewModel by activityViewModels()
    //Holds an Instance of Mqtt client class
    private lateinit var mqttClient2 : MqttClient2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the view, set the value of binding and return the root view
        _binding = FragmentConnectToBrokerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Observe if the user is Connected to Broker
        viewModel.isUserConnectedToBroker().observe(viewLifecycleOwner, { value ->
            when(value) {
                "SUCCESS" ->  //Navigate to Next Fragment
                    view.findNavController().navigate(
                        ConnectToBrokerFragmentDirections.actionConnectToBrokerFragmentToConnectToDeviceFragment(
                            message = "Connection to MQTT Broker Successful!"
                        )
                    )
                "FAILURE" ->// Something went wrong e.g. connection timeout or firewall problems
                    //Display Toast if there is a message
                    Toast.makeText(
                        context,
                        "Connection is not possible!",
                        Toast.LENGTH_LONG
                    ).show()
            }
        })

        //Find Layout and Navigate
        val connectButton = binding.connectButton
        connectButton.setOnClickListener {

            val clientId: String = MqttClient.generateClientId()
            val client = MqttAndroidClient(
                activity?.applicationContext, MQTT_SERVER_URI,
                clientId
            )

            //Save Client to VM
            viewModel.setMqttAndroidClient(client)
            viewModel.connectToBroker()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}