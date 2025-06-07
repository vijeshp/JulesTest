package com.example.imagepicker

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ImagePicker(onImagesSelected: (List<ImageModel>) -> Unit) {
    val context = LocalContext.current
    val images = remember { mutableStateOf<List<ImageModel>>(emptyList()) }
    val selectedImages = remember { mutableStateOf<Set<Uri>>(emptySet()) }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        )
    }

    // To track if the user has denied permission at least once (and thus we should show rationale)
    // This is a simplification; a more robust solution might use LocalInspectionMode.current
    // or check shouldShowRequestPermissionRationale which is not directly available in Composable side-effect.
    var permissionRequestedPreviously by remember { mutableStateOf(false) }
    var showSettingsButton by remember { mutableStateOf(false) }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
        if (!isGranted) {
            // If denied, and requested previously, it might be permanently denied.
            // This logic is a bit simplified. A real app might use Activity.shouldShowRequestPermissionRationale
            // or a more complex state to determine if it's "permanently denied".
            if (permissionRequestedPreviously) {
                 showSettingsButton = true
            }
            permissionRequestedPreviously = true
        } else {
            // Reset flags if permission is granted
            permissionRequestedPreviously = false
            showSettingsButton = false
        }
    }

    // Handle lifecycle events to re-check permission when app comes to foreground
    // This is important if the user grants permission from settings
     LocalLifecycleOwner.current.lifecycle.addObserver(remember {
        LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val newPermissionStatus = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
                if (newPermissionStatus != permissionGranted) {
                    permissionGranted = newPermissionStatus
                    if (newPermissionStatus) {
                        // If permission granted from settings, hide the settings button and reset flags
                        showSettingsButton = false
                        permissionRequestedPreviously = false
                    }
                }
            }
        }
    })


    LaunchedEffect(permissionGranted) { // React to changes in permissionGranted
        if (permissionGranted) {
            withContext(Dispatchers.IO) {
                val projection = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME // Though not used, good to have
                )
                val cursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${MediaStore.Images.Media.DATE_ADDED} DESC"
                )
                val imageList = mutableListOf<ImageModel>()
                cursor?.use {
                    val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    while (it.moveToNext()) {
                        val id = it.getLong(idColumn)
                        val contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        )
                        imageList.add(ImageModel(contentUri))
                    }
                }
                images.value = imageList
            }
        } else {
            images.value = emptyList() // Clear images if permission is revoked
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (permissionGranted) {
            if (images.value.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No images found or an error occurred.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(images.value, key = { it.uri }) { image ->
                        val isSelected = selectedImages.value.contains(image.uri)
                        Image(
                            painter = rememberAsyncImagePainter(model = image.uri),
                            contentDescription = "Image",
                            modifier = Modifier
                                .aspectRatio(1f)
                                .border(
                                    width = if (isSelected) 4.dp else 0.dp,
                                    color = if (isSelected) Color.Blue else Color.Transparent
                                )
                                .clickable {
                                    val currentSelected = selectedImages.value.toMutableSet()
                                    if (isSelected) {
                                        currentSelected.remove(image.uri)
                                    } else {
                                        currentSelected.add(image.uri)
                                    }
                                    selectedImages.value = currentSelected
                                },
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        } else {
            // Permission not granted UI
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Storage permission is required to load images.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (showSettingsButton) {
                        Text(
                            "Permission has been denied. Please enable it in app settings.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.fromParts("package", context.packageName, null)
                            context.startActivity(intent)
                        }) {
                            Text("Open Settings")
                        }
                    } else {
                        Button(onClick = {
                             permissionRequestedPreviously = true // Mark that we've now requested it
                            permissionLauncher.launch(permission)
                        }) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val result = images.value.filter { selectedImages.value.contains(it.uri) }
                onImagesSelected(result)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = selectedImages.value.isNotEmpty() && permissionGranted
        ) {
            Text("Done (${selectedImages.value.size})")
        }
    }
}
