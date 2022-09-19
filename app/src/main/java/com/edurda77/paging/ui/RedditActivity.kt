package com.edurda77.paging.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.android.example.paging.pagingwithnetwork.reddit.paging.asMergedLoadStates
import com.edurda77.paging.databinding.ActivityMainBinding
import com.edurda77.paging.databinding.ActivityRedditBinding
import com.edurda77.paging.network.RedditPostRepository
import com.edurda77.paging.presentation.PostsAdapter
import com.edurda77.paging.presentation.PostsLoadStateAdapter
import com.edurda77.paging.presentation.RedditViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter

class RedditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRedditBinding
    //private val viewModel by viewModels<RedditViewModel>()
    private val model: RedditViewModel by viewModels {
        object : AbstractSavedStateViewModelFactory(this, null) {
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                val repoTypeParam = intent.getIntExtra(KEY_REPOSITORY_TYPE, 0)
                val repoType = RedditPostRepository.Type.values()[repoTypeParam]
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
            //val glide = GlideApp.with(this)
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

        companion object {
            private const val KEY_REPOSITORY_TYPE = "repository_type"
            fun intentFor(context: Context, type: RedditPostRepository.Type): Intent {
                val intent = Intent(context, RedditActivity::class.java)
                intent.putExtra(KEY_REPOSITORY_TYPE, type.ordinal)
                return intent
            }
        }
}