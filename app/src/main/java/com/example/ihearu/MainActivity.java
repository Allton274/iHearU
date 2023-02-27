package com.example.ihearu;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    Slider slider1, slider2, slider3;
    Button confirmBtn, resetBtn;
    LinearLayout warningLayout;
    LocationCallback locationCallback;


    FusedLocationProviderClient fusedLocationProviderClient;
    SharedPreferences sharedPreferences;
    Location currentLocation;
    LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //TODO: Send Google Maps link with location. Format: https://www.google.com/maps/search/40.0485883,+-75.4527254/@40.0485883,-75.4527254,17z
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (getSupportActionBar() == null)
            setSupportActionBar(findViewById(R.id.toolbar));

        requestNecessaryPermissions();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLocation = location;
                }
            }
        };

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build();


        confirmBtn = findViewById(R.id.confirmBtn);
        resetBtn = findViewById(R.id.resetBtn);
        warningLayout = findViewById(R.id.profileWarningLayout);
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
                        == PackageManager.PERMISSION_GRANTED) {
                    SmsManager manager = SmsManager.getDefault();
                    String result = sharedPreferences.getString("emergencyMsg", "none");
                    StringBuilder textMessage = new StringBuilder("This message has been sent on behalf of ");
                    textMessage.append(sharedPreferences.getString("name", "null"));
                    textMessage.append(". It is triggered in response to an emergency phrase.\n");
                    textMessage.append("Their message: \"").append(result).append("\".\n");

                    textMessage.append("Their longitutde is: ").append(currentLocation.getLongitude()).append("; Their latitude is: ").append(currentLocation.getLatitude());

                    ArrayList<String> msgs = manager.divideMessage(textMessage.toString());

                    for(String m : msgs){
                        manager.sendTextMessage("6104822054", null, m, null, null);
                    }



                    Toast.makeText(MainActivity.this, "Done", Toast.LENGTH_SHORT).show();

                    slider1.setValue(0);
                    slider2.setValue(0);
                    slider3.setValue(0);
                } else {

                    requestNecessaryPermissions();

//                    fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
//                        @Override
//                        public void onSuccess(Location location) {
//                            currentLocation = location;
//
//                        }
//                    });

                }


            }
        });

        resetBtn.setOnClickListener(view -> {
            slider1.setValue(0);
            slider2.setValue(0);
            slider3.setValue(0);
        });


    }

    private void requestNecessaryPermissions() {
        ArrayList<String> permissions = new ArrayList<String>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.SEND_SMS);
        }

        if(permissions.size() > 0){
            String[] perms = new String[permissions.size()];
            for(int i = 0; i < perms.length; i++){
                perms[i] = permissions.get(i);
            }
            ActivityCompat.requestPermissions(this, perms, 0);
        }





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void checkConfirm() {
        if (slider1.getValue() == 10 && slider2.getValue() == 10 && slider3.getValue() == 10) {
            confirmBtn.setVisibility(View.VISIBLE);
            startLocationUpdates();
            if (sharedPreferences.getString("name", "").equals("") || sharedPreferences.getString("dangerPhrase", "").equals("") ||
                    sharedPreferences.getString("emergencyMsg", "").equals("")) {

                confirmBtn.setClickable(false);
                confirmBtn.setBackgroundColor(Color.parseColor("#808080"));
                warningLayout.setVisibility(View.VISIBLE);

            }

        } else {
            confirmBtn.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage("+16104822054", null, "message", null, null);
//                    Toast.makeText(getApplicationContext(), "SMS sent.",
//                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(profileIntent);
                return true;

            case R.id.addContact:
                Intent contactIntent = new Intent(getApplicationContext(), ActivityContact.class);
                startActivity(contactIntent);
                return true;

            case R.id.help:
                Toast.makeText(this, sharedPreferences.getString("name", "null"), Toast.LENGTH_SHORT).show();


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
            return;
        }

    }


}