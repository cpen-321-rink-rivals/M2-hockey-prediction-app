# Requirements and Design

## 1. Change History

| **Change Date**   | **Modified Sections**       | **Rationale**                 |     
| ----------------- | ---------------------       | -------------                 |
| 24. Oct.| Whole project| Our project has unfortunately been downscaled a bit due to a changing team size. This has mostly influenced the live event updating feature that we pivoted to a feature of the user filling out the bingo squares instead. This automatic update of filling out tickets is something that is time consuming but can be fit into our architecture as we already have the backend live game sync up a dn running and we would focus on this as the next milestone in further development of the project.   |
| 25. Oct.          | 4.1                         | Added missing Interfaces and split Friends component into its own for                                                                better seperation    |
| 25. Oct           | 4.4                         | Added new frameworks/libraries   |
| 25. Oct           | 4.5                         | Updated Backend dependicy diagram with new dependencies   |
| 25. Oct            | 3.5  use case 1            | Updated use case from creating the ticket before the events were selected to only creating the ticket after 9 events are chosen as we dont want empty ticekts.|
| 25. Oct             |3.5 use case 2              |Updated Use case 2 from choosing only a ticket to also choosing a game as this was easier to implement |
|25. Oct            | 3.5 use case 4                |We had to redefine use case 4 as we had to lower the project scope due to time constraints. We therefore do not have rankings, but instead we have a viewing of each persons ticket so events can be handled manually by the user.                         |
|  25. Oct    |3.5 use case 5        | We changed the way to add friends to be with a friend code because of easier implementation and time constraints       |
|  25. Oct    |3.4 feature 5, 4.3   | As mentioned before, we will not have rankings. |
|  25. Oct    | 3.7 Non. Func. Requirement 2   | Updated the non-functional requirement to have more of a justification |
|  26. Oct  | 3.7 Non. Func. Requirement 1   | Due to downsizing of the project, we have changes the non-functional req. to only involve the update of game status instead of all game events.  |
| 26. Oct. | 4.7 | Added description of testing of NF req. 1 & 2|


---

## 2. Project Description

Our app is a social points game based on real life ice hockey games. The purpose of the game is to predict events and score bingos before your opponents. You play against each other with self made bingo tickets. These are composed of a 3x3 grid where you can pick an event for each box. Players should pick the events they find most likely and can win by getting bingos, meaning 3 in a row.

---

## 3. Requirements Specification

### **3.1. List of Features**

**1. Sign up and authentication:** Users need to sign in to use the app. New users need to sign up before they sign in. Signed in users can sign out or delete their account.

**2. Manage friends:** A user can send friend requests to other users. Other users may accept or decline friend requests. If a user accepts a friend request, the user will become a friend and appear in the users friend list. Users can also delete friends.

**3. Manage bingo tickets:** Users can create bingo tickets for upcoming hockey games. One game can be chosen for one ticket but multiple tickets may be made for one game. The user can fill their bingo ticket by selecting 9 events from a list and placing them in a 3x3 grid. When the bingo ticket is complete, the user can save it and view it in a tickets list. As the real life hockey game starts, the ticket is assigned points based on a bingo point system.

**4. Manage challenges:** Users can create challenges if they have a bingo ticket. The challenge will have a unique game related to it based on the bingo ticket it has been created from, this will be the “Challenge game”.
One ticket may be used for one challenge but multiple challenges may use the same ticket.
The user who created the challenge will become the challenge owner and can invite other players from their friends list. The challenged users can choose to accept the invitation by selecting or creating their own bingo ticket if this ticket is also based on the “Challenge game”. Users can also decline the invitation and may leave challenges they accepted before.

**5. View challenges:** Users are able to see their challenges in 3 categories, live, upcoming and finished filtering on the time related to the “Challenge game”.
Users can view a specific challenge and see the score of the real life game as well as a points table of the challengers.

### **3.2. Use Case Diagram**
![Use case diagram](/documentation/images/UseCaseDiagram.png)


