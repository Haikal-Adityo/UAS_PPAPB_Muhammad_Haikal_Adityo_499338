package com.example.nutricare_uas.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nutricare_uas.DetailFragment

class MidnightReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "CLEAR_ALL_DATA_ALARM") {
            val detailFragment = DetailFragment()
            detailFragment.clearAllData()
        }
    }
}