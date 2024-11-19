package com.example.androidfullproject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.androidfullproject.ListItem.Food

@Dao
interface FoodDao {
    @Insert
    suspend fun insertFood(food: Food)

    @Query("SELECT * FROM food")
    suspend fun getAllFood(): List<Food>

    @Delete
    suspend fun deleteFood(food: Food)

    @Query("SELECT * FROM food WHERE id = :foodId")
    suspend fun getFoodById(foodId: Long): Food

    @Update
    suspend fun updateFood(food: Food)
}
