package com.mahmoudbashir.thirdwayvtaskchallange.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "items_table")
data class ItemModel(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id:Int,
    @ColumnInfo(name = "item_code")
    val itemCode: String,
    @ColumnInfo(name = "item_name")
    val itemName:String,
    @ColumnInfo(name = "item_type")
    val itemType:String,
    @ColumnInfo(name = "item_expire_date")
    val itemExpiryDate:String,
    @ColumnInfo(name = "item_expire_time_stamp")
    val itemExpiredTimeStamp:Long,
    @ColumnInfo(name = "is_expired")
   val isExpired:Boolean=false
)
