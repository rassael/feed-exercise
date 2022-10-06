package com.lightricks.feedexercise.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/***
 * todo: add Room's Data Access Object interface(s) here
 */

@Dao
interface FeedDao {

    /** Inserting a list of entities. */
    // todo: what should be the return value? what happen on failure?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // replace the row when there is a conflict
    suspend fun insertListOfItems(items : List<FeedItemEntity>)

    /** deleting all entities, should return a Completable too */
    @Query("DELETE FROM feedItemsDataBase")
    suspend fun deleteAll()

    /** querying all entities, should return an Observable that contains the list of entities */
    @Query("SELECT * FROM feedItemsDataBase")
    fun getAll(): Flow<List<FeedItemEntity>>  // the flow cause an update every time the DB change

    /** querying the count of entities */
    @Query("SELECT COUNT(id) FROM feedItemsDataBase")
    fun entitiesCount() : Flow<Int>
}
