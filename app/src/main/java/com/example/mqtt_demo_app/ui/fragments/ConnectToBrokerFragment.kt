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
import com.example.mqtt_demo_app.mqtt.MqttClientApi
import com.example.mqtt_demo_app.mqtt.MqttClientClass
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.eclipse.paho.client.mqttv3.*


/**
 * ConnectToBrokerFragment.kt-------- Fragment that connects to MQTT Broker with username and password
 * ----------------- developed by Theo Batsioulas 21/01/2022 for MQTT Demo App
 */

const val MQTT_SERVER_URI       = "tcp://broker.hivemq.com:1883"

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ConnectToBrokerFragment : Fragment() {

    //Get nullable reference to FragmentUserBindingClass
    private var _binding: FragmentConnectToBrokerBinding? = null
    //Get the value but once assigned you can't assign it to something else
    private val binding get() = _binding!!
    private val viewModel: DeviceViewModel by activityViewModels()
    //Holds an Instance of Mqtt client class
    private lateinit var mqttClient : MqttClientClass


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

        //Create Mqtt Client
        val clientId: String = MqttClient.generateClientId()
        mqttClient = MqttClientApi.createMqttAndroidClient(activity?.applicationContext, MQTT_SERVER_URI,
            clientId)

        //Observe VM for a successful connection to Broker
        viewModel.brokerActive.observe(viewLifecycleOwner, { value ->

            when(value) {
                true ->
                    //Navigate to next Fragment
                    view.findNavController().navigate(
                        ConnectToBrokerFragmentDirections.actionConnectToBrokerFragmentToConnectToDeviceFragment(
                            message = "Connection to MQTT Broker Successful!"
                        ))
                false ->
                    Toast.makeText(context, "You are not connected! Please try again!", Toast.LENGTH_SHORT).show()

            }
        })

        val connectButton = binding.connectButton
        connectButton.setOnClickListener {

            //Connects to Broker with USERNAME & PASSWORD, for Demo APP that we do not implement them we pass empty values
            viewModel.connectToMqttBroker("", "")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}