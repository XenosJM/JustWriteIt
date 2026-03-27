package com.badger.justwriteit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.badger.justwriteit.data.category.Category
import com.badger.justwriteit.data.note.Note
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * MainActivity - 메모 목록 화면
 *
 * 주요 기능:
 * 1. 메모 목록 표시 (RecyclerView)
 * 2. 메모 추가/수정/삭제
 * 3. 검색 기능
 * 4. 스와이프로 삭제
 */
class MainActivity : AppCompatActivity() {

    // 나중에 초기화할 변수들(lateinit)
    private lateinit var viewModel: NoteViewModel
    private lateinit var adapter: NoteAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddNote: FloatingActionButton
    private lateinit var editTextSearch: EditText
    private lateinit var textViewEmpty: TextView

    // 새 버튼들
    private lateinit var btnShowAll: MaterialButton
    private lateinit var btnToggleImportant: MaterialButton
    private lateinit var btnCategoryFilter: MaterialButton

    // 현재 관찰 중인 소스를 추적하기 위한 변수
    private var currentNotesSource: LiveData<List<Note>>? = null
    private var currentCategories: List<Category> = emptyList()

    // 메모 추가용 런처 등록
    // onCreate() 이전에 선언해야 함 (lazy도 가능)
    private val addNoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val title = data.getStringExtra("TITLE") ?: ""
            val content = data.getStringExtra("CONTENT") ?: ""
            val isImportant = data.getBooleanExtra("IMPORTANT", false)
            val categoryId = data.getIntExtra("CATEGORY_ID", 1)

