package com.lightricks.feedexercise.ui.feed

import androidx.lifecycle.*
import com.lightricks.feedexercise.data.FeedItem
import com.lightricks.feedexercise.data.FeedRepository
import com.lightricks.feedexercise.data.Repository
import com.lightricks.feedexercise.util.Event
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.IllegalArgumentException

/**
 * This view model manages the data for [FeedFragment].
 */
open class FeedViewModel(private val repository: Repository) : ViewModel() {
    private val stateInternal: MutableLiveData<State> = MutableLiveData<State>(DEFAULT_STATE)
    private val networkErrorEvent = MutableLiveData<Event<String>>()

    fun getIsLoading(): LiveData<Boolean> {
        return Transformations.map(stateInternal) { it.isLoading }
    }

    fun getIsEmpty(): LiveData<Boolean> {
        return Transformations.map(stateInternal) { it.feedItems?.isEmpty() }
    }

    fun getFeedItems(): LiveData<List<FeedItem>> {
        return Transformations.map(stateInternal) { it.feedItems }
    }


    fun getNetworkErrorEvent(): LiveData<Event<String>> = networkErrorEvent

    init {
        refresh()
        viewModelScope.launch { updateItems() }
    }

    private suspend fun updateItems() {
        repository.getItems()
                .collect { feedItems ->
                    updateState { copy(feedItems = feedItems) }
                }
    }

    fun refresh() {
        if (!stateInternal.value?.isLoading!!) {
            viewModelScope.launch {
                updateState { copy(isLoading = true) }  // isn't it happen from the fragment?
                try { repository.refresh() }
                catch (e: IOException) { networkErrorEvent.value = Event(e.message.toString()) }
                finally { updateState { copy(isLoading = false) }
                }
            }
        }
    }

    private fun updateState(transform: State.() -> State) {
        stateInternal.value = transform(getState())
    }

    private fun getState(): State {
        return stateInternal.value!!
    }

    data class State(
        val feedItems: List<FeedItem>?,
        val isLoading: Boolean
    )

    companion object {
        private val DEFAULT_STATE = State(
            feedItems = emptyList(),
            isLoading = false
        )
    }
}

/**
 * This class creates instances of [FeedViewModel].
 * It's not necessary to use this factory at this stage. But if we will need to inject
 * dependencies into [FeedViewModel] in the future, then this is the place to do it.
 */
class FeedViewModelFactory(private val repository: FeedRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(FeedViewModel::class.java)) {
            throw IllegalArgumentException("factory used with a wrong class")
        }
        @Suppress("UNCHECKED_CAST")
        return FeedViewModel(repository) as T
    }
}
