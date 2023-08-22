package com.miraimx.selectagoapp

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Datos constructor(private val contexto:Context, private val lineChart: LineChart) {
    private val frutos = arrayOf("Limon")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    @RequiresApi(Build.VERSION_CODES.O)
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var entradaLinea = ArrayList<Entry>()
    private var tipoFruta = ""
    private var fDesde = ""
    private var fHasta = ""
    private var produccion = ArrayList<String>()
    private var fechas = ArrayList<String>()

    fun confArrayAdapter(aAFrutos: Spinner){
        // Instanciando ArrayAdapters
        aAFrutos.adapter =  ArrayAdapter(contexto, R.layout.support_simple_spinner_dropdown_item, frutos)
        aAFrutos.setBackgroundColor(Color.parseColor("#62aa00"))
        aAFrutos .onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tipoFruta = frutos[p2]
                if (contexto::class.simpleName == "MainActivity"){
                    fDesde = LocalDate.now().format(dateFormatter)
                    fHasta = LocalDate.now().plusDays(5).format(dateFormatter)
                    val consulta = "select fecha, cantidad_parcela from detecciones WHERE fruto = ? " +
                            "ORDER BY fecha DESC LIMIT 5"
                    val selectionArgs = arrayOf(tipoFruta)
                    hallarDatos(consulta, selectionArgs)
                    confGrafica(fechas, produccion)
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    fun hallarDatos(consulta: String, args: Array<String>) {
        // OBJETOS DE BASE DATOS
        val mydb = SQLiteHelperKotlin(contexto)
        val db = mydb.readableDatabase
        try {
            //val selectionArgs = arrayOf(tipoFruta, fDesde, fHasta)
            if (tipoFruta.isNotEmpty() && fDesde.isNotEmpty() && fHasta.isNotEmpty()) {
                val cursor = db.rawQuery(consulta, args)
                if (cursor.moveToFirst()) {
                    do {
                        val fecha = cursor.getString(0)
                        val prod = cursor.getString(1)
                        fechas.add(fecha)
                        produccion.add(prod)
                    } while (cursor.moveToNext())
                } else {
                    Toast.makeText(contexto, "No se hallaron estimaciones",
                        Toast.LENGTH_SHORT).show()
                    fechas.clear()
                    produccion.clear()
                }
                cursor.close()
            } else {
                Toast.makeText(contexto, "Existen campos sin completar",
                    Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db?.close()
        }
    }

    private fun normalizaDatos(fechas: ArrayList<String>, produccion: ArrayList<String>) {
        /* Los registros al ser consultados de Descendente se necesitan almacenar
        de forma Ascendente para evitar el error de lectura. */
        entradaLinea.clear()
        for (i in 1 until fechas.size + 1) {
            try {
                val fecha = dateFormat.parse(fechas[fechas.size - i])
                val valor = produccion[fechas.size - i].toDouble()
                entradaLinea.add(Entry(fecha!!.time.toFloat(), valor.toFloat()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun confGrafica(fechas: ArrayList<String>, produccion: ArrayList<String>) {
        val xAxis = lineChart.xAxis
        normalizaDatos(fechas, produccion)

        val numRegistros = entradaLinea.size

        val dataset = LineDataSet(entradaLinea, "")
        val lineData = LineData(dataset)
        lineChart.data = lineData
        xAxis.valueFormatter = object : IndexAxisValueFormatter() {
            override fun getFormattedValue(value: Float, axis: AxisBase): String {
                return dateFormat.format(Date(value.toLong()))
            }
        }

        if (numRegistros >= 4) {
            xAxis.labelCount = 4
        } else {
            xAxis.labelCount = numRegistros + 1
        }

        lineChart.description.isEnabled = false
        dataset.color = ColorTemplate.JOYFUL_COLORS[3]
        dataset.valueTextColor = Color.BLACK
        dataset.valueTextSize = 18f
        lineChart.setNoDataText("Sin detecciones frutales")
        lineChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD)
        lineChart.setTouchEnabled(false)
        lineChart.invalidate()
        if (fechas.isEmpty() && produccion.isEmpty()){
            lineChart.clear()
        }

    }
}