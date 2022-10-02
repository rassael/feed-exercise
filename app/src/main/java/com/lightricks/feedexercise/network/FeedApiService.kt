package com.lightricks.feedexercise.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET


/**
 * todo: add the FeedApiService interface and the Retrofit and Moshi code here
 */

interface FeedApiService {

    companion object {
        private const val baseURI: String = "https://assets.swishvideoapp.com/"
        private var moshi: Moshi? = null
        private var retrofit: Retrofit? = null
        private var INSTANCE: FeedApiService? = null
        fun getFeedApiService(): FeedApiService {
            synchronized(this) {   // keep the instance thread-safe
                if (INSTANCE == null) {
                    moshi = Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    retrofit = Retrofit.Builder()
                            .addConverterFactory(MoshiConverterFactory.create(moshi!!))
                            .baseUrl(baseURI)
                            .build()
                    INSTANCE = retrofit!!.create(FeedApiService::class.java)
                }
            }
            return INSTANCE as FeedApiService
        }
    }

    /** method that executes a GET request and returns an Coroutine’s Response that contains the response */
    @GET("/Android/demo/feed.json")
    suspend fun getFeedItems(): GetFeedResponse
}
