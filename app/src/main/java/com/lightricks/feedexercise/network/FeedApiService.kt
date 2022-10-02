package com.lightricks.feedexercise.network

import retrofit2.Response
import retrofit2.http.GET


/**
 * todo: add the FeedApiService interface and the Retrofit and Moshi code here
 */

interface FeedApiService {

    /** method that executes a GET request and returns an Coroutine’s Response that contains the response */
    @GET("/Android/demo/feed.json")
    suspend fun getFeedItems(): Response<GetFeedResponse>
}
