package com.badger.justwriteit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape

// 카테고리 추가/ 수정 화면

// Intent로 전달받는 데이터:
// CATEGORY_ID : Int (수정 모드일때만)
// CATEGORY_TITLE : String
// CATEGORY_COLOR : String(Hex)

class AddEditCategoryActivity : AppCompatActivity() {


    private lateinit var editTextTitle: EditText
    private lateinit var colorPreview: View // 이제 이 뷰를 클릭해서 색상을 변경합니다.
    private lateinit var buttonSave: Button

    private var selectedColorHex: String = "#9E9E9E" // 기본값 (Gray)
    private var categoryId: Int = -1
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_category)

        initViews()
        loadCategoryData()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if(isEditMode) "카테고리 수정" else "카테고리 생성"

        // 색상 미리보기 뷰 클릭 시 컬러 피커 실행
        colorPreview.setOnClickListener {
            showColorPicker()
        }
        // 색상 선택 영역 전체(텍스트 포함) 클릭시에도 작동
        findViewById<View>(R.id.layoutColorPicker).setOnClickListener {
            showColorPicker()
        }

        buttonSave.setOnClickListener {
            saveCategory()
        }
    }

    private fun initViews() {
        editTextTitle = findViewById(R.id.editTextCategoryName)
        colorPreview = findViewById(R.id.viewColorPreview)
        buttonSave = findViewById(R.id.buttonSaveCategory)

        // 미리보기 뷰를 동그랗게 보이게 하거나 테두리를 주면 더 좋습니다. (XML에서 설정 추천)
    }

    /**
     * 자유로운 컬러 선택을 위한 다이얼로그 표시
     */
    private fun showColorPicker() {
        ColorPickerDialog
            .Builder(this)
            .setTitle("카테고리 색상 선택")
            .setColorShape(ColorShape.CIRCLE) // 원형 모양
            .setDefaultColor(selectedColorHex) // 현재 선택된 색상
            .setColorListener { color, colorHex ->
                // 사용자가 색상을 선택했을 때 호출됨
                selectedColorHex = colorHex
                colorPreview.setBackgroundColor(color)
            }
            .show()
    }

    private fun loadCategoryData() {
        intent?.let {
            categoryId = it.getIntExtra("CATEGORY_ID", -1)
            if (categoryId != -1) {
                isEditMode = true
                editTextTitle.setText(it.getStringExtra("CATEGORY_TITLE") ?: "")

                // 전달받은 Hex 값을 전역 변수에 저장하고 미리보기에 적용
                selectedColorHex = it.getStringExtra("CATEGORY_COLOR") ?: "#9E9E9E"
                colorPreview.setBackgroundColor(Color.parseColor(selectedColorHex))
            }
        }
    }

    private fun saveCategory() {
        val title = editTextTitle.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent().apply {
            putExtra("CATEGORY_TITLE", title)
            putExtra("CATEGORY_COLOR", selectedColorHex) // 선택된 Hex 전달
            if (isEditMode) {
                putExtra("CATEGORY_ID", categoryId)
            }
        }

        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

/*
    private lateinit var editTextTitle: EditText
    private lateinit var dropdownColor: AutoCompleteTextView
    private lateinit var colorPreview: View
    private lateinit var buttonSave: Button
    private lateinit var viewModel: NoteViewModel

    private var categoryId: Int = -1
    private var isEditMode = false

    // 화면에서 사용할 색상 목록 (이름 → HEX)
    private val colorMap = linkedMapOf(
        "default" to "#00000000" ,
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
        initViews()

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

    private fun initViews() {
        editTextTitle = findViewById(R.id.editTextCategoryName)
        dropdownColor = findViewById(R.id.dropdownCategoryColor)
        colorPreview = findViewById(R.id.viewColorPreview)
        buttonSave = findViewById(R.id.buttonSaveCategory)
    }

    // Intent에서 메모 데이터 로드
    private fun loadCategoryData() {
        intent?.let {
            categoryId = it.getIntExtra("CATEGORY_ID", -1)

            if (categoryId != -1) {
                // 수정 모드
                isEditMode = true
                editTextTitle.setText(it.getStringExtra("CATEGORY_TITLE") ?: "")
                *//* // 제목
                    val title = intent.getStringExtra("CATEGORY_TITLE") ?: ""
                    editTextTitle.setText(title)
                *//*
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

        if (colorHex == "TRANSPARENT") {
            Toast.makeText(this, "색상을 선택하세요", Toast.LENGTH_SHORT).show()
            dropdownColor.requestFocus()
            return
        }

        // 결과 데이터 준비
        val resultIntent = Intent().apply {
            putExtra("CATEGORY_TITLE", title)
            putExtra("CATEGORY_COLOR", colorHex)

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

}*/