### **3.3. Actors Description**

1. **[General user]**: The general user can use all the core functionalities such as signing up, managing friends and bingo tickets and accepting, declining and sending challenges.
2. **[The challenge owner]**: The challenge owner is a general user with the ability to close the challenges that are owned.
3. **[The challenge member]**: The challenge member is a general user with the ability to leave challenges.

### **3.4. Use Case Description**

- Use cases for feature 1: [Sign up and authentication]

1. **[Sign up]**: All users can sign up to create a profile.
2. **[Log in]**: All users can log in to their profile.
3. **[Log out]**: All users can log out of their profile.
4. **[Delete account]**: All users can delete their account.
5. **[Edit account]**: All users can change their username, bio, and profile picture.

- Use cases for feature 2: [Manage friends]

1. **[Send friend request]**: All users can request other users to become friends.
2. **[Accept friend request]**: All users can accept friend requests.
3. **[Reject friend request]**: All users can reject friend requests.
4. **[Remove friend]**: All users can remove existing friends.
5. **[View friends]**: All users can see a list of their current friends.

- Use cases for feature 3: [Manage bingo tickets]

1. **[Create a bingo ticket]**: All users can create a bingo ticket for an upcoming game by filling out a 3x3 grid with events from an event list and saving when done.
2. **[Delete bingo ticket]**: All users can delete bingo tickets, if no challenges refer to this ticket.
3. **[View bingo tickets]**: All users can view a list of their bingo tickets.

- Use cases for feature 4: [Manage challenges]

1. **[Send challenge]**: The user can use a bingo ticket to challenge one or several friends to create their own bingo ticket on the specific match and see who gets the highest score.
2. **[Accept challenge]**: The user can receive invites and accept them by creating a bingo ticket for the specific match they have been invited to.
3. **[Decline challenge]**: The user can decline any incoming challenge.
4. **[Close challenge]**: The challenge owner can close a challenge whereby the challenge will disappear for all members of the challenge.
5. **[Leave challenge]**: The challenge member can leave a challenge if the game has not begun.

- Use cases for feature 5: [View challenges]

1. **[View challenge details]**: All users can see current participants and their bingo tickets.
2. **[View active challenges]**: All users can view a list of challenges they are hosting or participating in.
3. **[View challenge rankings]**: All users can view challenge rankings as bingo tickets update automatically when users colour in squares.
4. **[View results]**: All users can view the final scores of a challenge after the game.

### **3.5. Formal Use Case Specifications (5 Most Major Use Cases)**

<a name="uc1"></a>

#### Use Case 1: [Creating a Bingo Ticket]

**Description**: A user chooses 9 events for an upcoming NHL game from a given list and places them in a 3×3 bingo ticket. When they have finished creating the ticket, the user can assign it a name.

**Primary actor(s)**: User

**Main success scenario**:

1. The user assigns a name to the bingo ticket.
2. The user selects an upcoming hockey game.
3. The system displays a list of events to choose from (30) based on the specific game.
4. The user selects 9 events based on the game and places them in the 3×3 grid.
5. The user creates the ticket.
6. The system confirms the ticket has been created.

**Failure scenario(s)**:

- 1a. The ticket name exceeds the character limit.
  - 1a1. The system displays an error message telling the user to remain within the character limit.

- 2a. No upcoming hockey games are available.
  - 2a1. The system displays an error message saying there are no upcoming games.

- 5a. The hockey game has begun while naming the ticket.
  - 5a1. The system displays an error message saying the game has already begun.
  - 5a2. The user is returned to the main menu.

...

<a name="uc2"></a>

#### Use Case 2: [Send Challenge]

**Description**: A user creates a challenge for an upcoming hockey game and invites friends to participate.

**Primary actor(s)**: User

**Main success scenario**:

1. The user selects a game that they want to make a challenge on
2. The user selects a bingo ticket that is made on the same game.
3. The user selects one or more friends to invite.
4. The user creates the challenge invitation.
5. The system notifies the invited friends of the challenge.

