package com.edurda77.paging.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.edurda77.paging.entity.RedditPost
import com.edurda77.paging.entity.SubredditRemoteKey
import com.edurda77.paging.utils.NAME_DB

@Database(
    entities = [RedditPost::class, SubredditRemoteKey::class],
    version = 1,
    exportSchema = false
)
abstract class RedditDb : RoomDatabase() {
    companion object {
        fun create(context: Context, useInMemory: Boolean): RedditDb {
            val databaseBuilder = if (useInMemory) {
                Room.inMemoryDatabaseBuilder(context, RedditDb::class.java)
            } else {
                Room.databaseBuilder(context, RedditDb::class.java, NAME_DB)
            }
            return databaseBuilder
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    abstract fun posts(): RedditPostDao
    abstract fun remoteKeys(): SubredditRemoteKeyDao
}