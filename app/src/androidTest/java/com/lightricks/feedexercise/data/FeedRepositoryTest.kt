package com.lightricks.feedexercise.data

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lightricks.feedexercise.database.FeedDatabase
import com.lightricks.feedexercise.database.FeedItemEntity
import com.lightricks.feedexercise.network.FeedApiService
import com.lightricks.feedexercise.network.GetFeedResponse
import com.lightricks.feedexercise.network.ItemDto
import com.lightricks.feedexercise.ui.feed.FeedViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import java.io.IOException


@RunWith(AndroidJUnit4::class)
class FeedRepositoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext<Context>()
    private val daoItems: List<ItemDto> = listOf(
        ItemDto(configuration = "lensflare-unleash-the-power-of-nature.json",
            id = "01E18PGE1RYB3R9YF9HRXQ0ZSD",
            isNew = false,
            isPremium = true,
            templateCategories = listOf("01DJ4TM160ETZR0NT4HA2M0ZTK", "01DJ4TM161MRR86QFAXJTWP7NM"),
            templateName = "lens-flare-template.json",
            templateThumbnailURI = "UnleashThePowerOfNatureThumbnail.jpg"),
        ItemDto(configuration = "accountingtravis.json",
            id = "01DX1RB94P35Q1A2W6AA5XCQZ9",
            isNew = false,
            isPremium = true,
            templateCategories = listOf("01DJ4TM160ETZR0NT4HA2M0ZTK", "01DJ4TM161P490A1DZ3AFKXNNF"),
            templateName = "lightleaks-template.json",
            templateThumbnailURI = "AccountingTravisThumbnail.jpg"),
        ItemDto(configuration = "yeti.json",
            id = "01EAEFVPZ6MFJEMCA8XB06HB01",
            isNew = true,
            isPremium = true,
            templateCategories = listOf("01DJ4TM160ETZR0NT4HA2M0ZTK", "01DJ4TM161P490A1DZ3AFKXNNF"),
            templateName = "fashion-template.json",
            templateThumbnailURI = "yeti-thumbnail.jpg"),
        ItemDto(configuration = "BusinessDev.json",
            id = "01DX1RB965Z96AD283559NJT9T",
            isNew = false,
            isPremium = true,
            templateCategories = listOf("01DJ4TM161P490A1DZ3AFKXNNF", "01DJ4TM160ETZR0NT4HA2M0ZTK"),
            templateName = "holistic-template.json",
            templateThumbnailURI = "BusinessDevDefaultThumbnail.jpg")
    )
    private val feedEntities: List<FeedItemEntity> = daoItems.toEntities()
    private val emptyEntitiesList: List<FeedItemEntity> = listOf<FeedItemEntity>()
    private var testDatabase: FeedDatabase = Room.inMemoryDatabaseBuilder(context,
        FeedDatabase::class.java).build()
    private  var testService: TestService = TestService()
    private var testRepository: Repository = FeedRepository(testDatabase, testService)
    private var testViewModel: FeedViewModel = FeedViewModel(testRepository)

    @After
    fun cleanAfter() = runTest {
        testDatabase.feedDao.deleteAll()
        testService.returnSuccess()
        testService.setItems(listOf<ItemDto>())
    }

    @Test
    fun refresh_dataBaseIaEmptyAndServiceReturnAnEmptyList_dataBaseShouldBeEmpty() = runTest {
        testService.setItems(listOf<ItemDto>())
        // test initial state:
        val itemsBeforeRefresh: List<FeedItemEntity> = testDatabase.feedDao.getAll().first()
        assertEquals(emptyEntitiesList, itemsBeforeRefresh)
        // refresh the repo:
        testRepository.refresh()
        // test:
        assertEquals(emptyEntitiesList, testDatabase.feedDao.getAll().first())
    }

    @Test
    fun refresh_dataBaseIsEmptyAndServiceReturnNonEmptyList_dataBaseShouldContainMatchesEntities() = runTest {
        testService.setItems(daoItems)
        // test initial state:
        assertEquals(emptyEntitiesList, testDatabase.feedDao.getAll().first())
        // refresh the repo:
        testRepository.refresh()
        val itemsAfterRefresh: List<FeedItemEntity> = testDatabase.feedDao.getAll().first()
        // test:
        assertEquals(feedEntities, itemsAfterRefresh)
    }

    @Test
    fun refresh_dataBaseIsNotEmptyAndServiceReturnAnEmptyList_dataBaseShouldBeEmpty() = runTest {
        // setup
        testService.setItems(daoItems)
        testRepository.refresh()
        // test initial state:
        assertEquals(feedEntities, testDatabase.feedDao.getAll().first())
        testService.setItems(listOf<ItemDto>())  // now service will return empty list
        testRepository.refresh()
        // test:
        assertEquals(emptyEntitiesList, testDatabase.feedDao.getAll().first())
    }

    @Test
    fun refresh_dataBaseIsNotEmptyAndServiceReturnTheSameListWithoutFirstItem_dataBaseShouldContainMatchesEntities() = runTest {
        // setup
        testService.setItems(daoItems)
        testRepository.refresh()
        // test initial state:
        assertEquals(feedEntities, testDatabase.feedDao.getAll().first())
        // set service items:
        testService.setItems(daoItems.drop(1))  // remove first element
        testRepository.refresh()
        // test:
        assertEquals(feedEntities.drop(1), testDatabase.feedDao.getAll().first())
    }

    @Test
    fun refresh_dataBaseIsEmptyAndServiceThrowAnException_dataBaseShouldBeEmpty() = runTest {
        // setup
        testService.setItems(listOf<ItemDto>())
        testRepository.refresh()
        // test initial state:
        assertEquals(emptyEntitiesList, testDatabase.feedDao.getAll().first())
        // call refresh function
        testService.returnError()  // now service will fail
        try {
            testRepository.refresh() }
        catch (e : IOException) { }  // supposed to fail
        // test:
        assertEquals(listOf<FeedItemEntity>(), testDatabase.feedDao.getAll().first())
    }

    @Test
    fun refresh_dataBaseIsNotEmptyAndServiceThrowAnException_dataBaseShouldContainTheSameContent() = runTest {
        // setup
        testService.setItems(daoItems)
        testRepository.refresh()
        // test initial state:
        assertEquals(feedEntities, testDatabase.feedDao.getAll().first())
        testService.returnError()  // now service will fail
        try {
            testRepository.refresh() }
        catch (e : IOException) { } // supposed to fail
        // test
        assertEquals(feedEntities ,testDatabase.feedDao.getAll().first())
    }
}

private class TestService : FeedApiService {

    private var serviceItems : List<ItemDto> = emptyList()
    private var isSuccess : Boolean = true

    fun returnSuccess() { isSuccess = true }
    fun returnError() { isSuccess = false }

    fun setItems(newList : List<ItemDto>) { serviceItems = newList }

    override suspend fun getFeedItems(): GetFeedResponse {
        return if (isSuccess) {
            GetFeedResponse(serviceItems)
        } else {
            throw IOException()
        }
    }
}
