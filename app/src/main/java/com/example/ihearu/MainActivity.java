package com.example.ihearu;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    Slider slider1, slider2, slider3;
    Button confirmBtn, resetBtn;
    LinearLayout warningLayout;
    LocationCallback locationCallback;


    FusedLocationProviderClient fusedLocationProviderClient;
    SharedPreferences sharedPreferences;
    Location currentLocation;
    LocationRequest locationRequest;

    ContactDao contactDao;
    CompositeDisposable mDisposable = new CompositeDisposable();

    ArrayList<Contact> textingContacts = new ArrayList<>();
    ArrayList<Contact> callingContacts = new ArrayList<>();
    ArrayList<Contact> emailingContacts = new ArrayList<>();
    Properties props = new Properties();
    Session session;
    StringBuilder formattedMessage;


    private final String username = "noreply.ihearu@gmail.com";
    private final String password = "mcnkfszbxsklslvq";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //TODO: Send Google Maps link with location. Format: https://www.google.com/maps/search/40.0485883,+-75.4527254/@40.0485883,-75.4527254,17z
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (getSupportActionBar() == null)
            setSupportActionBar(findViewById(R.id.toolbar));

        requestNecessaryPermissions();

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").build();

        contactDao = db.contactDao();

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        mDisposable.add(contactDao.getTextingContacts().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe((textContacts) -> textingContacts.addAll(textContacts)));

        mDisposable.add(contactDao.getEmailingContacts().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe((emailContacts) -> emailingContacts.addAll(emailContacts)));

        mDisposable.add(contactDao.getCallingContacts().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe((callContacts) -> callingContacts.addAll(callContacts)));

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });






        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {

                for (Location location : locationResult.getLocations()) {
                    currentLocation = location;
                }
            }
        };

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build();
        startLocationUpdates();

        confirmBtn = findViewById(R.id.confirmBtn);
        resetBtn = findViewById(R.id.resetBtn);
        warningLayout = findViewById(R.id.profileWarningLayout);
        slider1 = findViewById(R.id.slider1);

        slider2 = findViewById(R.id.slider2);

        slider3 = findViewById(R.id.slider3);


        slider1.addOnChangeListener(
                (slider, value, fromUser) -> checkConfirm());

        slider2.addOnChangeListener((slider, value, fromUser) -> checkConfirm());


        slider3.addOnChangeListener((slider, value, fromUser) -> checkConfirm());


        confirmBtn.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

                SmsManager manager = SmsManager.getDefault();
                String result = sharedPreferences.getString("emergencyMsg", "none");
                formattedMessage = new StringBuilder("This message has been sent on behalf of ");
                formattedMessage.append(sharedPreferences.getString("name", "null"));
                formattedMessage.append(". It is triggered in response to an emergency phrase.\n\n");
                formattedMessage.append("Their message: \"").append(result).append("\".\n\n");


                formattedMessage.append("Their longitude is: ").append(currentLocation.getLongitude())
                        .append("; Their latitude is: ").append(currentLocation.getLatitude());


                ArrayList<String> msgs = manager.divideMessage(formattedMessage.toString());
                for(Contact contact : textingContacts){
                    for(String m : msgs){
                        manager.sendTextMessage(contact.number, null, m, null, null);
                    }
                }

                for(Contact contact: emailingContacts){
                    sendEmail(contact);
                }




                Toast.makeText(MainActivity.this, "Informed your Contacts", Toast.LENGTH_SHORT).show();

                slider1.setValue(0);
                slider2.setValue(0);
                slider3.setValue(0);
            } else {

                requestNecessaryPermissions();

            }


        });

        resetBtn.setOnClickListener(view -> {
            slider1.setValue(0);
            slider2.setValue(0);
            slider3.setValue(0);



        });


    }

    private void requestNecessaryPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.SEND_SMS);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            permissions.add(Manifest.permission.INTERNET);
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
        if(slider1.getValue() == 10){
            Toast.makeText(this, "Slider done", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Listening done", Toast.LENGTH_SHORT).show();
        }
        if (slider1.getValue() == 10 && slider2.getValue() == 10 && slider3.getValue() == 10) {
            confirmBtn.setVisibility(View.VISIBLE);

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
        if (requestCode == 0) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("An unknown error occurred, please try again.").setTitle("Error")
                        .setIcon(R.drawable.warning_40px).show();

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
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mDisposable.clear();

    }

    public void sendEmail(Contact contact){
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(contact.email));
            message.setSubject("Emergency Alert Triggered by " + sharedPreferences.getString("name", "null"));
            message.setText(formattedMessage.toString());

            //TODO: How to add attatchment to email; useful in future?
//            MimeBodyPart messageBodyPart = new MimeBodyPart();
//
//            Multipart multipart = new MimeMultipart();
//
//            messageBodyPart = new MimeBodyPart();
//            String file = "path of file to be attached";
//            String fileName = "attachmentName"
//            DataSource source = new FileDataSource(file);
//            messageBodyPart.setDataHandler(new DataHandler(source));
//            messageBodyPart.setFileName(fileName);
//            multipart.addBodyPart(messageBodyPart);
//
//            message.setContent(multipart);

            Thread thread = new Thread(() -> {
                try{
                    Transport.send(message);
                } catch(Exception e){
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "An unknown error occurred. Try again", Toast.LENGTH_SHORT).show());
                }

            });

            thread.start();



        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}