package com.arianesanga.event.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class EventoViewModelFactory(private val repository: EventoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}