**Failure scenario(s)**:

- 3b. The owner has no friends in their list.
  - 3b1. The system displays an error message saying a challenge cannot be created without friends.

- 4a. No friends, no game or no ticket is selected.
  - 4a1. The system displays an error message requiring at least one friend, a game and a ticket.

- 4b. The hockey game has already begun while creating the challenge.
  - 4b1. The system displays an error message saying the game has already begun.

#### Use Case 3: [Accept Challenge]

**Description**: A user is prompted with an invitation to join a group challenge. They can accept by selecting or creating a bingo ticket for the specified game.

**Primary actor(s)**: User

**Main success scenario**:

1. The user views the pending challenge invitation.
2. The user chooses to accept the invitation.
3. The user either selects an existing bingo ticket for the specified game or creates a new one.
4. The system adds the user to the challenge and confirms acceptance.

**Alternate Triggers**
3.1 The user chooses to create a new ticket.
— Continue at “Creating a bingo ticket” use case.

**Failure scenario(s)**:

- 2a. The hockey game has already begun while accepting the invitation.

  - 2a1. The system displays an error message saying the game has already begun.

  - 2a2. The user is returned to the main menu.

- 4a. The hockey game has already begun while submitting a bingo ticket.

  - 4a1. The system displays an error message saying the game has already begun.

  - 4a2. The user is returned to the main menu.

#### Use Case 4: [View challenge tickets]

**Description**: A user selects an ongoing challenge and can view all the participating bingo tickets to see who predicted the most events right.

**Primary actor(s)**: User

**Main success scenario**:

1. The user navigates to their list of current challenges.
2. The user selects a specific live challenge.
3. The system displays a list of all participating bingo tickets.


#### Use Case 5: [Send friend request]

**Description**: A user finds another user via their email and requests to become friends.

**Primary actor(s)**: User

**Main success scenario**:

1. The user navigates to their list of current friends.
2. The user types the friendCode of the friend in a form.
3. The user clicks an icon to add a new friend.
4. The system notifies the user that the request has been successfully sent.

**Failure scenario(s)**:

- 2a. The user enters an invalid friendCode
  - 2a1. The system displays a message: “Please enter a valid friend code.”

- 4a. There is no user associated with the friendCode.
  - 4a1. The system notifies the user that no user is associated with that email.

- 4b. The user associated with the email is already a friend.
  - 4b1. The system notifies the user that the accounts are already friends.

### **3.6. Screen Mock-ups**

![Screen Mock-ups](/documentation/images/user-journey-sccreen-mockups.png)

### **3.7. Non-Functional Requirements**

<a name="nfr1"></a>

1. **[Live updates]**

   - **Description**: Bingo ticket status should update within 1 min of the event occurring.
   - **Justification**: The element of live interactivity is very important as the game depends on a live hockey event, and the game needs to be updated quickly for the user to follow the state of the challenge. We include this requirement as it is essential for any further develpoments of the app even though the current state of the app does not rely as heavily on the live event updates as the user fills out the bingo ticket themselves. 

2. **[Seamless bingo ticket building]**
   - **Description**: When creating a bingo ticket the system should be able to find events in less than 1 s.
   - **Justification**: The core feature of our app is building bingo tickets and ideally this is where the user will spend the most time. Studies and UX standards indicate that response times under 1 second are perceived as instantaneous, maintaining the user’s sense of flow and engagement. This ensures users can quickly iterate and build tickets without frustration or cognitive interruption.

---

## 4. Designs Specification

### **4.1. Main Components**

## 4.1. Main Components

