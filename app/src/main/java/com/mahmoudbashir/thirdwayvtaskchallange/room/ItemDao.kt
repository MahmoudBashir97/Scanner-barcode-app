package com.mahmoudbashir.thirdwayvtaskchallange.room

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import io.reactivex.Observable

@Dao
interface ItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewItem(itemModel: ItemModel)

    @Query("SELECT * FROM items_table ORDER BY item_expire_date Asc")
    fun getAllItems():LiveData<List<ItemModel>>

    @Query("SELECT * FROM items_table ORDER BY item_expire_date Asc")
    fun getAllStoredItems():Observable<List<ItemModel>>

    @Update
    suspend fun updateItem(item:ItemModel)

    @Delete
    suspend fun deleteSingleItem(itemModel: ItemModel)

}