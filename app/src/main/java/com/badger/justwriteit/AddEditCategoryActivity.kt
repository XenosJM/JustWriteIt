package com.badger.justwriteit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape

class AddEditCategoryActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var colorPreview: View
    private lateinit var buttonSave: Button

    private var selectedColorHex: String = "#9E9E9E"
    private var categoryId: Int = -1
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-Edge 설정
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContentView(R.layout.activity_add_edit_category)

        initViews()
        setupEdgeToEdge()
        loadCategoryData()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if(isEditMode) "카테고리 수정" else "카테고리 생성"

        colorPreview.setOnClickListener { showColorPicker() }
        findViewById<View>(R.id.layoutColorPicker).setOnClickListener { showColorPicker() }
        buttonSave.setOnClickListener { saveCategory() }

        updateStatusBarColor(selectedColorHex)
    }

    private fun initViews() {
        editTextTitle = findViewById(R.id.editTextCategoryName)
        colorPreview = findViewById(R.id.viewColorPreview)
        buttonSave = findViewById(R.id.buttonSaveCategory)
    }

    /**
     * 시스템 바와 콘텐츠가 겹치지 않도록 자동 패딩 적용
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

    private fun showColorPicker() {
        ColorPickerDialog
            .Builder(this)
            .setTitle("카테고리 색상 선택")
            .setColorShape(ColorShape.CIRCLE)
            .setDefaultColor(selectedColorHex)
            .setColorListener { color, colorHex ->
                selectedColorHex = colorHex
                colorPreview.setBackgroundColor(color)
                updateStatusBarColor(colorHex)
            }
            .show()
    }

    /**
     * 배경색 밝기에 따라 상태 표시줄 아이콘 색상을 검정/흰색으로 자동 반전
     */
    private fun updateStatusBarColor(colorHex: String) {
        try {
            val color = Color.parseColor(colorHex)
            window.statusBarColor = color
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            windowInsetsController.isAppearanceLightStatusBars = darkness < 0.5
        } catch (e: Exception) {}
    }

    private fun isColorLight(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness < 0.5
    }

    private fun loadCategoryData() {
        intent?.let {
            categoryId = it.getIntExtra("CATEGORY_ID", -1)
            if (categoryId != -1) {
                isEditMode = true
                editTextTitle.setText(it.getStringExtra("CATEGORY_TITLE") ?: "")
                selectedColorHex = it.getStringExtra("CATEGORY_COLOR") ?: "#9E9E9E"
                colorPreview.setBackgroundColor(Color.parseColor(selectedColorHex))
                updateStatusBarColor(selectedColorHex)
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
            // [중요 수정] "putExtra"라는 잘못된 키를 "CATEGORY_COLOR"로 수정하여 데이터를 정상 전달함
            putExtra("CATEGORY_COLOR", selectedColorHex)
            if (isEditMode) putExtra("CATEGORY_ID", categoryId)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
