package com.lightricks.feedexercise.data

import androidx.room.withTransaction
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.database.FeedItemEntity
import com.lightricks.feedexercise.network.FeedApiService
import com.lightricks.feedexercise.network.GetFeedResponse
import com.lightricks.feedexercise.network.TemplatesMetadataItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * This is our data layer abstraction. Users of this class don't need to know
 * where the data actually comes from (network, database or somewhere else).
 */
class FeedRepository(private val feedDatabase: FeedDatabase) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val baseURI: String = "https://assets.swishvideoapp.com/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(baseURI)
        .build()
    private val feedApiService: FeedApiService = retrofit.create(FeedApiService::class.java)

    suspend fun refresh() {
        withContext(Dispatchers.IO) {
            val retrofitResponse: Response<GetFeedResponse> = feedApiService.getFeedItems()
            if (!retrofitResponse.isSuccessful) {
                throw HttpException(retrofitResponse)
            }
            val metadataItems: List<TemplatesMetadataItem> =
                retrofitResponse.body()?.templatesMetadata!!
            val feedItems: List<FeedItemEntity> = metadataItems.toEntities()
            feedDatabase.withTransaction {  // run this block as an atomic command, to prevent dead-lock.
                // in addition, adding the new items immediately after the deleting. prevent the white screen)
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

fun List<TemplatesMetadataItem>.toEntities(): List<FeedItemEntity> {
    return map {
        FeedItemEntity(
            it.id,
            it.templateThumbnailURI,
            it.isPremium
        )
    }
}
