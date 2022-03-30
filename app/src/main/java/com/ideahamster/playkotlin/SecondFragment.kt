package com.ideahamster.playkotlin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.ideahamster.playkotlin.Dummy.DemoInterface
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
    private val events = _events.asSharedFlow().buffer(5)

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


        binding.buttonPublish.setOnClickListener {
            requestConfigSuccess()
            requestPushSuccess()
            requestLinesSuccess()
        }
        binding.buttonSecond.setOnClickListener {
//            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)

            lifecycleScope.launch {
//                fetchData()
                doStateWiseOperation()
            }
        }
    }

    private fun doStateWiseLoop(): Flow<Int> {
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
        requestConfigData()
            .catch { error ->
                Log.e("Flow", error.toString())
            }
            .collect {
                Log.i("Flow", "Result: $it")
            }
    }

    private suspend fun doStateWiseOperation() {
        var state: Int = 0;
        var stateFlow = flowOf("0")
        if(state ==0){
            stateFlow = stateFlow.flatMapMerge {
                requestConfigData()
            }
            state =1;
        }
        if(state ==1) {
            stateFlow = stateFlow.flatMapMerge {
                requestPushData()
            }
            state =2;
        }
        if(state ==2) {
            stateFlow = stateFlow.flatMapMerge {
                requestLinesData()
            }
            state =3;
        }
        stateFlow
            .flowOn(Dispatchers.Default)
            .catch { error ->
                Log.e("Flow", error.toString())
            }
            .collect {
                Log.i("Flow", "Result: $it")
            }
    }

    private fun requestConfigData(): Flow<String> {
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
            .map {
                Log.i("Flow", "requestConfigData: $it")
                it.response
            }
            .catch { error ->
                Log.e("Flow", "requestConfigData: $error")
                error.message?.let { error(it) }
            }
    }

    private fun requestPushData(): Flow<String> {
        return flow {
            requestPushSuccess()
            /* observeSdkEvent(
                 ConfigureSuccess::class.java,
                 ConfigureFailure::class.java
             )*/
            observeSdkResponse(
                PushSuccess::class.java,
                PushFailure::class.java,
                { configureSuccess: PushSuccess -> configureSuccess },
                { pushFailure: PushFailure -> RetryableException(pushFailure.toString()) }
            )
        }
            .map {
                Log.i("Flow", "requestPushData: $it")
                it.response
            }
            .catch { error ->
                Log.e("Flow", "requestPushData: $error")
                error.message?.let { error(it) }
            }
    }

    private fun requestLinesData(): Flow<String> {
        return flow {
            requestLinesSuccess()
            /* observeSdkEvent(
                 ConfigureSuccess::class.java,
                 ConfigureFailure::class.java
             )*/
            observeSdkResponse(
                LinesSuccess::class.java,
                LinesFailure::class.java,
                { linesSuccess: LinesSuccess -> linesSuccess },
                { linesFailure: LinesFailure -> RetryableException(linesFailure.toString()) }
            )
        }
            .map {
                Log.i("Flow", "requestLinesData: $it")
                it.response
            }
            .catch { error ->
                Log.e("Flow", "requestLinesData: $error")
                error.message?.let { error(it) }
            }
    }

    suspend fun <T, E, R> FlowCollector<R>.observeSdkResponse(
        successTargetClass: Class<T>,
        failureTargetClass: Class<E>,
        successTransformFunction: TransformFunction<T, R>,
        errorTransformFunction: TransformFunction<E, out Throwable>
    ) where T : SdkResponse, E : SdkResponse, R : SdkResponse {
        events
            .filterThenTransform(
                successTargetClass,
                failureTargetClass,
                successTransformFunction,
                errorTransformFunction
            )
            .take(1)
            .collect { response ->
                emit(response)
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

    suspend fun <R, E> FlowCollector<R>.observeSdkResponse(
        successTargetClass: Class<R>,
        failureTargetClass: Class<E>
    ) where R : SdkResponse, E : SdkResponse {
        flow {
            events
                .take(1)
                .collect { response ->
                    emit(response)
                }
        }
            .filterThenTransform(
                successTargetClass,
                failureTargetClass
            )
            .collect {
                emit(it)
            }
    }

    fun <T, R, E> Flow<Response<T>>.filterThenTransform(
        successTargetClass: Class<R>,
        failureTargetClass: Class<E>
    ): Flow<R> where T : SdkResponse {
        return transform { response ->
            when (response) {
                is Response.Success ->
                    if (successTargetClass.isInstance(response.responseBody)) {
                        return@transform emit(successTargetClass.cast(response.responseBody)!!)
                    }
                is Response.Error ->
                    if (failureTargetClass.isInstance(response.responseBody)) {
                        throw RetryableException(response.responseBody.toString())
                    }
            }
        }
    }


    fun <T : SdkResponse?> returnObject(sdkResponse: T, clazz: Class<out SdkResponse>): T {
        return if (clazz.isInstance(sdkResponse)) {
            clazz.cast(sdkResponse)
            sdkResponse
        } else {
            sdkResponse
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
            delayResponse()
            _events.emit(Response.Success(ConfigureSuccess("200")))
        }
    }

    private fun requestConfigError() {
        GlobalScope.launch {
            delayResponse()
            _events.emit(Response.Error(ConfigureFailure("400")))
        }
    }

    private fun requestPushSuccess() {
        GlobalScope.launch {
            delayResponse()
            _events.emit(Response.Success(PushSuccess("200")))
        }
    }

    private fun requestPushError() {
        GlobalScope.launch {
            delayResponse()
            _events.emit(Response.Error(PushFailure("400")))
        }
    }

    private fun requestLinesSuccess() {
        GlobalScope.launch {
            delayResponse()
            _events.emit(Response.Success(LinesSuccess("200")))
        }
    }

    private fun requestLinesError() {
        GlobalScope.launch {
            delayResponse()
            _events.emit(Response.Error(LinesFailure("400")))
        }
    }

    private suspend fun delayResponse() {
        delay(100)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun iterateList(list: List<Long>): Flow<List<Boolean>> {
        return flow<List<Boolean>> {
            var stateList: MutableList<Boolean> = ArrayList()
            for(id in list) {
                stateList.add(isEven(id))
            }
            emit(stateList)
        }
    }

     suspend fun isEven(num: Long): Boolean {
        delay(1000)
        return num%2 == 0L
    }

    fun isEvenFlow(num: Long): Flow<Boolean> {
        return flow {
            delay(1000)
            emit(num%2 == 0L)
        }
    }
}