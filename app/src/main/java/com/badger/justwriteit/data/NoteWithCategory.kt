package com.badger.justwriteit.data

import androidx.room.Embedded
import androidx.room.Relation
import com.badger.justwriteit.data.category.Category
import com.badger.justwriteit.data.note.Note

data class NoteWithCategory(
    @Embedded val note: Note,

    @Relation(
        parentColumn = "category_id",
        entityColumn = "id"
    )
    val category: Category
)
