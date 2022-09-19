package com.edurda77.paging.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.edurda77.paging.cache.RedditDb
import com.edurda77.paging.cache.RedditPostDao
import com.edurda77.paging.cache.SubredditRemoteKeyDao
import com.edurda77.paging.entity.RedditPost
import com.edurda77.paging.entity.SubredditRemoteKey
import com.edurda77.paging.network.RedditApi
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class PageKeyedRemoteMediator(
    private val db: RedditDb,
    private val redditApi: RedditApi,
    private val subredditName: String
) : RemoteMediator<Int, RedditPost>() {
    private val postDao: RedditPostDao = db.posts()
    private val remoteKeyDao: SubredditRemoteKeyDao = db.remoteKeys()

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, RedditPost>
    ): MediatorResult {
        try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {

                    val remoteKey = db.withTransaction {
                        remoteKeyDao.remoteKeyByPost(subredditName)
                    }

                    if (remoteKey.nextPageKey == null) {
                        return MediatorResult.Success(endOfPaginationReached = true)
                    }

                    remoteKey.nextPageKey
                }
            }

            val data = redditApi.getTop(
                subreddit = subredditName,
                after = loadKey,
                before = null,
                limit = when (loadType) {
                    LoadType.REFRESH -> state.config.initialLoadSize
                    else -> state.config.pageSize
                }
            ).data

            val items = data.children.map { it.data }

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    postDao.deleteBySubreddit(subredditName)
                    remoteKeyDao.deleteBySubreddit(subredditName)
                }

                remoteKeyDao.insert(SubredditRemoteKey(subredditName, data.after))
                postDao.insertAll(items)
            }

            return MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }
}
