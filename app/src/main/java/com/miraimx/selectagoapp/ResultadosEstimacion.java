package com.miraimx.selectagoapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.miraimx.selectagoapp.R;

public class ResultadosEstimacion extends AppCompatActivity {

    private TextView txtFruto, txtProduccion, txtValor, txtRecolectores, txtCostales, txtTransporte;
    private String fruto, transporte, produccion, peso, valor;
    private int recolectores, costales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultados_estimacion);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            //Poner el ícono al ActionBar
            actionBar.setIcon(R.drawable.tfl_logo);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#62aa00")));
        }

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
}