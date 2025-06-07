package com.example.imagepicker

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test

class ImagePickerLogicTest {

    @Test
    fun `test image selection and deselection`() {
        // This test directly manipulates a state holder similar to how the Composable would.
        // It's a way to unit test the selection logic itself.
        val selectedImagesState = mutableStateOf<Set<Uri>>(emptySet())

        val testUri1 = Uri.parse("content://media/external/images/media/1")
        val testUri2 = Uri.parse("content://media/external/images/media/2")

        // Simulate selecting image 1
        selectedImagesState.value = selectedImagesState.value + testUri1
        assertTrue("testUri1 should be selected", selectedImagesState.value.contains(testUri1))
        assertEquals("Size of selected set should be 1", 1, selectedImagesState.value.size)

        // Simulate selecting image 2
        selectedImagesState.value = selectedImagesState.value + testUri2
        assertTrue("testUri2 should be selected", selectedImagesState.value.contains(testUri2))
        assertEquals("Size of selected set should be 2", 2, selectedImagesState.value.size)

        // Simulate selecting image 1 again (should have no effect as it's a Set)
        selectedImagesState.value = selectedImagesState.value + testUri1
        assertEquals("Size of selected set should still be 2", 2, selectedImagesState.value.size)

        // Simulate deselecting image 1
        selectedImagesState.value = selectedImagesState.value - testUri1
        assertFalse("testUri1 should be deselected", selectedImagesState.value.contains(testUri1))
        assertTrue("testUri2 should still be selected", selectedImagesState.value.contains(testUri2))
        assertEquals("Size of selected set should be 1", 1, selectedImagesState.value.size)

        // Simulate deselecting image 2
        selectedImagesState.value = selectedImagesState.value - testUri2
        assertFalse("testUri2 should be deselected", selectedImagesState.value.contains(testUri2))
        assertTrue("Selected set should be empty", selectedImagesState.value.isEmpty())
    }

    @Test
    fun `onImagesSelected callback provides correct models`() {
        // Mock the necessary data and callback
        val testUri1 = Uri.parse("content://media/external/images/media/1")
        val testUri2 = Uri.parse("content://media/external/images/media/2")
        val testUri3 = Uri.parse("content://media/external/images/media/3")

        val allImageModels = listOf(
            ImageModel(testUri1),
            ImageModel(testUri2),
            ImageModel(testUri3)
        )
        val selectedUris = setOf(testUri1, testUri3)

        // This lambda will be our mock callback
        val onImagesSelectedCallback: (List<ImageModel>) -> Unit = mockk(relaxed = True)

        // Logic that would be in the "Done" button's onClick
        val resultModels = allImageModels.filter { selectedUris.contains(it.uri) }
        onImagesSelectedCallback(resultModels)

        // Verify the callback was called with the correct list of ImageModels
        val expectedSelectedModels = listOf(ImageModel(testUri1), ImageModel(testUri3))
        verify { onImagesSelectedCallback(expectedSelectedModels) }
    }

     @Test
    fun `initial state of selectedImages is empty`() {
        val selectedImagesState = mutableStateOf<Set<Uri>>(emptySet())
        assertTrue("Initially, selectedImages set should be empty", selectedImagesState.value.isEmpty())
    }
}
