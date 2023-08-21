package com.miraimx.selectagoapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class Ayuda extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayuda);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            //Poner el ícono al ActionBar
            actionBar.setIcon(R.drawable.tfl_logo);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#62aa00")));
        }
    }

    public void fnVerVideosTutoriales(View view) {
        String url = "https://www.youtube.com/channel/UCsnJEWQcQcMQF30tevhNKCQ"; // Reemplaza con tu enlace
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}