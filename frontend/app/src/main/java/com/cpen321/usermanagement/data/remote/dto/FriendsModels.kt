package com.cpen321.usermanagement.data.remote.dto

data class FriendData(
    val _id: String,
    val sender: SenderData,
    val receiver: SenderData,
    val status: String
)


data class PendingRequestData(
    val _id: String,
    val sender: SenderData,
    val receiver: String,
    val status: String
)

data class SenderData(
    val _id: String,
    val name: String,
    val email: String,
    val profilePicture: String?
)