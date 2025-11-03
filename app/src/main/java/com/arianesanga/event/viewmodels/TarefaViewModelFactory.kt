package com.arianesanga.event.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arianesanga.event.data.repository.TarefaRepository

class TarefaViewModelFactory(private val repository: TarefaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TarefaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TarefaViewModel(repository) as T // SEM CONTEXT
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
