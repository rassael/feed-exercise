package com.lightricks.feedexercise.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lightricks.feedexercise.network.FeedApiService

/**
 * todo: add the abstract class that extents RoomDatabase here
 */

@Database(entities = [FeedItemEntity ::class], version = 1)
abstract class FeedDatabase : RoomDatabase() {

    abstract val feedDao : FeedDao

    companion object {
        @Volatile private var INSTANCE: FeedDatabase? = null

        fun getInstance(context : Context): FeedDatabase {
            val instance = INSTANCE
            if (instance != null) {
                return instance
            }
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context,
                        FeedDatabase::class.java, "feed_database"
                    )
                        .build()
                }
                return INSTANCE!!
            }
        }
    }
}
