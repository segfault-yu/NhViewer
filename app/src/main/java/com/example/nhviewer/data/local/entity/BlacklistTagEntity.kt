package com.example.nhviewer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nhviewer.domain.model.Tag

@Entity(tableName = "blacklist_tag")
data class BlacklistTagEntity(
    @PrimaryKey val tagId: Int,
    val name: String,
    val type: String
)

fun BlacklistTagEntity.toDomain() = Tag(
    id = tagId,
    type = type,
    name = name,
    slug = name.lowercase().replace(" ", "-"),
    url = "/tag/$name",
    count = 0,
    description = null,
    isCommunity = null
)
