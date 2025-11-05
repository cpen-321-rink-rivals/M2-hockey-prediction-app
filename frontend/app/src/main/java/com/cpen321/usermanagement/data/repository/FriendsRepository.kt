package com.cpen321.usermanagement.data.repository

import com.cpen321.usermanagement.data.remote.dto.PendingRequestData
import com.cpen321.usermanagement.ui.viewmodels.Friend

interface FriendsRepository {
    suspend fun getFriends(currentUserId: String): Result<List<Friend>>

    suspend fun getPendingRequests(): Result<List<PendingRequestData>>

    suspend fun sendFriendRequest(friendCode: String): Result<Unit>

    suspend fun acceptFriendRequest(requestId: String): Result<Unit>

    suspend fun rejectFriendRequest(requestId: String): Result<Unit>

    suspend fun removeFriend(friendId: String): Result<Unit>
}
