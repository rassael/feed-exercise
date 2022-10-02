package com.lightricks.feedexercise.data

import androidx.room.withTransaction
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.database.FeedItemEntity
import com.lightricks.feedexercise.network.FeedApiService
import com.lightricks.feedexercise.network.GetFeedResponse
import com.lightricks.feedexercise.network.ItemDto
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * This is our data layer abstraction. Users of this class don't need to know
 * where the data actually comes from (network, database or somewhere else).
 */
class FeedRepository(private val feedDatabase: FeedDatabase) {

    private val feedApiService: FeedApiService = FeedApiService.getFeedApiService()

    suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val retrofitResponse: GetFeedResponse = feedApiService.getFeedItems()
            val itemDto: List<ItemDto> = retrofitResponse.itemDto
            val feedItems: List<FeedItemEntity> = itemDto.toEntities()
            feedDatabase.withTransaction {  // run this block as an atomic command in order to add
                // the new items immediately after the deleting (prevent the white screen)
                feedDatabase.feedDao.deleteAll()
                feedDatabase.feedDao.insertListOfItems(feedItems)
            }
        }
    }

    fun getItems(): Flow<List<FeedItem>> {
        return feedDatabase.feedDao.getAll().map { it.toFeedItems() }
    }
}

private const val uriPrefix: String =
    "https://assets.swishvideoapp.com/Android/demo/catalog/thumbnails/"

fun List<FeedItemEntity>.toFeedItems(): List<FeedItem> {
    return map {
        FeedItem(it.id, uriPrefix + it.thumbnailUrl, it.isPremium)
    }
}

fun List<ItemDto>.toEntities(): List<FeedItemEntity> {
    return map {
        FeedItemEntity(
            it.id,
            it.templateThumbnailURI,
            it.isPremium
        )
    }
}
