package com.miraimx.selectagoapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.miraimx.selectagoapp.ObjectDetectorHelper.DetectorListener
import org.tensorflow.lite.task.vision.detector.Detection
import kotlinx.coroutines.*

class Deteccion : AppCompatActivity() {

    lateinit var siguiente_btn: Button
    lateinit var detectar_btn: Button
    lateinit var seleccionar_btn: Button
    lateinit var imagen: ImageView
    lateinit var labelArboles: TextView
    var bitmap: Bitmap? = null
    lateinit var objectDetectorHelper: ObjectDetectorHelper
    var contadorArbol: Int = 1
    lateinit var fruto: String
    var arboles: Int? = null
    var muestra: Int? = null
    var detecciones: Int = 0
    var promedio: Int? = null
    var estimacion: Int? = null
    var cantidad: Int? = null


    // Variables para almacenar la categoría y el puntaje del objeto detectado
    var imagenSeleccionada: Int = 1
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deteccion)

        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
            //Poner el ícono al ActionBar
            actionBar.setIcon(R.drawable.tfl_logo)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setBackgroundDrawable(ColorDrawable(Color.parseColor("#62aa00")))
        }

        permisoCamera()

        // Incializamos vistas
        siguiente_btn = findViewById(R.id.btnSiguiente)
        seleccionar_btn = findViewById(R.id.select_btn)
        detectar_btn = findViewById(R.id.btnDetectar)
        imagen = findViewById(R.id.imagen);
        labelArboles = findViewById(R.id.labelArboles)

        fruto = intent.getStringExtra("fruto")!!
        arboles = intent.getIntExtra("arboles", 0)
        muestra = intent.getIntExtra("muestra", 0)
        //val precison = intent.getFloatExtra("precision", 0F)

        labelArboles.setText("Árbol $contadorArbol/$muestra")


        siguiente_btn.isEnabled = false
        seleccionar_btn.isEnabled = false
        imagen.isEnabled = false
        detectar_btn.isEnabled = false

        val resourceId = resources.getIdentifier("limonespersas", "drawable", packageName)
        //imagen.setImageResource(resourceId)
        val rev = BitmapFactory.decodeResource(resources, R.drawable.limonespersas)
        //val bitmapOriginal = BitmapFactory.decodeResource(resources, R.drawable.limonespersas)
        bitmap = ajustarBitmap(rev, 512, 512)
        imagen.setImageBitmap(bitmap)
        val toast = Toast.makeText(applicationContext, "Cargando...", Toast.LENGTH_LONG)
        toast.show()
        GlobalScope.launch(Dispatchers.Default) {
            // Código a ejecutar en segundo plano
            val objectDetectorListener: DetectorListener = object : DetectorListener {
                override fun onError(error: String) {
                    // Manejar el error de detección
                    //println("Ocurrió un error en la detección")
                }
                override fun onResults(
                    results: MutableList<Detection>?,
                    inferenceTime: Long,
                    imageHeight: Int,
                    imageWidth: Int
                ) {
                    var categoria: String? = null
                    var boundBox: MutableList<RectF>? = ArrayList()
                    var puntaje: Float? = null
                    //var cantidad: Int? = null
                    // Manejar los resultados de detección
                    // Itera sobre los objetos Detection en la lista results
                    if (results != null) {
                        //Obtenemos el tamaño de la lista para saber la cantidad de frutos detectados
                        cantidad = results.size

                        for (result in results) {
                            // Obtiene la lista de categorías del objeto Detection
                            val categorias = result.categories

                            // Verifica si hay al menos una categoría en la lista
                            if (categorias.isNotEmpty()) {
                                // Accede a la primera (Y única) categoría en la lista
                                val primeraCategoria = categorias[0]

                                // Guarda la categoría y el puntaje en las variables correspondientes
                                categoria = primeraCategoria.label
                                puntaje = primeraCategoria.score
                                if (boundBox != null) {
                                    boundBox.add(result.boundingBox)
                                }
                                //puntajes.add(puntaje!!)
                                // Rompe el bucle si solo deseas obtener la categoría y el puntaje del primer objeto detectado
                                //break
                            }
                        }
                    }
                    //Muestra un toast con la cantidad de frutos detectados
                    Toast.makeText(
                        applicationContext,
                        "Frutos detectados: $cantidad",
                        Toast.LENGTH_SHORT
                    ).show()
                    /* Imprime la categoría y el puntaje almacenados
                    println("Resultados: $results")
                    println("Cantidad: $cantidad")
                    println("Categoría: $categoria")
                    println("Puntaje: $puntaje")*/
                    if (boundBox != null) {
                        dibujarCuadros(boundBox)
                    }
                }
            }

            objectDetectorHelper = ObjectDetectorHelper(
                threshold = 0.5f,
                numThreads = 4,
                maxResults = 30,
                currentDelegate = 0,
                currentModel = 0,
                context = this@Deteccion,
                objectDetectorListener
            )
            withContext(Dispatchers.Main) {
                // Código para actualizar la interfaz de usuario después de la tarea en segundo plano
                siguiente_btn.isEnabled = true
                seleccionar_btn.isEnabled = true
                imagen.isEnabled = true
                detectar_btn.isEnabled = true
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        kotlin.runCatching {
            objectDetectorHelper.clearObjectDetector()
            coroutineScope.cancel()
        }.getOrNull()
    }

    // ---------------------------------- METODOS DE CAMARA ----------------------------------------
    fun fnTomarFoto(view: View?) {
        imagenSeleccionada = 0
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, 11)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 11 && resultCode == RESULT_OK) {
            try {
                val capturedBitmap = data?.extras?.get("data") as Bitmap
                bitmap = ajustarBitmap(capturedBitmap, 512, 512)
                //bitmap = data?.extras?.get("data") as Bitmap
                imagen.setImageBitmap(bitmap)

                //Función del botón que analiza la imagen
                if (bitmap != null) {
                    objectDetectorHelper.detect(bitmap!!, 0)
                } else {
                    //println("El bitmap es nulo")
                    Toast.makeText(applicationContext, "No se ha tomado una fotografía", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                //println("Ocurrió un error al conseguir la imagen de la cámara")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun permisoCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.CAMERA
                    ), 10
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 11) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    permisoCamera()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    fun fnCambiarImagen(view: View) {
        if (imagenSeleccionada == 0) {
            imagenSeleccionada = 1
            val resourceId = resources.getIdentifier("limon11", "drawable", packageName)
            imagen.setImageResource(resourceId)
            val rev = BitmapFactory.decodeResource(resources, R.drawable.limon11)
            bitmap = ajustarBitmap(rev,512, 512)
        } else if (imagenSeleccionada == 1) {
            imagenSeleccionada = 2
            val resourceId = resources.getIdentifier("limon2", "drawable", packageName)
            imagen.setImageResource(resourceId)
            val rev = BitmapFactory.decodeResource(resources, R.drawable.limon2)
            bitmap = ajustarBitmap(rev,512, 512)
        } else if (imagenSeleccionada == 2) {
            imagenSeleccionada = 3
            val resourceId = resources.getIdentifier("limon7", "drawable", packageName)
            imagen.setImageResource(resourceId)
            val rev  = BitmapFactory.decodeResource(resources, R.drawable.limon7)
            bitmap = ajustarBitmap(rev,512, 512)
        } else if (imagenSeleccionada == 3) {
            imagenSeleccionada = 4
            val resourceId = resources.getIdentifier("limonespersas", "drawable", packageName)
            imagen.setImageResource(resourceId)
            val rev = BitmapFactory.decodeResource(resources, R.drawable.limonespersas)
            bitmap = ajustarBitmap(rev,512, 512)
        } else if (imagenSeleccionada == 4) {
            imagenSeleccionada = 0
            val resourceId = resources.getIdentifier("limon12", "drawable", packageName)
            imagen.setImageResource(resourceId)
            val rev  = BitmapFactory.decodeResource(resources, R.drawable.limon12)
            bitmap = ajustarBitmap(rev,512, 512)
        } else {
            Toast.makeText(
                applicationContext,
                "Ha ocurrido un error con la imagen",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun ajustarBitmap(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val scaleFactorX = targetWidth.toFloat() / bitmap.width
        val scaleFactorY = targetHeight.toFloat() / bitmap.height

        val scaledWidth = (bitmap.width * scaleFactorX).toInt()
        val scaledHeight = (bitmap.height * scaleFactorY).toInt()

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }

    fun dibujarCuadros(boundingBoxes: MutableList<RectF>) {
        // Crear un nuevo bitmap mutable para dibujar las cajas delimitadoras y etiquetas
        val resultBitmap: Bitmap? = bitmap?.copy(Bitmap.Config.ARGB_8888, true)
        // Obtener el objeto Canvas para dibujar en el bitmap
        val canvas = resultBitmap?.let { Canvas(it) }
        val paint = Paint()
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5.0f
        paint.textSize = 20.0f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (deteccion in boundingBoxes) {
            if (canvas != null) {
                canvas.drawRect(deteccion, paint)
            }
        }
        imagen.setImageBitmap(resultBitmap)
    }

    fun fnSiguiente(view: View) {

        if (cantidad!=null){
            //Botón para avanzar el contador
            detecciones += cantidad!!
            cantidad = null
            if (contadorArbol < muestra!!){
                contadorArbol++
                labelArboles.setText("Árbol $contadorArbol/$muestra")
            }else{
                calcularDatos()
            }
        }else{
            Toast.makeText(applicationContext, "No se ha tomado ninguna imagen", Toast.LENGTH_SHORT).show()
        }
    }

    fun calcularDatos(){
        val promedio: Int = (detecciones/ muestra!!).toInt()
        val estimacion: Int = (promedio*arboles!!)

        val intent: Intent = Intent(this, ResultadosDeteccion::class.java)
        intent.putExtra("fruto", fruto)
        intent.putExtra("arboles", arboles)
        intent.putExtra("muestra", muestra)
        intent.putExtra("detecciones", detecciones)
        intent.putExtra("promedio", promedio)
        intent.putExtra("estimacion", estimacion)
        startActivity(intent)
    }

    fun fnDetectar(view: View) {
        //Función del botón que analiza la imagen
        if (bitmap != null) {
            objectDetectorHelper.detect(bitmap!!, 0)
        } else {
            //println("El bitmap es nulo")
        }

    }

}

