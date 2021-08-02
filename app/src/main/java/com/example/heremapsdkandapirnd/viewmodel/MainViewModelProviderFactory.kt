package com.example.heremapsdkandapirnd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.heremapsdkandapirnd.repository.RouteRepository

class MainViewModelProviderFactory(
    val routeRepository: RouteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(routeRepository) as T
    }
}