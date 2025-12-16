package com.badger.justwriteit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.badger.justwriteit.data.note.Note
import com.badger.justwriteit.data.NoteDatabase
import com.badger.justwriteit.data.category.Category
import com.badger.justwriteit.data.category.CategoryRepository
import com.badger.justwriteit.data.note.NoteRepository
import kotlinx.coroutines.launch

/**
 * ViewModel - UI와 데이터 사이의 다리
 *
 * 왜 ViewModel을 사용하나?
 * 1. 화면 회전시 데이터 보존
 * 2. UI 로직과 비즈니스 로직 분리
 * 3. 생명주기 관리 자동화
 *
 * AndroidViewModel vs ViewModel:
 * - AndroidViewModel: Application context 필요시
 * - ViewModel: context 불필요시
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    // Repository 초기화(선언)
    private val noteRepository: NoteRepository
    private val categoryRepository: CategoryRepository

    // LiveData - UI가 관찰(Observe)하는 데이터
    // 데이터 변경시 자동으로 UI 업데이드
    val allNotes: LiveData<List<Note>>
    val noteCount: LiveData<Int>
    val allCategories: LiveData<List<Category>>

    init {
        // Database 인스턴스 가져오기
        val db = NoteDatabase.getDatabase(application)
        val noteDAO = db.noteDAO()
        val categoryDAO = db.categoryDAO()

        // Repository 생성(초기화)
        noteRepository = NoteRepository(noteDAO)
        categoryRepository = CategoryRepository(categoryDAO)

        // LiveData 초기화
        allNotes = noteRepository.allNotes
        noteCount = noteRepository.noteCount
        allCategories = categoryRepository.allCategory
    }

    // viewModelScope : ViewModel이 제거될 때 자동으로 취소되는 코루틴 스코프

    // 메모 추가
    fun insertNote(note: Note) {
        viewModelScope.launch {
            // suspend 함수는 코루틴 안에서 호출해야함
            noteRepository.insert(note)
        }
    }

    // 카테고리 추가
    fun insertCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.insert(category)
        }
    }

    // 메모 수정
    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepository.update(note)
        }
    }

    // 카테고리 수정
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.update(category)
        }
    }

    // 메모 삭제
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.delete(note)
        }
    }

    // 카테고리 삭제
    fun deleteCategory(category: Category){
        viewModelScope.launch {
            // 삭제 해당 카테고리 사용하는 메모를 모두 기본으로 바꾸기
            noteRepository.moveNotesToDefault(category.id)
            // 카테고리 삭제
            categoryRepository.delete(category)
        }
    }

    // ID로 메모 삭제
    fun deleteByNoteId(noteId: Int) {
        viewModelScope.launch {
            noteRepository.deleteById(noteId)
        }
    }

    // 모든 메모 삭제
    fun deleteAllNote() {
        viewModelScope.launch {
            noteRepository.deleteAll()
        }
    }

    fun deleteAllCategory() {
        viewModelScope.launch {
            categoryRepository.deleteAll()
        }
    }

    // 제목으로 검색
    fun searchByTitle(query: String): LiveData<List<Note>> {
        return noteRepository.searchByTitle(query)
    }

    // 제목과 내용에서 검색
    fun searchNotes(query: String): LiveData<List<Note>> {
        return  noteRepository.searchNotes(query)
    }

    // 중요메모만 가져오기
    fun getImportantNotes(): LiveData<List<Note>> {
        return noteRepository.getImportantNotes()
    }

    // 카테고리 검색
    fun searchByCategoryName(query: String): LiveData<List<Category>> {
        return categoryRepository.searchByCategoryName(query)
    }
}

// ViewModel의 장점:
// 1. 화면 회전시 데이터 보존
//    - Activity는 재생성되지만 ViewModel은 유지
// 2. 코루틴 자동 관리
//    - viewModelScope가 생명주기 관리
// 3. LiveData로 자동 UI 업데이트
//    - observe() 한번만 하면 끝!

// Java에서는:
// - AsyncTask나 Handler 필요
// - 화면 회전시 데이터 날아감
// - 메모리 누수 위험
// - 콜백 지옥








