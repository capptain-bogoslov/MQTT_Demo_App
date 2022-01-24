package com.example.mqtt_demo_app.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.mqtt_demo_app.data.DeviceViewModel
import com.example.mqtt_demo_app.databinding.FragmentConnectToBrokerBinding
import com.example.mqtt_demo_app.mqtt.*
import com.example.mqtt_demo_app.mqtt.MqttClient2
import dagger.hilt.android.AndroidEntryPoint
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.MqttException

import org.eclipse.paho.client.mqttv3.IMqttToken

import org.eclipse.paho.client.mqttv3.IMqttActionListener

import org.eclipse.paho.android.service.MqttAndroidClient


/**
 * ConnectToBrokerFragment.kt-------- Fragment that connects to MQTT Broker with username and password
 * ----------------- developed by Theologos Batsioulas 21/01/2022 for MQTT Demo App
 */


@AndroidEntryPoint
class ConnectToBrokerFragment : Fragment() {

    //Get nullable reference to FragmentUserBindingClass
    private var _binding: FragmentConnectToBrokerBinding? = null
    //Get the value but once assigned you can't assign it to something else
    private val binding get() = _binding!!
    private val viewModel: DeviceViewModel by activityViewModels()
    //Holds an Instance of Mqtt client class
    private lateinit var mqttClient2 : MqttClient2


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the view, set the value of binding and return the root view
        _binding = FragmentConnectToBrokerBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get Arguments to connect to MQTT Broker from MQTTConstants.kt file
        val serverURI   = MQTT_SERVER_URI
        val clientId    = MQTT_CLIENT_ID
        val username    = MQTT_USERNAME
        val pwd         = MQTT_PWD


        //Find Layout and Navigate
        val connectButton = binding.connectButton
        connectButton.setOnClickListener {

                /*// Open MQTT Broker communication
                mqttClient = MqttClient(context, serverURI, clientId)

                // Connect and login to MQTT Broker
                mqttClient.connect( username,
                    pwd,
                    object : IMqttActionListener {
                        override fun onSuccess(asyncActionToken: IMqttToken?) {
                            Log.d(this.javaClass.name, "Connection success")

                            //Navigate to Next Fragment
                            view.findNavController().navigate(ConnectToBrokerFragmentDirections.actionConnectToBrokerFragmentToConnectToDeviceFragment(
                                message = "Connection to MQTT Broker Successful!"
                            ))
                        }

                        override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                            Log.d(this.javaClass.name, "Connection failure: ${exception.toString()}")

                            Toast.makeText(context, "MQTT Connection fails: ${exception.toString()}", Toast.LENGTH_SHORT).show()
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
                    })*/

            val clientId: String = MqttClient.generateClientId()
            val client = MqttAndroidClient(
                activity?.applicationContext, "tcp://broker.hivemq.com:1883",
                clientId
            )

            //Save Client to VM
            viewModel.setMqttAndroidClient(client)

            try {
                val token = client.connect()
                token.actionCallback = object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken) {
                        // We are connected
                        Log.d("MQTT Message", "onSuccess")

                        //Navigate to Next Fragment
                        view.findNavController().navigate(ConnectToBrokerFragmentDirections.actionConnectToBrokerFragmentToConnectToDeviceFragment(
                            message = "Connection to MQTT Broker Successful!"
                        ))
                    }

                    override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        Log.d("MQTT Message", "onFailure")
                    }
                }
            } catch (e: MqttException) {
                e.printStackTrace()
            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}