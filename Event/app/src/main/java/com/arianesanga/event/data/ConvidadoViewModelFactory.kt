package com.arianesanga.event.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ConvidadoViewModelFactory(private val repository: ConvidadoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConvidadoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConvidadoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
