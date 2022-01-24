package com.mahmoudbashir.thirdwayvtaskchallange.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import com.mahmoudbashir.thirdwayvtaskchallange.repository.ItemRepository
import kotlinx.coroutines.launch

class ItemViewModel(val repo :ItemRepository,val app:Application): AndroidViewModel(app) {


    fun insertNewItem(item: ItemModel) = viewModelScope.launch {
        repo.insertNewItem(item)
    }

    fun getAllStoredItems():LiveData<List<ItemModel>>{
        return repo.getAllItems()
    }

}