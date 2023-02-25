package com.example.aplicacionrest

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.aplicacionrest.clases.Neko
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
    }

    private lateinit var context:Context
    private lateinit var button: Button
    private lateinit var guardar: Button
    private lateinit var imageView: ImageView
    private lateinit var artista: TextView
    private lateinit var cuenta: TextView
    private lateinit var source: TextView
    private lateinit var imageURL: String
    private var nombre = ""

    fun getImage(interfaz: NekoWaifuAPIInterface) {
        interfaz.obtenerNekos().enqueue(object : Callback<Neko> {
            override fun onResponse(call: Call<Neko>, response: Response<Neko>) {
                if (response.code() == 200) {
                    val cuerpo = response.body()
                    val artistName = cuerpo?.results?.get(0)?.artistName
                    val artistAccount = cuerpo?.results?.get(0)?.artistHref
                    val artsource = cuerpo?.results?.get(0)?.sourceUrl
                    Picasso.get().load(cuerpo?.results?.get(0)?.url).into(imageView)
                    if (cuerpo != null) {
                        imageURL = cuerpo.results[0].url.toString()
                        artista.text = artistName
                        cuenta.text = artistAccount
                        source.text = artsource
                    }
                }
                // 400, 300, resto de codigos.
            }

            override fun onFailure(call: Call<Neko>, t: Throwable) {
                // Gestionar la exepcion
                Toast.makeText(context, "Ocurrio un error al contactar el servidor.",Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun alerDialog() {
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle("GUardar PNG")
        alert.setMessage("Elegir compresion")
        val linear = LinearLayout(this)
        linear.orientation = LinearLayout.VERTICAL
        val text = TextView(this)
        text.setPadding(10, 10, 10, 10)
        val seek = SeekBar(this)
        val input = EditText(this)
        var compresion = 0
        linear.addView(text)
        linear.addView(seek)
        linear.addView(input)
        seek.progress = 100
        var progreso = "Compresion ${seek.progress}"
        text.text = progreso
        input.setRawInputType(Configuration.KEYBOARDHIDDEN_NO)
        alert.setView(linear)
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                progreso = "Compresion $progress"
                text.text = progreso
                compresion = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // you can probably leave this empty
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // you can probably leave this empty
            }
        })
        alert.setPositiveButton("Ok",
            DialogInterface.OnClickListener { dialog, whichButton ->
                nombre = input.text.toString()
                if  (nombre.isBlank() || nombre.isEmpty()) {
                    nombre = "newImage"
                }
                requestPermissions(compresion)
            })
        alert.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, whichButton ->})
        alert.show()
    }

    private fun intentGallery() {
        val root = Environment.getExternalStorageDirectory().absolutePath
        val directory = File("$root/NekoBestAPI")
        val imagePath = File(directory, "$nombre.png")
        val uri = FileProvider.getUriForFile(context,context.applicationContext.packageName + ".fileprovider",imagePath)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val resolveInfos = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val galleryOptions = Array<CharSequence>(resolveInfos.size) { "" }
        for (i in resolveInfos.indices) {
            galleryOptions[i] = resolveInfos[i].loadLabel(context.packageManager)
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Seleccionar aplicación de la galería")
        builder.setItems(galleryOptions) { _, item ->
            // Obtener la actividad seleccionada
            val resolveInfo = resolveInfos[item]

            // Crear un Intent explícito para abrir la imagen en la actividad seleccionada
            val explicitIntent = Intent(intent)
            explicitIntent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name)
            context.startActivity(explicitIntent)
        }
        builder.show()
    }

    private fun requestPermissions(compression: Int) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE)
        } else {
            // El permiso ya ha sido concedido.
            // Aquí es donde puedes guardar la imagen.
            val bitmap: Bitmap?
            try {
                bitmap = (imageView.drawable as BitmapDrawable).bitmap
                // Download Image from URL
                if (bitmap != null) {
                    val root = Environment.getExternalStorageDirectory().absolutePath
                    val directory = File("$root/NekoBestAPI")
                    val filePath: String
                    if (directory.exists()) {
                        directory.mkdir()
                    }
                    val file = File(directory, "$nombre.png")
                    filePath = file.canonicalPath
                    val output: OutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, compression, output)
                    output.flush()
                    output.close()
                    Toast.makeText(context, "Se guardo la imagen $nombre.png en la carpeta $filePath",Toast.LENGTH_SHORT).show()
                    println("file:/$filePath")
                    intentGallery()
                }
            } catch (e: Exception) {
                println(e.printStackTrace())
                Toast.makeText(context, "No se puede guardar la imagen porque todavia no se ha descargado.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun inten(url:String) {
        if (!url.isNullOrBlank()) {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.buttonNeko)
        guardar = findViewById(R.id.buttonGuardar)
        imageView = findViewById(R.id.imageViewNeko)
        artista = findViewById(R.id.nekoTextArtist)
        cuenta = findViewById(R.id.nekoTextCuenta)
        source = findViewById(R.id.nekoTextSource)
        context = this
        var retrofit = Retrofit.Builder().baseUrl("https://nekos.best/api/v2/")
            .addConverterFactory(GsonConverterFactory.create()).build()
        var interfaz: NekoWaifuAPIInterface = retrofit.create(NekoWaifuAPIInterface::class.java)
        getImage(interfaz)
        button.setOnClickListener() {
            getImage(interfaz)
        }
        guardar.setOnClickListener() {
            alerDialog()
        }
        cuenta.setOnClickListener {
            val url = cuenta.text
            inten(url.toString())
        }
        source.setOnClickListener {
            val url = source.text
            inten(url.toString())
        }
    }
}