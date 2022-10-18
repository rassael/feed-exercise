package com.lightricks.feedexercise.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lightricks.feedexercise.ui.feed.FeedViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock

@RunWith(JUnit4::class)
class FeedViewModelTest {

    private val feedItems: List<FeedItem> = getFeedItems()

    // cancel asynchronicity - in order to tun the tests in one thread.
    // using for running the test on test dedicated dispatcher
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // allow to run liveData's setValue from other thread than the main thread (always return true from isMainThread function)
    @get:Rule var rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun getItems_repoReturnAnEmptyList_shouldReturnAnEmptyList(): Unit = runTest {
        val repository: Repository = mock {
            on { getItems() }.doReturn( flow { emit(emptyList()) } )
            onBlocking { refresh() }.doSuspendableAnswer { }
        }
        // Init:
        val viewModel = FeedViewModel(repository)
        val getItemsTestObserver = viewModel.getFeedItems().testObserver()

        // Test:
        Assert.assertEquals(listOf<FeedItem>(), getItemsTestObserver.requireCurrentValue())
    }

    @Test
    fun getItems_repoReturnNonEmptyList_shouldReturnTheSameList(): Unit = runTest {
        val repository : Repository = mock {
            onBlocking { refresh() }.doSuspendableAnswer { }
            on { getItems() }.doReturn( MutableStateFlow(feedItems) )
        }
        // Init:
        val viewModel = FeedViewModel(repository)
        val getItemsTestObserver = viewModel.getFeedItems().testObserver()

        // Test:
        Assert.assertEquals(feedItems, getItemsTestObserver.requireCurrentValue())
    }

    @Test
    fun init_repoReturnNonEmptyList_itemsShouldContainTheItemsFromRepo() = runTest {
        val repository: Repository = mock {
            onBlocking { refresh() }.doSuspendableAnswer { }
            on { getItems() }.doReturn( MutableStateFlow(feedItems) )
        }
        // Init:
        val viewModel = FeedViewModel(repository)
        val feedItemsTestObserver = viewModel.getFeedItems().testObserver()

        // Test:
        Assert.assertEquals(feedItemsTestObserver.requireCurrentValue(), feedItems)
    }

    @Test
    fun init_whenCreatingViewModel_afterInitIsLoadingShouldBeFalse() = runTest {
        val repository: Repository = mock {
            onBlocking { refresh() }.doSuspendableAnswer { }
            on { getItems() }.doReturn( MutableStateFlow(feedItems) )
        }
        // Init:
        val viewModel = FeedViewModel(repository)
        val isLoadingTestObserver = viewModel.getIsLoading().testObserver()

        // Test:
        Assert.assertEquals(isLoadingTestObserver.requireCurrentValue(), false)
    }

    @Test
    fun refresh_whileRefreshingTheRepo_isLoadingShouldBeTrue() = runTest {
        val refreshTrigger = Channel<Unit>()
        val repository: Repository = mock {
            on { getItems() }.doReturn(flow { emit(emptyList()) })
            onBlocking { refresh() }.doSuspendableAnswer { refreshTrigger.receive() }
        }
        // Init:
        val viewModel = FeedViewModel(repository)
        val isLoadingTestObserver = viewModel.getIsLoading().testObserver()

        // Test:
        viewModel.refresh()
        Assert.assertEquals(isLoadingTestObserver.requireCurrentValue(), true)
    }
}

fun getFeedItems(): List<FeedItem> {
    return listOf(
        FeedItem("01E18PGE1RYB3R9YF9HRXQ0ZSD",
            "UnleashThePowerOfNatureThumbnail.jpg",
            true),
        FeedItem("01DX1RB94P35Q1A2W6AA5XCQZ9",
            "AccountingTravisThumbnail.jpg",
            true),
        FeedItem("01EAEFVPZ6MFJEMCA8XB06HB01",
            "yeti-thumbnail.jpg",
            true),
        FeedItem("01DX1RB965Z96AD283559NJT9T",
            "BusinessDevDefaultThumbnail.jpg",
            true))
}
