package com.example.mqtt_demo_app.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mqtt_demo_app.R
import com.example.mqtt_demo_app.database.Device
import com.example.mqtt_demo_app.databinding.RecyclerItemViewBinding

/**
 * DeviceListAdapter.kt-------- Adapter Class that will create the RecyclerView that will hold the Device List data in Fragment
 * ----------------- developed by Theologos Batsioulas 21/01/2022 for MQTT Demo App
 */

class DeviceListAdapter(
    private val onDeviceClicked: (Device) -> Unit
): ListAdapter<Device, DeviceListAdapter.DeviceViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Device>() {
            override fun areItemsTheSame(oldItem: Device, newItem: Device): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Device, newItem: Device): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val viewHolder = DeviceViewHolder(
            RecyclerItemViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            onDeviceClicked(getItem(position))
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeviceViewHolder(
        private var binding: RecyclerItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: Device) {
            binding.deviceName.text = device.deviceName
            binding.deviceBrand.text = device.deviceBrand
            binding.deviceType.text = device.deviceType
            binding.deviceStatus.text = device.status

            //Change Layout depending on Status Value
            val drawable1 = ContextCompat.getDrawable(binding.deviceStatus.context, R.drawable.lamp_bright_24)
            val drawable2 = ContextCompat.getDrawable(binding.deviceStatus.context, R.drawable.lamp_grey_24)
            when (binding.deviceStatus.text) {
                "Running" -> {
                    binding.deviceStatus.setTextColor(ContextCompat.getColor(binding.deviceStatus.context, R.color.colorSecondary))
                    binding.deviceStatus.setCompoundDrawablesWithIntrinsicBounds(drawable1, null, null, null)
                }
                "Offline" -> {
                    binding.deviceStatus.setTextColor(ContextCompat.getColor(binding.deviceStatus.context, R.color.red))
                    binding.deviceStatus.setCompoundDrawablesWithIntrinsicBounds(drawable2, null, null, null)
                }
                "Idle" -> {
                    binding.deviceStatus.setTextColor(ContextCompat.getColor(binding.deviceStatus.context, R.color.grey))
                    binding.deviceStatus.setCompoundDrawablesWithIntrinsicBounds(drawable2, null, null, null)
                }
            }
            binding.deviceMessage.text = device.message
        }
    }
}



