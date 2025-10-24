package com.cpen321.usermanagement.data.remote.dto

data class UpdateProfileRequest(
    val name: String? = null,
    val bio: String? = null,
    val hobbies: List<String>? = null,
    val languagesSpoken: List<String>? = null,
    val profilePicture: String? = null
)

data class ProfileData(
    val user: User
)

data class PublicProfileData(
    val _id: String,
    val name: String,
    val bio: String?,
    val friendCode: String,
    val profilePicture: String,
    val hobbies: List<String> = emptyList(),
)

data class User(
    val _id: String,
    val email: String,
    val name: String,
    val bio: String?,
    val friendCode: String,
    val profilePicture: String,
    val hobbies: List<String> = emptyList(),
    val languagesSpoken: List<String> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class UploadImageData(
    val image: String
)