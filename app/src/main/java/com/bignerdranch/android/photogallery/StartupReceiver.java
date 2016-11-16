package com.bignerdranch.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by sheng on 16/9/27.
 *
 */
public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Receiverd broadcast intent:" + intent.getAction());
        boolean alarmIsno = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PollService.PREF_IS_ALARM_ON, false);
        PollService.setServiceAlarm(context, alarmIsno);

    }
}