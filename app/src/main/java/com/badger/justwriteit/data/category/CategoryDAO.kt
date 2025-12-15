package com.badger.justwriteit.data.category

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface CategoryDAO {

    // 카테고리 생성
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(category: Category): Long
    // 카테고리 업데이트
    @Update(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun update(category: Category)
    // 카테고리 삭제
    @Delete
    suspend fun delete(category: Category)
    // 카테고리 모두 삭제
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
    // 모든 카테고리 보기
    @Query("SELECT * FROM categories")
    fun getAllCategories(): LiveData<List<Category>>
    // 카테고리 검색
    @Query("SELECT * FROM categories where category_name LIKE '%' || :searchQuery || '%'")
    fun searchByCategoryName(searchQuery: String): LiveData<List<Category>>

}