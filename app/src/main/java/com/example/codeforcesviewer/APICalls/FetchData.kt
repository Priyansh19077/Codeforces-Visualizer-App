package com.example.codeforcesviewer

import com.example.codeforcesviewer.UserData.UserContests
import com.example.codeforcesviewer.UserData.UserPublicData
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val url = "https://codeforces.com/api/"
interface ProfileDataFetch {

    @GET("user.info")
    fun getUserData(
            @Query("handles")handle : String,
            @Query("lang")lang : String = "en") : retrofit2.Call<UserPublicData>

    @GET("user.ratedList?activeOnly=false&lang=en")
    fun getAllUsers() : retrofit2.Call<UserPublicData>

    @GET("user.rating")
    fun getUserRatedContests(
            @Query("handle")handle : String,
            @Query("lang")lang : String = "en") : retrofit2.Call<UserContests>
}



object FetchData {
    val instance : ProfileDataFetch
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        instance = retrofit.create(ProfileDataFetch::class.java)
    }
}