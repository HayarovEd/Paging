package com.edurda77.paging.ui

import android.app.Application
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.edurda77.paging.cache.RedditDb
import com.edurda77.paging.network.RedditApi
import com.edurda77.paging.network.RedditPostRepository
import com.edurda77.paging.repository.DbRedditPostRepository
import com.edurda77.paging.repository.InMemoryByItemRepository
import com.edurda77.paging.repository.InMemoryByPageKeyRepository

interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(context: Context): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator(
                        app = context.applicationContext as Application,
                        useInMemoryDb = false)
                }
                return instance!!
            }
        }

        @VisibleForTesting
        fun swap(locator: ServiceLocator) {
            instance = locator
        }
    }

    fun getRepository(type: RedditPostRepository.Type): RedditPostRepository

    fun getRedditApi(): RedditApi
}

open class DefaultServiceLocator(val app: Application, val useInMemoryDb: Boolean) : ServiceLocator {
    private val db by lazy {
        RedditDb.create(app, useInMemoryDb)
    }

    private val api by lazy {
        RedditApi.create()
    }

    override fun getRepository(type: RedditPostRepository.Type): RedditPostRepository {
        return when (type) {
            RedditPostRepository.Type.IN_MEMORY_BY_ITEM -> InMemoryByItemRepository(
                redditApi = getRedditApi()
            )
            RedditPostRepository.Type.IN_MEMORY_BY_PAGE -> InMemoryByPageKeyRepository(
                redditApi = getRedditApi()
            )
            RedditPostRepository.Type.DB -> DbRedditPostRepository(
                db = db,
                redditApi = getRedditApi()
            )
        }
    }

    override fun getRedditApi(): RedditApi = api
}