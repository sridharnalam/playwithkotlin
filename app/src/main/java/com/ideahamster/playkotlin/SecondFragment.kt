package com.ideahamster.playkotlin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.ideahamster.playkotlin.common.function.TransformFunction
import com.ideahamster.playkotlin.databinding.FragmentSecondBinding
import com.ideahamster.playkotlin.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {
    var TAG = "SecondFragment"
    private var _binding: FragmentSecondBinding? = null
    private val _events = MutableSharedFlow<Response<SdkResponse>>()
    private val events = _events.asSharedFlow()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)

            val job1 = lifecycleScope.launch {
                doStateWiseOperation().collect {
                    Log.i(TAG, "Final value $it")
                }
            }
        }
    }

    private suspend fun doStateWiseOperation(): Flow<Int> {
        return flow {
            emit(Random.nextInt(6))
        }.flatMapMerge { key ->
            Log.i(TAG, "Random input $key")
            var state = key
            var stateFlow: Flow<Int> = process(state)
            state--
            while(state > 0) {
                stateFlow =stateFlow.flatMapMerge {
                    process(it)
                }
                state--
            }
             stateFlow
        }
    }

    private suspend fun process(value: Int): Flow<Int> {
        return flow {
            Log.i(TAG, "Value ${when(value) {
                1-> "One"
                2-> "Two"
                3-> "Three"
                4-> "Four"
                5-> "Five"
                else -> "Other"
            }}")
            val newVal = value-1;
            emit(newVal)
        }
    }


    private suspend fun fetchData() {
        requestData()
            .catch { error ->
                Log.e("Flow", error.toString())
            }
            .collect {
                Log.i("Flow", "Result: $it")
            }
    }

    private fun requestData(): Flow<ConfigureSuccess> {
        return flow {
            requestConfigSuccess()
            /* observeSdkEvent(
                 ConfigureSuccess::class.java,
                 ConfigureFailure::class.java
             )*/
            observeSdkResponse(
                ConfigureSuccess::class.java,
                ConfigureFailure::class.java,
                { configureSuccess: ConfigureSuccess -> configureSuccess },
                { configureFailure: ConfigureFailure -> RetryableException(configureFailure.toString()) }
            )
        }
            .catch { error ->
                Log.e("Flow", "requestData: $error")
                error.message?.let { error(it) }
            }
    }

    private suspend fun FlowCollector<ConfigureSuccess>.observeEventFlow() {
        flow {
            //                requestConfigSuccess()
            events.collect {
                emit(it)
            }
        }
            .filterThenTransform(
                ConfigureSuccess::class.java,
                ConfigureFailure::class.java
            )
            .catch { error ->
                Log.e("Flow", "Case 1: $error")
                error.message?.let { error(it) }
            }
            .collect {
                emit(it)
                Log.i("FLOW", "Case 1: $it")
                currentCoroutineContext().job.cancel()
            }
    }

    suspend fun <T, E, R> FlowCollector<R>.observeSdkResponse(
        successTargetClass: Class<T>,
        failureTargetClass: Class<E>,
        successTransformFunction: TransformFunction<T, R>,
        errorTransformFunction: TransformFunction<E, out Throwable>
    ) where T : SdkResponse, E : SdkResponse, R : SdkResponse {
        flow {
            events.collect { response ->
                emit(response)
            }
        }
            .filterThenTransform(
                successTargetClass,
                failureTargetClass,
                successTransformFunction,
                errorTransformFunction
            )
            .collect {
                emit(it)
                currentCoroutineContext().job.cancel()
            }
    }


    private fun <T, S, E, R> Flow<Response<T>>.filterThenTransform(
        successTargetClass: Class<S>,
        failureTargetClass: Class<E>,
        successTransformFunction: TransformFunction<S, R>,
        failureTransformFunction: TransformFunction<E, out Throwable>,
    ): Flow<R> where T : SdkResponse {
        return transform { response ->
            when (response) {
                is Response.Success ->
                    if (successTargetClass.isInstance(response.responseBody)) {
                        val value = successTargetClass.cast(response.responseBody)
                        return@transform emit(successTransformFunction.apply(value))
                    }
                is Response.Error ->
                    if (failureTargetClass.isInstance(response.responseBody)) {
                        val value = failureTargetClass.cast(response.responseBody)
                        error(failureTransformFunction.apply(value))
                    }
            }
        }
    }

    private suspend fun <R, E> FlowCollector<R>.observeSdkEvent(
        successTargetClass: Class<R>,
        failureTargetClass: Class<E>
    ) where R : SdkResponse, E : SdkResponse {
        flow {
            events.collect { response ->
                emit(response)
            }
        }
            .filterThenTransform(
                successTargetClass,
                failureTargetClass
            )
            .catch { error ->
                error.message?.let {
                    error(it)
                }
            }
            .collect {
                emit(it)
                currentCoroutineContext().job.cancel()
            }
    }

    private fun <T, R, E> Flow<Response<T>>.filterThenTransform(
        successTargetClass: Class<R>,
        failureTargetClass: Class<E>
    ): Flow<R> where T : SdkResponse {
        return transform { response ->
            when (response) {
                is Response.Success ->
                    if (successTargetClass.isInstance(response.responseBody)) {
                        return@transform emit(successTargetClass.cast(response.responseBody))
                    }
                is Response.Error ->
                    if (failureTargetClass.isInstance(response.responseBody)) {
                        error(response.responseBody)
                    }
            }
        }
    }

    private inline fun <T> Flow<T>.filterResponse1(crossinline predicate: suspend (T) -> Boolean): Flow<T> =
        transform { value ->
            if (predicate(value)) return@transform emit(value)
        }


    fun <T : SdkResponse?> returnObject(sdkResponse: T, clazz: Class<out SdkResponse>): T {
        return if (clazz.isInstance(sdkResponse)) {
            clazz.cast(sdkResponse)
            sdkResponse
        } else {
            sdkResponse
        }
    }

    private fun <T : SdkResponse> Flow<Response<T>>.filterAndTransformResponse(
        successTargetClass: Class<out SdkResponse>,
        failureTargetClass: Class<out SdkResponse>
    ): Flow<T> {
        return transform { future ->
            when (future) {
                is Response.Success ->
                    if (successTargetClass.isInstance(future.responseBody)) {
//                        future.value as ConfigureSuccess
//                        return@transform emit(successTargetClass.cast(future.response))
                        return@transform emit(future.responseBody)
                    }
                is Response.Error ->
                    if (failureTargetClass.isInstance(future.responseBody)) {
//                        future.value as ConfigureFailure
//                        return@transform emit(failureTargetClass.cast(future.response))
                        return@transform emit(future.responseBody)
                    }
            }
            /*if (successTargetClass.isInstance(future) || failureTargetClass.isInstance(future)) {
                return@transform emit(future)
            }*/
        }
    }

    private fun <T> Flow<T>.filterResponsed(
        successTargetClass: Class<out SdkResponse>,
        failureTargetClass: Class<out SdkResponse>
    ): Flow<T> {
        return transform { value ->
            if (successTargetClass.isInstance(value)) return@transform emit(value)
            else if (failureTargetClass.isInstance(value)) return@transform emit(value)
        }
    }

    private fun requestUnknownResponse() {
        GlobalScope.launch {
            delay(1000)
            _events.emit(Response.Success(UnknownResponse("0")))
        }
    }

    private fun requestConfigSuccess() {
        GlobalScope.launch {
            delay(2000)
            _events.emit(Response.Success(ConfigureSuccess("200")))
        }
    }

    private fun requestConfigError() {
        GlobalScope.launch {
            delay(2000)
            _events.emit(Response.Error(ConfigureFailure("400")))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}