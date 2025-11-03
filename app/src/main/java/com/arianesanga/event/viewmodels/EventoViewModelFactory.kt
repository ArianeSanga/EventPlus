package com.arianesanga.event.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.repository.EventoRepository

class EventoViewModelFactory(private val repository: EventoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}