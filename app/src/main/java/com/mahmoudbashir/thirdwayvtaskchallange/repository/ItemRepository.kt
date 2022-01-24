package com.mahmoudbashir.thirdwayvtaskchallange.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.mahmoudbashir.thirdwayvtaskchallange.application
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import com.mahmoudbashir.thirdwayvtaskchallange.room.ItemsDatabase
import io.reactivex.Observable
import retrofit2.Response

class ItemRepository(private val db:ItemsDatabase, val app :application): IRepository {

    override suspend fun insertNewItem(itemModel: ItemModel)= db.dao().insertNewItem(itemModel)

    override fun getAllItems(): LiveData<List<ItemModel>>  = db.dao().getAllItems()

    fun getAllStoredItems(): Observable<List<ItemModel>>  = db.dao().getAllStoredItems()

    suspend fun updateItem(itemModel: ItemModel) = db.dao().updateItem(itemModel)
}