1. **[Users]**
   - **Purpose**: Handles user profile management including viewing, updating, and deleting user accounts.
   - **Interfaces**:
        1. **HTTP/REST Interfaces** (Frontend → Backend):
            - `GET /api/user/profile`
                - **Parameters**: `Authorization: Bearer {token}`
                - **Returns**: `User`
                - **Purpose**: Retrieves the authenticated user's complete profile data including name, email, bio, and profile picture.
            - `GET /api/user/:id`
                - **Parameters**: `Authorization: Bearer {token}`, `userId: String`
                - **Returns**: `PublicUserInfo`
                - **Purpose**: Fetches public profile information for any user by their ID (name, profile picture, friend code).
            - `PUT /api/user/profile`
                - **Parameters**: `Authorization: Bearer {token}`, `{ name?: String, bio?: String, profilePictureURL?: String }`
                - **Returns**: `User`
                - **Purpose**: Updates the authenticated user's profile fields such as username, bio, or profile picture URL.
            - `DELETE /api/user/profile`
                - **Parameters**: `Authorization: Bearer {token}`
                - **Returns**: `{ message: String }`
                - **Purpose**: Permanently deletes the authenticated user's account and all associated data.
        2. **Java-style Interfaces** (Backend Internal):
            - `create(userInfo: GoogleUserInfo): User`
                - **Purpose**: Creates a user document in database with ID.
            - `update(userId: mongoose.Types.ObjectId, user: Partial<User>): User`
                - **Purpose**: Updates user document in database with new field values.
            - `delete(userId: mongoose.Types.ObjectId): void`
                - **Purpose**: Removes user document and cascades deletion to related data.
            - `findById(_id: mongoose.Types.ObjectId): User`
                - **Purpose**: Retrieves a user document from database by ID.
            - `findUserInfoById(userId: string): PublicUserInfo`
                - **Purpose**: Retrieves only public info from user document from database by ID.
            - `findByGoogleId(googleId: String): User`
                - **Purpose**: Finds a user account by their Google OAuth ID.
            - `findByFriendCode(code: String): User`
                - **Purpose**: Finds a user account by their friend code.

---

2. **[Friends]**
   - **Purpose**: Manages friend relationships including sending, accepting, rejecting, and removing friend connections.
   - **Interfaces**:
        1. **HTTP/REST Interfaces** (Frontend → Backend):
            - `POST /api/friends/request`
                - **Parameters**: `Authorization: Bearer {token}`, `{ friendCode: String }`
                - **Returns**: `{ message: String }`
                - **Purpose**: Sends a friend request to another user using their unique friend code.
            - `POST /api/friends/accept`
                - **Parameters**: `Authorization: Bearer {token}`, `{ requesterId: String }`
                - **Returns**: `{ message: String }`
                - **Purpose**: Accepts a pending incoming friend request from specified user.
            - `POST /api/friends/reject`
                - **Parameters**: `Authorization: Bearer {token}`, `{ requesterId: String }`
                - **Returns**: `{ message: String }`
                - **Purpose**: Declines a pending friend request without establishing connection.
            - `GET /api/friends/list`
                - **Parameters**: `Authorization: Bearer {token}`
                - **Returns**: `Friend[]`
                - **Purpose**: Retrieves the complete list of all accepted friends with their profile information.
            - `GET /api/friends/pending`
                - **Parameters**: `Authorization: Bearer {token}`
                - **Returns**: `{ incoming: PendingRequest[], outgoing: PendingRequest[] }`
                - **Purpose**: Lists all incoming and outgoing pending friend requests.
            - `DELETE /api/friends/:friendId`
                - **Parameters**: `Authorization: Bearer {token}`, `friendId: String`
                - **Returns**: `{ message: String }`
                - **Purpose**: Removes an existing friend connection permanently from both users' friend lists.
        2. **Java-style Interfaces** (Backend Internal):
            - `sendRequest(senderId: String, receiverId: String): FriendRequest`
                - **Purpose**: Creates a pending friend request entry in database.
            - `acceptRequest(requestId: string): FriendRequest`
                - **Purpose**: Sets the friendRequest's status to accepted in the database.
            - `rejectRequest(requestId: string: FriendRequest)`
                - **Purpose**: Sets the friendRequest's status to rejected in the database.
            - `getFriends(userId: string): FriendRequest[] `
                - **Purpose**: Gets a list of users friends.
            - `removeFriend(userId: string, friendId: string): FriendRequest`
                - **Purpose**: Deletes friend relationship from database.
            - `getPendingRequests(userId: string): FriendRequest`
                - **Purpose**: get all friend requests not accepted and with the userId as sender or receiver.

