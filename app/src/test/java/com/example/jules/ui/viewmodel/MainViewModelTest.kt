package com.example.jules.ui.viewmodel

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MainViewModelTest {

    @Test
    fun `test MainViewModel instantiation`() {
        val viewModel = MainViewModel()
        assertNotNull(viewModel)
    }
}
