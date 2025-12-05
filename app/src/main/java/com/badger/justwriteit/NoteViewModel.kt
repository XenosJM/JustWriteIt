package com.badger.justwriteit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.badger.justwriteit.data.note.Note
import com.badger.justwriteit.data.NoteDatabase
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
    private val repository: NoteRepository

    // LiveData - UI가 관찰(Observe)하는 데이터
    // 데이터 변경시 자동으로 UI 업데이드
    val allNotes: LiveData<List<Note>>
    val noteCount: LiveData<Int>

    init {
        // Database 인스턴스 가져오기
        val noteDAO = NoteDatabase.getDatabase(application).noteDAO()

        // Repository 생성(초기화)
        repository = NoteRepository(noteDAO)

        // LiveData 초기화
        allNotes = repository.allNotes
        noteCount = repository.noteCount
    }

    // viewModelScope : ViewModel이 제거될 때 자동으로 취소되는 코루틴 스코프

    // 메모 추가
    fun insert(note: Note) {
        viewModelScope.launch {
            // suspend 함수는 코루틴 안에서 호출해야함
            repository.insert(note)
        }
    }

    // 메모 수정
    fun updae(note: Note) {
        viewModelScope.launch {
            repository.update(note)
        }
    }

    // 메모 삭제
    fun delete(note: Note) {
        viewModelScope.launch {
            repository.delete(note)
        }
    }

    // ID로 메모 삭제
    fun deleteById(noteId: Int) {
        viewModelScope.launch {
            repository.deleteById(noteId)
        }
    }

    // 모든 메모 삭제
    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    // 제목으로 검색
    fun searchByTitle(query: String): LiveData<List<Note>> {
        return repository.searchByTitle(query)
    }

    // 제목과 내용에서 검색
    fun searchNotes(query: String): LiveData<List<Note>> {
        return  repository.searchNotes(query)
    }

    // 중요메모만 가져오기
    fun getImportantNotes(): LiveData<List<Note>> {
        return repository.getImportantNotes()
    }

    // 샘플 데이더 추가 (테스트용)
    fun insertSampleData(){
        viewModelScope.launch {
            repository.insertSampleData()
        }
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








