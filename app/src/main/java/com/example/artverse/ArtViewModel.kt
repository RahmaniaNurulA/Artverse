package com.example.artverse

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ArtViewModel : ViewModel() {
    var artObjects by mutableStateOf<List<ArtObject>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun loadArtObjects(context: Context) {
        if (artObjects.isNotEmpty()) return // Jangan load ulang jika sudah ada data

        viewModelScope.launch {
            isLoading = true
            artObjects = fetchArtObjects(context)
            isLoading = false
        }
    }
}