---

3. **[Bingo Tickets]**
   - **Purpose**: Handles creation, retrieval, and deletion of bingo tickets for NHL game predictions.
   - **Interfaces**:
        1. **HTTP/REST Interfaces** (Frontend → Backend):
            - `POST /api/tickets`
                - **Parameters**: `Authorization: Bearer {token}`, `{ userId: String, name: String, game: Game, events: String[9] }`
                - **Returns**: `BingoTicket`
                - **Purpose**: Creates a new bingo ticket with 9 selected events for a specific NHL game.
            
            - `GET /api/tickets/user/:userId`
                - **Parameters**: `Authorization: Bearer {token}`, `userId: String`
                - **Returns**: `BingoTicket[]`
                - **Purpose**: Retrieves all bingo tickets created by a specific user, sorted by creation date.
            
            - `GET /api/tickets/:id`
                - **Parameters**: `Authorization: Bearer {token}`, `ticketId: String`
                - **Returns**: `BingoTicket`
                - **Purpose**: Fetches a specific bingo ticket by its unique ID including all selected events.
            
            - `DELETE /api/tickets/:id`
                - **Parameters**: `Authorization: Bearer {token}`, `ticketId: String`
                - **Returns**: `{ message: String }`
                - **Purpose**: Deletes a bingo ticket permanently from the database.
        2. **Java-style Interfaces**
             We do not have internal backend interactions for tickets as we do for the other backend components.


---

