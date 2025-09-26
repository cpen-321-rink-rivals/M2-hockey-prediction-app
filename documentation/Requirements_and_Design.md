# Requirements and Design

## 1. Change History

| **Change Date**   | **Modified Sections** | **Rationale** |
| ----------------- | --------------------- | ------------- |
| _Nothing to show_ |

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

3. **[Create a bingo ticket]**: All users can create a bingo ticket for an upcoming game by filling out a 3x3 grid with events from an event list and saving when done.
4. **[Delete bingo ticket]**: All users can delete bingo tickets, if no challenges refer to this ticket.
5. **[View bingo tickets]**: All users can view a list of their bingo tickets.

- Use cases for feature 4: [Manage challenges]

1. **[Send challenge]**: The user can use a bingo ticket to challenge one or several friends to create their own bingo ticket on the specific match and see who gets the highest score.
2. **[Accept challenge]**: The user can receive invites and accept them by creating a bingo ticket for the specific match they have been invited to.
3. **[Decline challenge]**: The user can decline any incoming challenge.
4. **[Close challenge]**: The challenge owner can close a challenge whereby the challenge will disappear for all members of the challenge.
5. **[Leave challenge]**: The challenge member can leave a challenge if the game has not begun.

- Use cases for feature 5: [View challenges]

1. **[View challenge details]**: All users can see current participants and their bingo tickets.
2. **[View active challenges]**: All users can view a list of challenges they are hosting or participating in.
3. **[View challenge rankings]**: All users can view challenge rankings as bingo tickets update automatically during the game.
4. **[View results]**: All users can view the final scores of a challenge after the game.

### **3.5. Formal Use Case Specifications (5 Most Major Use Cases)**

<a name="uc1"></a>

#### Use Case 1: [Creating a Bingo Ticket]

**Description**: A user chooses 9 events for an upcoming NHL game from a given list and places them in a 3×3 bingo ticket. When they have finished creating the ticket, the user can assign it a name.

**Primary actor(s)**: User

**Main success scenario**:

1. The user selects an upcoming hockey game.
2. The user assigns a name to the bingo ticket.
3. The user creates the ticket.
4. The system confirms the ticket has been created.
5. The system generates a list of events to choose from (~30).
6. The user selects 9 events and places them in the 3×3 grid.
7. The system updates the ticket.
8. The system confirms the ticket has been updated.


**Alternate Triggers**
1.1 The user enters from a challenge invitation.
— The game is already specified by the challenge.
— Continue at step 2.

**Failure scenario(s)**:

- 1a. No upcoming hockey games are available.
  - 1a1. The system displays an error message saying there are no upcoming games.

- 2a. The ticket name exceeds the character limit.
  - 2a1. The system displays an error message telling the user to remain within the character limit.

- 3a. The hockey game has begun while naming the ticket.
  - 3a1. The system displays an error message saying the game has already begun.
  - 3a2. The user is returned to the main menu.

...

<a name="uc2"></a>

#### Use Case 2: [Send Challenge]

**Description**: A user creates a challenge for an upcoming hockey game and invites friends to participate.

**Primary actor(s)**: User

**Main success scenario**:

1. The user selects a bingo ticket they have created.
2. The user selects one or more friends to invite.
3. The user sends the challenge invitation.
4. The system notifies the invited friends of the challenge.

**Failure scenario(s)**:

- 2a. No friends are selected.

  - 2a1. The system displays an error message requiring at least one friend.

- 2b. The owner has no friends in their list.

  - 2b1. The system displays an error message saying a challenge cannot be created without friends.
  - The owner returns to the main menu.

- 3a. The hockey game has already begun while creating the challenge.

  - 3a1. The system displays an error message saying the game has already begun.

  - 3a2. The owner is returned to the main menu.

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

#### Use Case 4: [View challenge rankings]

**Description**: A user selects an ongoing challenge and can view the live progress of all participating bingo tickets as they automatically update.

**Primary actor(s)**: User

**Main success scenario**:

1. The user navigates to their list of current challenges.
2. The user selects a specific ongoing challenge.
3. The user opens the “Current Rankings” view.
4. The system displays a list of all participating bingo tickets ranked by score.
5. The system updates the scores automatically as game events occur.

