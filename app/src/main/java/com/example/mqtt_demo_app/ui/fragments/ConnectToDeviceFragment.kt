package com.example.mqtt_demo_app.ui.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mqtt_demo_app.R
import com.example.mqtt_demo_app.adapters.DeviceListAdapter
import com.example.mqtt_demo_app.ui.DeviceViewModel
import com.example.mqtt_demo_app.databinding.FragmentConnectToDeviceBinding
import com.example.mqtt_demo_app.mqtt.MqttClient2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttClient

/**
 * ConnectToDeviceFragment.kt-------- Fragment where a User can see the connected Devices and Add another one in DB
 * ----------------- developed by Theologos Batsioulas 22/01/2022 for MQTT Demo App
 */

@AndroidEntryPoint
class ConnectToDeviceFragment : Fragment() {

    private lateinit var message: String
    //Get nullable reference to FragmentUserBindingClass
    private var _binding: FragmentConnectToDeviceBinding? = null
    //Get the value but once assigned you can't assign it to something else
    private val binding get() = _binding!!
    //VM
    private val viewModel: DeviceViewModel by activityViewModels()


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //Get arguments from Previous Arguments
        arguments?.let {
            message = it.getString("message").toString()
        }

        //Display Toast if there is a message
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()

        //Get the MqttAndroidClient to Connect to MQTT Broker
        val mqttClient = viewModel.getMqttAndroidClient()


        //If no Internet Access disconnect Client and return to Connect to Broker Fragment
        if (!checkInternetConnection(requireActivity())) disconnectMqttClient(mqttClient)

        //Handle the Back Button Press to disconnect and return to Home
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
               disconnectMqttClient(mqttClient)
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the view, set the value of binding and return the root view
        _binding = FragmentConnectToDeviceBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        //Navigate to Add || Edit Device and pass the necessary arguments
        val deviceAdapter = DeviceListAdapter {
            val action =
                ConnectToDeviceFragmentDirections.actionConnectToDeviceFragmentToAddDeviceFragment(
                    type = "EDIT",
                    deviceId = it.id,
                    deviceType = it.deviceType,
                    deviceName = it.deviceName,
                    deviceBrand = it.deviceBrand,
                    topicId = it.topicId
                )
            view.findNavController().navigate(action)
        }

        recyclerView.adapter = deviceAdapter

        //Update the cached copy of the devices in the adapter
        lifecycle.coroutineScope.launch {
            viewModel.getAllDevices().observe(viewLifecycleOwner, { devices ->
                devices?.let { deviceAdapter.submitList(it) }
            })
        }

        val faButton = binding.fab
        faButton.setOnClickListener {
            val action = ConnectToDeviceFragmentDirections.actionConnectToDeviceFragmentToAddDeviceFragment(
                type = "ADD",
                deviceId = 0,
                deviceBrand = "",
                deviceType = "",
                deviceName = "",
                topicId = ""
            )
            view.findNavController().navigate(action)
        }

    }

    //Function that will check Internet connectivity
    private fun checkInternetConnection(context: Context): Boolean {

            // register activity with the connectivity manager service
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


            // NetworkCapabilities to check what type of network has the internet connection only above Android M
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Returns a Network object corresponding to
                // the currently active default data network.
                val network = connectivityManager.activeNetwork ?: return false

                // Representation of the capabilities of an active network.
                val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

                return when {
                    // Indicates this network uses a Wi-Fi transport,
                    // or WiFi has network connectivity
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                    // Indicates this network uses a Cellular transport. or
                    // Cellular has network connectivity
                    activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                    // else return false
                    else -> false
                }
            } else {
                // if the android version is below M
                @Suppress("DEPRECATION") val networkInfo =
                    connectivityManager.activeNetworkInfo ?: return false
                @Suppress("DEPRECATION")
                return networkInfo.isConnected
            }
    }


    fun disconnectMqttClient(mqttClient: MqttClient2) {
        if (mqttClient.isConnected()) {
            // Disconnect from MQTT Broker
            mqttClient.disconnect(object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {

                    Toast.makeText(context, "MQTT Disconnection success", Toast.LENGTH_SHORT).show()

                    // Disconnection success, come back to Connect Fragment
                    findNavController().navigate(R.id.action_connectToDeviceFragment_to_connectToBrokerFragment)
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.d(this.javaClass.name, "Failed to disconnect")
                }
            })
        } else {
            //Navigate to Start if there is no connection to server
            findNavController().navigate(ConnectToDeviceFragmentDirections.actionConnectToDeviceFragmentToConnectToBrokerFragment("No connection available, Please Connect Again!"))
            Log.d(this.javaClass.name, "Impossible to disconnect, no server connected")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}