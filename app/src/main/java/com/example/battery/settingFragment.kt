package com.example.battery

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter

import android.widget.Spinner


class settingFragment : Fragment() {

    private lateinit var percen_spinner: Spinner
    private lateinit var time_chooser_spinner: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        percen_spinner = findViewById(R.id.low_rate_spinner)
//        time_chooser_spinner = findViewById(R.id.off_time_spinner)
//
//        val low_percent_array = resources.getStringArray(R.array.low_percent_arrray)// get array text
//        val time_chooser_array = resources.getStringArray(R.array.time_chooser_arrray)// get array text
//
//        val adapter = ArrayAdapter(this, R.layout.spinner_item_custom, low_percent_array)// create adapter
//        val adapter2 = ArrayAdapter(this,R.layout.spinner_item_custom, time_chooser_array)
//
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)// set layout for adapter
//        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)// set layout for adapter
//        percen_spinner.adapter = adapter
//        time_chooser_spinner.adapter = adapter2
    }

}