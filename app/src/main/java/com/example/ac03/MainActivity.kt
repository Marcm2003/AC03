package com.example.ac03

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ac03.adapters.ArticleListAdapter
import com.example.ac03.room.Article
import com.example.ac03.room.ArticleDao
import com.example.ac03.room.Settings
import com.example.ac03.room.SettingsDao
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var articleDao: ArticleDao
    private lateinit var settingsDao: SettingsDao
    private lateinit var articleListAdapter: ArticleListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val appInstance = application as App
        articleDao = appInstance.db.articleDao()
        settingsDao = appInstance.db.settingsDao()

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        articleListAdapter = ArticleListAdapter(this, GlobalScope)
        recyclerView.adapter = articleListAdapter


        val fabAddArticle: FloatingActionButton = findViewById(R.id.fab_add_article)
        fabAddArticle.setOnClickListener {
            showAddArticleDialog()
        }

        articleListAdapter = ArticleListAdapter(this, GlobalScope)
        recyclerView.adapter = articleListAdapter

        articleListAdapter.onItemClick = { article ->
            showEditArticleActivity(article)
        }

        val filterButton: FloatingActionButton = findViewById(R.id.filter)
        filterButton.setOnClickListener {
            showFilterDialog()
        }

        val settingsButton: FloatingActionButton = findViewById(R.id.settings)
        settingsButton.setOnClickListener {
            showSettingsDialog()
        }

        articleListAdapter.onDeleteClickListener = { article ->
            showDeleteConfirmationDialog(article)
        }

        val settings = Settings(1, 21.0f)
        GlobalScope.launch(Dispatchers.IO) {
            settingsDao.insertSettings(settings)
        }

        searchView = findViewById(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterArticlesByDescription(newText.orEmpty())
                return true
            }
        })

        showAllArticles()
    }


    private fun showAddArticleDialog() {
        val builder = AlertDialog.Builder(this, R.style.DialogStyle)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_article, null)
        val editTextCode = dialogLayout.findViewById<EditText>(R.id.editTextCode)
        val editTextDescription = dialogLayout.findViewById<EditText>(R.id.editTextDescription)
        val spinnerFamily = dialogLayout.findViewById<Spinner>(R.id.spinnerFamily)
        val editTextPrice = dialogLayout.findViewById<EditText>(R.id.editTextPrice)
        val checkBoxStock = dialogLayout.findViewById<CheckBox>(R.id.checkBoxStock)
        val editTextStock = dialogLayout.findViewById<EditText>(R.id.editTextStock)

        val familyOptions = arrayOf(
            "", getString(R.string.electr_nica),
            getString(R.string.roba), getString(R.string.llibre), getString(R.string.joguina), getString(R.string.alimentacio)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, familyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFamily.adapter = adapter

        builder.setView(dialogLayout)
        builder.setPositiveButton("Add") { _, _ ->
            val code = editTextCode.text.toString()
            val description = editTextDescription.text.toString()
            val family = spinnerFamily.selectedItem.toString()
            val price = editTextPrice.text.toString().toFloatOrNull() ?: 0.0F
            val actualStock = editTextStock.text.toString().toFloatOrNull() ?: 0F
            val stockActivated = checkBoxStock.isChecked

            if (code.isBlank()) {
                showToast(getString(R.string.code_and_description_are_required))
            } else if (actualStock < 0 && stockActivated) {
                showToast(getString(R.string.stock_must_be_0_or_positive_when_stock_is_activated))
            } else {
                GlobalScope.launch {
                    if (isCodeAlreadyExists(code)) {
                        showToast(getString(R.string.article_with_the_same_code_already_exists))
                    } else {
                        val newArticle = Article(code, description, family, price, stockActivated, actualStock)
                        addNewArticle(newArticle)
                    }
                }
            }
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private suspend fun isCodeAlreadyExists(code: String): Boolean {
        return withContext(Dispatchers.IO) {
            articleDao.getArticleWithCode(code) != null
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this, R.style.DialogStyle)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_settings, null)
        val editTextIva = dialogLayout.findViewById<EditText>(R.id.editTextIva)

        GlobalScope.launch(Dispatchers.Main) {
            val currentIva = withContext(Dispatchers.IO) {
                settingsDao.getIvaPercentage()
            } ?: 0.0f
            editTextIva.setText(currentIva.toString())
        }

        builder.setView(dialogLayout)
        builder.setPositiveButton(getString(R.string.save)) { _, _ ->
            val newIva = editTextIva.text.toString().toFloatOrNull() ?: 0.0F
            val newSettings = Settings(1, newIva)

            GlobalScope.launch(Dispatchers.IO) {
                settingsDao.insertSettings(newSettings)
            }

            Toast.makeText(this, getString(R.string.iva_saved) + " $newIva%", Toast.LENGTH_SHORT).show()
            notifyDataSetChanged()

        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()

    }


    private fun showFilterDialog() {
        val builder = AlertDialog.Builder(this, R.style.DialogStyle)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_filter, null)
        val checkBoxFilterByDescription = dialogLayout.findViewById<CheckBox>(R.id.checkBoxFilterByDescription)
        val checkBoxFilterByStock = dialogLayout.findViewById<CheckBox>(R.id.checkBoxFilterByStock)
        val checkBoxSortByCode = dialogLayout.findViewById<CheckBox>(R.id.checkBoxSortByCode)
        val clearFilterButton = dialogLayout.findViewById<Button>(R.id.buttonClearFilters)

        checkBoxFilterByDescription.isChecked = sharedPreferences.getBoolean("filterByDescription", false)
        checkBoxFilterByStock.isChecked = sharedPreferences.getBoolean("filterByStock", false)
        checkBoxSortByCode.isChecked = sharedPreferences.getBoolean("sortByCode", false)

        builder.setView(dialogLayout)

        checkBoxFilterByDescription.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("filterByDescription", isChecked).apply()
            if (isChecked) {
                filterArticlesByDescription("")
            } else {
                showAllArticles()
            }
        }

        checkBoxFilterByStock.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("filterByStock", isChecked).apply()
            if (isChecked) {
                filterArticlesByStockActivated()
            } else {
                showAllArticles()
            }
        }

        checkBoxSortByCode.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("sortByCode", isChecked).apply()
            if (isChecked) {
                sortArticlesByCode()
            } else {
                showAllArticles()
            }
        }

        clearFilterButton.setOnClickListener {
            checkBoxFilterByDescription.isChecked = false
            checkBoxFilterByStock.isChecked = false
            checkBoxSortByCode.isChecked = false

            sharedPreferences.edit().clear().apply()

            showAllArticles()
        }

        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private val sharedPreferences by lazy {
        getSharedPreferences("FilterPreferences", Context.MODE_PRIVATE)
    }



    private fun showDeleteConfirmationDialog(article: Article) {
        val deleteBuilder = AlertDialog.Builder(this, R.style.DialogStyle)
        deleteBuilder.setTitle(getString(R.string.confirm_delete))
        val messageTextView = TextView(this)
        messageTextView.text = getString(R.string.are_you_sure_you_want_to_delete_this_article)
        messageTextView.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        messageTextView.setPadding(20, 20, 20, 20)

        deleteBuilder.setView(messageTextView)

        deleteBuilder.setPositiveButton(getString(R.string.yes)) { _, _ ->
            deleteArticle(article.CODIARTICLE)
        }

        deleteBuilder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
            dialog.cancel()
        }

        deleteBuilder.show()
    }


    private fun notifyDataSetChanged() {
        articleListAdapter.notifyDataSetChanged()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun sortArticlesByCode() {
        GlobalScope.launch(Dispatchers.IO) {
            val sortedArticles = articleDao.getArticlesSortedByCode()
            launch(Dispatchers.Main) {
                articleListAdapter.submitList(sortedArticles)
            }
        }
    }


    private fun showEditArticleActivity(article: Article) {
        val intent = Intent(this, EditArticleActivity::class.java)
        intent.putExtra("articleCode", article.CODIARTICLE)
        startActivity(intent)
    }



    @OptIn(DelicateCoroutinesApi::class)
    private fun showAllArticles() {
        GlobalScope.launch(Dispatchers.IO) {
            val articles = articleDao.getAllArticles()
            launch(Dispatchers.Main) {
                articleListAdapter.submitList(articles)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun addNewArticle(article: Article) {
        GlobalScope.launch(Dispatchers.IO) {
            articleDao.insert(article)
            showAllArticles()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun filterArticlesByDescription(text: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val filteredArticles = if (text.isNotEmpty()) {
                articleDao.getArticlesWithDescription("%$text%")
            } else {
                articleDao.getAllArticles()
            }

            launch(Dispatchers.Main) {
                articleListAdapter.submitList(filteredArticles)
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun filterArticlesByStockActivated() {
        GlobalScope.launch(Dispatchers.IO) {
            val filteredArticles = articleDao.getStockActivatedArticles()
            launch(Dispatchers.Main) {
                articleListAdapter.submitList(filteredArticles)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun deleteArticle(articleId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            articleDao.deleteArticleById(articleId)
            showAllArticles()
        }
    }

}
