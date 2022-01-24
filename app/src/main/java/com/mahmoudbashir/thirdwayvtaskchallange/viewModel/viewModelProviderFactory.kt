package com.mahmoudbashir.thirdwayvtaskchallange.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mahmoudbashir.thirdwayvtaskchallange.repository.ItemRepository

class viewModelProviderFactory(
    private val app: Application,
    private val repos: ItemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ItemViewModel( repos,app) as T
    }
}