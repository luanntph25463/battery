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
        // % battery
        fun onBatteryLevelChanged(level: Int)

        // temperature
        fun onBatterytemperature(level: Int)

        // status battery
        fun onBatteryisChanging(isharing: Boolean)

        // TypeChanged
        fun onConnectionTypeChanged(wifi: String)

        // speed changed
        fun onSpeedChanged(speed: Float)

        // rating
        fun onRating(speed: String)

        // provider
        fun onProviderChanged(networkOperatorName: String)
        fun BluttothName(name: String)

    }

    override fun onReceive(context: Context, intent: Intent) {
        // if action = battry_changed
        if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
            // get  current battery
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            // The battery scale value is stored in the scale variable.
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            val batteryPercentage = (level.toFloat() / scale.toFloat() * 100).toInt()

            // truyen batteryPercentage in onBatteryLevelChanged
            listener?.onBatteryLevelChanged(batteryPercentage)


            // truyen temperature in onBatterytemperature
            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10
            listener?.onBatterytemperature(temperature)

            // get status
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            // check status battery changing or pin Full
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            listener.onBatteryisChanging(isCharging)


        }

           // network connection

        val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connManager.activeNetwork
        val networkCapabilities = connManager.getNetworkCapabilities(network)



        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                listener?.onConnectionTypeChanged("Wi-Fi")
                val telephonyManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
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



                // get name provider
                val networkOperatorName = telephonyManager.networkOperatorName
                listener?.onProviderChanged(networkOperatorName)


            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                listener?.onConnectionTypeChanged("Mobile Data")
            }else{
                listener?.onConnectionTypeChanged("Not Connected")
            }



            val uid = Process.myUid()
            // get  tốc độ dowload
            val rxBytes = TrafficStats.getUidRxBytes(uid)
            val txBytes = TrafficStats.getUidTxBytes(uid)

            // Tính toán tốc độ mạng
            val rxSpeedMbps = (rxBytes * 8 / 1024 / 1024).toFloat() // Tốc độ download (Mbps)
            val txSpeedMbps = (txBytes * 8 / 1024 / 1024).toFloat() // Tốc độ upload (Mbps)
            val txtAverage = (rxSpeedMbps + txSpeedMbps) / 2
            // set onSppedChanged
            listener?.onSpeedChanged(txtAverage)

        }

        // blutooth

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            listener?.BluttothName("Thiết Bị Không Được Hỗ Trợ Blutooth")
        } else {
            val bluetoothName = getBluetoothName()
            listener?.BluttothName(bluetoothName)
        }

    }

    private fun getBluetoothName(): String {
        var bluetoothName = ""
        try {
            val bluetoothManagerField: Field? =
                BluetoothAdapter::class.java.getDeclaredField("mService")
            bluetoothManagerField?.isAccessible = true
            val bluetoothManagerService: Any? = bluetoothManagerField?.get(bluetoothAdapter)

            if (bluetoothManagerService != null) {
                val bluetoothNameField: Field? =
                    bluetoothManagerService.javaClass.getDeclaredField("mName")
                bluetoothNameField?.isAccessible = true
                bluetoothName = bluetoothNameField?.get(bluetoothManagerService) as? String ?: ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return bluetoothName
    }


}


