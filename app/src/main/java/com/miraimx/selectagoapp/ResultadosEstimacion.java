package com.miraimx.selectagoapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class ResultadosEstimacion extends AppCompatActivity {

    private TextView txtFruto, txtProduccion, txtValor, txtRecolectores, txtCostales, txtTransporte;
    private String fruto, transporte, produccion, peso, valor;
    private int recolectores, costales;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados_estimacion);

        ActionBar actionBar = getSupportActionBar();
        AdRequest adRequest = new AdRequest.Builder().build();
        if (actionBar != null) {
            //Poner el ícono al ActionBar
            actionBar.setIcon(R.drawable.tfl_logo);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#62aa00")));
        }

        InterstitialAd.load(this,"ca-app-pub-1183027072386754/9938389254", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d(TAG, loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });

        txtFruto = findViewById(R.id.txtTipoFruto);
        txtProduccion = findViewById(R.id.txtEstimacionProduccion);
        txtValor = findViewById(R.id.txtValorProduccion);
        txtRecolectores = findViewById(R.id.txtRecolectoresNecesarios);
        txtCostales = findViewById(R.id.txtNumeroCostales);
        txtTransporte = findViewById(R.id.txtNumeroTransportes);

        Intent intent = getIntent();
        fruto = intent.getStringExtra("fruto");
        produccion = intent.getStringExtra("produccion");
        valor = intent.getStringExtra("valor");
        recolectores = intent.getIntExtra("recolectores",0 );
        costales = intent.getIntExtra("costales", 0);
        transporte = intent.getStringExtra("transporte");
        peso = intent.getStringExtra("peso");

        mostrarDatos();

    }

    private void mostrarDatos(){
        txtFruto.setText("Tipo De Fruto: "+fruto);
        txtProduccion.setText(produccion +" frutos = "+peso);
        txtValor.setText("$"+valor);
        txtRecolectores.setText("Recolectores necesarios: "+recolectores);
        txtCostales.setText("Número de costales: "+costales);
        txtTransporte.setText(transporte);
    }

    public void salirEstimacion(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }
}