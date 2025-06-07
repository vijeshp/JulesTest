// Updated MainActivity for ImagePicker demonstration
package com.example.jules

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.imagepicker.ImagePicker // Import from the library
import com.example.imagepicker.ImageModel // Import from the library
import com.example.jules.ui.theme.JulesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Keep if it was there
        setContent {
            JulesTheme {
                Surface( // Use Surface as a general container
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ImagePickerSampleScreen()
                }
            }
        }
    }
}

@Composable
fun ImagePickerSampleScreen(modifier: Modifier = Modifier) {
    var showImagePicker by remember { mutableStateOf(false) }
    var selectedImagesFromPicker by remember { mutableStateOf<List<ImageModel>>(emptyList()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), // Padding for the content
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Center content vertically
    ) {
        Button(onClick = { showImagePicker = true }) {
            Text("Open Image Picker")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedImagesFromPicker.isNotEmpty()) {
            Text("Selected Images (${selectedImagesFromPicker.size}):", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(selectedImagesFromPicker) { imageModel ->
                    AsyncImage(
                        model = imageModel.uri,
                        contentDescription = "Selected Image",
                        modifier = Modifier.size(100.dp) // Fixed size for previews
                    )
                }
            }
        } else {
            Text("No images selected yet.", style = MaterialTheme.typography.bodyMedium)
        }
    }

    // This part handles the ImagePicker overlay
    if (showImagePicker) {
        // The ImagePicker will be composed on top of the current screen content
        // It should handle its own background and full-screen presentation if needed.
        ImagePicker(
            onImagesSelected = { images ->
                selectedImagesFromPicker = images
                showImagePicker = false // Hide picker after selection
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() { // Renamed preview function to avoid conflict if GreetingPreview existed
    JulesTheme {
        ImagePickerSampleScreen()
    }
}