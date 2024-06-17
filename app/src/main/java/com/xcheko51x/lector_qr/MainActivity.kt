package com.xcheko51x.lector_qr

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.xcheko51x.lector_qr.ui.theme.Lector_QRTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InicioScreen()
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun InicioScreen() {
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    )

    LaunchedEffect(key1 = Unit) {
        permissions.launchMultiplePermissionRequest()
    }

    var comentarios by remember { mutableStateOf("") }
    var resultadoScan by remember { mutableStateOf("") }
    var buttonColor by remember { mutableStateOf(R.color.purple_500) }


    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            val aux = result.contents ?: ""
            resultadoScan = aux.replace(";", "\n")
        }
    )

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp, 8.dp)
                    .align(Alignment.CenterHorizontally),
                value = resultadoScan,
                onValueChange = { },
                label = {
                    Text(
                        text = "RESULTADO ESCANER",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 12.sp
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp),
                minLines = 5,
                readOnly = true
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp, 8.dp)
                    .align(Alignment.CenterHorizontally),
                value = comentarios,
                onValueChange = {
                    comentarios = it
                },
                label = {
                    Text(
                        text = "COMENTARIOS",
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontSize = 12.sp
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp),
                minLines = 5
            )
        }

        Column(
            modifier = Modifier
                .height(50.dp)
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    if (resultadoScan.isNotEmpty()) {
                        guardarInfo(
                            info = resultadoScan,
                            comentarios = comentarios
                        )
                        resultadoScan = ""
                        comentarios = ""
                    } else {
                        val scanOptions = ScanOptions()
                        scanOptions.setBeepEnabled(true)
                        scanOptions.setCaptureActivity(CaptureActivityPortrait::class.java)
                        scanOptions.setOrientationLocked(false)
                        scanLauncher.launch(scanOptions)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                if (resultadoScan.isNotEmpty()) {
                    buttonColor = R.color.teal_700
                    Text(text = "Guardar")
                } else {
                    buttonColor = R.color.purple_700
                    Text(text = "Escanear")
                }
            }
        }
    }
}

fun guardarInfo(
    info: String,
    comentarios: String
) {
    val data = "${info.replace("\n", ",")},${comentarios.replace("\n", " ")}\n"

    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

    val folder = File(path, Constantes.NOM_FOLDER)

    if (!folder.exists()) {
        folder.mkdirs()
    }

    val csvFile = File(folder, Constantes.NOM_FILE)
    if (!csvFile.exists()) {
        csvFile.createNewFile()

        val header = "NOMBRE,EMPRESA,CORREO,TELEFONO,COMENTARIOS\n"
        FileOutputStream(csvFile, true).use { fos ->
            fos.bufferedWriter().use {
                it.append(header)
            }
        }
    }

    FileOutputStream(csvFile, true).use { fos ->
        fos.bufferedWriter().use {
            it.append(data)
        }
    }


}