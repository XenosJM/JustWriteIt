package com.badger.justwriteit.data.category

import androidx.lifecycle.LiveData

class CategoryRepository(private val categoryDAO: CategoryDAO) {

    val allCategory: LiveData<List<Category>> = categoryDAO.getAllCategories()

    // 카테고리 추가
    suspend fun insert(category: Category): Long{
        return categoryDAO.insert(category)
    }

    // 카테고리 업데이트
    suspend fun update(category: Category) {
        categoryDAO.update(category)
    }

    // 카테고리 삭제
    suspend fun delete(category: Category){
        categoryDAO.delete(category)
    }

    // 모든 카테고리 삭제
    suspend fun deleteAll(){
        categoryDAO.deleteAll()
    }

    fun searchByCategoryName(query: String): LiveData<List<Category>>{
        return categoryDAO.searchByCategoryName(query)
    }
}
/** TODO 일단 카테고리 정리 하고 최대 몇개까지 있어야 할지, 또 isImportant 항목을 카테고리로
 * TODO 편입시킬것인지 고민도 해보고 또한 변경하게 되면 구조또한 변경해야한다.
*/