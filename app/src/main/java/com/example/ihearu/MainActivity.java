package com.example.ihearu;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.telecom.TelecomManager;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements RecognitionListener {
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
    volatile SpeechRecognizer speechRecognizer;


    private final String username = "noreply.ihearu@gmail.com";
    private final String password = "mcnkfszbxsklslvq";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //TODO: Send Google Maps link with location. Format: https://www.google.com/maps/search/40.0485883,+-75.4527254/@40.0485883,-75.4527254,17z
        super.onCreate(savedInstanceState);
            
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancelAll();

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
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        stopService(new Intent(this, RecognizerService.class));

        if(sharedPreferences.getBoolean("isListening", false)){
            Intent i = new Intent(this, RecognizerService.class);
            startForegroundService(i);
        }


        mDisposable.add(contactDao.getTextingContacts().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe((textContacts) -> {
                    textingContacts.clear();
                    textingContacts.addAll(textContacts);}));

        mDisposable.add(contactDao.getEmailingContacts().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe((emailContacts) -> {
                    emailingContacts.clear();
                    emailingContacts.addAll(emailContacts);
                } ));

        mDisposable.add(contactDao.getCallingContacts().observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe((callContacts) ->{
                    callingContacts.clear();
                    callingContacts.addAll(callContacts);
                } ));

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {

                currentLocation = locationResult.getLastLocation();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                Log.d("Long Longitude", "" + (long)currentLocation.getLongitude());
                Log.d("Double Longitude", "" + currentLocation.getLongitude());
                editor.putFloat("longitude", (float) currentLocation.getLongitude());
                editor.putFloat("latitude", (float) currentLocation.getLatitude());

                editor.apply();


            }
        };

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1200000).build();
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

                informContacts();

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.SEND_SMS);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.INTERNET);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);

        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CALL_PHONE);
        }


        if (permissions.size() > 0) {
            String[] perms = new String[permissions.size()];
            for (int i = 0; i < perms.length; i++) {
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(2).setVisible(sharedPreferences.getBoolean("isListening", false));
        return super.onPrepareOptionsMenu(menu);
    }

    private void checkConfirm() {


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
            if (grantResults.length == 0
                    || grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("An unknown error occurred, please try again.").setTitle("Error")
                        .setIcon(R.drawable.warning_40px).show();
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
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

            case R.id.restartListening:
                stopService(new Intent(this, RecognizerService.class));
                startService(new Intent(this, RecognizerService.class));
                Toast.makeText(this, "Restarted listening", Toast.LENGTH_SHORT).show();


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startLocationUpdates() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
        }

    }

    public void informContacts() {

        SmsManager manager = SmsManager.getDefault();
        TelecomManager callManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        String result = sharedPreferences.getString("emergencyMsg", "none");
        formattedMessage = new StringBuilder("This message has been sent on behalf of ");
        formattedMessage.append(sharedPreferences.getString("name", "null"));
        formattedMessage.append(". It is triggered in response to an emergency phrase.\n\n");
        formattedMessage.append("Their message: \"").append(result).append("\".\n\n");


        formattedMessage.append("Their longitude is: ").append(currentLocation.getLongitude())
                .append("; Their latitude is: ").append(currentLocation.getLatitude());


        ArrayList<String> msgs = manager.divideMessage(formattedMessage.toString());
        
        for (Contact contact : textingContacts) {
            for (String m : msgs) {
                manager.sendTextMessage(contact.number, null, m, null, null);
            }
            manager.sendTextMessage(contact.number, null, "https://www.google.com/maps/search/"
                    + currentLocation.getLatitude() + ",+" + currentLocation.getLongitude() + "/@" +
                    currentLocation.getLatitude() + "," + currentLocation.getLongitude() + ",17z", null, null);
        }

        for (Contact contact : emailingContacts) {
            sendEmail(contact);
        }
        for (Contact contact : callingContacts) {

            Uri uri = Uri.fromParts("tel", contact.number, null);
            Bundle extras = new Bundle();
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, false);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    callManager.placeCall(uri, extras);
                }
            }
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
            message.setText(formattedMessage.toString() + "https://www.google.com/maps/search/" + sharedPreferences.getFloat("latitude", 0) + ",+" +
                    sharedPreferences.getFloat("longitude", 0) + "/@" + sharedPreferences.getFloat("latitude", 0) + "," + sharedPreferences.getFloat("longitude", 0) + ",17z");
            //TODO: How to add attachment to email; useful in future? Use StaticMap API/de.pentabyte library to implement this
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

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d("Speech Ended", "end");

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

        if(hypothesis != null){
            Log.d("toes", hypothesis.getHypstr());
            if(hypothesis.getHypstr().equals(sharedPreferences.getString("dangerPhrase", "null"))){

                informContacts();
                createNotificationChannel();
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "iHearU")
                        .setContentTitle("Confirmed Speech")
                        .setSmallIcon(R.drawable.confirmed_phrase_notif)
                        .setColor(getResources().getColor(R.color.black))
                        .setContentText("Your Danger Phrase was triggered and your contacts have been informed. If this is a false alarm, please notify your contacts")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Your Danger Phrase was triggered and your contacts have been informed. If this is a false alarm, please notify your contacts"))
                        .setPriority(NotificationCompat.PRIORITY_LOW)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setSilent(true)
//                        .setVibrate(new long[0])
                        .setAutoCancel(true);
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(2, builder.build());
                }
                speechRecognizer.stop();

            }
        }
//            speechRecognizer.stop();



    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if(hypothesis != null){
            Log.d("Result Hypothesis", hypothesis.getHypstr());
            speechRecognizer.startListening("keywordSearch");

        }




    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();

    }

    @Override
    public void onTimeout() {

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "iHearU Alert";
            String description = "Listening stopped";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("iHearU", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
    }
}