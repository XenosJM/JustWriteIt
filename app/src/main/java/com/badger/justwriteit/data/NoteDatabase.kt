package com.badger.justwriteit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.badger.justwriteit.data.note.Note
import com.badger.justwriteit.data.note.NoteDAO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Note::class], // 테이블 목록
    version = 1, // 버전(스키마 변경시 증가)
    exportSchema = false // 스키마 히스토리 저장 여부
)
abstract class NoteDatabase : RoomDatabase() {

    // DAO 접근 함수
    abstract fun noteDAO(): NoteDAO

    // Companion object = Java의 Static
    // Singleton 패턴 구현
    companion object {
        // Volatile: 멀티스레드 환경에서 항상 최신값 보장
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        // 데이터베이스 인스턴스 가져오기
        // synchronized로 스레드 안정성 보장
        fun getDatabase(context: Context): NoteDatabase {
            // INSTANCE가 null이 아니면 반환, null이면 생성
            return INSTANCE ?: synchronized(this) {
                // Double-checked locking
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database" // 데이터베이스 파일 이름
                )
                    // 메인스레드에서 쿼리 허용(개발용, 실사용에서는 비추천)
                    // .allowMainThreadQueries()

                    // 데이터베이스 생성시 콜백
                    .addCallback(DatabaseCallback())

                    // 마이그레이션 전략(버전 업그레이드시)
                    // .addMigration(MIGRATION_1_2)

                    // 마이그레이션 없이 재생성 (개발용, 데이터 손실!)
                    .fallbackToDestructiveMigration() // 마이그레이션 실패시 DB 재생성

                    .build()

                INSTANCE = instance
                instance
            }
        }

        // 데이터베이스 생성시 샘플 데이터 추가 (선택사항)
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // 데이터베이스 생성시 샘플 데이터 추가
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.noteDAO())
                    }
                }
            }
        }

        // 샘플 데이터 추가
        private suspend fun populateDatabase(noteDAO: NoteDAO) {
            // 샘플 메모 추가
            val sampleNotes = listOf(
                Note(
                    title = "Room Database 시작",
                    content = "첫 번째 메모입니다. Room은 SQLite 위에 구축된 강력한 데이터베이스입니다.",
                    isImportant = true
                ),
                Note(
                    title = "Kotlin 학습",
                    content = "Java에서 Kotlin으로 전환 중입니다. 문법이 간결해서 좋네요!",
                    isImportant = false
                ),
                Note(
                    title = "할일 목록",
                    content = "1. Room 학습\n2. 보안 구현\n3. 앱 빌드",
                    isImportant = true
                )
            )

            noteDAO.insertAll(sampleNotes)
        }

        // 마이그레이션 예제 (버전 1 -> 2)
        // 테이블 구조가 변경될 때 사용
        /*
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 새 컬럼 추가 예제
                database.execSQL(
                    "ALTER TABLE notes ADD COLUMN category TEXT DEFAULT 'general' NOT NULL"
                )
            }
        }
        */
    }
}

// Kotlin 핵심 개념:
// 1. companion object: static 멤버 (인스턴스 없이 접근)
// 2. @Volatile: 멀티스레드 가시성 보장
// 3. synchronized: 스레드 안전 잠금
// 4. Elvis 연산자 (?: ): null이면 우측 실행
// 5. let 스코프 함수: null이 아닐 때만 실행

// Java 비교:
/*
public class NoteDatabase extends RoomDatabase {
    private static volatile NoteDatabase INSTANCE;

    public static NoteDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            synchronized (NoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        NoteDatabase.class,
                        "note_database"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
*/