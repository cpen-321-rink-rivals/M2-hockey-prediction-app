package com.cpen321.usermanagement.fakes

import com.cpen321.usermanagement.data.remote.dto.PendingRequestData
import com.cpen321.usermanagement.data.remote.dto.SenderData
import com.cpen321.usermanagement.data.repository.FriendsRepository
import com.cpen321.usermanagement.ui.viewmodels.Friend
import kotlinx.coroutines.delay

class FakeFriendsRepository : FriendsRepository {

    var emptyState = false

    private val fakeFriends = mutableListOf(
        Friend(id = "1", name = "Alex"),
        Friend(id = "2", name = "Jamie")
    )

    private val fakePending = mutableListOf(
        PendingRequestData(
            _id = "p1",
            sender = SenderData("3", "Taylor", "taylor@example.com", null),
            receiver = "currentUserId",
            status = "pending"
        )
    )

    override suspend fun getFriends(userId: String): Result<List<Friend>> {
        return if (emptyState) Result.success(emptyList())
        else Result.success(fakeFriends.toList())
    }

    override suspend fun getPendingRequests(): Result<List<PendingRequestData>> {
        return if (emptyState) Result.success(emptyList())
        else Result.success(fakePending.toList())
    }

    override suspend fun sendFriendRequest(friendCode: String): Result<Unit> {
        delay(200)
        return when {
            friendCode.isBlank() -> Result.failure(Exception("Friend code cannot be empty"))
            friendCode == "VALIDCODE" -> Result.success(Unit)
            else -> Result.failure(Exception("Invalid friend code"))
        }
    }

    override suspend fun acceptFriendRequest(requestId: String): Result<Unit> {
        fakeFriends.add(Friend("3", "Taylor"))
        fakePending.removeAll { it._id == requestId }
        return Result.success(Unit)
    }

    override suspend fun rejectFriendRequest(requestId: String): Result<Unit> {
        fakePending.removeAll { it._id == requestId }
        return Result.success(Unit)
    }

    override suspend fun removeFriend(friendId: String): Result<Unit> {
        fakeFriends.removeAll { it.id == friendId }
        return Result.success(Unit)
    }
}
