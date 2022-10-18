package com.lightricks.feedexercise.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import java.util.function.Predicate

class TestObserver<T> : Observer<T> {
    private val emittedValuesInternal = mutableListOf<T>()
    val emittedValues: List<T>
        get() = emittedValuesInternal.toList()

    override fun onChanged(t: T) {
        emittedValuesInternal.add(t)
    }

    fun getCurrentValue(): T? {
        return emittedValues.lastOrNull()
    }

    fun requireCurrentValue(): T {
        return getCurrentValue()!!
    }

    fun assertEmpty() {
        assertThat(emittedValuesInternal).isEmpty()
    }

    fun assertValues(vararg expectedValues: T) {
        assertThat(emittedValuesInternal).containsExactly(*expectedValues)
    }

    fun assertValuesByInvocation(vararg expectedValues: T, invocation: () -> Unit) {
        val oldValueCount = emittedValuesInternal.size
        invocation()
        val newValues = emittedValuesInternal.drop(oldValueCount)
        assertThat(newValues).isEqualTo(listOf(*expectedValues))
    }

    fun reset() {
        emittedValuesInternal.clear()
    }

    /**
     * Asserts that this [TestObserver] received exactly one value for which the provided predicate returns true.
     */
    fun assertValue(valuePredicate: Predicate<T>): TestObserver<T> {
        if (emittedValuesInternal.isEmpty()) {
            throw AssertionError("No value")
        }
        assertThat(valuePredicate.test(emittedValuesInternal[0])).isTrue()
        if (emittedValuesInternal.count() > 1) {
            throw AssertionError("Value present but other values as well")
        }
        return this
    }
}

fun <T> LiveData<T>.testObserver() = TestObserver<T>().also { observeForever(it) }

//fun <T> Flow<T>.testObserver() = TestObserver<T>().also { runTest { collect() } }
