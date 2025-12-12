package com.badger.justwriteit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "category_name")
    val categoryName: String,

    @ColumnInfo(name = "color")
    val color: String, // TODO 컬러 코드 표를 넣어서  int 값으로 처리할것인지 아니면 레이아웃의 컬러이름을 가져와서 그냥 넣을것인지 고민조금 해보기

)
