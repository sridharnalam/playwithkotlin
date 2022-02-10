package com.ideahamster.playkotlin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ideahamster.playkotlin.databinding.FragmentFirstBinding
import com.ideahamster.playkotlin.model.ConfigureFailure
import com.ideahamster.playkotlin.model.ConfigureSuccess
import com.ideahamster.playkotlin.model.SdkResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    var TAG = "FLOW_TAG"
    private var _binding: FragmentFirstBinding? = null
    private val eventsPublisher = MutableSharedFlow<SdkResponse>()
    val eventsSubscriber = eventsPublisher.asSharedFlow()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Listen to the specified events
        collectEvents()
        collectEvents()

        binding.buttonFirst.setOnClickListener {
            Log.d(TAG, "Next OnClick(): executing thread ${Thread.currentThread().name}")
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        binding.buttonTest.setOnClickListener {
            Log.d(TAG, "Test OnClick(): executing thread ${Thread.currentThread().name}")
//            globalScopeMemoryLeak()
//            lifeCycleScope()
//            withContext()
//            withAsync()
//            withLaunch()
//            basicFlow()
//            understandFlows()
//            playWithFlowIntervals()
//            retryWhen()
            sharedFlowExample()
            Log.i(TAG, "Test OnClick() executed")
        }
    }

    private fun globalScopeMemoryLeak() {
        Log.d(TAG, "globalScopeMemoryLeak() thread ${Thread.currentThread().name}")
        GlobalScope.launch(Dispatchers.Default) {
            while (true) {
                Log.i(TAG, "SCOPE: main activity is running on thread  ${Thread.currentThread().name}")
                delay(1000L)
            }
        }

        GlobalScope.launch(Dispatchers.Default) {
            Log.d(TAG, "GlobalScope: Launching thread ${Thread.currentThread().name}")
            Log.i(TAG, "SCOPE: Launching details activity")
            delay(10_000L)
            startActivity(Intent(requireContext(), DetailsActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun lifeCycleScope() {
        Log.d(TAG, "lifeCycleScope() thread ${Thread.currentThread().name}")
        lifecycleScope.launch(Dispatchers.Default) {
            while (true) {
                Log.i(TAG, "SCOPE: main activity is running on thread ${Thread.currentThread().name}")
                delay(1000L)
            }
        }

        lifecycleScope.launch(Dispatchers.Default) {
            Log.d(TAG, "lifecycleScope: Launching activity thread ${Thread.currentThread().name}")
            Log.i(TAG, "SCOPE: Launching details activity")
            delay(10_000L)
            startActivity(Intent(requireContext(), DetailsActivity::class.java))
            requireActivity().finish()
        }
    }

    private fun withContext() {
        Log.d(TAG, "withContext() thread ${Thread.currentThread().name}")
        GlobalScope.launch {
            Log.d(TAG, "GlobalScope: Begin thread ${Thread.currentThread().name}")
            Log.i(TAG, "before launch")
            val result1 = withContext(Dispatchers.IO) { getResultOne() }
            val result2 = withContext(Dispatchers.IO) { getResultTwo() }
            Log.i(TAG, "After launch")
            val finalResult = result1 + result2
            Log.d(TAG, "GlobalScope: End thread ${Thread.currentThread().name}")
            Log.i(TAG, "Final result : $finalResult")
        }
    }

    private fun withAsync() {
        Log.d(TAG, "withAsync() thread ${Thread.currentThread().name}")
        GlobalScope.launch {
            Log.d(TAG, "GlobalScope: Begin thread ${Thread.currentThread().name}")
            Log.i(TAG, "before launch")
            val result1 = GlobalScope.async(Dispatchers.IO) { getResultOne() }
            val result2 = GlobalScope.async(Dispatchers.IO) { getResultTwo() }
            Log.i(TAG, "After launch")
            val finalResult = result1.await() + result2.await()
            Log.d(TAG, "GlobalScope: End thread ${Thread.currentThread().name}")
            Log.i(TAG, "Final result : $finalResult")
        }
    }

    private fun withLaunch() {
        Log.d(TAG, "withLaunch() thread ${Thread.currentThread().name}")
        GlobalScope.launch {
            Log.d(TAG, "GlobalScope: Begin thread ${Thread.currentThread().name}")
            var resultOne: String? = null;
            var resultTwo: String? = null;
            Log.i(TAG, "before launch")
            GlobalScope.launch(Dispatchers.IO) { resultOne = getResultOne() }
            GlobalScope.launch(Dispatchers.IO) { resultTwo = getResultTwo() }
            Log.i(TAG, "After launch")
            var result = resultOne + resultTwo
            Log.d(TAG, "GlobalScope: End thread ${Thread.currentThread().name}")
            Log.i(TAG, "Final result : $result")
        }
    }

    private suspend fun getResultOne(): String {
        Log.d(TAG, "getResultOne() thread ${Thread.currentThread().name}")
        Log.i(TAG, "Called getResultOne")
        delay(1000L)
        val message = "ResultOne"
        Log.i(TAG, "Launch $message")
        return message
    }

    private suspend fun getResultTwo(): String {
        Log.d(TAG, "getResultTwo() thread ${Thread.currentThread().name}")
        Log.i(TAG, "Called getResultTwo")
        delay(100L)
        val message = "ResultTwo"
        Log.i(TAG, "Launch $message")
        return message
    }

    private fun basicFlow() {
        Log.d(TAG, "basicFlow() thread ${Thread.currentThread().name}")
        GlobalScope.launch(Dispatchers.IO) {
            Log.d(TAG, "GlobalScope thread ${Thread.currentThread().name}")
            flow {
                Log.d(TAG, "flow thread ${Thread.currentThread().name}")
                emit(1)
            }
                .flowOn(Dispatchers.Default)
                .collect {
                    Log.d(TAG, "Collected thread ${Thread.currentThread().name}")
                    Log.i(TAG, "Collected value $it")
                }
        }
    }

    private fun understandFlows() {
        Log.d(TAG, "understandFlows() thread ${Thread.currentThread().name}")
        GlobalScope.launch(Dispatchers.IO) {
            Log.d(TAG, "GlobalScope: thread ${Thread.currentThread().name}")
            flow {
                Log.d(TAG, "main flow: thread ${Thread.currentThread().name}")
                delay(100L)
                emit(true)
            }.flatMapMerge { flag ->
                delay(100L)
                flatmapFlow(flag)
            }.map { value ->
                Log.d(TAG, "main flow map: thread ${Thread.currentThread().name}")
                Log.i(TAG, "flow() map $value")
                "$value 1"
            }.catch { ex ->
                Log.d(TAG, "main flow catch: thread ${Thread.currentThread().name}")
                ex.message?.let { it1 -> Log.e(TAG, "flow() catch block $it1") }
            }.flowOn(Dispatchers.IO)
                .collect { value ->
                    delay(100L)
                    Log.i(TAG, "main flow() collected value $value")
                    Log.d(TAG, "main flow collected: thread ${Thread.currentThread().name}")
                }
        }
    }

    private fun flatmapFlow(flag: Boolean): Flow<String> {
        Log.d(TAG, "flatmapFlow() thread ${Thread.currentThread().name}")
        return flow {
            Log.d(TAG, "flow: thread ${Thread.currentThread().name}")
            delay(100L)
            emit(flag)
        }.flatMapMerge { value ->
            innerFlow(value)
        }.map { value ->
            Log.i(TAG, "flatmapFlow() map $value")
            "$value 2"
        }.catch { cause ->
            Log.e(TAG, "flatmapFlow() flow cause ${cause.message!!}")
            Log.d(TAG, "flow catch: thread ${Thread.currentThread().name}")
            cause.message?.let { emit(it) }
        }.flowOn(Dispatchers.Default)
    }

    private fun innerFlow(flag: Boolean): Flow<String> {
        Log.d(TAG, "innerFlow() thread ${Thread.currentThread().name}")
        return flow<Boolean> {
            Log.d(TAG, "flow: thread ${Thread.currentThread().name}")
            delay(500)
            emit(flag)
        }.map { isTrue ->
            if (isTrue) {
                Log.i(TAG, "innerFlow() 3")
                return@map "3"
            }
            throw Exception("Custom exception for inner flow")
        }.catch { cause ->
            Log.e(TAG, "innerFlow() flow cause ${cause.message!!}")
            Log.d(TAG, "flow catch: thread ${Thread.currentThread().name}")
            cause.message?.let { emit("0") }
        }.flowOn(Dispatchers.Default)
    }

    private fun playWithFlowIntervals() {
        Log.d(TAG, "playWithFlowIntervals(): thread ${Thread.currentThread().name}")
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(TAG, "lifecycleScope: thread ${Thread.currentThread().name}")
            flowInterval(10_000, 5_000)
                .catch {
                    Log.e(TAG, "Exception ${it.message}")
                    Log.d(TAG, "flow catch: thread ${Thread.currentThread().name}")
                }
                .flowOn(Dispatchers.Default)
                .collect {
                    Log.i(TAG, "Collected ${it}")
                    Log.d(TAG, "flow collected: thread ${Thread.currentThread().name}")
                }
        }
    }

    private fun flowInterval(delayMillis: Long, initialDelayMillis: Long = 0L) = flow {
        Log.d(TAG, "flowInterval() flow: thread ${Thread.currentThread().name}")

        require(delayMillis > 0) { "delayMillis must be positive" }
        require(initialDelayMillis >= 0) { "initialDelayMillis cannot be negative" }

        if (initialDelayMillis > 0) {
            delay(initialDelayMillis)
        }
        emit(System.currentTimeMillis())
        while (true) {
            delay(delayMillis)
            emit(System.currentTimeMillis())
        }
    }
        .cancellable()
        .buffer()

    private fun retryWhen() {
        Log.d(TAG, "retryWhen() thread ${Thread.currentThread().name}")
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(TAG, "lifecycleScope: thread ${Thread.currentThread().name}")
            flow {
                Log.d(TAG, "flow: thread ${Thread.currentThread().name}")

                val value = Random.nextInt(10)
                Log.i(TAG, "Generated value $value")
                flowCollector(value)
            }.map { value: Int ->
                Log.d(TAG, "flow map: thread ${Thread.currentThread().name}")
                if (value % 3 == 0) {
                    2 * value
                } else {
                    throw RetryableException("Non divisible number by 3")
                }
            }.retryWhen { cause, attempt ->
                Log.d(TAG, "retry block: thread ${Thread.currentThread().name}")
                Log.e(TAG, "Exception caught for retry : ${cause.message}")
                if (cause is RetryableException) {
                    Log.i(TAG, "Retrying attempt $attempt")
                    delay(2000L)
                    attempt < 3
                } else {
                    false
                }
            }.catch { cause ->
                Log.e(TAG, "Final Exception : ${cause.message}")
                Log.d(TAG, "final catch: thread ${Thread.currentThread().name}")
            }.collect {
                Log.i(TAG, "Doubled result $it")
                Log.d(TAG, "final flow collected: thread ${Thread.currentThread().name}")
            }
        }
    }

    private suspend fun FlowCollector<Int>.flowCollector(value: Int) {
        Log.d(TAG, "flowCollector() thread ${Thread.currentThread().name}")
        flow<Int> {
            Log.d(TAG, "flow: thread ${Thread.currentThread().name}")
            if (value > 5) {
                throw Exception("Value greater then limit 5")
            } else {
                emit(value)
            }
        }.collect {
            Log.d(TAG, "flow collected: thread ${Thread.currentThread().name}")
            emit(it)
        }
    }

    private fun sharedFlowExample() {
        Log.d(TAG, "sharedFlowExample() thread ${Thread.currentThread().name}")
        GlobalScope.launch {
            Log.d(TAG, "lifecycleScope: thread ${Thread.currentThread().name}")
            delay(2000) // delay time to process the request
            val nextInt = Random.nextInt(1000)
            if (nextInt % 2 == 0) {
                produceEvent(ConfigureSuccess(nextInt.toString()))
            } else {
                produceEvent(ConfigureFailure(nextInt.toString()))
            }
        }
    }

    private suspend fun produceEvent(sdkResponse: SdkResponse) {
        if (ConfigureSuccess::class.java.isInstance(sdkResponse)) {
            eventsPublisher.emit(sdkResponse) // suspends until all subscribers receive it
        }
    }

    private fun collectEvents() {
        Log.d(TAG, "collectEvents() thread ${Thread.currentThread().name}")
        lifecycleScope.launch {
            Log.d(TAG, "GlobalScope: thread ${Thread.currentThread().name}")
            eventsSubscriber
                .filterIsInstance<ConfigureSuccess>()
                .flowOn(Dispatchers.Default)
                .collect { value: SdkResponse ->
                    delay(2_000L)
                    Log.i(TAG, "Sub value $value")
                    Log.d(TAG, "flow collected: thread ${Thread.currentThread().name}")
                }
        }
    }

    /** Beginning of Utility methods **/
    public inline fun <reified R> filterIsInstance(): Flow<R> = eventsSubscriber.filter { it is R } as Flow<R>

    fun <T : SdkResponse> filterResponse(targetClass: Class<out SdkResponse>): Flow<T> {

        return eventsSubscriber.filter { it -> targetClass.isInstance(it) } as Flow<T>
    }

    private fun isInstanceOf(obj: Any, targetClass: Class<out SdkResponse?>): Boolean {
        return targetClass.isInstance(obj)
    }
    /** End of Utility methods **/
}