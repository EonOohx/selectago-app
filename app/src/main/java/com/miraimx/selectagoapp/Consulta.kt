package com.miraimx.selectagoapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Consulta : AppCompatActivity() {
    val frutos = arrayOf("Limon", "Mango", "Jitomate", "Mandarina")
    private var tipoFruta: String = ""
    private var fDesde: String = ""
    private var fHasta: String = ""
    private lateinit var fechaDesde: TextView
    private lateinit var fechaHasta: TextView
    private lateinit var tablaDatos: TableLayout
    private lateinit var lineChart: LineChart
    private var entradaLinea = ArrayList<Entry>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateFinalFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consulta)

        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            //Poner el ícono al ActionBar
            actionBar.setIcon(R.drawable.tfl_logo)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#62aa00")))
        }

        // INSTANCIA DE VISTAS
        val opcionFrutas: Spinner = findViewById(R.id.spnTipoFrutoCd)
        val btnAceptar: Button =findViewById(R.id.btnAceptarConsulta)
        fechaDesde = findViewById(R.id.inDesde)
        fechaHasta = findViewById(R.id.inHasta)
        lineChart = findViewById(R.id.cProduccion)
        tablaDatos = findViewById(R.id.lstRProduccion)
        // CONFIGURACIÓN DE ARRAYADAPTERS
        confArrayAdapter(opcionFrutas)
        // INSTANCIA DE EVENTOS
        fechaDesde.setOnClickListener {
            fechaSeleccion(fechaDesde)
        }
        fechaHasta.setOnClickListener {
            fechaSeleccion(fechaHasta)
        }
        btnAceptar.setOnClickListener {
            hallarDatos()
        }
    }
    private fun confArrayAdapter(aAFrutos: Spinner) {
        // Instanciando ArrayAdapters
        aAFrutos.adapter =  ArrayAdapter(this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, frutos)
        aAFrutos.setBackgroundColor(Color.parseColor("#62aa00"))
        aAFrutos .onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                tipoFruta = frutos[p2]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun cargaTablas(tablaDatos: TableLayout, fechas: ArrayList<String>, produccion: ArrayList<String>) {
        // CABECERA DE LA TABLA
        tablaDatos.removeAllViews()
        val headerRow = TableRow(this)
        headerRow.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT)

        // FORMATO DE TEXTVIEW
        val header1 = TextView(this)
        header1.text = "Fecha"
        header1.textAlignment = View.TEXT_ALIGNMENT_CENTER
        header1.setTextSize(20f)
        header1.setTypeface(null, Typeface.BOLD)
        header1.setBackgroundColor(Color.parseColor("#a2d001"))
        header1.setPadding(20, 20, 20, 20)
        headerRow.addView(header1)

        val header2 = TextView(this)
        header2.text = "Producción"
        header2.textAlignment = View.TEXT_ALIGNMENT_CENTER
        header2.setTextSize(20f)
        header2.setTypeface(null, Typeface.BOLD)
        header2.setBackgroundColor(Color.parseColor("#a2d001"))
        header2.setPadding(20, 20, 20, 20)
        headerRow.addView(header2)

        // CARGANDO DATOS
        tablaDatos.addView(headerRow)

        // ANEXANDO DATOS A TABLA
        for (i in produccion.indices) {
            val dataRow = TableRow(this)

            // AJUSTE DE TAMAÑO DE FILAS EN RELACION A MARGENES
            dataRow.layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT)

            // CREACIÓN DE OBJETO TEXTVIEW FECHAS
            val data1 = TextView(this)
            data1.textAlignment = View.TEXT_ALIGNMENT_CENTER
            data1.setTextSize(20f)
            data1.setTextColor(Color.BLACK)

            // EXTRAYENDO FECHAS
            val date: Date
            val fecha: String
            try {
                date = dateFormat.parse(fechas[i])
                fecha = dateFinalFormat.format(date)
            } catch (e: ParseException) {
                throw RuntimeException(e)
            }


            data1.text = fecha
            data1.setPadding(20, 20, 20, 20)
            dataRow.addView(data1)

            // CREACIÒN DE OBJETO TEXTVIEW PRODUCCIÓN
            val data2 = TextView(this)
            data2.textAlignment = View.TEXT_ALIGNMENT_CENTER
            data2.setTextSize(20f)
            data2.setTextColor(Color.BLACK)
            data2.text = produccion[i]
            data2.setPadding(20, 20, 20, 20)
            dataRow.addView(data2)

            // RENDERIZACIÒN DE FILAS
            tablaDatos.addView(dataRow)
        }
    }

    fun fechaSeleccion(view: View) {
        // OBTENER FECHA ACTUAL
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val mes = calendar.get(Calendar.MONTH)
        val dia = calendar.get(Calendar.DAY_OF_MONTH)

        // DESDE
        if (view == fechaDesde) {
            val datePickerDialog = DatePickerDialog(this, { _, i, i1, i2 ->
                calendar.set(i, i1, i2)
                val selectedDate = calendar.time
                fDesde = dateFormat.format(selectedDate)
                fechaDesde.text = dateFinalFormat.format(selectedDate)
            }, year, mes, dia)
            datePickerDialog.show()
            // HASTA
        } else if (view == fechaHasta) {
            val datePickerDialog = DatePickerDialog(this, { _, i, i1, i2 ->
                calendar.set(i, i1, i2)
                val selectedDate = calendar.time
                fHasta = dateFormat.format(selectedDate)
                calendar.add(Calendar.DATE, 1)
                fechaHasta.text = dateFinalFormat.format(selectedDate)
            }, year, mes, dia)
            datePickerDialog.show()
        }
    }

    private fun obtenerDatos(fechas: ArrayList<String>, produccion: ArrayList<String>) {
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

    private fun confGrafica(lineChart: LineChart, fechas: ArrayList<String>, produccion: ArrayList<String>) {
        val xAxis = lineChart.xAxis
        obtenerDatos(fechas, produccion)

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
        lineChart.setTouchEnabled(false)
        lineChart.invalidate()
    }

    fun hallarDatos() {
        val produccion = ArrayList<String>()
        val fechas = ArrayList<String>()
        // OBJETOS DE BASE DATOS
        val mydb = SQLiteHelperKotlin(this)
        val db = mydb.readableDatabase
        try {
            val selectionArgs = arrayOf(tipoFruta, fDesde.replace("/", "-"),
                fHasta.replace("/", "-"))
            println("$fDesde Hasta $fHasta")
            if (tipoFruta.isNotEmpty() && fDesde.isNotEmpty() && fHasta.isNotEmpty()) {
                val consulta = "select fecha, cantidad_parcela from detecciones WHERE fruto = ? " +
                        "AND fecha BETWEEN ? AND ? ORDER BY fecha DESC"
                val cursor = db.rawQuery(consulta, selectionArgs)
                if (cursor.moveToFirst()) {
                    do {
                        val fecha = cursor.getString(0)
                        val prod = cursor.getString(1)
                        fechas.add(fecha)
                        produccion.add(prod)
                    } while (cursor.moveToNext())
                    // CARGAR DATOS A LA TABLA
                    cargaTablas(tablaDatos, fechas, produccion)
                    // CARGAR DATOS A LA GRAFICA
                    confGrafica(lineChart, fechas, produccion)
                } else {
                    Toast.makeText(this, "No se hallaron estimaciones",
                        Toast.LENGTH_SHORT).show()
                }
                cursor.close()
            } else {
                Toast.makeText(this, "Existen campos sin completar",
                    Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db?.close()
        }
    }
}