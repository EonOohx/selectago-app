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
import kotlin.math.ceil
import kotlin.math.roundToInt

class Estimar : Fragment() {
    val frutos = arrayOf("Limon", "Mango", "Jitomate", "Mandarina")
    private val transporte = Array(4) { arrayOf("", "") }
    private lateinit var txtPrecioVenta: EditText
    private lateinit var txtDia: EditText
    private var tipoFruta: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_estimar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Instanciando elementos de la vista
        val opcionFrutas: Spinner = view.findViewById(R.id.spnTipoFruto)
        val btnEstimar: Button = view.findViewById(R.id.btnAceptarEstimacion)
        txtPrecioVenta = view.findViewById(R.id.txtPrecioVenta)
        txtDia = view.findViewById(R.id.txtDias)
        // Configuración de ArrayAdapter
        confArrayAdapter(opcionFrutas)
        // INSTANCIA DE EVENTOS
        btnEstimar.setOnClickListener {
            aceptarConfEst()
        }
        // Instanciando transportes
        datosTransporte()

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

    private fun aceptarConfEst() {
        try {
            val precioVenta = txtPrecioVenta.text.toString().toDouble()
            val capacidadCostal = 20.0
            val gramoFruto = 80.0
            val costalesDiarios = 5
            val dias = txtDia.text.toString().toInt()
            val traslado: String

            val valores = registro()
            if (valores.isNotEmpty()) {
                val resultadoPeso: String
                val pesoTotal = (valores.toDouble() * gramoFruto) / 1000 // En kilos
                val costales = pesoTotal / capacidadCostal
                val valorProduccion = String.format("%.2f", pesoTotal * precioVenta)
                val diasHombre = (costales / costalesDiarios.toDouble()).roundToInt()
                var trabajadores = diasHombre / dias.toDouble()
                resultadoPeso = if (pesoTotal > 1000) {
                    "${pesoTotal / 1000}t"
                } else {
                    "$pesoTotal Kg"
                }

                val camiones = medioTraslado(pesoTotal / 1000.0)

                traslado = if (camiones[0] == "Trailer") {
                    if (camiones[1] != "1.0") {
                        "${camiones[1].toInt()} Trailers de 25 t"
                    } else {
                        "1 Trailer de 25 t"
                    }
                } else {
                    "1 ${camiones[0]}"
                }

                trabajadores = if (trabajadores < 0.5) {
                    1.0
                } else {
                    ceil(trabajadores)
                }

                val intent = Intent(requireContext(), ResultadosEstimacion::class.java)
                intent.putExtra("fruto", tipoFruta)
                intent.putExtra("produccion", valores)
                intent.putExtra("valor", valorProduccion)
                intent.putExtra("recolectores", trabajadores.toInt())
                intent.putExtra("costales", ceil(costales).toInt())
                intent.putExtra("transporte", traslado)
                intent.putExtra("peso", resultadoPeso)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Sin detecciones realizadas", Toast.LENGTH_SHORT).show()
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Existen campos sin completar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registro(): String {
        var datos = ""
        val mydb = SQLiteHelperKotlin(requireContext())
        val db = mydb.readableDatabase
        try {
            val whereArgs = arrayOf(tipoFruta) // Argumentos para la cláusula WHERE si es necesario
            val consulta = "SELECT cantidad_parcela, fecha FROM detecciones WHERE fruto = ? ORDER BY fecha DESC LIMIT 1"
            val cursor = db.rawQuery(consulta, whereArgs)
            if (cursor.moveToFirst()) {
                do {
                    datos = cursor.getString(0)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db?.close()
        }
        return datos
    }

    private fun datosTransporte() {
        // TIPO
        transporte[0][0] = "Camioneta de Carga"
        transporte[1][0] = "Rabón"
        transporte[2][0] = "Torton"
        transporte[3][0] = "Trailer"
        // CAPACIDAD MAXIMA
        transporte[0][1] = "3.5"
        transporte[1][1] = "15"
        transporte[2][1] = "20"
        transporte[3][1] = "25"
    }

    private fun medioTraslado(estimacionToneladas: Double): Array<String> {
        val camiones = Array(2) { "" }
        var cantidadTransporte = 1.0
        var max: Double
        var i = 0
        while (i < 4) {
            max = transporte[i][1].toDouble()
            if (estimacionToneladas <= max) {
                break
            } else {
                if (max == 25.0) {
                    cantidadTransporte = ceil(estimacionToneladas / max)
                    break
                }
            }
            i++
        }
        camiones[0] = transporte[i][0]
        camiones[1] = cantidadTransporte.toString()
        return camiones
    }
}