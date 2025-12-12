package com.badger.justwriteit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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

    // ViewHolder - 각 아이템의 뷰를 보관
    // Java의 static class와 유사하지만 더 간결함

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewTitle)
        private val contentTextView: TextView = itemView.findViewById(R.id.textViewContent)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewDate)
        private val importantIndicator: View = itemView.findViewById(R.id.viewImportant)

        // 데이터를 뷰에 바인딩
        fun bind(note: Note, onNoteClick: (Note) -> Unit, onNoteLongClick: (Note) -> Unit) {
            titleTextView.text = note.title
            contentTextView.text = note.content
            dateTextView.text = formatDate(note.createdAt)

            // 중요 메모 표시
            importantIndicator.visibility = if (note.isImportant) View.VISIBLE else View.GONE

            // 클릭 리스너
            itemView.setOnClickListener { onNoteClick(note) }

            // 롱클릭 리스너 (삭제용)
            itemView.setOnLongClickListener {
                onNoteLongClick(note)
                true // 이벤트 소비
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
        val note = getItem(position) // ListAdapter가 제공하는 함수
        holder.bind(note, onNoteClick, onNoteLongClick)
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

    // 같은 아이템인지 확인 (ID 비교)
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    // 내용이 같은지 확인 (전체 데이터 비교)
    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        // data class는 자동으로 equals() 구현
        return oldItem == newItem
    }
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







