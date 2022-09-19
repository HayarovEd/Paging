package com.edurda77.paging.ui

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.android.example.paging.pagingwithnetwork.reddit.paging.asMergedLoadStates
import com.edurda77.paging.databinding.ActivityRedditBinding
import com.edurda77.paging.network.RedditPostRepository
import com.edurda77.paging.presentation.PostsAdapter
import com.edurda77.paging.presentation.PostsLoadStateAdapter
import com.edurda77.paging.presentation.RedditViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter

class RedditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRedditBinding
    private val model: RedditViewModel by viewModels {
        object : AbstractSavedStateViewModelFactory(this, null) {
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                val repoType = RedditPostRepository.Type.DB
                val repo = ServiceLocator.instance(this@RedditActivity)
                    .getRepository(repoType)
                @Suppress("UNCHECKED_CAST")
                return RedditViewModel(repo, handle) as T
            }
        }
    }
    private lateinit var adapter: PostsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedditBinding.inflate(layoutInflater)
        setContentView(binding.root)
            initAdapter()
            initSwipeToRefresh()
            initSearch()
        }

        private fun initAdapter() {
            adapter = PostsAdapter()
            binding.rvList.adapter = adapter.withLoadStateHeaderAndFooter(
                header = PostsLoadStateAdapter(adapter),
                footer = PostsLoadStateAdapter(adapter)
            )

            lifecycleScope.launchWhenCreated {
                adapter.loadStateFlow.collect { loadStates ->
                    binding.swipeRefresh.isRefreshing = loadStates.mediator?.refresh is LoadState.Loading
                }
            }

            lifecycleScope.launchWhenCreated {
                model.posts.collectLatest {
                    adapter.submitData(it)
                }
            }

            lifecycleScope.launchWhenCreated {
                adapter.loadStateFlow

                    .asMergedLoadStates()
                    .distinctUntilChangedBy { it.refresh }
                    .filter { it.refresh is LoadState.NotLoading }
                    .collect { binding.rvList.scrollToPosition(0) }
            }
        }

        private fun initSwipeToRefresh() {
            binding.swipeRefresh.setOnRefreshListener { adapter.refresh() }
        }

        private fun initSearch() {
            binding.input.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    updatedSubredditFromInput()
                    true
                } else {
                    false
                }
            }
            binding.input.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    updatedSubredditFromInput()
                    true
                } else {
                    false
                }
            }
        }

        private fun updatedSubredditFromInput() {
            binding.input.text.trim().toString().let {
                if (it.isNotBlank()) {
                    model.showSubreddit(it)
                }
            }
        }
}