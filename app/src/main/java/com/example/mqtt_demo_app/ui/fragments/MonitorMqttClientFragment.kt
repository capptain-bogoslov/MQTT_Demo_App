package com.example.mqtt_demo_app.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.mqtt_demo_app.ui.DeviceViewModel
import com.example.mqtt_demo_app.databinding.FragmentMonitorMqttClientBinding
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
    private var deviceId: Int =0
    //Get the VM
    private val viewModel: DeviceViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Get arguments from previous Fragment
        arguments?.let {
            deviceBrand = it.getString("deviceBrand").toString()
            deviceType = it.getString("deviceType").toString()
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


        viewModel.setCallBackForPushMessages()

        viewModel.getMessageReceived().observe(viewLifecycleOwner, { value->
            //Message to appear when Connection is Lost
            when(value) {
                "FAILURE" ->
                    Toast.makeText(context, "Connection Lost", Toast.LENGTH_LONG).show()

            }
        })

        viewModel.getPayload().observe(viewLifecycleOwner, { payload ->
            time.text = payload
            progressBar.progress = (abs(60-payload.toInt()))
        })

    }

}