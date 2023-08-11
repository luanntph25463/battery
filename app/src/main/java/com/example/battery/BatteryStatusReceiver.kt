package com.example.battery

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Process
import android.telephony.TelephonyManager
import android.util.Log
import java.lang.reflect.Field


class BatteryStatusReceiver(private val listener: BatteryListener) : BroadcastReceiver() {
    private lateinit var bluetoothAdapter: BluetoothAdapter


    interface BatteryListener {
        fun onBatteryLevelChanged(level: Int)
        fun onBatterytemperature(level: Int)
        fun onBatteryisChanging(isharing: Boolean)


        fun onConnectionTypeChanged(wifi: String)
        fun onSpeedChanged(speed: Float)
        fun onRating(speed: String)
        fun onProviderChanged(networkOperatorName: String)
        fun BluttothName(name: String)

    }

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            // get battery
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            val batteryPercentage = (level.toFloat() / scale.toFloat() * 100).toInt()

            // truyen batteryPercentage in onBatteryLevelChanged
            listener.onBatteryLevelChanged(batteryPercentage)


            // truyen temperature in onBatterytemperature
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10
            listener.onBatterytemperature(temperature)

            // get status
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            //
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            listener.onBatteryisChanging(isCharging)


            // network connection
        }


        // wifi

        val connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connManager.activeNetwork
        val networkCapabilities = connManager.getNetworkCapabilities(network)



        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                listener?.onConnectionTypeChanged("Wi-Fi")
               val  telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                // Get information about the current Wi-Fi connection
                val signalStrength = telephonyManager.signalStrength
                val gsmSignalStrength: Int = signalStrength?.gsmSignalStrength ?: 0
                // Chuyển đổi giá trị sang đơn vị dBm
                val rssi: Int = -113 + 2 * gsmSignalStrength

                // Chia độ mạnh yếu của tín hiệu GSM
                val signalLevel: String = when {
                    rssi >= -50 -> "Mạnh"
                    rssi >= -75 -> "Trung bình"
                    rssi >= -90 -> "Yếu"
                    else -> "Rất yếu"
                }
                listener?.onRating(signalLevel)

                val networkOperatorName = telephonyManager.networkOperatorName
                listener?.onProviderChanged(networkOperatorName)


            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                listener?.onConnectionTypeChanged("Mobile Data")
            }

            val uid = Process.myUid()
            val rxBytes = TrafficStats.getUidRxBytes(uid)
            val txBytes = TrafficStats.getUidTxBytes(uid)

// Tính toán tốc độ mạng
            val rxSpeedMbps = (rxBytes * 8 / 1024 / 1024).toFloat() // Tốc độ download (Mbps)
            val txSpeedMbps = (txBytes * 8 / 1024 / 1024).toFloat() // Tốc độ upload (Mbps)
            val txttb = (rxSpeedMbps + txSpeedMbps ) /2
            listener?.onSpeedChanged(txttb)


            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

//            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            val networkOperatorName = telephonyManager.networkOperatorName
//            listener?.onProviderChanged(networkOperatorName)
        }



        // blutooth


        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            // Bluetooth không được hỗ trợ trên thiết bị này
        }else{
            val bluetoothName = getBluetoothName()
            listener?.BluttothName(bluetoothName)
        }

    }
    private fun getBluetoothName(): String {
        var bluetoothName = ""
        try {
            val bluetoothManagerField: Field? = BluetoothAdapter::class.java.getDeclaredField("mService")
            bluetoothManagerField?.isAccessible = true
            val bluetoothManagerService: Any? = bluetoothManagerField?.get(bluetoothAdapter)

            if (bluetoothManagerService != null) {
                val bluetoothNameField: Field? = bluetoothManagerService.javaClass.getDeclaredField("mName")
                bluetoothNameField?.isAccessible = true
                bluetoothName = bluetoothNameField?.get(bluetoothManagerService) as? String ?: ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bluetoothName
    }



}


