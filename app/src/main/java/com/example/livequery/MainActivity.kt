package com.example.livequery

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.parse.Parse
import com.parse.ParseCloud
import com.parse.ParseObject
import com.parse.FunctionCallback
import com.parse.ParseException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Parse.initialize(
            Parse.Configuration.Builder(this)
                .applicationId("tX9ZQExQLeKvRuLm6pDCZs6BGKYQ8Gndl7pWAfbr")
                .clientKey("ldEUsizlTZgbj6P5pEYXJ8e11iN44E3cRemmn80R")
                .server("https://parseapi.back4app.com/")
                .build()
        )

        setContent {
            MaterialTheme {
                CloudCodeDemoScreen()
            }
        }
    }
}

@Composable
fun CloudCodeDemoScreen() {

    var messages by remember { mutableStateOf(listOf<String>()) }
    var loading by remember { mutableStateOf(false) }

    /** FUNCIÓN QUE LLAMA A CLOUD CODE **/
    fun fetchMessages() {
        loading = true

        ParseCloud.callFunctionInBackground(
            "obtenerMensajes",
            hashMapOf<String, Any>(),
            object : FunctionCallback<List<Any>> {
                override fun done(result: List<Any>?, e: ParseException?) {

                    loading = false

                    if (e != null) {
                        Log.e("CLOUD", "Error al obtener mensajes: ${e.localizedMessage}")
                        return
                    }

                    Log.d("CLOUD", "Resultado: $result")

                    if (result != null) {
                        messages = result.map { item ->

                            val map = item as? Map<*, *> ?: emptyMap<String, Any>()

                            val texto = map["texto"] as? String ?: "(sin texto)"
                            val fecha = map["createdAt"]?.toString() ?: "(sin fecha)"

                            "📨 $texto\n🕒 $fecha"
                        }
                    }
                }
            }
        )
    }

    /** LLAMAR UNA SOLA VEZ **/
    LaunchedEffect(Unit) {
        fetchMessages()
    }

    /** UI **/
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "☁️ Cloud Code – Back4App",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        /** LISTA DE MENSAJES **/
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (loading) {
            CircularProgressIndicator()
        }

        /** BOTÓN: AGREGAR MENSAJE **/
        Button(
            onClick = {
                val msg = ParseObject("Mensajes")
                msg.put("texto", "Mensaje ${System.currentTimeMillis()}")
                msg.saveInBackground {
                    fetchMessages()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar mensaje")
        }

        /** BOTÓN: REFRESCAR **/
        Button(
            onClick = { fetchMessages() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Refrescar mensajes")
        }
    }
}






