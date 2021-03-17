package com.example.codeforcesviewer

import com.example.codeforcesviewer.UserData.ContestData.UserContests
import com.example.codeforcesviewer.UserData.SubmissionData.UserSubmissions
import com.example.codeforcesviewer.UserData.UserInfo.UserPublicData
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit


const val url = "https://codeforces.com/api/"

interface ProfileDataFetch {

    @GET("user.info")
    fun getUserData(
            @Query("handles") handle: String,
            @Query("lang") lang: String = "en"): retrofit2.Call<UserPublicData>

    @GET("user.ratedList?activeOnly=false&lang=en")
    fun getAllUsers(): retrofit2.Call<UserPublicData>

    @GET("user.rating")
    fun getUserRatedContests(
            @Query("handle") handle: String,
            @Query("lang") lang: String = "en"): retrofit2.Call<UserContests>

    @GET("user.status")
    fun getSubmissions(
            @Query("handle") handle : String,
            @Query("lang") lang : String = "en",
            @Query("from") from : Int = 1,
            @Query("count") count : Int = 100000000) : retrofit2.Call<UserSubmissions>
}


object FetchData {
    var okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    val instance: ProfileDataFetch

    init {
        val retrofit = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        instance = retrofit.create(ProfileDataFetch::class.java)
    }
}