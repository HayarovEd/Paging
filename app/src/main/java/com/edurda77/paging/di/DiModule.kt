/*
package com.edurda77.paging.di

import com.edurda77.paging.network.RedditApi
import com.edurda77.paging.network.RedditPostRepository
import com.edurda77.paging.repository.InMemoryByItemRepository
import com.edurda77.paging.utils.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiModule {

    @Provides
    @Singleton
    fun create(): RedditApi {


        val client = OkHttpClient.Builder()
            .build()
        return Retrofit.Builder()
            .baseUrl(HttpUrl.parse(BASE_URL)!!)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RedditApi::class.java)
    }
    @Provides
    @Singleton
    fun provideMainRemoteData(redditApi: RedditApi) : InMemoryByItemRepository = InMemoryByItemRepository(redditApi)
}*/
