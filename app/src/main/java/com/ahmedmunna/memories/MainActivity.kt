package com.ahmedmunna.memories

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ahmedmunna.memories.ui.GalleryViewModel
import com.ahmedmunna.memories.ui.MainScreen
import com.ahmedmunna.memories.ui.screens.SplashScreen
import com.ahmedmunna.memories.ui.theme.MemoriesTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: GalleryViewModel by viewModels()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            viewModel.setPermission(hasMediaPermission())
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.setPermission(hasMediaPermission())

        setContent {
            val ui by viewModel.ui.collectAsStateWithLifecycle()
            var splashDone by rememberSaveable { mutableStateOf(false) }

            MemoriesTheme(ui.settings.themeMode, ui.settings.dynamicColor) {
                Box(Modifier.fillMaxSize()) {
                    // The gallery starts loading underneath the splash so it is ready when the splash fades.
                    MainScreen(
                        vm = viewModel,
                        onRequestPermission = { permissionLauncher.launch(requiredPermissions()) },
                    )
                    AnimatedVisibility(visible = !splashDone, exit = fadeOut()) {
                        SplashScreen(onFinished = { splashDone = true })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // The user may have changed the permission in system settings.
        viewModel.setPermission(hasMediaPermission())
    }

    private fun hasMediaPermission(): Boolean {
        val needed = if (Build.VERSION.SDK_INT >= 33) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return needed.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    }

    private fun requiredPermissions(): Array<String> {
        val list = if (Build.VERSION.SDK_INT >= 33) {
            mutableListOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        list += Manifest.permission.ACCESS_MEDIA_LOCATION
        return list.toTypedArray()
    }
}
