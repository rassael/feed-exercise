package com.lightricks.feedexercise.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * todo: add the abstract class that extents RoomDatabase here
 */

@Database(entities = [FeedItemEntity ::class], version = 1)
abstract class FeedDatabase : RoomDatabase() {

    abstract val feedDao : FeedDao

    companion object {
        private var INSTANCE: FeedDatabase? = null
        fun getFeedDataBase(context : Context): FeedDatabase {
            synchronized(this) {  // keep the instance thread-safe
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context,
                            FeedDatabase::class.java, "feed_database")
                            .build()
                }
            }
            return INSTANCE as FeedDatabase
        }
    }
}
