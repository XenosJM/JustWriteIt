package com.badger.justwriteit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt

// 카테고리 추가/ 수정 화면

// Intent로 전달받는 데이터:
// CATEGORY_ID : Int (수정 모드일때만)
// CATEGORY_TITLE : String
// CATEGORY_COLOR : String(Hex)

class AddEditCategoryActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var dropdownColor: AutoCompleteTextView
    private lateinit var colorPreview: View
    private lateinit var buttonSave: Button

    private var categoryId: Int = -1
    private var isEditMode = false

    // 화면에서 사용할 색상 목록 (이름 → HEX)
    private val colorMap = linkedMapOf(
        "Red" to "#F44336",
        "Blue" to "#2196F3",
        "Green" to "#4CAF50",
        "Orange" to "#FF9800",
        "Purple" to "#9C27B0",
        "Gray" to "#9E9E9E"
    )
    // 드롭다운에 보여줄 텍스트 목록
    private val colorNames by lazy { colorMap.keys.toList() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_category)

        // view 초기화
        editTextTitle = findViewById(R.id.editTextCategoryName)
        dropdownColor = findViewById(R.id.dropdownCategoryColor)
        colorPreview = findViewById(R.id.viewColorPreview)
        buttonSave = findViewById(R.id.buttonSaveCategory)

        //  Intent에서 데이터 가져오기
        loadCategoryData()

        // 툴바 설정
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if(isEditMode) "카테고리 수정" else "카테고리 생성"


        // 드롭다운 어댑터 세팅
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            colorNames
        )
        dropdownColor.setAdapter(adapter)

        dropdownColor.setOnItemClickListener { _, _, position, _ ->
            val selectedName = colorNames[position]
            val hexColor = colorMap[selectedName]!!

            // 미리보기 색상 변경
            colorPreview.setBackgroundColor(hexColor.toColorInt())
        }

        buttonSave.setOnClickListener {
            saveCategory()
        }

    }

    // Intent에서 메모 데이터 로드
    private fun loadCategoryData() {
        intent?.let {
            categoryId = it.getIntExtra("CATEGORY_ID", -1)

            if (categoryId != -1) {
                // 수정 모드
                isEditMode = true
                editTextTitle.setText(it.getStringExtra("CATEGORY_TITLE") ?: "")
                /* // 제목
                    val title = intent.getStringExtra("CATEGORY_TITLE") ?: ""
                    editTextTitle.setText(title)
                */
                // 색상(Hex) -> 이름으로 역매핑후 드롭다운 표시
                val hex = it.getStringExtra("CATEGORY_COLOR") ?: ""
                val colorName = colorMap.entries.firstOrNull { it.value == hex }?.key
                if (colorName != null) {

                    dropdownColor.setText(colorName, false) // false: 필터링/ 자동완성 동작 억제
                    colorPreview.setBackgroundColor(hex.toColorInt())
                } else {
                    // 혹시나 데이터가 없는경우
                    dropdownColor.setText("", false)
                }
            }
        }
    }

    // 카테고리 저장
    private fun AddEditCategoryActivity.saveCategory() {

        val title = editTextTitle.text.toString().trim()
        val selectedColorName = dropdownColor.text.toString().trim()
        val colorHex = colorMap[selectedColorName]

        // 입력 검증
        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력하세요", Toast.LENGTH_SHORT).show()
            editTextTitle.requestFocus()
            return
        }

        if (colorHex == null) {
            Toast.makeText(this, "색상을 선택하세요", Toast.LENGTH_SHORT).show()
            dropdownColor.requestFocus()
            return
        }

        // 결과 데이터 준비
        val resultIntent = Intent().apply {
            putExtra("TITLE", title)
            putExtra("COLOR", colorHex)

            if (isEditMode) {
                putExtra("CATEGORY_ID", categoryId )
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

}
