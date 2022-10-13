package com.example.ihearu;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.material.slider.Slider;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Slider slider1, slider2, slider3;
    Button confirmBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        slider1 = findViewById(R.id.slider1);

        slider2 = findViewById(R.id.slider2);

        slider3 = findViewById(R.id.slider3);

        confirmBtn = findViewById(R.id.confirmBtn);




    }
}