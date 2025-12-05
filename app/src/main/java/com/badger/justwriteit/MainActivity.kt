package com.badger.justwriteit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.badger.justwriteit.data.Note
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
    private lateinit var btnClearSearch: Button
    private lateinit var textViewEmpty: TextView

    // Activity Result API - 최신 방식!

    // 메모 추가용 런처 등록
    // onCreate() 이전에 선언해야 함 (lazy도 가능)
    private val addNoteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult() // 계약(contract)
    ) { result ->
        // result.resultCode: RESULT_OK, RESULT_CANCELED 등
        // result.data: Intent?

        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!! // 데이터값이 null이 아님을 확신하고 강제로 non null로 강제하는 문법
            val title = data.getStringExtra("TITLE") ?: ""
            val content = data.getStringExtra("CONTENT") ?: ""
            val isImportant = data.getBooleanExtra("IMPORTANT", false)

            // 새 메모 추가
            val note = Note(
                title = title,
                content = content,
                isImportant = isImportant
            )
            viewModel.insert(note)
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

            if (noteId != -1) {
                val note = Note(
                    id = noteId,
                    title = title,
                    content = content,
                    isImportant = isImportant,
                    createdAt = createdAt,
                    updatedAt = System.currentTimeMillis()
                )
                viewModel.updae(note)
                Toast.makeText(this, "메모 수정 완료", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= 35) {
            // API 35 이상 전용 UI 설정
            changeUI()
        } else {
            // API 34 이하 UI 설정
        }

        // Toolbar 설정
        setSupportActionBar(findViewById(R.id.toolbar))

        // View 초기화
        initViews()

        // ViewModel 초기화
        // ViewModelProvider가 ViewModel 생명주기를 관리함
        viewModel = ViewModelProvider(this)[NoteViewModel::class.java]

        // RecyclerView 설정
        setupRecyclerView()

        // LiveData 관찰 시작
        observeData()

        // 버튼 리스너 설정
        setupListeners()

        // 스와이프로 삭제 기능
        setupSwipeToDelete()
    }

    // Build 버전에 따른UI 변경 함수
    private fun changeUI() {
        val root = findViewById<View>(R.id.lootLayout)
        val fabBtn = findViewById<View>(R.id.fabAddNote)

        // 상태표시줄 만큼 툴바를 내려주는 코드
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            val navigationBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.setPadding(0, statusBar, 0, navigationBar)
            insets
        }

        // fabAddNote 버튼 올리는 코드
        ViewCompat.setOnApplyWindowInsetsListener(fabBtn) { view, insets ->
            val navigationBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = navigationBar + 32 // 32dp, 여유 padding
            }
            insets
        }
    }


    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewNotes)
        fabAddNote = findViewById(R.id.fabAddNote)
        editTextSearch = findViewById(R.id.editTextSearch)
        btnClearSearch = findViewById(R.id.btnClearSearch)
        textViewEmpty = findViewById(R.id.textViewEmpty)
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
                }
                editNoteLauncher.launch(intent) // 런처사용
            },
            onNoteLongClick = { note ->
                // 롱클릭 -> 삭제 확인 다이얼로그
                showDeleteConfirmDialog(note)
            }
        )

        // RecyclerView 설정
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        // 아이템 간격 추가 (선택사항)
        // recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    // LiveData 관찰
    // 데이터 변경시 자동으로 UI 업데이트
    private fun observeData() {
        // 모든 메모 관찰
        viewModel.allNotes.observe(this) { notes ->
            // 리스트가 변경될 때마다 호출됨
            adapter.submitList(notes)

            // 메모 없을 때 안내 메시지 표시
            if (notes.isEmpty()) {
                textViewEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                textViewEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        // 메모 개수 관찰 (선택사항)
        viewModel.noteCount.observe(this) { count ->
            // Toolbar 제목 업데이트
            supportActionBar?.title = "내 메모장($count)"
        }
    }

    // 버튼 리스너 설정
    private fun setupListeners() {
        // FAB 클릭 - 새 메모 추가
        fabAddNote.setOnClickListener {
            val intent = Intent(this, AddEditNoteActivity::class.java)
            addNoteLauncher.launch(intent)
        }

        // 검색창 텍스트 변경 리스너
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 실시간 검색
                val query = s.toString()
                if (query.isEmpty()) {
                    // 검색어 없으면 전체 표시
                    viewModel.allNotes.observe(this@MainActivity) { notes ->
                        adapter.submitList(notes)
                    }
                } else {
                    // 검색 실행
                    viewModel.searchNotes(query).observe(this@MainActivity) { notes ->
                        adapter.submitList(notes)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 검색 초기화 버튼
        btnClearSearch.setOnClickListener {
            editTextSearch.text.clear()
            viewModel.allNotes.observe(this) { notes ->
                adapter.submitList(notes)
            }
        }
    }

    // 스와이프로 삭제 기능
    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(
            object  : ItemTouchHelper.SimpleCallback(
                0, // 드래그 방향 (0 = 드래그 비활성화)
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT // 스와이프 방향
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean = false

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // 스와이프된 위치의 메모 가져오기
                    val position = viewHolder.bindingAdapterPosition

                    // 유효한 위치인지 확인(필수!)
                    if (position != RecyclerView.NO_POSITION) {
                        val note = adapter.getNoteAt(position)
                        showDeleteConfirmDialog(note) // 스크롤하다 실수로 바로 삭제될수있기에 보완하기위해 함수 사용

//                        // 메모 삭제
//                        viewModel.delete(note)
//
//                        // Toast 메시지
//                        Toast.makeText(
//                            this@MainActivity,
//                            "${note.title} 삭제됨",
//                            Toast.LENGTH_SHORT
//                        ).show()
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
                viewModel.delete(note)
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
            R.id.action_delete_all -> {
                showDeleteAllConfirmDialog()
                true
            }
            R.id.action_sample_data -> {
                viewModel.insertSampleData()
                Toast.makeText(this, "샘플 데이터 추가완료", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_important -> {
                viewModel.getImportantNotes().observe(this) { notes ->
                    adapter.submitList(notes)
                }
                Toast.makeText(this, "중요 메모만 표시", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 전체 삭제 확인 다이얼로그
    private fun showDeleteAllConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("모든 메모 삭제")
            .setMessage("정말로 모든 메모를 삭제하시겠습니까?\n이 작업은 되돌릴 수 없습니다.")
            .setPositiveButton("삭제") { _, _ ->
                viewModel.deleteAll()
                Toast.makeText(this, "모든 메모가 삭제되었습니다", Toast.LENGTH_SHORT).show()
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









