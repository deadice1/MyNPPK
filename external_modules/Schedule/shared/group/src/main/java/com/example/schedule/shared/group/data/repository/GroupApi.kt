package com.example.schedule.shared.group.data.repository

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

data class GroupResponse(
    val status: String,
    val data: List<GroupDto>,
)

data class GroupDto(
    val id: Long,
    @SerializedName("name")
    val name: String,
)

interface GroupApi {
    @GET("api/groups")
    suspend fun getGroups(): GroupResponse
}