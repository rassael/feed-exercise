package com.lightricks.feedexercise.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * todo: add Room's Entity data class(es) here
 */

@Entity(tableName = "feedItemsDataBase")
data class FeedItemEntity (
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "thumbnailUrl") val thumbnailUrl: String,
    @ColumnInfo(name = "isPremium") val isPremium: Boolean
)
