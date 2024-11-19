package com.example.androidfullproject

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.androidfullproject.ListItem.User
import com.example.androidfullproject.ListItem.Food
import com.example.androidfullproject.ListItem.Quote

@Database(entities = [User::class, Food::class, Quote::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun foodDao(): FoodDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
        fun destroyInstance() {
            INSTANCE?.let { db ->
                Thread {
                    db.clearAllTables()
                    INSTANCE = null
                }.start()
            }
        }
    }
}
