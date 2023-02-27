package com.example.ihearu;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.ihearu.databinding.ActivityContactBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ActivityContact extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityContactBinding binding;
    RecyclerView contactList;
    TextView noContactsText;
    AppDatabase db;

    ContactDao contactDao;
    ArrayList<Contact> contacts = new ArrayList<>();
    ProgressBar progressBar;
    private final CompositeDisposable mDisposable = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        contactList = findViewById(R.id.contactsList);
        noContactsText = findViewById(R.id.noContactsText);
        progressBar = findViewById(R.id.progressBar);


        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-name").build();

        contactDao = db.contactDao();

        binding.fab.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ActivityAddContact.class);
//            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(ActivityContact.this).toBundle()); TODO: FIGURE OUT HOW TO MAKE THIS TRANSITION STUFF WORK
            startActivity(intent);
//                contacts.clear();
//                Contact cont = new Contact("Abdi", "610", "adityadora", false, true, true);
//
//
//
//                contactDao.addContactAsync(cont).blockingAwait();

        });






//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_activity_contact);
//        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//

//
//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_activity_contact);
//        return NavigationUI.navigateUp(navController, appBarConfiguration)
//                || super.onSupportNavigateUp();
//

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDisposable.add(contactDao.getAllContactsAsync().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(allContacts -> handleDBCompleted(allContacts), Throwable::printStackTrace));


    }

    @Override
    protected void onPause() {
        super.onPause();

        mDisposable.clear();
//        contacts.clear();
    }

    private void handleDBCompleted(List<Contact> contactsList){
        contacts.clear();
        contacts.addAll(contactsList);
        runOnUiThread(() -> progressBar.setVisibility(View.GONE));

        if(contacts.size() > 0){
            String[] contactNames = new String[contacts.size()];
            for(int i = 0; i < contacts.size(); i++){
                contactNames[i] = contacts.get(i).name;
            }
            runOnUiThread(() -> {
                noContactsText.setVisibility(View.GONE);
                contactList.setAdapter(new ContactsAdapter(contactNames));
                contactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            });

            return;
        }

        contactList.setAdapter(null);
        runOnUiThread(() -> noContactsText.setVisibility(View.VISIBLE));

    }


}