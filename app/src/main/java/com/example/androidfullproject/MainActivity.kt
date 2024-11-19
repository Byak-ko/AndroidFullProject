package com.example.androidfullproject

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidfullproject.ListItem.User
import com.example.androidfullproject.ListItem.Food
import com.example.androidfullproject.ListItem.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var database: AppDatabase
    private lateinit var adapter: MultiTypeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = AppDatabase.getDatabase(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener {
            showAddItemDialog()
        }

        val refreshButton = findViewById<Button>(R.id.refreshButton)
        refreshButton.setOnClickListener {
            lifecycleScope.launch {
                loadQuotesFromNetwork()
                refreshData()
            }
        }

        lifecycleScope.launch {
            // Тестові дані
           // val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.pizza)
           // val imagePath = saveImageToExternalStorage(bitmap, "pizza.png")
          //  Log.d("ImagePath", "Image saved at: $imagePath")
           // database.foodDao().insertFood(Food(name = "Піца", calories = 285, image = imagePath ?: ""))
            refreshData()
        }
    }


    private suspend fun saveImageToExternalStorage(bitmap: Bitmap, fileName: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val contentUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                contentUri?.let { uri ->
                    val outputStream = contentResolver.openOutputStream(uri)
                    outputStream?.use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                    uri.toString()
                } ?: run {
                    Log.e("MainActivity", "Failed to insert image")
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun showAddItemDialog() {
        val options = arrayOf("User", "Food", "Quote")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select item type")
            .setItems(options) { _, which ->
                when (options[which]) {
                    "User" -> openEditActivity("user", null)
                    "Food" -> openEditActivity("food", null)
                    "Quote" -> openEditActivity("quote", null)
                }
            }
        builder.create().show()
    }

    private fun openEditActivity(type: String, itemId: Long?) {
        val intent = Intent(this, EditActivity::class.java).apply {
            putExtra("ITEM_TYPE", type)
            itemId?.let { putExtra("ITEM_ID", it) }
        }
        startActivity(intent)
    }

    private suspend fun refreshData() {
        val users = database.userDao().getAllUsers()
        val food = database.foodDao().getAllFood()
        val quotes = database.quoteDao().getAllQuotes()

        val items = mutableListOf<ListItem>()
        items.addAll(users)
        items.addAll(food)
        items.addAll(quotes)

        adapter = MultiTypeAdapter(items, ::onEditClick, ::onDeleteClick)
        findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
    }

    private fun onEditClick(item: ListItem) {
        when (item) {
            is User -> openEditActivity("user", item.id)
            is Food -> openEditActivity("food", item.id)
            is Quote -> openEditActivity("quote", item.id)
        }
    }

    private fun onDeleteClick(item: ListItem) {
        lifecycleScope.launch {
            when (item) {
                is Food -> database.foodDao().deleteFood(item)
                is User -> database.userDao().deleteUser(item)
                is Quote -> database.quoteDao().deleteQuote(item)
            }
            refreshData()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            refreshData()
        }
    }

    suspend fun loadQuotesFromNetwork() {
        try {
            val quotesResponse = RetrofitClient.instance.getQuote()
            quotesResponse?.let {
                if (it.quote.isNotEmpty() && it.author.isNotEmpty()) {
                    database.quoteDao().insertQuote(Quote(text = it.quote, author = it.author))
                    Log.d("Network", "Quote added: ${it.quote} by ${it.author}")
                    refreshData()
                } else {
                    Log.w("Network", "Empty quote or author")
                }
            }
        } catch (e: Exception) {
            Log.e("Network", "Error fetching quotes: ${e.message}")
        }
    }
}