            // 새 메모 추가
            val note = Note(
                title = title,
                content = content,
                isImportant = isImportant,
                categoryId = categoryId
            )
            viewModel.insertNote(note)
            Toast.makeText(this, "메모 저장 완료", Toast.LENGTH_SHORT).show()
        }
    }

    // 메모 수정용 런처 등록
    private val editNoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val noteId = data.getIntExtra("NOTE_ID", -1)
            val title = data.getStringExtra("TITLE") ?: ""
            val content = data.getStringExtra("CONTENT") ?: ""
            val isImportant = data.getBooleanExtra("IMPORTANT", false)
            val createdAt = data.getLongExtra("CREATED_AT", System.currentTimeMillis())
            val categoryId = data.getIntExtra("CATEGORY_ID", 1)

            if (noteId != -1) {
                val note = Note(
                    id = noteId,
                    title = title,
                    content = content,
                    isImportant = isImportant,
                    createdAt = createdAt,
                    updatedAt = System.currentTimeMillis(),
                    categoryId = categoryId
                )
                viewModel.updateNote(note)
                Toast.makeText(this, "메모 수정 완료", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 카테고리 추가용 런처
    private val addCategoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val title = data.getStringExtra("CATEGORY_TITLE") ?: ""
            val color = data.getStringExtra("CATEGORY_COLOR") ?: ""

            val category = Category(
                title = title,
                color = color
            )
            viewModel.insertCategory(category)
            Toast.makeText(this, "카테고리 생성 완료", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar 설정
        setSupportActionBar(findViewById(R.id.toolbar))

        // View 초기화
        initViews()

        // ViewModel 초기화
        // ViewModelProvider가 ViewModel 생명주기를 관리함
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        // RecyclerView 설정
        setupRecyclerView()

        // 카테고리 목록 상시 관찰 (어댑터 색상 띠 및 필터 다이얼로그용)
        viewModel.allCategories.observe(this) { categories ->
            currentCategories = categories
            adapter.setCategories(categories)
        }

        setNotesSource(viewModel.allNotes)

        viewModel.noteCount.observe(this) { count ->
            supportActionBar?.title = "내 메모장($count)"
        }

        // 버튼 리스너 설정
        setupListeners()

        // 스와이프로 삭제 기능
        setupSwipeToDelete()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewNotes)
        fabAddNote = findViewById(R.id.fabAddNote)
        editTextSearch = findViewById(R.id.editTextSearch)
        textViewEmpty = findViewById(R.id.textViewEmpty)

        // 새 필터 버튼 초기화
        btnShowAll = findViewById(R.id.btnShowAll)
        btnToggleImportant = findViewById(R.id.btnToggleImportant)
        btnCategoryFilter = findViewById(R.id.btnCategoryFilter)
    }

    private fun setNotesSource(source: LiveData<List<Note>>) {
        // 기존 관찰자 제거 (중복 업데이트 방지)
        currentNotesSource?.removeObservers(this)
        currentNotesSource = source
        currentNotesSource?.observe(this) { notes ->
            adapter.submitList(notes)
            updateEmptyView(notes.isEmpty())
        }
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            textViewEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            textViewEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        fabAddNote.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            addNoteLauncher.launch(intent)
        }

        // 전체 보기 버튼
        btnShowAll.setOnClickListener {
            btnToggleImportant.isChecked = false
            setNotesSource(viewModel.allNotes)
        }

        // 중요 토글 버튼
        btnToggleImportant.setOnClickListener {
            if (btnToggleImportant.isChecked) {
                setNotesSource(viewModel.getImportantNotes())
            } else {
                setNotesSource(viewModel.allNotes)
            }
        }

        // 카테고리 필터 버튼 (드롭다운 UI)
        btnCategoryFilter.setOnClickListener {
            showCategoryFilterDialog(currentCategories)
        }

        // 검색창 리스너
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = editTextSearch.text.toString().trim()
                if (query.isEmpty()) {
                    setNotesSource(viewModel.allNotes)
                } else {
                    setNotesSource(viewModel.searchNotes(query))
                }
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editTextSearch.windowToken, 0)
                true
            } else false
        }
    }

    private fun showCategoryFilterDialog(categories: List<Category>) {
        if (categories.isEmpty()) {
            Toast.makeText(this, "카테고리가 없습니다. 추가해주세요.", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AddEditCategoryActivity::class.java)
            addCategoryLauncher.launch(intent)
            return
        }

        // 카테고리 이름 옆에 색상을 표시하기 위해 CharSequence 배열 생성
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
            .setTitle("카테고리 선택 (길게 눌러 삭제)")
            .setItems(categoryItems) { _, which ->
                val selectedCategory = categories[which]
                setNotesSource(viewModel.getNotesByCategory(selectedCategory.id))
                Toast.makeText(this, "${selectedCategory.title} 필터링", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("필터 해제") { _, _ ->
                setNotesSource(viewModel.allNotes)
            }
            .setPositiveButton("카테고리 추가") { _, _ ->
                val intent = Intent(this, AddEditCategoryActivity::class.java)
                addCategoryLauncher.launch(intent)
            }
            .show().apply {
                // 다이얼로그의 리스트 뷰를 가져와서 롱클릭 리스너 설정
                val listView = this.listView
                listView?.setOnItemLongClickListener { _, _, position, _ ->
                    val selectedCategory = categories[position]
                    showCategoryDeleteConfirmDialog(selectedCategory)
                    this.dismiss() // 롱클릭 시 다이얼로그 닫기
                    true
                }
            }
    }

    private fun showCategoryDeleteConfirmDialog(category: Category) {
        MaterialAlertDialogBuilder(this)
            .setTitle("카테고리 삭제")
            .setMessage("'${category.title}' 카테고리를 삭제하시겠습니까?\n이 카테고리의 모든 메모는 '기본' 카테고리로 이동됩니다.")
            .setPositiveButton("삭제") { _, _ ->
                viewModel.deleteCategory(category)
                Toast.makeText(this, "카테고리가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                // 삭제 후 필터 해제 (필터링 중이었다면)
                setNotesSource(viewModel.allNotes)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    // RecyclerView 설정
    private fun setupRecyclerView() {
        // Adapter 생성
        adapter = NoteAdapter(
            onNoteClick = { note ->
                // 메모 클릭 -> 수정 화면으로
                // ✅ 새 방식: launcher.launch()
                val intent = Intent(this, AddEditNoteActivity::class.java).apply {
                    putExtra("NOTE_ID", note.id)
                    putExtra("NOTE_TITLE", note.title)
                    putExtra("NOTE_CONTENT", note.content)
                    putExtra("NOTE_IMPORTANT", note.isImportant)
                    putExtra("CREATED_AT", note.createdAt)
                    putExtra("CATEGORY_ID", note.categoryId)
                }
                editNoteLauncher.launch(intent)
            },
            onNoteLongClick = { note ->
                // 롱클릭 -> 삭제 확인 다이얼로그
                showDeleteConfirmDialog(note)
            }
        )

        // RecyclerView 설정
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    // 스와이프로 삭제 기능
    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // 스와이프된 위치의 메모 가져오기
                    val position = viewHolder.bindingAdapterPosition

                    // 유효한 위치인지 확인(필수!)
                    if (position != RecyclerView.NO_POSITION) {
                        showDeleteConfirmDialog(adapter.getNoteAt(position))
                    }
                }
            }
        )

        // RecyclerView에 ItemTouchHelper 연결
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    // 삭제 확인 다이얼로그
    private fun showDeleteConfirmDialog(note: Note) {
        MaterialAlertDialogBuilder(this)
            .setTitle("메모 삭제")
            .setMessage("'${note.title}'를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                // 클릭 처리
                viewModel.deleteNote(note)
                Toast.makeText(this, "삭제되었습니다", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소") { _, _ ->
                adapter.notifyDataSetChanged()
            }
            .show()
    }

    // 옵션 메뉴 생성
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // 옵션 메뉴 클릭
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // 모든 메모 삭제 버튼
            R.id.action_delete_all -> {
                showDeleteAllConfirmDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 전체 삭제 확인 다이얼로그
    private fun showDeleteAllConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("모든 메모 삭제")
            .setMessage("정말로 모든 메모를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                viewModel.deleteAllNote()
                Toast.makeText(this, "모든 메모 삭제 완료", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}



// ========================================
// Activity Result API 설명
// ========================================

/*
✅ 새 방식의 장점:

1. 타입 안전성
   - 계약(Contract)으로 입출력 타입 명확
   - 컴파일 타임에 오류 검사

2. 간결한 코드
   - Request code 불필요
   - 각 launcher마다 별도 콜백

3. 생명주기 인식
   - Activity가 파괴되어도 안전
   - 결과 손실 방지

4. 테스트 용이
   - Mock 테스트 쉬움


❌ 구식 방식 (Deprecated):
startActivityForResult(intent, REQUEST_CODE)
override fun onActivityResult(requestCode: Int, ...)

✅ 새 방식:
val launcher = registerForActivityResult(Contract) { result -> }
launcher.launch(intent)


📝 주의사항:
- registerForActivityResult()는 onCreate() 전에 호출
- 클래스 필드로 선언 (lazy도 가능)
- 동적으로 생성하면 안됨 (메모리 누수)
*/


// 핵심 개념 정리:
//
// 1. ViewModel + LiveData
//    - 데이터 변경시 자동 UI 업데이트
//    - observe() 한번만 설정하면 끝
//
// 2. RecyclerView + ListAdapter
//    - submitList()만 호출하면 자동 갱신
//    - DiffUtil이 변경사항 계산
//
// 3. 코루틴 (viewModelScope)
//    - DB 작업을 백그라운드에서 자동 실행
//    - UI 스레드 블록 없음
//
// 4. ItemTouchHelper
//    - 스와이프로 삭제 기능
//    - 드래그앤드롭도 가능

/*
// 버튼 리스너 설정
    private fun setupListeners() {
        // FAB 클릭 - 새 메모 추가
        fabAddNote.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            addNoteLauncher.launch(intent)
        }

        // 검색창 텍스트 변경 리스너
        editTextSearch.setOnEditorActionListener {_, actionId, _ ->
            // TODO 제목으로 찾기와 제목과 내용으로 찾기 기능을 사용하기 위한 체크포인트를 만들어볼것
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = editTextSearch.text.toString().trim()

                if (query.isEmpty()) {
                    // 검색어 없으면 전체 표시
                    viewModel.allNotes.observe(this@MainActivity) { notes ->
                        adapter.submitList(notes)
                    }
                } else {
                    // 검색 실행
                    viewModel.searchNotes(query).observe(this@MainActivity) { notes ->
                        // 검색 실행후 키보드 닫기
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(editTextSearch.windowToken, 0)

                        adapter.submitList(notes)
                    }
                }
                true
            } else {
                false
            }
        }

        // 검색 초기화 버튼
        btnClearSearch.setOnClickListener {
            editTextSearch.text.clear()
            viewModel.allNotes.observe(this) { notes ->
                adapter.submitList(notes)
            }
        }

        /** TODO 카테고리 목록 불러오기 기능 버튼, 카테고리 삭제는 메모가 똑같이 작동하도록 만들것
         * 카테고리 불러오기 기능 버튼이 클릭이 되면 액션바가 생겨서 뒤로가기 버튼이 새로 생기도록 하며,
         * fab 버튼의 기능 또한 메모 추가가 아닌 카테고리 추가 버튼으로 변경 되도록할것 또는 카테고리 목록
         * 맨 아래 또는 맨 윗쪽에 새로 버튼영역이 생기도록하고 거길 눌러서 추가 하도록 하는 버튼을 생성할것.
         */
    }



 */



