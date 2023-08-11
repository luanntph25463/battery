package com.example.battery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_home.img
import kotlinx.android.synthetic.main.fragment_home.percent_in
import kotlinx.android.synthetic.main.fragment_home.percent_in2
import kotlinx.android.synthetic.main.fragment_home.percent_in3
import kotlinx.android.synthetic.main.fragment_home.percent_in4
import kotlinx.android.synthetic.main.fragment_home.wifi_in
import kotlinx.android.synthetic.main.fragment_home.wifi_in2
import kotlinx.android.synthetic.main.fragment_home.wifi_in3
import kotlinx.android.synthetic.main.fragment_home.wifi_in4
import java.util.concurrent.TimeUnit


class HomeFragment : Fragment(), BatteryStatusReceiver.BatteryListener  {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationManager: NotificationManager

    private lateinit var batteryStatusReceiver: BatteryStatusReceiver

    private val channelId = "Pin notification"
    private val notificationId = 1

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            // khoi tao notification
            createNotificationChannel()
            notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager



            batteryStatusReceiver = BatteryStatusReceiver(this)
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            requireContext().registerReceiver(batteryStatusReceiver, intentFilter)

            val intentFilters = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            requireContext().registerReceiver(batteryStatusReceiver, intentFilters)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(batteryStatusReceiver)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "notification Pin"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance)
            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    // gui giu lieu len notification
    // nhan vao gia tri isCharging kiem tra xem co dang sac hay k
    // nhan gia tri so luong pin
    private fun sendBatteryStatusNotification(batteryLevel: Int?) {
        val builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.home)
            .setContentTitle("Battery Status")
            .setContentText("Battery level: $batteryLevel%")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Nếu đang sạc, hiển thị thông báo về tình trạng sạc


        notificationManager.notify(notificationId, builder.build())
    }

    override fun onBatteryLevelChanged( level: Int) {
        percent_in.text = " $level %"

        // send notification
        sendBatteryStatusNotification(level)

    }

    override fun onBatterytemperature(temperature: Int) {
        percent_in2.text = " $temperature °C"

    }

    override fun onBatteryisChanging(ischaring: Boolean) {
        if (!ischaring) {
                    percent_in3.text = "Not charging"
                    img.setImageResource(R.drawable.baseline_battery_unknown_24)


                    // luu thoi gian hien tai
                } else {
                    // luu thoi gian hien tai
                    val currentTime = System.currentTimeMillis()
                    saveChargingTime(currentTime)
                    // get time charging with get getSavedChargingTime
                    // lay thoi gian cu
                    val lastChargingTime = getSavedChargingTime()
                    percent_in4.text = lastChargingTime
                    img.setImageResource(R.drawable.baseline_battery_charging_full_24)
                    percent_in3.text = " Charging"
                }

    }

    override fun onConnectionTypeChanged(wifi: String) {
        wifi_in.text = wifi
    }

    override fun onSpeedChanged(speed: Float) {
        wifi_in2.text = speed.toString()

    }

    override fun onRating(speed: String) {
        wifi_in3.text = speed
    }

    override fun onProviderChanged(networkOperatorName: String) {
        wifi_in4.text = networkOperatorName
    }

    override fun BluttothName(name: String) {
        wifi_in4.text = name
        Log.d("this","$name")

    }

    // luu vao sharedd preferences
    private fun convertTimeToString(time: Long): String {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - time
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedTime)

        return if (hours >= 1) {
            "$hours giờ trước"
        } else {
            "Ít hơn 1 giờ trước"
        }
    }


    private fun saveChargingTime(time: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong("LastChargingTime", time)
        editor.apply()
    }
    // lay gia tri cu da luu vao sharedd preferences
    private fun getSavedChargingTime(): String {
        val lastChargingTime = sharedPreferences.getLong("LastChargingTime", 0)
        if (lastChargingTime > 0) {
            val chargingTime = convertTimeToString(lastChargingTime)
            return chargingTime
        }
        return "Chưa có dữ liệu"
    }
    // khoi tao  notificationChannal
}