**Failure scenario(s)**:

- 2a. The hockey game has already ended.

  - 2a1. The system replaces the “Current Rankings” option with a “View Results” option.
  - 2a2. Continue at “View Results” use case.

- 2b. The hockey game has not started yet.

  - 2b1. The system does not display the “Current Rankings” option.

- 5a. Live game data feed is unavailable.
  - 5a1. The system displays a message: “Live updates temporarily unavailable, please refresh later.”
  - 5a2. Rankings remain frozen at last known update.

#### Use Case 5: [Send friend request]

**Description**: A user finds another user via their email and requests to become friends.

**Primary actor(s)**: User

**Main success scenario**:

1. The user navigates to their list of current friends.
2. The user clicks an icon to add a new friend.
3. The user types the email of the friend in a form.
4. The user confirms the form data and sends the request.
5. The system notifies the user that the request has been successfully sent.

**Failure scenario(s)**:

- 3a. The user enters an invalid email

  - 3a1. The system displays a message: “Please enter a valid email.”

- 4a. There is no user associated with the email.

  - 4a1. The system notifies the user that no user is associated with that email.

- 4b. The user associated with the email is already a friend.
  - 4b1. The system notifies the user that the accounts are already friends.

### **3.6. Screen Mock-ups**

![Screen Mock-ups](/documentation/images/user-journey-sccreen-mockups.png)

### **3.7. Non-Functional Requirements**

<a name="nfr1"></a>

1. **[Live updates]**

   - **Description**: Bingo ticket information should update within 1 min of the event occurring.
   - **Justification**: The element of live interactivity is very important as the game depends on a live hockey event, and events need to be updated quickly for the user to follow the state of the challenge.

2. **[Seamless bingo ticket building]**
   - **Description**: When creating a bingo ticket the system should be able to find events in less than 1 s.
   - **Justification**: The core feature of our app is building bingo tickets and ideally this is where the user will spend the most time. This process therefore has to be seamless so that the user's flow of thought can remain uninterrupted and they can act quickly on thoughts and ideas.

---

## 4. Designs Specification

### **4.1. Main Components**

1. **[Users]**
   - **Purpose**: Handles user profiles, and the friends graph (send/accept/decline/remove).
2. **[Bingo Tickets]**
   - **Purpose**: Handles creation, deletion, viewing and logic of bingo tickets.
3. **[Challenges]**
   - **Purpose**: Handles managing, viewing and logic of challenges.
4. **[Live Event Ingest & Scoring Engine]**
   - **Purpose**: Fetches, handles and updates NHL data so it can be accessed by the other components.

### **4.2. Databases**

1. **[MongoDB]**
   - **Purpose**: Stores users/friends, tickets, challenges, and read-optimized projections (e.g., current rankings).

### **4.3. External Modules**

1. **[NHL API]**
   - **Purpose**: Fetches schedule metadata and live event data to drive scoring and rankings updates.
2. **[Google authentications]**
   - **Purpose**: Handles user authentication when signing up/deleting account and logging in/out.

### **4.4. Frameworks and Libraries**

1. **[Express. js]**
   - **Purpose**: Enable web development in Node.js
   - **Reason**: It is the easiest way for us to get started develeping. It helps with routing, requests and responses.
2. **[Retrofit]**
   - **Purpose**: Make HTTP requests in Android.
   - **Reason**: We need to show API data to the frontend. 
3. **[Mongoose]**
   - **Purpose**: Interacting with MongoDB 
   - **Reason**: Making it easier to work with the database. 

### **4.5. Dependencies Diagram**

![High level backend diagram](/documentation/images/high-level-backend-diagram.png)


### **4.6. Use Case Sequence Diagram (5 Most Major Use Cases)**

1. [**[Create Bingo Ticket]**](#uc1)\
   ![Create bingo ticket use case sequence diagram](/documentation/images/createBingoTicketUseCase.svg)
2. ...

### **4.7. Design and Ways to Test Non-Functional Requirements**

1. [**[WRITE_NAME_HERE]**](#nfr1)
   - **Validation**: ...
2. ...
