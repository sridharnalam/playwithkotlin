package com.ideahamster.playkotlin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

class TestableViewModelFactory(
    private val externalScope: CoroutineScope,private val dispatcher: CoroutineDispatcher
): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TestableViewModel(externalScope, dispatcher) as T
    }
}