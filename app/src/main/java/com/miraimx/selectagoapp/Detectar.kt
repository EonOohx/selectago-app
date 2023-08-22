package com.miraimx.selectagoapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlin.math.pow


class Detectar : Fragment() {
    private val frutos = arrayOf("Limon")
    private lateinit var cantidadArboles: EditText
    private var tipoFruta: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detectar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Instanciando elementos de vista
        val opcionFrutas: Spinner = view.findViewById(R.id.opcionFrutas)
        val btnAceptar: Button = view.findViewById(R.id.btnAceptarConfMu)
        cantidadArboles = view.findViewById(R.id.intCantArbol)
        // Configuración de ArrayAdapters
        confArrayAdapter(opcionFrutas)
        // Asignación de Eventos
        btnAceptar.setOnClickListener {
            aceptarConfMu()
        }
    }

    private fun confArrayAdapter(aAFrutos: Spinner) {
        // Instanciando ArrayAdapters
        aAFrutos.adapter = activity?.let {
            ArrayAdapter(
                it,
                androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, frutos)
        }
        aAFrutos.setBackgroundColor(Color.parseColor("#62aa00"))
        aAFrutos .onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tipoFruta = frutos[p2]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun aceptarConfMu() {
        try {
            val numArboles = Integer.parseInt(cantidadArboles.text.toString())
            val nivelConfianza = 1.94
            val estimacion = 0.5
            val margenError = 0.5

            val muestra = (numArboles * nivelConfianza.pow(2) * estimacion * (1 - estimacion)) /
                    ((numArboles - 1) * margenError.pow(2) + nivelConfianza.pow(2) * estimacion * (1 - estimacion))

            val intent = Intent(requireContext(), Deteccion::class.java)
            intent.putExtra("fruto", tipoFruta)
            intent.putExtra("arboles", numArboles)
            intent.putExtra("muestra", muestra.toInt())
            startActivity(intent)
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Existen campos sin completar", Toast.LENGTH_SHORT).show()
        }

    }

}