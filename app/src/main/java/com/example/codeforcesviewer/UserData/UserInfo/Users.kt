package com.example.codeforcesviewer.UserData.UserInfo

data class Users(
    val avatar: String?,
    val email: String?,
    val city: String?,
    val contribution: Int,
    val country: String,
    val firstName: String?,
    val friendOfCount: Int,
    val handle: String,
    val lastName: String?,
    val lastOnlineTimeSeconds: Long,
    val maxRank: String?,
    val maxRating: Int?,
    val organization: String?,
    val rank: String?,
    val rating: Int?,
    val registrationTimeSeconds: Long,
    val titlePhoto: String
)