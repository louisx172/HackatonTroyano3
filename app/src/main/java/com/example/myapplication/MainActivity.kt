package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.gson.Gson
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException

// Clases de datos para parsear el JSON
data class MiJson(
    val timestamp: String,
    val dato: Dato
)

data class Dato(
    val tipo: String,
    val contenido: Contenido
)

data class Contenido(
    val dato1: String,
    val dato2: String,
    val dato3: String
)

// Función para leer el JSON desde assets
fun cargarJsonDesdeAssets(context: Context, fileName: String): String {
    return try {
        context.assets.open(fileName).bufferedReader().use { it.readText() }
    } catch (e: IOException) {
        e.printStackTrace()
        ""
    }
}

// Función para parsear el JSON a un objeto MiJson usando Gson
fun parsearJson(context: Context): MiJson? {
    val jsonString = cargarJsonDesdeAssets(context, "datos.json")
    return Gson().fromJson(jsonString, MiJson::class.java)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )

        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Scaffold() { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { /* Acción del botón */ }) {
                Text("Haz clic aquí")
            }

            // Mostrar los datos del JSON
            JsonDataScreen()

            Spacer(modifier = Modifier.height(56.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp), // Mantiene el mapa con tamaño fijo
                contentAlignment = Alignment.Center
            ) {
                OSMMapScreen()
            }
        }
    }
}

@Composable
fun JsonDataScreen() {
    val context = LocalContext.current
    // Se lee y parsea el JSON al iniciar la composición
    val datos = remember { parsearJson(context) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Timestamp: ${datos?.timestamp ?: "No disponible"}")
        Spacer(modifier = Modifier.height(4.dp))
        Text("Tipo: ${datos?.dato?.tipo ?: "No disponible"}")
        Spacer(modifier = Modifier.height(4.dp))
        Text("Dato 1: ${datos?.dato?.contenido?.dato1 ?: "No disponible"}")
        Text("Dato 2: ${datos?.dato?.contenido?.dato2 ?: "No disponible"}")
        Text("Dato 3: ${datos?.dato?.contenido?.dato3 ?: "No disponible"}")
    }
}

@Composable
fun OSMMapScreen() {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(10.0)

                val startPoint = GeoPoint(19.432608, -99.133209)
                controller.setCenter(startPoint)

                val marker = Marker(this)
                marker.position = startPoint
                marker.title = "Ciudad de México"
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                overlays.add(marker)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        MainScreen()
    }
}
