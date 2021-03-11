package com.example.codeforcesviewer

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

const val url = "https://codeforces.com/api/"
interface ProfileDataFetch {

    @GET("user.info")
    fun getUserData(@Query("handles")handle : String) : retrofit2.Call<UserData>

    @GET("user.ratedList?activeOnly=false")
    fun getAllUsers() : retrofit2.Call<UserData>
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