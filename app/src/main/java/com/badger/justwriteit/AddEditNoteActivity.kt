package com.badger.justwriteit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.badger.justwriteit.data.category.Category
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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
    
    private lateinit var layoutCategorySelect: View
    private lateinit var viewCategoryColor: View
    private lateinit var textViewCategoryName: TextView
    
    private lateinit var viewModel: NoteViewModel

    private var noteId: Int = -1
    private var createdAt: Long = 0
    private var isEditMode = false
    private var selectedCategoryId: Int = 1 // 기본 카테고리 ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-Edge 설정 (배경 확장)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_add_edit_note)

        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        // View 초기화
        initViews()
        
        // Edge-to-Edge 패딩 설정 (콘텐츠 겹침 방지)
        setupEdgeToEdge()
        
        // Intent에서 데이터 가져오기
        loadNoteData()

        // Toolbar 설정
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (isEditMode) "메모 수정" else "새 메모"

        // 카테고리 선택 클릭
        layoutCategorySelect.setOnClickListener {
            showCategorySelectionDialog()
        }

        // 저장 버튼 클릭
        buttonSave.setOnClickListener {
            saveNote()
        }
        
        // 초기 카테고리 표시 업데이트
        updateCategoryUI()
    }

    private fun initViews() {
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)
        checkBoxImportant = findViewById(R.id.checkBoxImportant)
        buttonSave = findViewById(R.id.buttonSave)

        layoutCategorySelect = findViewById(R.id.layoutCategorySelect)
        viewCategoryColor = findViewById(R.id.viewCategoryColor)
        textViewCategoryName = findViewById(R.id.textViewCategoryName)
    }

    /**
     * 상태표시줄과 내비게이션 바 영역을 계산하여 최상위 뷰에 패딩 적용
     */
    private fun setupEdgeToEdge() {
        val rootLayout = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
    }

    /**
     * 화면의 빈 공간이나 다른 뷰를 탭했을 때 EditText의 포커스를 해제하고 키보드를 숨깁니다.
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    v.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
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
                selectedCategoryId = it.getIntExtra("CATEGORY_ID", 1)
            }
        }
    }

    private fun showCategorySelectionDialog() {
        val categories = viewModel.allCategories.value ?: emptyList()
        if (categories.isEmpty()) {
            Toast.makeText(this, "카테고리가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryItems = categories.map { category ->
            val label = "● ${category.title}"
            val spannable = SpannableString(label)
            try {
                val colorInt = Color.parseColor(category.color)
                spannable.setSpan(
                    ForegroundColorSpan(colorInt),
                    0, 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } catch (e: Exception) {}
            spannable
        }.toTypedArray()

        MaterialAlertDialogBuilder(this)
            .setTitle("카테고리 선택")
            .setItems(categoryItems) { _, which ->
                val selected = categories[which]
                selectedCategoryId = selected.id
                updateCategoryUI(selected)
            }
            .show()
    }

    private fun updateCategoryUI(category: Category? = null) {
        if (category != null) {
            textViewCategoryName.text = category.title
            try {
                val color = Color.parseColor(category.color)
                viewCategoryColor.setBackgroundColor(color)
                updateStatusBarColor(category.color) // 카테고리 색상에 맞춰 상태바 업데이트
            } catch (e: Exception) {
                viewCategoryColor.setBackgroundColor(Color.LTGRAY)
            }
        } else {
            // 초기 로딩 시 또는 카테고리 정보가 없을 때 ViewModel에서 찾기
            viewModel.allCategories.observe(this) { categories ->
                val found = categories.find { it.id == selectedCategoryId }
                if (found != null) {
                    textViewCategoryName.text = found.title
                    try {
                        val color = Color.parseColor(found.color)
                        viewCategoryColor.setBackgroundColor(color)
                        updateStatusBarColor(found.color)
                    } catch (e: Exception) {
                        viewCategoryColor.setBackgroundColor(Color.LTGRAY)
                    }
                }
            }
        }
    }

    /**
     * 상태 표시줄 배경색을 변경하고 아이콘 가독성을 확보합니다.
     */
    private fun updateStatusBarColor(colorHex: String) {
        try {
            val color = Color.parseColor(colorHex)
            window.statusBarColor = color
            
            // 색상 밝기에 따라 아이콘 색상 반전 (WindowInsetsController 사용)
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            windowInsetsController.isAppearanceLightStatusBars = darkness < 0.5
        } catch (e: Exception) {}
    }

    private fun saveNote() {
        val title = editTextTitle.text.toString().trim()
        val content = editTextContent.text.toString().trim()
        val isImportant = checkBoxImportant.isChecked

        // 입력 검증
        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 결과 데이터 준비
        val resultIntent = Intent().apply {
            putExtra("TITLE", title)
            putExtra("CONTENT", content)
            putExtra("IMPORTANT", isImportant)
            putExtra("CATEGORY_ID", selectedCategoryId)

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
}
