package com.example.ac03.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.NonNull


@Entity(tableName = "articles")
data class Article(
    @PrimaryKey
    @NonNull
    val CODIARTICLE: String,
    val DESCRIPCIO: String,
    val FAMILIA: String?,
    val PREUSENSEIVA: Float,
    val ESTOC_ACTIVAT: Boolean,
    val ESTOC_ACTUAL: Float
)
