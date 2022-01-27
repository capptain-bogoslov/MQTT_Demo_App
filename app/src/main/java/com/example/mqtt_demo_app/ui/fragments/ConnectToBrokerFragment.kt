package com.example.mqtt_demo_app.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mqtt_demo_app.R
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
const val MQTT_USERNAME         = ""
const val MQTT_PWD              = ""


@AndroidEntryPoint
class ConnectToBrokerFragment : Fragment() {

    private lateinit var message: String
    //Get nullable reference to FragmentUserBindingClass
    private var _binding: FragmentConnectToBrokerBinding? = null
    //Get the value but once assigned you can't assign it to something else
    private val binding get() = _binding!!
    private val viewModel: DeviceViewModel by activityViewModels()
    //Holds an Instance of Mqtt client class
    private lateinit var mqttClient : MqttClient2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Get arguments from previous Fragment
        arguments?.let {
            message = it.getString("message").toString()
        }

        //Display Toast if there is a message
        if (message.isNotEmpty())Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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

        /*//Observe if the user is Connected to Broker
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
        })*/

        //Find Layout and Navigate
        val connectButton = binding.connectButton
        connectButton.setOnClickListener {

            val clientId: String = MqttClient.generateClientId()
            mqttClient = MqttClient2(
                activity?.applicationContext, MQTT_SERVER_URI,
                clientId
            )

            //Save Client to VM
            viewModel.setMqttAndroidClient(mqttClient)
            //viewModel.connectToBroker()

            // Connect and login to MQTT Broker
            mqttClient.connect(
                MQTT_USERNAME,
                MQTT_PWD,
                object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {

                        //Navigate to next Fragment
                        view.findNavController().navigate(
                            ConnectToBrokerFragmentDirections.actionConnectToBrokerFragmentToConnectToDeviceFragment(
                                message = "Connection to MQTT Broker Successful!"
                            )
                        )
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {

                        Toast.makeText(
                            context,
                            "MQTT Connection fails: ${exception.toString()}",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                },
                object : MqttCallback {
                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        val msg = "Receive message: ${message.toString()} from topic: $topic"
                        Log.d(this.javaClass.name, msg)

                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }

                    override fun connectionLost(cause: Throwable?) {
                        Log.d(this.javaClass.name, "Connection lost ${cause.toString()}")

                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken?) {
                        Log.d(this.javaClass.name, "Delivery complete")
                    }
                })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}