package com.edurda77.paging.entity

class ListingData(
    val children: List<RedditChildrenResponse>,
    val after: String?,
    val before: String?
)