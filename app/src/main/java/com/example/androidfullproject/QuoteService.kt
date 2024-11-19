package com.example.androidfullproject

import retrofit2.http.GET

data class QuoteResponse(
    val id: Int,
    val quote: String,
    val author: String,
    val length: Int,
    val tags: List<String>
)


interface QuoteService {
    @GET("/api/quotes/random")
    suspend fun getQuote(): QuoteResponse
}
