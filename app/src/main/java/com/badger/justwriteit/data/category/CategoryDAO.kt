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

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(category: List<Category>)

    // 카테고리 업데이트
    @Update
    suspend fun update(category: Category)
    // 카테고리 삭제
    @Delete
    suspend fun delete(category: Category)
    // 카테고리 모두 삭제
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
    // 모든 카테고리 보기, 1번은 메모 작성시 부여되는 기본 default 색이기 때문에 수정/삭제가 일어나면 안되므로 노출을 막음.
    @Query("SELECT * FROM categories WHERE id != 1" )
    fun getAllCategories(): LiveData<List<Category>>
    // 카테고리 검색
    @Query("SELECT * FROM categories where title LIKE '%' || :searchQuery || '%'")
    fun searchByCategoryName(searchQuery: String): LiveData<List<Category>>

}