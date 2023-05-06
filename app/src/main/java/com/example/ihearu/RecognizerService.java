package com.example.ihearu;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telecom.TelecomManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class RecognizerService extends Service implements RecognitionListener {
    public static SpeechRecognizer recognizer;
    SharedPreferences sharedPreferences;
    Notification notification;
    public static boolean stoppedFromNotif = false;
    public static boolean disabledSetting = false;
    boolean triggeredKeyPhrase = false;
    String groupNotification = "listeningNotifications";
    NotificationManagerCompat notificationManager;
    FusedLocationProviderClient fusedLocationProviderClient;

    ArrayList<Contact> textingContacts = new ArrayList<>();
    ArrayList<Contact> callingContacts = new ArrayList<>();
    ArrayList<Contact> emailingContacts = new ArrayList<>();
    CompositeDisposable mDisposable = new CompositeDisposable();
    ContactDao contactDao;
    Session session;
    Properties props = new Properties();
    StringBuilder formattedMessage;

    private final String username = "noreply.ihearu@gmail.com";
    private final String password = "mcnkfszbxsklslvq";
    AudioManager audioManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Intent stopListener = new Intent(this, StopListenerReceiver.class);
        stopListener.setAction("Stop Listening");
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopListener, PendingIntent.FLAG_MUTABLE);

        notificationManager = NotificationManagerCompat.from(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").build();

        contactDao = db.contactDao();

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




        notification = new NotificationCompat.Builder(this, "iHearU")
                .setContentTitle("iHearU Listening")
                .setSmallIcon(R.drawable.mic_listening)
                .setColor(getResources().getColor(R.color.black))
                .setContentText("Your microphone is being used by iHearU to listen for your Danger Phrase")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSilent(true)
                .setOngoing(true)
                .addAction(R.drawable.mic_listening, "Stop Listening", stopPendingIntent)

//                        .setVibrate(new long[0])
//                .setAutoCancel(true)
                .build();

        startForeground(1, notification);
        notificationManager.cancel(3);


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try{
                File assetDir = new File(getExternalFilesDir(null), "sync");
                recognizer = SpeechRecognizerSetup.defaultSetup()
                        .setAcousticModel(new File(assetDir, "en-us-ptm"))
                        .setDictionary(new File(assetDir, "cmudict-en-us.dict"))

                        .getRecognizer();
                recognizer.addListener(RecognizerService.this);
                recognizer.addKeyphraseSearch("keywordSearch", sharedPreferences.getString("dangerPhrase", "null"));

                recognizer.startListening("keywordSearch");
            } catch(Exception e){
//                Toast.makeText(getApplicationContext(), "An error occurred. The speech recognition could not be started.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        });


    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//
//        return super.onStartCommand(intent, flags, START_STICKY);
//    }

    @Override
    public IBinder onBind(Intent intent) {

        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onEndOfSpeech() {
        Log.d("Speech Ended", "end");

        recognizer.stop();


        recognizer.startListening("keywordSearch");

    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {

        if(hypothesis != null){
            Log.d("toes", hypothesis.getHypstr());

//            if(hypothesis.getHypstr().equals(sharedPreferences.getString("dangerPhrase", "null"))){
//                Toast.makeText(this, "Confirmed speech: " + hypothesis.getHypstr(), Toast.LENGTH_SHORT).show();
////
////                Log.d("toes", hypothesis.getHypstr());
//
//
////                informContacts();
////                createNotificationChannel();

//
//
//            }
            recognizer.stop();
        }



    }

    @Override
    public void onResult(Hypothesis hypothesis) {

        Log.d("Not stopped", "checking");
        if(hypothesis != null){
            Log.d("Result Hypothesis", hypothesis.getHypstr());
            if(hypothesis.getHypstr().equalsIgnoreCase(sharedPreferences.getString("dangerPhrase", "null"))){
                Toast.makeText(this, "Confirmed speech: " + hypothesis.getHypstr(), Toast.LENGTH_SHORT).show();
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
                        .setAutoCancel(true)
                        .setGroup(groupNotification);
                triggeredKeyPhrase = true;

                informContacts();


                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    notificationManager.notify(5, builder.build());
                }
                stopSelf();
                return;
            }

            recognizer.startListening("keywordSearch");
        }

    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();

    }

    @Override
    public void onTimeout() {

        recognizer.stop();
        recognizer.startListening("keywordSearch");

    }


    @Override
    public void onDestroy() {
        super.onDestroy();


        if(!stoppedFromNotif && !triggeredKeyPhrase && !disabledSetting){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, RecognizerService.class));
            }
        }



        if(!disabledSetting) {



            notificationManager.cancel(1);
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "iHearU")
                    .setContentTitle("Listening Stopped")
                    .setSmallIcon(R.drawable.ihearu_logo_notif)
                    .setColor(getResources().getColor(R.color.black))
                    .setContentText("Click here to relaunch iHearU and start listening again")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
//                .setVibrate(new long[0])
                    .setAutoCancel(true)
                    .setGroup(groupNotification);
            
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify(3, builder.build());
            }
        }

        recognizer.cancel();
        recognizer.shutdown();
        mDisposable.clear();




//        stoppedFromNotif = false;

    }

    public void informContacts() {

        SmsManager manager = SmsManager.getDefault();
        TelecomManager callManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        String result = sharedPreferences.getString("emergencyMsg", "none");
        formattedMessage = new StringBuilder("This message has been sent on behalf of ");
        formattedMessage.append(sharedPreferences.getString("name", "null"));
        formattedMessage.append(". It is triggered in response to an emergency phrase.\n\n");
        formattedMessage.append("Their message: \"").append(result).append("\".\n\n");


        formattedMessage.append("Their longitude is: ").append(sharedPreferences.getFloat("longitude", 0))
                .append("; Their latitude is: ").append(sharedPreferences.getFloat("latitude", 0));


        ArrayList<String> msgs = manager.divideMessage(formattedMessage.toString());
        msgs.add("https://www.google.com/maps/search/" + sharedPreferences.getFloat("latitude", 0) + ",+" +
                sharedPreferences.getFloat("longitude", 0) + "/@" + sharedPreferences.getFloat("latitude", 0) + "," + sharedPreferences.getFloat("longitude", 0) + ",17z");
        for (Contact contact : textingContacts) {
            for (String m : msgs) {
                manager.sendTextMessage(contact.number, null, m, null, null);
            }
        }


        

        for (Contact contact : emailingContacts) {
            sendEmail(contact);
        }
        for (Contact contact : callingContacts) {

            Uri uri = Uri.fromParts("tel", contact.number, null);
            Bundle extras = new Bundle();
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    callManager.placeCall(uri, extras);
                    audioManager.setStreamVolume(AudioManager.STREAM_RING, 0, 0);
                    audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 0, 0);
                }
            }

        }

    }

    public void sendEmail(Contact contact){
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(contact.email));
            message.setSubject("Emergency Alert Triggered by " + sharedPreferences.getString("name", "null"));
            message.setText(formattedMessage.toString() + "\n\nhttps://www.google.com/maps/search/" + sharedPreferences.getFloat("latitude", 0) + ",+" +
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
                }

            });
            thread.start();

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}