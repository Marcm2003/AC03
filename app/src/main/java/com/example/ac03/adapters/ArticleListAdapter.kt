package com.example.ac03.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.ac03.App
import com.example.ac03.room.Article
import com.example.ac03.R
import com.example.ac03.room.SettingsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArticleListAdapter(private val context: Context, private val scope: CoroutineScope) :
    ListAdapter<Article, ArticleListAdapter.ArticleViewHolder>(ArticleDiffCallback()) {

    var onDeleteClickListener: ((Article) -> Unit)? = null
    var onItemClick: ((Article) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(currentItem)
        }

        holder.itemView.findViewById<View>(R.id.buttonDeleteArticle).setOnClickListener {
            onDeleteClickListener?.invoke(currentItem)
        }

        val db = (context.applicationContext as App).db
        val settingsDao: SettingsDao = db.settingsDao()

        scope.launch {
            val settings = settingsDao.getSettings()
            val iva = settings?.iva
            val articlePriceIVA = currentItem.PREUSENSEIVA + (currentItem.PREUSENSEIVA * (iva!! / 100))

            withContext(Dispatchers.Main) {
                val articlePriceIVATextView = holder.itemView.findViewById<TextView>(R.id.textArticlePriceIVA)
                articlePriceIVATextView.text = articlePriceIVA.toString()
            }
        }
    }

    inner class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val articleCode: TextView = itemView.findViewById(R.id.textArticleCode)
        private val articleDescription: TextView = itemView.findViewById(R.id.textArticleDescription)
        private val articleFamily: TextView = itemView.findViewById(R.id.textArticleFamily)
        private val articlePrice: TextView = itemView.findViewById(R.id.textArticlePrice)
        private val articleStock: TextView = itemView.findViewById(R.id.textArticleStock)

        fun bind(article: Article) {
            articleCode.text = article.CODIARTICLE
            articleDescription.text = article.DESCRIPCIO
            articleFamily.text = article.FAMILIA
            articlePrice.text = article.PREUSENSEIVA.toString()

            if (article.ESTOC_ACTIVAT) {
                articleStock.visibility = View.VISIBLE
                articleStock.text = article.ESTOC_ACTUAL.toString()
            } else {
                articleStock.visibility = View.GONE
            }

            setItemBackgroundColor(article.FAMILIA, itemView)
        }

        private fun setItemBackgroundColor(family: String?, itemView: View) {
            val colorId = when (family) {
                context.getString(R.string.electr_nica) -> R.color.Electrònica
                context.getString(R.string.roba) -> R.color.Roba
                context.getString(R.string.llibre) -> R.color.Llibre
                context.getString(R.string.joguina) -> R.color.Joguina
                context.getString(R.string.alimentacio) -> R.color.Alimentació
                else -> R.color.noColor
            }
            itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, colorId))
        }
    }

    class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.CODIARTICLE == newItem.CODIARTICLE
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}
