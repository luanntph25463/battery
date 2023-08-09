package com.example.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager


class BatteryStatusReceiver( private val listener :  BatteryListener) : BroadcastReceiver() {
    interface BatteryListener {
        fun onBatteryLevelChanged(level: Int)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPercentage = (level.toFloat() / scale.toFloat() * 100).toInt()

            listener.onBatteryLevelChanged(batteryPercentage)
        }

    }

    companion object {
        fun register(context: Context, listener: BatteryListener): BatteryStatusReceiver {
            val receiver = BatteryStatusReceiver(listener)
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(receiver, intentFilter)
            return receiver
        }

        fun unregister(context: Context, receiver: BatteryStatusReceiver) {
            context.unregisterReceiver(receiver)
        }
    }
}