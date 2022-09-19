package com.edurda77.paging.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.edurda77.paging.network.RedditApi
import com.edurda77.paging.network.RedditPostRepository

class InMemoryByPageKeyRepository(private val redditApi: RedditApi) : RedditPostRepository {
    override fun postsOfSubreddit(subReddit: String, pageSize: Int) = Pager(
        PagingConfig(pageSize)
    ) {
        PageKeyedSubredditPagingSource(
            redditApi = redditApi,
            subredditName = subReddit
        )
    }.flow
}
