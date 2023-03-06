package com.example.ihearu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EditContactActivity extends AppCompatActivity {

    Contact contactInfo;
    Toolbar toolbar;
    EditText name, phoneNumber, email;
    SwitchCompat doEmail, doText, doCall;

    ContactDao contactDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        contactInfo = (Contact) getIntent().getSerializableExtra("contact");
        toolbar = findViewById(R.id.toolbar);
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").build();

        contactDao = db.contactDao();


        name = findViewById(R.id.nameField);
        phoneNumber = findViewById(R.id.numberField);
        email = findViewById(R.id.emailField);

        doEmail = findViewById(R.id.doEmail);
        doCall = findViewById(R.id.doCall);
        doText = findViewById(R.id.doText);

        name.setText(contactInfo.name);
        phoneNumber.setText(contactInfo.number);
        email.setText(contactInfo.email);

        doEmail.setChecked(contactInfo.doEmail);
        doCall.setChecked(contactInfo.doCall);
        doText.setChecked(contactInfo.doText);
        getSupportActionBar().setTitle(getSupportActionBar().getTitle() + contactInfo.name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_contact, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.deleteContact) {

            //TODO: Implement what to do if deleted
            AlertDialog.Builder builder = new AlertDialog.Builder(EditContactActivity.this);
            builder.setMessage("Are you sure you want to delete this contact?").setTitle("Delete Contact")
                    .setIcon(R.drawable.warning_40px)
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    contactDao.removeContactAsync(contactInfo).subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread()).subscribe();
                                    finish();
                                }
                            }

                    ).setNegativeButton("Cancel", ((dialogInterface, i) -> {
                    })).show();
        } else if (item.getItemId() == R.id.confirmEditContact) {


            if (!hasRequiredFields()) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EditContactActivity.this);
                    alertBuilder.setMessage("You must enter the name, phone number, and email address of the contact and select at least one action.")
                            .setTitle("Required Fields").setIcon(R.drawable.warning_40px)
                            .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());

                    AlertDialog dialog = alertBuilder.create();
                    dialog.show();
            } else {
                Contact newContact = new Contact(name.getText().toString(),
                        phoneNumber.getText().toString(), email.getText().toString(), doEmail.isChecked(),
                        doText.isChecked(), doCall.isChecked());
                newContact.setContactId(contactInfo.contactId);

                contactDao.updateContactAsync(newContact).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io()).subscribe();
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }




    public boolean hasRequiredFields(){

        return ((!name.getText().toString().equals("") && !phoneNumber.getText().toString().equals("")
                && !email.getText().toString().equals("")) && (doEmail.isChecked() || doText.isChecked() || doCall.isChecked()));
    }
}