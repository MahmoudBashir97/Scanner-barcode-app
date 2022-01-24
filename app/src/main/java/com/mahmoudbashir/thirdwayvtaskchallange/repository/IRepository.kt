package com.mahmoudbashir.thirdwayvtaskchallange.repository

import androidx.lifecycle.LiveData
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import retrofit2.Response

interface IRepository {

    suspend fun insertNewItem(itemModel: ItemModel)

    fun getAllItems():LiveData<List<ItemModel>>
}