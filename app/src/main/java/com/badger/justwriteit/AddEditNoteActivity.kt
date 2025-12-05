package com.badger.justwriteit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * AddEditNoteActivity - 메모 추가/수정 화면
 *
 * Intent로 전달받는 데이터:
 * - NOTE_ID: 수정시 메모 ID (없으면 새 메모)
 * - NOTE_TITLE: 기존 제목
 * - NOTE_CONTENT: 기존 내용
 * - NOTE_IMPORTANT: 중요 여부
 */
class AddEditNoteActivity : AppCompatActivity() {
    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private lateinit var checkBoxImportant: CheckBox
    private lateinit var buttonSave: Button

    private var noteId: Int = -1
    private var createdAt: Long = 0
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_note)

        // View 초기화
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)
        checkBoxImportant = findViewById(R.id.checkBoxImportant)
        buttonSave = findViewById(R.id.buttonSave)

        // Intent에서 데이터 가져오기
        loadNoteData()

        // Toolbar 설정
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEditMode) " 메모 수정" else "새 메모"

        // 저장 버튼 클릭
        buttonSave.setOnClickListener {
            saveNote()
        }
    }

    // Intent에서 메모 데이터 로드
    private fun loadNoteData() {
        intent?.let {
            noteId = it.getIntExtra("NOTE_ID", -1)

            if (noteId != -1) {
                // 수정 모드
                isEditMode = true
                editTextTitle.setText(it.getStringExtra("NOTE_TITLE") ?: "")
                editTextContent.setText(it.getStringExtra("NOTE_CONTENT") ?: "")
                checkBoxImportant.isChecked = it.getBooleanExtra("NOTE_IMPORTANT", false)
                createdAt = it.getLongExtra("CREATED_AT", System.currentTimeMillis())
            }
        }
    }

    // 메모 저장
    private fun AddEditNoteActivity.saveNote() {
        val title = editTextTitle.text.toString().trim()
        val content = editTextContent.text.toString().trim()
        val isImportant = checkBoxImportant.isChecked

        // 입력 검증
        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력하세요", Toast.LENGTH_SHORT).show()
            editTextTitle.requestFocus()
            return
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "내용을 입력하세요", Toast.LENGTH_SHORT).show()
            editTextContent.requestFocus()
            return
        }

        // 결과 데이터 준비
        val resultIntent = Intent().apply {
            putExtra("TITLE", title)
            putExtra("CONTENT", content)
            putExtra("IMPORTANT", isImportant)

            if (isEditMode) {
                putExtra("NOTE_ID", noteId)
                putExtra("CREATED_AT", createdAt)
            }
        }

        // 결과 설정 후 종료
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    // 뒤로가기 버튼 (Toolbar)
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    // 하드웨어 뒤로가기 버튼
    override fun onBackPressed() {
        // q변경사항 있으면 확인(선택사항)
        super.onBackPressed()
    }

}










