package com.mahmoudbashir.thirdwayvtaskchallange.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel

@Database(entities = [ItemModel::class], exportSchema = false, version = 1)
abstract class ItemsDatabase:RoomDatabase() {

    abstract fun dao():ItemDao

    companion object{
        @Volatile
        private var instance:ItemsDatabase?=null
        private val LOCK = Any()
        operator fun invoke(context:Context) = instance?: synchronized(LOCK){
            instance?: createDatabase(context).also { instance=it }
        }
        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ItemsDatabase::class.java,
            "userList_db"
        ).build()
    }


}