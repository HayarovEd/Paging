package com.edurda77.paging.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.edurda77.paging.entity.RedditPost
import com.edurda77.paging.network.RedditApi
import retrofit2.HttpException
import java.io.IOException

class ItemKeyedSubredditPagingSource(
    private val redditApi: RedditApi,
    private val subredditName: String
) : PagingSource<String, RedditPost>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, RedditPost> {
        return try {
            val items = redditApi.getTop(
                subreddit = subredditName,
                after = if (params is LoadParams.Append) params.key else null,
                before = if (params is LoadParams.Prepend) params.key else null,
                limit = params.loadSize
            ).data.children.map { it.data }

            LoadResult.Page(
                data = items,
                prevKey = items.firstOrNull()?.name,
                nextKey = items.lastOrNull()?.name
            )
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, RedditPost>): String? {

        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.name
        }
    }
}