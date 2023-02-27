package com.example.ihearu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.room.Room;

import android.os.Bundle;
import android.transition.Slide;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ActivityAddContact extends AppCompatActivity {

    CompositeDisposable disposable = new CompositeDisposable();

    EditText name, phoneNumber, email;
    SwitchCompat doEmail, doText, doCall;
    ContactDao contactDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        setContentView(R.layout.activity_add_contact);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        name = findViewById(R.id.nameField);
        phoneNumber = findViewById(R.id.numberField);
        email = findViewById(R.id.emailField);

        doEmail = findViewById(R.id.doEmail);
        doText = findViewById(R.id.doText);
        doCall = findViewById(R.id.doCall);





        getWindow().setExitTransition(new Slide());




//        Contact cont = new Contact("iojdc", "610", "adityadora", false, true, true);
//
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").allowMainThreadQueries().build();

        contactDao = db.contactDao();
//
//        contactDao.addContactAsync(cont).blockingAwait();
//
//
//        disposable.add(contactDao.addContactAsync(cont).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
//                .subscribe(() -> Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show()));
//
//
//        finish();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.confirmAddContact:
                Contact cont = new Contact(name.getText().toString(), phoneNumber.getText().toString(), email.getText().toString(), doEmail.isChecked(), doText.isChecked(), doCall.isChecked());
                disposable.add(contactDao.addContactAsync(cont).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> Toast.makeText(getApplicationContext(), "Added Contact: " + name.getText().toString(), Toast.LENGTH_SHORT).show()));
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}