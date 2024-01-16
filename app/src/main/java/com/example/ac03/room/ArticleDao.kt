package com.example.ac03.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles")
    fun getAllArticles(): List<Article>

    @Query("SELECT * FROM articles WHERE DESCRIPCIO LIKE '%' || :text || '%'")
    fun getArticlesWithDescription(text: String): List<Article>

    @Query("SELECT * FROM articles WHERE CODIARTICLE = :code")
    fun getArticleWithCode(code: String): Article

    @Query("SELECT * FROM articles WHERE FAMILIA = :family")
    fun getArticlesWithFamily(family: String): List<Article>

    @Query("SELECT * FROM articles WHERE PREUSENSEIVA >= :minPrice AND PREUSENSEIVA <= :maxPrice")
    fun getArticlesWithPriceRange(minPrice: Float, maxPrice: Float): List<Article>

    @Query("SELECT * FROM articles WHERE ESTOC_ACTIVAT = :stock")
    fun getArticlesWithStock(stock: Boolean): List<Article>

    @Query("SELECT * FROM articles WHERE ESTOC_ACTUAL >= :minStock AND ESTOC_ACTUAL <= :maxStock")
    fun getArticlesWithStockRange(minStock: Float, maxStock: Float): List<Article>

    @Query("SELECT * FROM articles WHERE ESTOC_ACTUAL = 0")
    fun getArticlesWithoutStock(): List<Article>

    @Query("SELECT * FROM articles WHERE ESTOC_ACTIVAT = 1")
    fun getStockActivatedArticles(): List<Article>

    @Query("SELECT * FROM articles ORDER BY CODIARTICLE ASC")
    fun getArticlesSortedByCode(): List<Article>

    @Insert
    fun insert(article: Article)

    @Query("DELETE FROM articles WHERE CODIARTICLE = :articleId")
    fun deleteArticleById(articleId: String)

    @Update
    fun update(updatedArticle: Article)




}

