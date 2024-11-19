package com.example.androidfullproject

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidfullproject.ListItem.User
import com.example.androidfullproject.ListItem.Food
import com.example.androidfullproject.ListItem.Quote

class MultiTypeAdapter(
    private val itemList: MutableList<ListItem>,
    private val onEditClick: (ListItem) -> Unit,
    private val onDeleteClick: (ListItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_FOOD = 0
        const val TYPE_USER = 1
        const val TYPE_QUOTE = 2
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindButtons(item: ListItem, onDelete: (ListItem) -> Unit, onEdit: (ListItem) -> Unit) {
            itemView.findViewById<Button>(R.id.deleteButton).setOnClickListener { onDelete(item) }
            itemView.findViewById<Button>(R.id.editButton).setOnClickListener { onEdit(item) }
        }
    }

    class FoodViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val foodImage: ImageView = itemView.findViewById(R.id.foodImage)
        val foodName: TextView = itemView.findViewById(R.id.foodName)
        val foodCalories: TextView = itemView.findViewById(R.id.foodCalories)
    }

    class UserViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.userName)
        val userAge: TextView = itemView.findViewById(R.id.userAge)
    }

    class QuoteViewHolder(itemView: View) : BaseViewHolder(itemView) {
        val quoteText: TextView = itemView.findViewById(R.id.quoteText)
        val quoteAuthor: TextView = itemView.findViewById(R.id.quoteAuthor)
    }

    override fun getItemViewType(position: Int): Int {
        return when (itemList[position]) {
            is Food -> TYPE_FOOD
            is User -> TYPE_USER
            is Quote -> TYPE_QUOTE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_FOOD -> FoodViewHolder(inflater.inflate(R.layout.food_item, parent, false))
            TYPE_USER -> UserViewHolder(inflater.inflate(R.layout.user_item, parent, false))
            TYPE_QUOTE -> QuoteViewHolder(inflater.inflate(R.layout.quote_item, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = itemList[position]
        when (holder) {
            is FoodViewHolder -> {
                val food = item as Food
                holder.foodName.text = food.name
                holder.foodCalories.text = "${food.calories} ккал"
                val bitmap = BitmapFactory.decodeFile(food.image)
                holder.foodImage.setImageBitmap(bitmap)
                holder.bindButtons(food, { removeItem(position); onDeleteClick(it) }, onEditClick)
            }
            is UserViewHolder -> {
                val user = item as User
                holder.userName.text = user.name
                holder.userAge.text = user.age.toString()
                holder.bindButtons(user, { removeItem(position); onDeleteClick(it) }, onEditClick)
            }
            is QuoteViewHolder -> {
                val quote = item as Quote
                holder.quoteText.text = quote.text
                holder.quoteAuthor.text = quote.author
                holder.bindButtons(quote, { removeItem(position); onDeleteClick(it) }, onEditClick)
            }
        }
    }

    fun removeItem(position: Int) {
        if (position in itemList.indices) {
            itemList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun getItemCount(): Int = itemList.size
}
