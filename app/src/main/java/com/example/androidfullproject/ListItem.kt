package com.example.androidfullproject

import androidx.room.Entity
import androidx.room.PrimaryKey

sealed class ListItem {
    @Entity(tableName = "users")
    data class User(
        @PrimaryKey(autoGenerate = true) val id: Long=0,
        val name: String,
        val age: Int
    ) : ListItem()
    @Entity(tableName = "food")
    data class Food(
        @PrimaryKey(autoGenerate = true) val id: Long=0,
        val name: String,
        val calories: Int,
        val image: String
    ) : ListItem()
    @Entity(tableName = "quotes")
    data class Quote(
        @PrimaryKey(autoGenerate = true) val id: Long=0,
        val text: String,
        val author: String
    ): ListItem()
}