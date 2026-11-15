package com.ahmedmunna.memories.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ahmedmunna.memories.ui.components.MemoriesLogo

private val LICENSES = listOf(
    "AndroidX (Core, Activity, Lifecycle, DataStore, ExifInterface)" to "Apache License 2.0",
    "Jetpack Compose and Material 3" to "Apache License 2.0",
    "Coil image loading" to "Apache License 2.0",
    "AndroidX Media3 (ExoPlayer)" to "Apache License 2.0",
    "Dagger Hilt" to "Apache License 2.0",
    "Kotlin and kotlinx.coroutines" to "Apache License 2.0",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "—"
    }
    var showPrivacy by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))
            MemoriesLogo(96.dp)
            Spacer(Modifier.height(16.dp))
            Text("Memories", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text("Your Photos, Your Memories.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AboutRow("Developer", "Ahmed Munna")
                AboutRow("Version", version)
                AboutRow("Package", context.packageName)
                AboutRow("Copyright", "© 2026 Ahmed Munna. All rights reserved.")
            }

            TextButton(onClick = { showPrivacy = true }, modifier = Modifier.padding(top = 16.dp)) { Text("Privacy") }

            Text(
                "Open-source licenses",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            )
            LICENSES.forEach { (name, license) ->
                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(name, style = MaterialTheme.typography.bodyMedium)
                    Text(license, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showPrivacy) {
        AlertDialog(
            onDismissRequest = { showPrivacy = false },
            confirmButton = { TextButton(onClick = { showPrivacy = false }) { Text("Close") } },
            title = { Text("Privacy") },
            text = {
                Text(
                    "This version of Memories reads photos and videos on your device to display them. " +
                        "It does not use the internet, has no account, and sends no data anywhere. " +
                        "A hosted privacy policy will be linked here before cloud features are released.",
                    textAlign = TextAlign.Start,
                )
            },
        )
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
