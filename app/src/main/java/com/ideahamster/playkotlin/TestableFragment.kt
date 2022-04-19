package com.ideahamster.playkotlin

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ideahamster.playkotlin.databinding.FragmentFirstBinding
import com.ideahamster.playkotlin.databinding.FragmentTestableBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

class TestableFragment : Fragment() {
    var TAG = "FLOW_TAG"

    private var _binding: FragmentTestableBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = TestableFragment()
    }

    private lateinit var viewModel: TestableViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, TestableViewModelFactory(GlobalScope, Dispatchers.Default))[TestableViewModel::class.java]

        binding.btnPlay.setOnClickListener {
            Log.d(TAG, "Play OnClick(): executing thread ${Thread.currentThread().name}")
            viewModel.doGlobalScopeWork()
            viewModel.doAnotherGlobalScopeWork()
            viewModel.doViewModelScopeWork()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}