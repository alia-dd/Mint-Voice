package com.example.langep;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner languageSpinner;
    private String selectedLanguageCode = "en-US"; // default selected lang

    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;
    private static final int PERMISSION_REQUEST_CODE = 200;

    TextView myText_to_v;
    ImageButton myVoiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize views
        languageSpinner = findViewById(R.id.language_spinner);
        myText_to_v = findViewById(R.id.textTV);
        myVoiceBtn = findViewById(R.id.voicebtn);

        // change status bar color (black in this case)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        }

        // fill the spinner with languages
        ArrayList<Lang> langList = new ArrayList<>();
        langList.add(new Lang("English", "en-US"));
        langList.add(new Lang("French", "fr-FR"));
        langList.add(new Lang("Arabic", "ar-SA"));
        langList.add(new Lang("Finnish", "fi-FI"));
        langList.add(new Lang("Hindi", "hi-IN"));

        // ask for mic permission before anything
        checkAudioPermission();

        // create adapter and attach it to spinner
        ArrayAdapter<Lang> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, langList);
        languageSpinner.setAdapter(adapter);

        // listen for language changes
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Lang selected = (Lang) parent.getItemAtPosition(position);
                selectedLanguageCode = selected.getCode(); // update selected lang
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // handle voice button click
        myVoiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setPressed(true); // give a visual feedback
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        speak(); // trigger speech recognition
                    }
                }, 250); // give ripple time to animate
            }
        });
    }

    // mic permission check (ask if not granted)
    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
            myVoiceBtn.setEnabled(false); // disable button until permission is granted
        } else {
            myVoiceBtn.setEnabled(true); // all good
        }
    }

    // speech recognizer function
    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        // if device doesn't support speech input
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "Speech input not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        // setup intent options
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguageCode); // use selected lang
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak something");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT); // launch recognizer
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // fail-safe
        }
    }

    // handle result from voice input
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                myText_to_v.setText(result.get(0)); // show what was said
            }
        }
    }

    // handle permission result when user responds
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                myVoiceBtn.setEnabled(true); // enable the button
            } else {
                Toast.makeText(this, "Permission denied. Voice input won't work.", Toast.LENGTH_LONG).show();
                myVoiceBtn.setEnabled(false); // keep it disabled
            }
        }

    }
}
