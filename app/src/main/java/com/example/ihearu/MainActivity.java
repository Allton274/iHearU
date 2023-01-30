package com.example.ihearu;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ActionMenuView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.material.slider.Slider;

import java.util.Timer;

public class MainActivity extends AppCompatActivity {
    Slider slider1, slider2, slider3;
    Button confirmBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        
        if(getSupportActionBar() == null)
            setSupportActionBar(findViewById(R.id.toolbar));
        else{
            Toast.makeText(this, "Already bar", Toast.LENGTH_SHORT).show();
        }






        confirmBtn = findViewById(R.id.confirmBtn);
        slider1 = findViewById(R.id.slider1);

        slider2 = findViewById(R.id.slider2);

        slider3 = findViewById(R.id.slider3);






        slider1.addOnChangeListener(
                (slider, value, fromUser) -> checkConfirm());

        slider2.addOnChangeListener(new Slider.OnChangeListener() {

            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                checkConfirm();
            }
        });


        slider3.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                checkConfirm();
            }
        });


        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS},
                                PackageManager.PERMISSION_GRANTED);
                    }
                else{
//                    SEND TEXT LIKE THIS

                    SmsManager manager = SmsManager.getDefault();
                    manager.sendTextMessage("6104822054", null, "Yo", null, null);

                    Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();

                    slider1.setValue(0);
                    slider2.setValue(0);
                    slider3.setValue(0);


                }



            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void checkConfirm() {
        if (slider1.getValue() == 10 && slider2.getValue() == 10 && slider3.getValue() == 10) {
            confirmBtn.setVisibility(View.VISIBLE);

        } else {
            confirmBtn.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("+16104822054", null, "message", null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.profile:
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(intent);
                return true;

            case R.id.addContact:
                Toast.makeText(this, "Add Contact", Toast.LENGTH_SHORT).show();

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}