4. **[Challenges]**
   - **Purpose**: Manages challenge lifecycle including creation, participation, invitations, and status tracking.
   - **Interfaces**:
        1. **HTTP/REST Interfaces** (Frontend → Backend):
            - `GET /api/challenges`
                - **Parameters**: `Authorization: Bearer {token}`
                - **Returns**: `Challenge[]`
                - **Purpose**: Retrieves all challenges with pagination support.
            - `GET /api/challenges/user`
                - **Parameters**: `Authorization: Bearer {token}`
                - **Returns**: `{ owned: Challenge[], joined: Challenge[], invited: Challenge[] }`
                - **Purpose**: Fetches all challenges where user is owner, member, or has pending invitation.
            - `GET /api/challenges/:id`
                - **Parameters**: `Authorization: Bearer {token}`, `challengeId: String`
                - **Returns**: `Challenge`
                - **Purpose**: Retrieves detailed information for a specific challenge including all members and their tickets.
            - `GET /api/challenges/game/:gameId`
                - **Parameters**: `Authorization: Bearer {token}`, `gameId: String`
                - **Returns**: `Challenge[]`
                - **Purpose**: Fetches all challenges associated with a specific NHL game.
            - `POST /api/challenges`
                - **Parameters**: `Authorization: Bearer {token}`, `{ title: String, description: String, gameId: String, invitedUserIds: String[], maxMembers?: Number, ticketId?: String }`
                - **Returns**: `Challenge`
                - **Purpose**: Creates a new challenge for an NHL game and sends invitations to specified friends.
            - `PUT /api/challenges/:id`
                - **Parameters**: `Authorization: Bearer {token}`, `challengeId: String`, `{ title?: String, description?: String, status?: String, maxMembers?: Number }`
                - **Returns**: `Challenge`
                - **Purpose**: Updates challenge details (owner only) such as title, description, or member limit.
            - `DELETE /api/challenges/:id`
                - **Parameters**: `Authorization: Bearer {token}`, `challengeId: String`
                - **Returns**: `{ message: String }`
                - **Purpose**: Permanently deletes a challenge and notifies all members (owner only).
            - `POST /api/challenges/:id/join`
                - **Parameters**: `Authorization: Bearer {token}`, `challengeId: String`, `{ ticketId: String }`
                - **Returns**: `{ message: String }`
                - **Purpose**: Joins user to a challenge with their selected bingo ticket.
            - `POST /api/challenges/:id/leave`
                - **Parameters**: `Authorization: Bearer {token}`, `challengeId: String`
                - **Returns**: `{ message: String }`
                - **Purpose**: Removes user from a challenge if game has not started yet.
            - `POST /api/challenges/:id/decline`
                - **Parameters**: `Authorization: Bearer {token}`, `challengeId: String`
                - **Returns**: `{ message: String }`
                - **Purpose**: Declines a challenge invitation without joining.
            - `PATCH /api/challenges/:id/status`
                - **Parameters**: `Authorization: Bearer {token}`, `challengeId: String`, `{ status: String }`
                - **Returns**: `Challenge`
                - **Purpose**: Updates challenge status (used by game sync system for PENDING→ACTIVE→LIVE→FINISHED transitions).
        2. **Java-style Interfaces** (Backend Internal):
            - `create(challengeData: CreateChallengeInput, ownerId: String): Challenge`
                - **Purpose**: Creates new challenge document with owner automatically added as member.
            - `update(id: string, data: Partial<Challenge>): Challenge`
                - **Purpose**: Updates challenge document.
            - `delete(id: string, ownerId: string): void`
                - **Purpose**: deletes challenge document.
            - `findById(id: string): Challenge`
                - **Purpose**: Find a challenge by id.
            - `findAll(page: number = 1, limit: number = 10): { challenges: Challenge[]; total: number }`
                - **Purpose**: Get all challenges paginated
            - `getUserChallenges(userId: string, status?: string): Challenge[]`
                - **Purpose**: gets all challenges that a user is a part of.
            - `joinChallenge(challengeId: string, userId: string, ticketId: string): Challenge`
                - **Purpose**: lets a user join a challenge
            - `leaveChallenge(challengeId: string, userId: string): Challenge`
                - **Purpose**: lets the user leave challenge
            - `declineInvitation(challengeId: string, userId: string) Challenge | null`
                - **Purpose**: lets the user decline the invitation
            - `updateStatus(challengeId: string, status: string, ownerId?: string): Challenge | null`
                - **Purpose**: updates the challenge status
            - `findByGameId(gameId: string): Challenge[]`
                - **Purpose**: finds all the challenges with a given gameId

---

5. **[NHL Service (Game Status Sync)]**
   - **Purpose**: Fetches, caches, and processes NHL game data from external API for schedule and game status tracking.
   - **Interfaces**:
        1. **Java-style Interfaces** (Backend Internal):
            - `getGameStatus(gameId: String): Promise<GameStatus | null>`
                - **Purpose**: Fetches current game state from NHL API schedule endpoint with 30-second caching for performance, returns game status or falls back to direct endpoint if not found in schedule.
            - `getGameStatusDirect(gameId: String): Promise<GameStatus | null>`
                - **Purpose**: Retrieves game state directly from NHL API game-specific landing endpoint (`/gamecenter/{gameId}/landing`) bypassing schedule lookup.
            - `isGameLive(gameState: String): boolean`
                - **Purpose**: Determines if a game is currently in progress by checking if state matches LIVE, CRIT, or PRE.
            - `isGameFinished(gameState: String): boolean`
                - **Purpose**: Checks if a game has concluded by verifying state is OFF or FINAL.
            - `isGameScheduled(gameState: String): boolean`
                - **Purpose**: Verifies if a game is scheduled for the future by checking if state is FUT or SCHEDULED.
            - `clearCache(gameId?: String): void`
                - **Purpose**: Removes cached game status for specific game ID or clears entire cache if no ID provided.
            - `getTimeUntilStart(startTimeUTC: String): number`
                - **Purpose**: Calculates milliseconds until game starts from UTC timestamp, returns negative value if game already started.
            - `getPollingInterval(gameStatus: GameStatus): number`
                - **Purpose**: Determines optimal polling frequency in milliseconds based on game state (30s for live, 1min near start, 5min within hour, 10min otherwise).
        2. **HTTP/REST Interfaces** (Backend → External NHL API):
            - `GET https://api-web.nhle.com/v1/schedule/now`
                - **Returns**: `ScheduleResponse`
                - **Purpose**: Backend fetches game schedule for status sync job.
            - `GET https://api-web.nhle.com/v1/gamecenter/{gameId}/landing`
                - **Parameters**: `gameId: String`
                - **Returns**: `GameData`
                - **Purpose**: Retrieves detailed game information including current state for specific game. Was supposed to bridge to a future implementation of automatic check for events completion.

