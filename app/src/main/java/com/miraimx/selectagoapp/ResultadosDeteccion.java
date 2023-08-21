package com.miraimx.selectagoapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.miraimx.selectagoapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
public class ResultadosDeteccion extends AppCompatActivity {

    TextView txtArboles, txtMuestra, txtDetectados, txtPromedio, txtEstimacion;
    private String fruto;
    private int arboles, muestra, detecciones, promedio, estimacion;
    //private String fechaConf;
    private InterstitialAd mInterstitialAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados_deteccion);
        ActionBar actionBar = getSupportActionBar();
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest,
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



        if (actionBar != null) {
            //Poner el ícono al ActionBar
            actionBar.setIcon(R.drawable.tfl_logo);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#62aa00")));
        }

        txtArboles = findViewById(R.id.txtNumeroArboles);
        txtMuestra = findViewById(R.id.txtArbolesMuestreados);
        txtDetectados = findViewById(R.id.txtFrutosDetectados);
        txtPromedio = findViewById(R.id.txtFrutosArbol);
        txtEstimacion = findViewById(R.id.txtResultado);

        Intent intent = getIntent();
        fruto = intent.getStringExtra("fruto");
        arboles = intent.getIntExtra("arboles", 0);
        muestra = intent.getIntExtra("muestra", 0);
        detecciones = intent.getIntExtra("detecciones", 0);
        promedio = intent.getIntExtra("promedio", 0);
        estimacion = intent.getIntExtra("estimacion", 0);

        mostrarDeteccion();
    }

    private void mostrarDeteccion(){
        txtArboles.setText("Número de árboles: "+arboles);
        txtMuestra.setText("Árboles muestreados: "+muestra);
        txtDetectados.setText("Frutos Detectados: "+detecciones);
        txtPromedio.setText("Promedio por árbol: "+promedio);
        txtEstimacion.setText(estimacion+" frutos");
    }

    public void insertarDatosDeteccion(View view) {
        try (SQLiteHelperKotlin miBaseDeDatos = new SQLiteHelperKotlin(this)){
        SQLiteDatabase db = miBaseDeDatos.getWritableDatabase();
        SQLiteDatabase consulta = miBaseDeDatos.getReadableDatabase();
        String fecha = fechasFormatos();
        String [] selectionArgs = {fruto, fecha};
        String query = "select fecha from detecciones WHERE fruto = ? AND fecha = ? ";
        Cursor cursor = consulta.rawQuery(query, selectionArgs);
        int num_registros = cursor.getCount();
        ContentValues valores = new ContentValues();
            if (num_registros == 0) {
                // Insertar datos en la tabla
                valores.put("fruto", fruto);
                valores.put("fecha", fecha);
                valores.put("cantidad_arbol", arboles);
                valores.put("cantidad_parcela", estimacion);
                db.insert("detecciones", null, valores);
            }else{
                valores.put("cantidad_arbol", arboles);
                valores.put("cantidad_parcela", estimacion);
                String [] args = new String [] {fruto, fecha};
                db.update("detecciones", valores, "fruto=? AND fecha=?",args);
            }
            consulta.close();
            db.close();
            Toast.makeText(this, "Resultados guardados", Toast.LENGTH_SHORT).show();
        }catch(Exception ignored){
            Toast.makeText(this, "No se pudieron guardar los resultados", Toast.LENGTH_SHORT).show();
        }finally {
            salirDatos(view);

        }
    }

    private String fechasFormatos(){

        // Obtener la fecha actual
        Date currentDate = new Date();

        // Definir el patrón de formato deseado
        //String pattern = "dd/MM/yyyy HH:mm:ss";
        String pattern = "yyyy-MM-dd";
        //String patternConfirmation = "dd/MM/yyyy";

        // Crear un objeto SimpleDateFormat con el patrón
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        //fechaConf = (new SimpleDateFormat(patternConfirmation, Locale.getDefault())).format(currentDate);
        // Formatear la fecha
        return dateFormat.format(currentDate);
    }

    public void salirDatos(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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