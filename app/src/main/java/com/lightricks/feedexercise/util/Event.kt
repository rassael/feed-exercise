package com.lightricks.feedexercise.util

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 * This is useful, because we want to handle each event only once.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}
