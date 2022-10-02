package com.lightricks.feedexercise.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.database.FeedItemEntity
import com.lightricks.feedexercise.network.FeedApiService
import com.lightricks.feedexercise.network.GetFeedResponse
import com.lightricks.feedexercise.network.TemplatesMetadataItem
import com.lightricks.feedexercise.ui.feed.FeedViewModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.HttpException
import retrofit2.Response


@RunWith(AndroidJUnit4::class)
class FeedRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext<Context>()
    private val allTemplateItemsList: List<TemplatesMetadataItem> =
        fakeFeedResponse().body()?.templatesMetadata ?: ArrayList()
    private val allFeedEntities: List<FeedItemEntity> = allTemplateItemsList.toEntities()
    private val emptyFeedItemsList: List<FeedItem> = ArrayList()
    private lateinit var testDatabase: FeedDatabase
    private lateinit var testService: TestService
    private lateinit var testRepository: Repository
    private lateinit var testViewModel: FeedViewModel


    @Before
    fun createDataBaseRepoAnsService() {
        testDatabase = Room.inMemoryDatabaseBuilder(context, FeedDatabase::class.java).build()
        testService = TestService()
        testRepository = FeedRepository(testDatabase, testService)
        testViewModel = FeedViewModel(testRepository)
    }

    @Test
    fun testRefresh_emptyWebsite_successGet() = runBlocking {
        val itemsBeforeRefresh: List<FeedItemEntity> = testDatabase.feedDao.getAll().first()
        testService.setItems(ArrayList())
        testRepository.refresh()
        val itemsAfterRefresh: List<FeedItemEntity> = testDatabase.feedDao.getAll().first()
        assertEquals(emptyFeedItemsList, itemsBeforeRefresh)
        assertEquals(itemsAfterRefresh.toFeedItems(), emptyFeedItemsList)
    }

    @Test
    fun testRefresh_fullWebsite_successGet() = runBlocking {
        val itemsBeforeRefresh: List<FeedItemEntity> = testDatabase.feedDao.getAll().first()
        testService.setItems(allTemplateItemsList)
        testRepository.refresh()
        val itemsAfterRefresh: List<FeedItemEntity> = testDatabase.feedDao.getAll().first()
        assertEquals(emptyFeedItemsList, itemsBeforeRefresh)
        assertEquals(allFeedEntities, itemsAfterRefresh)
    }

    @Test
    fun testRefresh_emptyAfterFull_successGet() = runBlocking {
        testService.setItems(allTemplateItemsList)
        testRepository.refresh()
        testService.setItems(ArrayList())
        testRepository.refresh()
        val itemsAfterDeleting: List<FeedItemEntity> = testDatabase.feedDao.getAll().first()
        assertEquals(emptyFeedItemsList, itemsAfterDeleting)
    }

    @Test
    fun testRefresh_removeFirstItems_successGet() = runBlocking {
        testService.setItems(allTemplateItemsList)
        testRepository.refresh()
        testService.setItems(allTemplateItemsList.drop(1))  // remove first element
        testRepository.refresh()
        val itemsAfterDeleting: List<FeedItemEntity> = testDatabase.feedDao.getAll().first()
        assertEquals(allFeedEntities.drop(1), itemsAfterDeleting)
    }

    @Test
    fun testRefresh_currentEmpty_GetFailure() = runBlocking {
        testService.setItems(ArrayList())
        testRepository.refresh()
        testService.returnError()  // now fail
        try { testRepository.refresh() }
        catch (e : HttpException) { Log.d("networkErr", "error") }  // supposed to fail
        val currentItems: List<FeedItemEntity> = testDatabase.feedDao.getAll().first()
        assertEquals(ArrayList<FeedItemEntity>(), currentItems)
    }

    @Test
    fun testRefresh_currentNotEmpty_GetFailure() = runBlocking {
        testService.setItems(allTemplateItemsList)
        testRepository.refresh()
        testService.returnError()  // now fail
        try { testRepository.refresh() }
        catch (e : HttpException) { Log.d("networkErr", "error") }  // supposed to fail
        val currentItems: List<FeedItemEntity> = testDatabase.feedDao.getAll().first()
        assertEquals(allFeedEntities ,currentItems)
    }

    private fun fakeFeedResponse(): Response<GetFeedResponse> {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val adapter: JsonAdapter<GetFeedResponse> = moshi.adapter(GetFeedResponse::class.java)
        val jsonName = "get_feed_response.json"
        val jsonFile = context.assets.open(jsonName).bufferedReader().use { it.readText() }
        return Response.success(adapter.fromJson(jsonFile))
    }
}

private class TestService : FeedApiService {

    private var serviceItems : List<TemplatesMetadataItem> = ArrayList()
    private var isSuccess : Boolean = true

    fun returnSuccess() { isSuccess = true }
    fun returnError() { isSuccess = false }

    fun setItems(newList : List<TemplatesMetadataItem>) { serviceItems = newList }

    override suspend fun getFeedItems(): Response<GetFeedResponse> {
        return if (isSuccess) {
            Response.success(GetFeedResponse(serviceItems))
        } else {
            val errorResponse =
                "{\n" + "  \"type\": \"error\",\n" +
                        "  \"message\": \"Service failed.\"\n" + "}"
            val errorResponseBody = ResponseBody.create(MediaType.parse("text"), errorResponse)
            Response.error(401, errorResponseBody)
        }
    }
}
