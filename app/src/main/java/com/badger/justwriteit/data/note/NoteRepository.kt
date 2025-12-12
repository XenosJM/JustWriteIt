package com.badger.justwriteit.data.note

import androidx.lifecycle.LiveData

/**
 * Repository 패턴
 * - ViewModel과 데이터 소스 사이의 중재자
 * - 데이터 소스를 추상화 (로컬 DB, 네트워크 API 등)
 * - 비즈니스 로직 분리
 *
 * 왜 Repository를 사용하나?
 * - 나중에 데이터 소스 변경 쉬움 (Room -> Firebase 등)
 * - 테스트 용이
 * - 코드 재사용성
 */
class NoteRepository(private  val noteDAO: NoteDAO) {

    // LiveData는 자동으로 백그라운드에서 실행되므로 suspend가 불필요함
    val allNotes: LiveData<List<Note>> = noteDAO.getAllnotes()
    val noteCount: LiveData<Int> = noteDAO.getNoteCount()

    // 메모추가, suspend 함수는 코루틴에서 호출
    suspend fun insert(note: Note): Long {
        return noteDAO.insert(note)
    }

    // 메모 수정
    suspend fun update(note: Note) {
        noteDAO.update(note)
    }

    // 메모 삭제
    suspend fun delete(note: Note) {
        noteDAO.delete(note)
    }

    // ID로 메모 삭제
    suspend fun deleteById(noteId: Int) {
        noteDAO.deleteById(noteId)
    }

    // 모든 메모 삭제
    suspend fun deleteAll() {
        noteDAO.deleteAll()
    }

    // ID로 메모 가져오기
    suspend fun getNoteById(noteId: Int) : Note? {
        return noteDAO.getNoteById(noteId)
    }

    // 제목으로 검색
    fun searchByTitle(query: String): LiveData<List<Note>> {
        return noteDAO.searchByTitle(query)
    }

    // 제목과 내용에서 검색
    fun searchNotes(query: String): LiveData<List<Note>> {
        return  noteDAO.searchNotes(query)
    }

    // 중요 메모만 가져오기
    fun getImportantNotes(): LiveData<List<Note>> {
        return noteDAO.getImportantNotes()
    }

    // 샘플 데이터 추가(테스트용)
    suspend fun insertSampleData() {
        val samples = listOf(
            Note(
                title = "회의 준비",
                content = "내일 오전 10시 팀 회의 자료 준비",
                isImportant = true
            ),
            Note(
                title = "장보기",
                content = "우유, 빵, 계란, 야채",
                isImportant = false
            ),
            Note(
                title = "운동",
                content = "저녁 7시 헬스장 - 상체 운동",
                isImportant = false
            )
        )

        noteDAO.insertAll(samples)
    }

}

// Repository 패턴의 장점:
// 1. 단일 진실 공급원(Single Source of Truth)
// 2. 데이터 소스 추상화
// 3. 오프라인 우선 전략 구현 가능
// 4. 캐싱 전략 구현 용이

// 예: 나중에 네트워크 API 추가시
/*
class NoteRepository(
    private val noteDao: NoteDao,
    private val apiService: NoteApiService  // 추가
) {
    suspend fun syncWithServer() {
        // 서버에서 데이터 가져와서 로컬 DB 업데이트
        val notesFromServer = apiService.getNotes()
        noteDao.insertAll(notesFromServer)
    }
}
*/