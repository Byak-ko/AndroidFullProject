package com.example.androidfullproject

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import com.example.androidfullproject.ListItem.User
import com.example.androidfullproject.ListItem.Food
import com.example.androidfullproject.ListItem.Quote

class EditActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var caloriesEditText: EditText
    private lateinit var quoteTextEditText: EditText
    private lateinit var authorEditText: EditText
    private lateinit var foodImageView: ImageView
    private lateinit var selectImageButton: Button

    private var itemId: Long = -1
    private var itemType: String? = null
    private var selectedImage: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        database = AppDatabase.getDatabase(this)

        nameEditText = findViewById(R.id.editName)
        ageEditText = findViewById(R.id.editAge)
        caloriesEditText = findViewById(R.id.editCalories)
        quoteTextEditText = findViewById(R.id.editQuoteText)
        authorEditText = findViewById(R.id.editAuthor)
        foodImageView = findViewById(R.id.foodImageView)
        selectImageButton = findViewById(R.id.selectImageButton)

        itemId = intent.getLongExtra("ITEM_ID", -1)
        itemType = intent.getStringExtra("ITEM_TYPE")

        setupVisibility()
        if (itemId != -1L) loadData()

        selectImageButton.setOnClickListener { pickImageFromGallery() }
        findViewById<Button>(R.id.saveButton).setOnClickListener { saveData() }
    }

    private fun setupVisibility() {
        val visibilityMap = mapOf(
            "user" to listOf(
                R.id.editNameLabel to View.VISIBLE,
                R.id.editName to View.VISIBLE,
                R.id.editAgeLabel to View.VISIBLE,
                R.id.editAge to View.VISIBLE
            ),
            "food" to listOf(
                R.id.editNameLabel to View.VISIBLE,
                R.id.editName to View.VISIBLE,
                R.id.editCaloriesLabel to View.VISIBLE,
                R.id.editCalories to View.VISIBLE,
                R.id.foodImageView to View.VISIBLE,
                R.id.selectImageButton to View.VISIBLE
            ),
            "quote" to listOf(
                R.id.editQuoteTextLabel to View.VISIBLE,
                R.id.editQuoteText to View.VISIBLE,
                R.id.editAuthorLabel to View.VISIBLE,
                R.id.editAuthor to View.VISIBLE
            )
        )

        visibilityMap[itemType]?.forEach { (viewId, visibility) ->
            findViewById<View>(viewId).visibility = visibility
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            when (itemType) {
                "user" -> database.userDao().getUserById(itemId)?.let {
                    nameEditText.setText(it.name)
                    ageEditText.setText(it.age.toString())
                } ?: showError("User not found")
                "food" -> database.foodDao().getFoodById(itemId)?.let { food ->
                    nameEditText.setText(food.name)
                    caloriesEditText.setText(food.calories.toString())
                    if (food.image.isNotEmpty()) {
                        val bitmap = BitmapFactory.decodeFile(food.image)
                        foodImageView.setImageBitmap(bitmap)
                    } else {
                        foodImageView.setImageDrawable(null)
                    }
                } ?: showError("Food not found")
                "quote" -> database.quoteDao().getQuoteById(itemId)?.let {
                    quoteTextEditText.setText(it.text)
                    authorEditText.setText(it.author)
                } ?: showError("Quote not found")
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val imageUri: Uri? = result.data?.data
            val inputStream: InputStream? = imageUri?.let { contentResolver.openInputStream(it) }
            selectedImage = BitmapFactory.decodeStream(inputStream)
            foodImageView.setImageBitmap(selectedImage)
        }
    }

    private fun saveData() {
        val newName = nameEditText.text.toString()
        val newAge = ageEditText.text.toString().toIntOrNull()
        val newCalories = caloriesEditText.text.toString().toIntOrNull()
        val newQuoteText = quoteTextEditText.text.toString()
        val newAuthor = authorEditText.text.toString()

        lifecycleScope.launch {
            when {
                itemId != -1L -> updateItem(newName, newAge, newCalories, newQuoteText, newAuthor)
                else -> insertItem(newName, newAge, newCalories, newQuoteText, newAuthor)
            }
            finish()
        }
    }

    private suspend fun updateItem(newName: String, newAge: Int?, newCalories: Int?, newQuoteText: String, newAuthor: String) {
        when (itemType) {
            "user" -> database.userDao().updateUser(User(itemId, newName, newAge ?: 0))
            "food" -> {
                val food = Food(
                    id = itemId,
                    name = newName,
                    calories = newCalories ?: 0,
                    image = selectedImage?.let { saveImageToInternalStorage(it, "food_$itemId.png") } ?: ""
                )
                database.foodDao().updateFood(food)
            }
            "quote" -> database.quoteDao().updateQuote(Quote(itemId, newQuoteText, newAuthor))
        }
    }

    private suspend fun insertItem(newName: String, newAge: Int?, newCalories: Int?, newQuoteText: String, newAuthor: String) {
        when (itemType) {
            "user" -> database.userDao().insertUser(User(name = newName, age = newAge ?: 0))
            "food" -> {
                val food = Food(
                    name = newName,
                    calories = newCalories ?: 0,
                    image = selectedImage?.let { saveImageToInternalStorage(it, "food_${System.currentTimeMillis()}.png") } ?: ""
                )
                database.foodDao().insertFood(food)
            }
            "quote" -> database.quoteDao().insertQuote(Quote(text = newQuoteText, author = newAuthor))
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap, fileName: String): String? {
        return try {
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d("EditActivity", message)
    }
}
