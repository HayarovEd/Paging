package com.edurda77.paging.network

import androidx.paging.PagingData
import com.edurda77.paging.entity.RedditPost
import kotlinx.coroutines.flow.Flow

interface RedditPostRepository {
    fun postsOfSubreddit(subReddit: String, pageSize: Int): Flow<PagingData<RedditPost>>

    enum class Type {
        IN_MEMORY_BY_ITEM,
        IN_MEMORY_BY_PAGE,
        DB
    }
}