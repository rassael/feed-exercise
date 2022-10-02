package com.lightricks.feedexercise.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.lightricks.feedexercise.network.GetFeedResponse
import com.lightricks.feedexercise.ui.feed.FeedViewModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeoutException

@RunWith(JUnit4::class)
class FeedViewModelTest {

    private val allFeedItems: List<FeedItem> = getAllItems()

    // need to use test dispatcher because we can't observe on the main dispatcher.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // for get the liveData value in the test
    @get:Rule var rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun testGetItems_emptyList(): Unit = runTest {
        val repository: Repository = mock(Repository::class.java)
        `when`(repository.getItems()).thenReturn(flow { emit(ArrayList()) })
        `when`(repository.refresh()).thenReturn(Unit)
        val viewModel = FeedViewModel(repository)
        Assert.assertEquals(ArrayList<FeedItem>(), viewModel.getFeedItems().getOrAwaitValue())
    }

    @Test
    fun testGetItems_notEmptyList(): Unit = runTest {
        val repository : Repository = mock(Repository::class.java)
        `when`(repository.getItems()).thenReturn(MutableStateFlow(allFeedItems))
        `when`(repository.refresh()).then {  }
        val viewModel = FeedViewModel(repository)
        Assert.assertEquals(allFeedItems, viewModel.getFeedItems().getOrAwaitValue())
    }
}


fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: java.util.concurrent.TimeUnit = java.util.concurrent.TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    this.observeForever(observer)
    afterObserve.invoke()
    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        this.removeObserver(observer)
        throw TimeoutException("LiveData value was never set.")
    }
    @Suppress("UNCHECKED_CAST")
    return data as T
}

