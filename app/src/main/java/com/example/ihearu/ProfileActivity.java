package com.example.ihearu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    EditText name, dangerPhrase, emergencyMsg;

    String currentName, currentDangerPhrase, currentEmergencyMsg;
    boolean currentIsListening;

    SwitchCompat isListening;
    MenuItem confirmProfile;
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);



        name = findViewById(R.id.yourNameField);
        dangerPhrase = findViewById(R.id.dangerPhrase);
        emergencyMsg = findViewById(R.id.emergencyMsg);


        isListening = findViewById(R.id.listeningEnabled);
        
        name.setText(sharedPreferences.getString("name", ""));
        dangerPhrase.setText(sharedPreferences.getString("dangerPhrase", ""));
        emergencyMsg.setText(sharedPreferences.getString("emergencyMsg", ""));

        isListening.setChecked(sharedPreferences.getBoolean("isListening", false));


        currentName = name.getText().toString();
        currentDangerPhrase = dangerPhrase.getText().toString();
        currentEmergencyMsg = emergencyMsg.getText().toString();
        currentIsListening = isListening.isChecked();
        
        isListening.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b){
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this)
                        .setTitle("Important Caution!")
                        .setMessage(R.string.caution_message)
                        .setIcon(R.drawable.warning_40px)
                        .setPositiveButton("OK", ((dialogInterface, i) -> {}
                        ))
                        .setNegativeButton("Cancel", (dialogInterface, i) -> isListening.setChecked(false))
                        .setCancelable(false);
                builder.show();

            }

            checkDifferentValues();
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkDifferentValues();


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        dangerPhrase.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkDifferentValues();


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        emergencyMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkDifferentValues();


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });






//        if (savedInstanceState == null) {
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.profile, new SettingsFragment())
//                    .commit();
//        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);

        confirmProfile = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.confirmProfile) {
            if (!hasRequiredFields()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You must enter your name, Danger Phrase, and emergency message.")
                        .setTitle("Required Fields").setIcon(R.drawable.warning_40px)
                        .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();
            } else if (dangerPhrase.getText().toString().split(" ").length < 4) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("The Danger Phrase must be at least 4 words long.")
                        .setTitle("Danger Phrase too Short").setIcon(R.drawable.warning_40px)
                        .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("name", name.getText().toString());
                editor.putString("dangerPhrase", dangerPhrase.getText().toString());
                editor.putString("emergencyMsg", emergencyMsg.getText().toString());
                editor.putBoolean("isListening", isListening.isChecked());
                editor.apply();

                currentIsListening = isListening.isChecked();
                currentEmergencyMsg = emergencyMsg.getText().toString();
                currentName = name.getText().toString();
                currentDangerPhrase = dangerPhrase.getText().toString();

                Toast.makeText(this, "Changes Saved", Toast.LENGTH_SHORT).show();
                confirmProfile.setVisible(false);
                if (isListening.isChecked()) {
                    stopService(new Intent(this, RecognizerService.class));
                    startService(new Intent(this, RecognizerService.class));
                } else {
                    RecognizerService.disabledSetting = true;
                    stopService(new Intent(this, RecognizerService.class));
                }


            }
        }
        return super.onOptionsItemSelected(item);

    }

    private boolean hasRequiredFields(){
        return (!name.getText().toString().equals("") && !dangerPhrase.getText().toString().equals("")
        && !emergencyMsg.getText().toString().equals(""));
    }

    private void checkDifferentValues(){
        confirmProfile.setVisible(!currentName.equals(name.getText().toString()) || !currentDangerPhrase.equals(dangerPhrase.getText().toString()) ||
                !currentEmergencyMsg.equals(emergencyMsg.getText().toString()) || currentIsListening != isListening.isChecked());

    }


    public static class SettingsFragment extends PreferenceFragmentCompat {
        String dangerText;
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);


            //TODO: To test sending email, enable dropdown with list of all contacts with ``dropDownPreference.setEntries()``

            SwitchPreference listeningEnabled = findPreference("isListening");
            EditTextPreference dangerPhrase = findPreference("dangerPhrase");

            assert dangerPhrase != null;
            dangerText = dangerPhrase.getText();

            assert listeningEnabled != null;
            listeningEnabled.setOnPreferenceChangeListener((preference, newValue) -> {

                if((boolean) newValue){


                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                            .setTitle("Important Caution!")
                            .setMessage(R.string.caution_message)
                                    .setIcon(R.drawable.warning_40px)
                            .setPositiveButton("OK", ((dialogInterface, i) -> {
                                        getContext().startService(new Intent(getContext(), RecognizerService.class));
                                Toast.makeText(getContext(), "Listening Enabled", Toast.LENGTH_SHORT).show();
                                    }
                                    ))
                            .setNegativeButton("Cancel", (dialogInterface, i) -> listeningEnabled.setChecked(false))
                            .setCancelable(false);
                    builder.show();

                }
                else{
                    RecognizerService.disabledSetting = true;
                    getContext().stopService(new Intent(getContext(), RecognizerService.class));
                    Toast.makeText(getContext(), "Listening Disabled", Toast.LENGTH_SHORT).show();
                }
                return true;
            });



            dangerPhrase.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
                @Override
                public void onBindEditText(@NonNull EditText editText) {

                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if(charSequence.toString().split(" ").length > 3){
                                dangerPhrase.setEnabled(true);

                            }
                            else{
                                if(dangerPhrase.isEnabled()){
                                    dangerPhrase.setEnabled(false);
                                }
                            }

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {


                        }
                    });

                }
            });

        }
    }
}