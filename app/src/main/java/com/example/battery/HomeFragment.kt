package com.example.battery

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat

import java.util.concurrent.TimeUnit


class HomeFragment : Fragment() {
    private lateinit var batteryStatusReceiver: BatteryStatusReceiver
    private lateinit var percent_in2: TextView
    private lateinit var percent_in3: TextView
    private lateinit var percent_in: TextView
    private lateinit var percent_in4: TextView
    private lateinit var img: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationManager: NotificationManager

    private val channelId = "Pin notification"
    private val notificationId = 1
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createNotificationChannel()
        notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        percent_in2 = view.findViewById(R.id.percent_in2)
        percent_in3 = view.findViewById(R.id.percent_in3)
        percent_in4 = view.findViewById(R.id.percent_in4)
        percent_in = view.findViewById(R.id.percent_in)
        img = view.findViewById(R.id.img)
        // khoi tao  shared perference
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        // Create an instance of BatteryStatusReceiver
        batteryStatusReceiver = BatteryStatusReceiver()

        // Obtain a valid Context object

        // Register the BroadcastReceiver
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        requireContext().registerReceiver(batteryStatusReceiver, filter)
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
// lay time hien hien tai tru di time cu
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
    // luu vao sharedd preferences
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
    private fun sendBatteryStatusNotification(batteryLevel: Int?, isCharging: Boolean?) {
        val builder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.home)
            .setContentTitle("Battery Status")
            .setContentText("Battery level: $batteryLevel%")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // Nếu đang sạc, hiển thị thông báo về tình trạng sạc
        if (isCharging == true) {
            builder.setContentText("Battery level: $batteryLevel% (Charging)")
        }

        notificationManager.notify(notificationId, builder.build())
    }

    private inner class BatteryStatusReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                // lay so luong pin
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPercentage = (level.toFloat() / scale.toFloat() * 100).toInt()
                percent_in.text = " $batteryPercentage %"
                // lay nhiet do may
                val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10

                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
                sendBatteryStatusNotification(batteryPercentage, isCharging)

                if (!isCharging) {
                    percent_in3.text = "Not charging"
                    img.setImageResource(R.drawable.baseline_battery_unknown_24)

                    img.setImageResource(R.drawable.baseline_battery_charging_full_24)
                    percent_in3.text = " Charging"
                    // luu thoi gian hien tai
                } else {
                    // luu thoi gian hien tai
                    val currentTime = System.currentTimeMillis()
                    saveChargingTime(currentTime)
                    // get time charging with get getSavedChargingTime
                    // lay thoi gian cu
                    val lastChargingTime = getSavedChargingTime()
                    percent_in4.text = lastChargingTime
                }


                if (level <= 20 && !isCharging) {
//            ivBatteryWarning.visibility = View.VISIBLE
                    percent_in3.text = " PiN Yeu"
                } else {
//            ivBatteryWarning.visibility = View.GONE

                }

                percent_in2.text = "$temperature °C "
            }
        }
    }
}