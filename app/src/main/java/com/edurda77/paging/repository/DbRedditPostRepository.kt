package com.edurda77.paging.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.edurda77.paging.cache.RedditDb
import com.edurda77.paging.network.RedditApi
import com.edurda77.paging.network.RedditPostRepository

class DbRedditPostRepository(val db: RedditDb, val redditApi: RedditApi) : RedditPostRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun postsOfSubreddit(subReddit: String, pageSize: Int) = Pager(
        config = PagingConfig(pageSize),
        remoteMediator = PageKeyedRemoteMediator(db, redditApi, subReddit)
    ) {
        db.posts().postsBySubreddit(subReddit)
    }.flow
}