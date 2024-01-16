package com.example.ac03

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ac03.adapters.ArticleListAdapter
import com.example.ac03.room.Article
import com.example.ac03.room.ArticleDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditArticleActivity : AppCompatActivity() {

    private lateinit var articleCodeTextView: TextView
    private lateinit var descriptionEditText: EditText
    private lateinit var familySpinner: Spinner
    private lateinit var priceEditText: EditText
    private lateinit var stockActivatedCheckBox: CheckBox
    private lateinit var stockEditText: EditText
    private lateinit var saveButton: Button

    private lateinit var articleDao: ArticleDao
    private lateinit var articleListAdapter: ArticleListAdapter

    private var articleDescription: String = ""
    private var articleFamily: String = ""
    private var articlePrice: Float = 0.0F
    private var articleStockActivated: Boolean = false
    private var articleStock: Float = 0F

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_article)

        articleCodeTextView = findViewById(R.id.textViewArticleCode)
        descriptionEditText = findViewById(R.id.editTextArticleDescription)
        familySpinner = findViewById(R.id.spinnerFamily)
        priceEditText = findViewById(R.id.editTextArticlePrice)
        stockActivatedCheckBox = findViewById(R.id.checkBoxArticleStockActivated)
        stockEditText = findViewById(R.id.editTextArticleStock)
        saveButton = findViewById(R.id.buttonSaveArticle)

        val familyOptions = arrayOf("", getString(R.string.electr_nica),
            getString(R.string.roba), getString(R.string.llibre), getString(R.string.joguina), getString(R.string.alimentacio))
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, familyOptions)
        familySpinner.adapter = adapter

        val articleCode = intent.getStringExtra("articleCode")

        val db = (applicationContext as App).db
        articleDao = db.articleDao()

        if (savedInstanceState == null) {
            loadArticleData(articleCode)
        } else {
            articleDescription = savedInstanceState.getString("articleDescription", "")
            articleFamily = savedInstanceState.getString("articleFamily", "")
            articlePrice = savedInstanceState.getFloat("articlePrice", 0.0F)
            articleStockActivated = savedInstanceState.getBoolean("articleStockActivated", false)
            articleStock = savedInstanceState.getFloat("articleStock", 0F)
        }

        saveButton.setOnClickListener {
            articleDescription = descriptionEditText.text.toString()
            articleFamily = familySpinner.selectedItem.toString()
            articlePrice = priceEditText.text.toString().toFloatOrNull() ?: 0.0F
            articleStockActivated = stockActivatedCheckBox.isChecked
            articleStock = stockEditText.text.toString().toFloatOrNull() ?: 0F

            saveArticleChanges(articleCode)
        }
    }

    private fun loadArticleData(articleCode: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val article = articleDao.getArticleWithCode(articleCode!!)
            runOnUiThread {
                articleCodeTextView.text = "Code: ${article.CODIARTICLE}"
                descriptionEditText.setText(article.DESCRIPCIO)
                familySpinner.setSelection((familySpinner.adapter as ArrayAdapter<String>).getPosition(article.FAMILIA))
                priceEditText.setText(article.PREUSENSEIVA.toString())
                stockActivatedCheckBox.isChecked = article.ESTOC_ACTIVAT
                stockEditText.setText(article.ESTOC_ACTUAL.toString())
            }
        }
    }

    private fun saveArticleChanges(articleCode: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val updatedArticle = Article(
                articleCode!!,
                articleDescription,
                articleFamily,
                articlePrice,
                articleStockActivated,
                articleStock
            )
            articleDao.update(updatedArticle)

            articleListAdapter.notifyDataSetChanged()

            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("articleDescription", articleDescription)
        outState.putString("articleFamily", articleFamily)
        outState.putFloat("articlePrice", articlePrice)
        outState.putBoolean("articleStockActivated", articleStockActivated)
        outState.putFloat("articleStock", articleStock)
    }
}
