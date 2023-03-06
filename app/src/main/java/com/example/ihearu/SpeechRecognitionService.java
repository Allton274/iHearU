package com.example.ihearu;

import android.content.Context;
import android.content.ContextParams;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.speech.RecognitionService;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class SpeechRecognitionService extends RecognitionService {

    @Override
    protected void onStartListening(Intent intent, RecognitionService.Callback callback) {
        Context attributionContext = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            attributionContext = createContext(new ContextParams.Builder()
                    .setNextAttributionSource(callback.getCallingAttributionSource())
                    .build());
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        AudioRecord recorder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            recorder = new AudioRecord.Builder()
                    .setContext(attributionContext).build();
        }

        recorder.startRecording();

    }

    @Override
    protected void onCancel(Callback callback) {

    }

    @Override
    protected void onStopListening(Callback callback) {



    }
}
