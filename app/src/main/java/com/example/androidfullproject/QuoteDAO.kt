package com.example.androidfullproject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.androidfullproject.ListItem.Quote

@Dao
interface QuoteDao {
    @Insert
    @Transaction
    suspend fun insertQuote(quote: Quote)

    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotes(): List<Quote>

    @Query("DELETE FROM quotes")
    suspend fun deleteAll()

    @Delete
    suspend fun deleteQuote(quote: Quote)

    @Query("SELECT * FROM quotes WHERE id = :quoteId")
    suspend fun getQuoteById(quoteId: Long): Quote

    @Update
    suspend fun updateQuote(quote: Quote)
}
