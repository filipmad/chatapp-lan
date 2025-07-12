import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import androidx.compose.runtime.*
import androidx.compose.material.*
import androidx.compose.ui.window.*


// This adds Column, TextField, and Button
import androidx.compose.foundation.layout.*





@Composable
fun App(sendMessage: suspend (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Message") }
        )
        Button(onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                sendMessage(text)
            }
        }) {
            Text("Send")
        }
    }
}

fun main() = application {
    val client = HttpClient(CIO) {
        install(WebSockets)
    }

    var session: DefaultClientWebSocketSession? = null

    CoroutineScope(Dispatchers.IO).launch {
        client.webSocket("ws://192.168.1.100:8080/ws") {
            session = this
            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        println("Received: ${frame.readText()}")
                    }
                }
            } catch (e: Exception) {
                println("Connection closed: ${e.message}")
            }
        }
    }

    Window(onCloseRequest = ::exitApplication, title = "WebSocket Client") {
        App(sendMessage = { message ->
            session?.send(message)
        })
    }
}
