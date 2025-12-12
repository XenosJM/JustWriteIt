package com.badger.justwriteit.data.note

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAO (Data Access Object) - 데이터베이스 작업을 정의하는 인터페이스
 * Room이 자동으로 구현체를 생성해줍니다!
 *
 * 중요: SQL 쿼리를 컴파일 타임에 검증
 */
@Dao
interface NoteDAO {
    /**
     * 메모 추가
     * suspend 키워드 = 코루틴에서 실행 (백그라운드 스레드)
     * @return 삽입된 행의 ID
     */
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(note: Note): Long

    // 여러 메모 한번에 추가
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertAll(note: List<Note>)

    // 메모 수정, 기본적으로 Primary Key로 매칭
    @Update
    suspend fun update(note: Note)

    // 메모 삭제
    @Delete
    suspend fun delete(note: Note)

    // 특정 ID의 메모 삭제
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun  deleteById(noteId: Int)

    // 모든 메모 삭제
    @Query("DELETE FROM notes")
    suspend fun deleteAll()

    /**
     * 모든 메모 가져오기 (최신순)
     * LiveData는 자동으로 UI 업데이트 (옵저버 패턴)
     * suspend 불필요 - LiveData는 자동으로 백그라운드 실행
     */
    @Query("SELECT * FROM notes ORDER BY created_at DESC")
    fun getAllnotes(): LiveData<List<Note>>

    // ID로 메모 찾기
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?

    // 제목으로 검색, LIKE 연산자 사용
    @Query("SELECT * FROM notes WHERE title LIKE '%' || :searchQuery || '%' ORDER BY created_at DESC")
    fun searchByTitle(searchQuery: String): LiveData<List<Note>>

    // 중요 메모만 가져오기
    @Query("SELECT * FROM notes WHERE is_important = 1 ORDER BY created_at DESC")
    fun getImportantNotes(): LiveData<List<Note>>

    // 제목과 내용에서 검색, 복잡한 쿼리 예제
    @Query("""
        SELECT * FROM notes 
        WHERE title LIKE '%' || :query || '%' 
        OR content LIKE '%' || :query || '%'
        ORDER BY created_at DESC
    """)
    fun searchNotes(query: String): LiveData<List<Note>>

    // 메모 개수 가져오기
    // Flow를 사용하면 실시간 업데이트 가능
    @Query("SELECT COUNT(*) FROM notes")
    fun getNoteCount(): LiveData<Int>

}

// Kotlin의 suspend 함수:
// - 코루틴에서만 호출 가능
// - 백그라운드 스레드에서 자동 실행
// - UI 스레드 블록 없이 DB 작업 가능

// Java에서는:
// - Executor나 AsyncTask 사용 필요
// - 콜백 지옥 발생 가능
// - 코드가 훨씬 복잡

// LiveData:
// - 데이터 변경시 자동으로 UI 업데이트
// - 생명주기 인식 (액티비티 종료시 자동 정리)
// - 메모리 누수 방지