---


### **4.2. Databases**

1. **[MongoDB]**
   - **Purpose**: Stores users/friends, tickets, challenges, and read-optimized projections.

### **4.3. External Modules**

1. **[NHL API]**
   - **Purpose**: Fetches schedule metadata for ticket and challenge creation.
2. **[Google authentications]**
   - **Purpose**: Handles user authentication when signing up/deleting account and logging in/out.

### **4.4. Frameworks and Libraries**

1. **[Express. js]**
   - **Purpose**: Enable web development in Node.js
   - **Reason**: It is the easiest way for us to get started develeping. It helps with routing, requests and responses.
2. **[Retrofit]**
   - **Purpose**: Make HTTP requests in Android.
   - **Reason**: We need to show API data to the frontend. 
2. **[OkHttp]**
   - **Purpose**: HTTP client.
   - **Reason**: Used together with Retrofit to make http requests.
3. **[Mongoose]**
   - **Purpose**: Interacting with MongoDB 
   - **Reason**: Making it easier to work with the database. 
4. **[Socket.io]**
   - **Purpose**: Live updates support
   - **Reason**: Enables us to implement the live updates when for example user creates and invites another user.
4. **[Axios]**
   - **Purpose**: HTTP client for backend
   - **Reason**: Enabling us to fetch NHL data every 60 seconds from the Typescript backend.
### **4.5. Dependencies Diagram**

![High level backend diagram](/documentation/images/high-level-backend-diagram.png)


### **4.6. Use Case Sequence Diagram (5 Most Major Use Cases)**

1. [**[Create Bingo Ticket]**](#uc1)\
   ![Create bingo ticket use case sequence diagram](/documentation/images/seq-dia-1.png)
1. [**[Send Challenge]**](#uc2)\
   ![Send Challenge use case sequence diagram](/documentation/images/seq-dia-2.png)
1. [**[Accept Challenge]**](#uc3)\
   ![Accept Challenge use case sequence diagram](/documentation/images/seq-dia-3.png)
1. [**[View challenge rankings]**](#uc4)\
   ![View challenge rankings use case sequence diagram](/documentation/images/seq-dia-4.png)
1. [**[Send friend request]**](#uc5)\
   ![Send friend request use case sequence diagram](/documentation/images/seq-dia-5.png)

### **4.7. Design and Ways to Test Non-Functional Requirements**

1. [**[Live updates]**](#nfr1)
   - **Validation**: We have tested and validated the game sync by logging messages in the backend terminal when a game sync has been run and, if any, which changes has been made to the game status. We have also tested by letting the backend server run in the cloud overnight and when the real live event has finished the users challenges has been correctly moved into the group of finished challenges.

2. [**[Seamless bingo ticket building]**](#nfr2)
   - **Validation**: We have continously tested this as we have been developing as it has involved making a lot of tickets. We have also tested by creating fetching the game events 10 times and logged the time stats in `/documentation/logs/fetching_events.txt`. This shows no longer than a quarter of a second on avg, which is far better than the 1 second requirement.
