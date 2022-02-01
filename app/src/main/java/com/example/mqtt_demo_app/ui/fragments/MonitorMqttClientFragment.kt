package com.example.mqtt_demo_app.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.example.mqtt_demo_app.R
import com.example.mqtt_demo_app.ui.DeviceViewModel
import com.example.mqtt_demo_app.databinding.FragmentMonitorMqttClientBinding
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.lang.NumberFormatException
import kotlin.math.abs


/**
 * MonitorMqttClientFragment.kt-------- Fragment that will receive Push messages from another MQTT Client(Device) through MQTT Broker
 * ----------------- developed by Theo Batsioulas 21/01/2022 for MQTT Demo App
 */

@ExperimentalCoroutinesApi
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
            deviceId = it.getInt("deviceId")
            deviceBrand = it.getString("deviceBrand").toString()
            deviceType = it.getString("deviceType").toString()
            deviceName = it.getString("deviceName").toString()
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
        val status = binding.status
        val message = binding.message

        //Create the Callback to receive the Published messages from Device
        viewModel.setCallbackToClient()

        //Observe Time value that is saved in DB
        viewModel.time.observe(viewLifecycleOwner, {value->
            if (value == "-1") {
                //There is NO CONNECTION to Broker
                Toast.makeText(context, "Connection Lost! Please connect again", Toast.LENGTH_LONG).show()
                //Navigate to start Destination to connect again
                //findNavController().navigate(R.id.action_monitorMqttClientFragment_to_connectToBrokerFragment)
                time.text = "-"

            } else {
                time.text = value
                try {
                    progressBar.progress = (abs(50 -value.toInt()))
                } catch (e1: NumberFormatException) {
                    Toast.makeText(context, "Not Valid Input from Client", Toast.LENGTH_LONG).show()

                }
            }
        })
        //Observe Status from DB
        viewModel.status.observe(viewLifecycleOwner, { value ->
            status.text = value
            //Change text color depending on input
            if(value=="Running") status.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSecondary))
            if (value=="Idle") status.setTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        })
        //Observe Message from DB
        viewModel.message.observe(viewLifecycleOwner, {value ->
            message.text = value
        })

    }
}