package com.cpen321.usermanagement.data.remote.dto

import com.google.gson.annotations.SerializedName

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val ownerId: String,
    val gameId: String,
    val status: ChallengeStatus,
    val memberIds: List<String>,
    val invitedUserIds: List<String>,
    val maxMembers: Int,
    val ticketIds: Map<String, String>,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val gameStartTime: String? = null,
)
enum class ChallengeStatus {
    @SerializedName("pending")
    PENDING,

    @SerializedName("active")
    ACTIVE,

    @SerializedName("live")
    LIVE,

    @SerializedName("finished")
    FINISHED,

    @SerializedName("cancelled")
    CANCELLED
}

data class CreateChallengeRequest(
    val title: String,
    val description: String,
    val gameId: String,
    val invitees: List<String>? = null,
    val maxMembers: Int? = null,
    val gameStartTime: String? = null
)



data class ChallengeResponse(
    val success: Boolean,
    val data: Challenge,
    val message: String?
)

data class ChallengesListResponse(
    val success: Boolean,
    val data: List<Challenge>,
    val pagination: Pagination
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int,
    val pages: Int
)

data class UserChallengesResponse(
    val success: Boolean,
    val data: UserChallengesData,
    val total: Int
)

data class UserChallengesData(
    val pending: List<Challenge>,
    val active: List<Challenge>,
    val live: List<Challenge>,
    val finished: List<Challenge>,
    val cancelled: List<Challenge>
)
