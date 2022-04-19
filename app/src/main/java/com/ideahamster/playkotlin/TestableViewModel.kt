package com.ideahamster.playkotlin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TestableViewModel(var externalScope: CoroutineScope, var dispatcher: CoroutineDispatcher) : ViewModel() {
    private val TAG: String = "FLOW_TAG"

    fun doGlobalScopeWork() {
        val handler = CoroutineExceptionHandler { dispatcher, exception ->
            Log.e(TAG,"doGlobalScopeWork() CoroutineExceptionHandler got $exception with suppressed ${exception.suppressed.contentToString()}")
        }
        viewModelScope
        externalScope.launch(handler) {
            Log.d(TAG, "doGlobalScopeWork() GlobalScope thread ${Thread.currentThread().name}")
            flow {
                Log.d(TAG, "doGlobalScopeWork() flow thread ${Thread.currentThread().name}")
                delay(1000)
                emit("200")
            }
                .flowOn(dispatcher)
                .catch {
                    Log.d(TAG, "doGlobalScopeWork() catch thread ${Thread.currentThread().name}")
                    Log.e(TAG, "doGlobalScopeWork() exception: ${it.toString()}")
                }
                .collect {
                    Log.d(TAG, "doGlobalScopeWork() collected thread ${Thread.currentThread().name}")
                    Log.d(TAG, "doGlobalScopeWork() result: $it")
                }
        }
    }

    fun doAnotherGlobalScopeWork() {
        val handler = CoroutineExceptionHandler { dispatcher, exception ->
            Log.e(TAG,"doAnotherGlobalScopeWork() CoroutineExceptionHandler got $exception with suppressed ${exception.suppressed.contentToString()}")
        }
        GlobalScope.launch(handler) {
            Log.d(TAG, "doAnotherGlobalScopeWork() GlobalScope thread ${Thread.currentThread().name}")
            flow<String> {
                Log.d(TAG, "doAnotherGlobalScopeWork() flow thread ${Thread.currentThread().name}")
                delay(100)
                throw Exception("Forced failure")
//                emit("200")
            }
                .flowOn(dispatcher)
                .catch {
                    Log.d(TAG, "doAnotherGlobalScopeWork() catch thread ${Thread.currentThread().name}")
                    Log.e(TAG, "doAnotherGlobalScopeWork() exception: ${it.toString()}")
                }
                .collect {
//                    throw Exception("Forced failure")
                    Log.d(TAG, "doAnotherGlobalScopeWork() collected thread ${Thread.currentThread().name}")
                    Log.d(TAG, "doAnotherGlobalScopeWork() result: $it")
                }
        }
    }

    fun doViewModelScopeWork() {
        val handler = CoroutineExceptionHandler { dispatcher, exception ->
            Log.e(TAG,"doViewModelScopeWork() CoroutineExceptionHandler got $exception with suppressed ${exception.suppressed.contentToString()}")
        }
        viewModelScope.launch(handler) {
            Log.d(TAG, "doViewModelScopeWork() viewModelScope thread ${Thread.currentThread().name}")
            flow {
                Log.d(TAG, "doViewModelScopeWork() flow thread ${Thread.currentThread().name}")
                delay(1000)
                emit("200")
            }
                .flowOn(dispatcher)
                .catch {
                    Log.d(TAG, "doViewModelScopeWork() catch thread ${Thread.currentThread().name}")
                    Log.e(TAG, "doViewModelScopeWork() exception: ${it.toString()}")
                }
                .collect {
                    Log.d(TAG, "doViewModelScopeWork() collected thread ${Thread.currentThread().name}")
                    Log.d(TAG, "doViewModelScopeWork() result: $it")
                }
        }
    }
}