package com.example.mqtt_demo_app.ui.fragments


import android.os.Build
import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.DividerItemDecoration





/**
 * ConnectToDeviceFragment.kt-------- Fragment where a User can see the connected Devices and Add another one in DB
 * ----------------- developed by Theo Batsioulas 22/01/2022 for MQTT Demo App
 */

@ExperimentalCoroutinesApi
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

        //Handle the Back Button Press to disconnect and return to Home
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
               viewModel.disconnectFromMqttBroker()
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

        //Observe if User is disconnected properly
        viewModel.connected.observe(viewLifecycleOwner, {value ->
            if (value == false) {
                    findNavController()
                        .navigate(R.id.action_connectToDeviceFragment_to_connectToBrokerFragment)
                }
            })

        //RecyclerView reference
        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        //Add a divider between item Recycler View Items
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )


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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}