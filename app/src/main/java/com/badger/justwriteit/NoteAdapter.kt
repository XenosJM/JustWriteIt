package com.badger.justwriteit

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.badger.justwriteit.data.category.Category
import com.badger.justwriteit.data.note.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView Adapter - 리스트 아이템을 표시
 *
 * ListAdapter vs RecyclerView.Adapter:
 * - ListAdapter: DiffUtil 자동 처리 (추천!)
 * - RecyclerView.Adapter: 수동 notify 필요
 */
class NoteAdapter (
    private val onNoteClick: (Note) -> Unit, // 콜백
    private val onNoteLongClick: (Note) -> Unit // 롱클릭 콜백
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    private var categories: List<Category> = emptyList()

    fun setCategories(categories: List<Category>) {
        this.categories = categories
        notifyDataSetChanged()
    }

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val contentTextView: TextView = itemView.findViewById(R.id.textViewContent)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        private val importantIndicator: View = itemView.findViewById(R.id.viewImportant)
        private val categoryTag: View = itemView.findViewById(R.id.viewCategoryTag)

        fun bind(note: Note, categories: List<Category>, onNoteClick: (Note) -> Unit, onNoteLongClick: (Note) -> Unit) {
            titleTextView.text = note.title
            contentTextView.text = note.content
            dateTextView.text = formatDate(note.createdAt)

            // 중요 메모 표시 (빨간 띠)
            importantIndicator.visibility = if (note.isImportant) View.VISIBLE else View.GONE

            // 카테고리 색상 띠 적용
            val category = categories.find { it.id == note.categoryId }
            if (category != null) {
                try {
                    categoryTag.setBackgroundColor(Color.parseColor(category.color))
                } catch (e: Exception) {
                    categoryTag.setBackgroundColor(Color.LTGRAY)
                }
            } else {
                categoryTag.setBackgroundColor(Color.LTGRAY)
            }

            itemView.setOnClickListener { onNoteClick(note) }
            itemView.setOnLongClickListener {
                onNoteLongClick(note)
                true
            }
        }

        // 날짜 포맷
        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    // ViewHolder 생성
    // 레이아웃 inflate ( XML 파일을 메모리에 실제 View로 변환하는 것)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView)
    }

    // ViewHolder에 데이터 바인딩
    // position에 해당하는 데이터를 표시
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = getItem(position)
        holder.bind(note, categories, onNoteClick, onNoteLongClick)
    }

    // 특정 위치의 Note 가져오기
    // 외부에서 점근할 수 있도록 public 함수
    fun getNoteAt(position: Int): Note {
        return getItem(position)
    }
}

/**
 * DiffUtil.ItemCallback - 리스트 변경 감지
 *
 * 효율적인 업데이트:
 * - 전체 리스트 갱신 대신 변경된 아이템만 업데이트
 * - 자동으로 애니메이션 적용
 */
class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean = oldItem == newItem
}


// RecyclerView 작동 원리:
//
// 1. onCreateViewHolder(): 뷰 생성 (재사용 가능한 뷰 풀 생성)
// 2. onBindViewHolder(): 데이터 바인딩 (스크롤시 계속 호출)
// 3. getItemCount(): 아이템 개수
//
// ListAdapter의 장점:
// - submitList()만 호출하면 자동으로 변경 감지
// - notifyDataSetChanged() 불필요
// - 애니메이션 자동 적용
//
// Java에서는:
// - notifyItemInserted(), notifyItemRemoved() 수동 호출
// - 복잡한 상태 관리
// - 애니메이션 수동 구현

// Kotlin 람다의 장점:
// - onNoteClick: (Note) -> Unit
//   = Java의 interface OnNoteClickListener { void onClick(Note note); }
// - 훨씬 간결하고 타입 안전