fun getAllItems(): List<FeedItem> {
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val adapter: JsonAdapter<GetFeedResponse> = moshi.adapter(GetFeedResponse::class.java)
    return adapter.fromJson("{\n" +
            "  \"templatesMetadata\": [\n" +
            "    {\n" +
            "      \"configuration\": \"lensflare-unleash-the-power-of-nature.json\",\n" +
            "      \"id\": \"01E18PGE1RYB3R9YF9HRXQ0ZSD\",\n" +
            "      \"isNew\": false,\n" +
            "      \"isPremium\": true,\n" +
            "      \"templateCategories\": [\n" +
            "        \"01DJ4TM160ETZR0NT4HA2M0ZTK\",\n" +
            "        \"01DJ4TM161MRR86QFAXJTWP7NM\"\n" +
            "      ],\n" +
            "      \"templateName\": \"lens-flare-template.json\",\n" +
            "      \"templateThumbnailURI\": \"UnleashThePowerOfNatureThumbnail.jpg\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"configuration\": \"accountingtravis.json\",\n" +
            "      \"id\": \"01DX1RB94P35Q1A2W6AA5XCQZ9\",\n" +
            "      \"isNew\": false,\n" +
            "      \"isPremium\": true,\n" +
            "      \"templateCategories\": [\n" +
            "        \"01DJ4TM160ETZR0NT4HA2M0ZTK\",\n" +
            "        \"01DJ4TM161P490A1DZ3AFKXNNF\"\n" +
            "      ],\n" +
            "      \"templateName\": \"lightleaks-template.json\",\n" +
            "      \"templateThumbnailURI\": \"AccountingTravisThumbnail.jpg\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"configuration\": \"yeti.json\",\n" +
            "      \"id\": \"01EAEFVPZ6MFJEMCA8XB06HB01\",\n" +
            "      \"isNew\": true,\n" +
            "      \"isPremium\": true,\n" +
            "      \"templateCategories\": [\n" +
            "        \"01DJ4TM160ETZR0NT4HA2M0ZTK\",\n" +
            "        \"01DJ4TM161P490A1DZ3AFKXNNF\"\n" +
            "      ],\n" +
            "      \"templateName\": \"fashion-template.json\",\n" +
            "      \"templateThumbnailURI\": \"yeti-thumbnail.jpg\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"configuration\": \"BusinessDev.json\",\n" +
            "      \"id\": \"01DX1RB965Z96AD283559NJT9T\",\n" +
            "      \"isNew\": false,\n" +
            "      \"isPremium\": true,\n" +
            "      \"templateCategories\": [\n" +
            "        \"01DJ4TM161P490A1DZ3AFKXNNF\",\n" +
            "        \"01DJ4TM160ETZR0NT4HA2M0ZTK\"\n" +
            "      ],\n" +
            "      \"templateName\": \"holistic-template.json\",\n" +
            "      \"templateThumbnailURI\": \"BusinessDevDefaultThumbnail.jpg\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"configuration\": \"start-living.json\",\n" +
            "      \"id\": \"01EAEFVXVT6VS2R24GR7Q40XE0\",\n" +
            "      \"isNew\": true,\n" +
            "      \"isPremium\": true,\n" +
            "      \"templateCategories\": [\n" +
            "        \"01DJ4TM160ETZR0NT4HA2M0ZTK\",\n" +
            "        \"01DJ4TM161NNZMQQEXSX6DE4QJ\"\n" +
            "      ],\n" +
            "      \"templateName\": \"fashion-template.json\",\n" +
            "      \"templateThumbnailURI\": \"start-living-thumbnail.jpg\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"configuration\": \"beach4july.json\",\n" +
            "      \"id\": \"01DX1RB94MBAKC6ECYGMKVJT4B\",\n" +
            "      \"isNew\": false,\n" +
            "      \"isPremium\": false,\n" +
            "      \"templateCategories\": [\n" +
            "        \"01E2846M7HM3B12V0YTJM0M5KF\"\n" +
            "      ],\n" +
            "      \"templateName\": \"fashion-template.json\",\n" +
            "      \"templateThumbnailURI\": \"Beach4JulyThumbnail.jpg\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"configuration\": \"sliderevealboundingbox-happy-independence-day.json\",\n" +
            "      \"id\": \"01EC34AY3EQTA5294ZRHC6FQMJ\",\n" +
            "      \"isNew\": true,\n" +
            "      \"isPremium\": true,\n" +
            "      \"templateCategories\": [\n" +
            "        \"01E2846M7HM3B12V0YTJM0M5KF\"\n" +
            "      ],\n" +
            "      \"templateName\": \"slide-reveal-boundingbox-template.json\",\n" +
            "      \"templateThumbnailURI\": \"sliderevealboundingbox-happy-independence-day-thumbnail.jpg\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"configuration\": \"recipes4july.json\",\n" +
            "      \"id\": \"01DX1RB923YYSRNFQF90TJNP03\",\n" +
            "      \"isNew\": false,\n" +
            "      \"isPremium\": true,\n" +
            "      \"templateCategories\": [\n" +
            "        \"01E2846M7HM3B12V0YTJM0M5KF\"\n" +
            "      ],\n" +
            "      \"templateName\": \"fashion-template.json\",\n" +
            "      \"templateThumbnailURI\": \"Recipes4JulyThumbnail.jpg\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"configuration\": \"independence-day-celebration.json\",\n" +
            "      \"id\": \"01EBX3ZC3HZEXJYKEQ8SZAMGDY\",\n" +
            "      \"isNew\": true,\n" +
            "      \"isPremium\": true,\n" +
            "      \"templateCategories\": [\n" +
            "        \"01E2846M7HM3B12V0YTJM0M5KF\"\n" +
            "      ],\n" +
            "      \"templateName\": \"classic.json\",\n" +
            "      \"templateThumbnailURI\": \"independence-day-celebration-thumbnail.jpg\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"configuration\": \"happy-valentine's-day.json\",\n" +
            "      \"id\": \"01E7674TVS66G1AGGMDE3YJVD7\",\n" +
            "      \"isNew\": false,\n" +
            "      \"isPremium\": true,\n" +
            "      \"templateCategories\": [\n" +
            "        \"01E2846M7HM3B12V0YTJM0M5KF\"\n" +
            "      ],\n" +
            "      \"templateName\": \"classic.json\",\n" +
            "      \"templateThumbnailURI\": \"happy-valentine's-day-thumbnail.jpg\"\n" +
            "    }\n" +
            "  ]\n" +
            "}\n")?.templatesMetadata?.toEntities()?.toFeedItems() ?: ArrayList()
}
