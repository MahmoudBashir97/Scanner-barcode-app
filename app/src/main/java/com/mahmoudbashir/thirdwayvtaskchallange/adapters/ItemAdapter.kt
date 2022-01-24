package com.mahmoudbashir.thirdwayvtaskchallange.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mahmoudbashir.thirdwayvtaskchallange.R
import com.mahmoudbashir.thirdwayvtaskchallange.model.ItemModel
import kotlinx.android.synthetic.main.single_item_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {


     fun updateList(){
        notifyDataSetChanged()
    }

    private val differCallback = object : DiffUtil.ItemCallback<ItemModel>(){
        override fun areItemsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
            return oldItem.itemType == newItem.itemType
        }

        override fun areContentsTheSame(oldItem: ItemModel, newItem: ItemModel): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,differCallback)

    class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.single_item_layout,parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = differ.currentList[position]

        holder.itemView.apply {

            txt_item_name.text = item.itemName
            txt_item_type.text = item.itemType
            txt_item_expire_date.text = item.itemExpiryDate.split("G")[0]


            when(item.itemType){
                "Food" -> {
                    Glide.with(this).load(R.drawable.food).into(img_item)
                }
                "Drink" ->{
                    Glide.with(this).load(R.drawable.pepsi_png).into(img_item)
                }
                "Chipse" ->{
                    Glide.with(this).load(R.drawable.chipsy).into(img_item)
                }
                "Electronic" ->{
                    Glide.with(this).load(R.drawable.electronics).into(img_item)
                }
                else -> {
                    Glide.with(this).load(R.drawable.food).into(img_item)
                }
            }

        }
    }

    override fun getItemCount(): Int = differ.currentList.size

}