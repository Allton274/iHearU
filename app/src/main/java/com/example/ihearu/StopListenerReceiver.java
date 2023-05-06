package com.example.ihearu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StopListenerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        RecognizerService.stoppedFromNotif = true;

        context.stopService(new Intent(context, RecognizerService.class));
    }
}