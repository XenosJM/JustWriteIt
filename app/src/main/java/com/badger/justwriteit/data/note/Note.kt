package com.badger.justwriteit.data.note

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity - 데이터베이스 테이블을 나타내는 클래스
 *
 * Java와 비교:
 * - Kotlin의 data class는 자동으로 equals, hashCode, toString 생성
 * - Java에서는 일일이 getter/setter, equals, hashCode 작성 필요
 */
@Entity(tableName = "notes") // 테이블 이름 지정
data class Note(
    @PrimaryKey(autoGenerate = true) // 자동 증가 ID
    val id: Int = 0,

    @ColumnInfo(name = "title") // 컬럼 이름(생략이 가능)
    val title: String,

    @ColumnInfo(name = "content")
    val content: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),  // 기본값 설정

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_important")
    val isImportant: Boolean = false,  // 중요 메모 표시

    @ColumnInfo(name = "category_id")
    val categoryId: Int

)

// Kotlin data class 장점:
// 1. val/var로 자동 프로퍼티 생성
// 2. copy() 함수 자동 생성
// 3. 생성자 파라미터가 곧 클래스 필드

// Java로 작성하면:
/*
@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String content;

    public Note(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    // ... 등등 getter/setter 모두 작성
}
*/
